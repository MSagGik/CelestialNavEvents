package io.github.msaggik.celestialnavevents.internal.lunar

import io.github.msaggik.celestialnavevents.internal.common.AstronomicalUtility.adjustForNextAscension
import io.github.msaggik.celestialnavevents.internal.common.AstronomicalUtility.calculateEventTiming
import io.github.msaggik.celestialnavevents.internal.common.AstronomicalUtility.calculateLocalStellarTime
import io.github.msaggik.celestialnavevents.internal.common.HOURS_PER_DAY
import io.github.msaggik.celestialnavevents.internal.common.JULIAN_DATE_J2000
import io.github.msaggik.celestialnavevents.internal.common.LUNAR_MONTH_DAYS
import io.github.msaggik.celestialnavevents.internal.common.RADIANS_PER_DEGREE
import io.github.msaggik.celestialnavevents.internal.common.TimeUtils
import io.github.msaggik.celestialnavevents.internal.common.TimeUtils.convertMillisToJulianDate
import io.github.msaggik.celestialnavevents.internal.model.position.Position
import io.github.msaggik.celestialnavevents.internal.model.position.Position.Companion.calculateMoonPosition
import io.github.msaggik.celestialnavevents.internal.model.settings.HorizonCorrection
import io.github.msaggik.celestialnavevents.model.events.common.riseset.Event
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarEventDay
import io.github.msaggik.celestialnavevents.model.measurement.Coordinate
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingLunarState
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.apply
import kotlin.collections.first
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.sortedBy
import kotlin.let
import kotlin.math.cos
import kotlin.math.sin
import kotlin.ranges.until

/*
 * Copyright 2025 Maxim Sagaciyang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Contains helper functions specific to lunar position and event calculations.
 *
 * This object provides internal utility methods tailored for handling Moon-specific
 * astronomical phenomena, including:
 * - Determining rise/set events of the Moon;
 * - Calculating illumination and lunar phase;
 * - Estimating meridian and antimeridian crossings;
 * - Modeling lunar movement across a sidereal day.
 *
 * Intended for internal use within lunar computation modules. Not applicable to other
 * celestial objects such as the Sun or planets.
 */
internal object LunarUtils {

    /**
     * Computes core lunar astronomical events for a specific date and location.
     *
     * This function calculates Moon rise/set events, current lunar age, and illumination
     * percentage by interpolating the Moon's position over a sidereal day.
     *
     * The result includes time-based events and additional metadata such as lunar day length,
     * meridian crossing, and phase-related values.
     *
     * @param horizonCorrection Correction model for atmospheric refraction and observer's horizon.
     * @param latitude Observer's geographic latitude.
     * @param longitude Observer's geographic longitude.
     * @param dateTime Date and time for which events are computed (with time zone).
     * @return A [LunarEventDay] object encapsulating all computed lunar events and properties.
     */
    internal fun calculateLunarEventCommon(
        horizonCorrection: HorizonCorrection,
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarEventDay {
        val coordinate = Coordinate(
            latitude,
            longitude
        )
        val utcZdt = dateTime.withZoneSameInstant(ZoneOffset.UTC)
        val utcOffsetHours = dateTime.offset.totalSeconds / 3600

        val midnight = dateTime.toLocalDate().atStartOfDay(dateTime.zone)

        val jd = convertMillisToJulianDate(
            date = midnight,
            utToTtCorrection = TimeUtils.estimateDeltaT(utcZdt.year + utcZdt.dayOfYear / 365.25)
        ).toInt()

        var daysSinceEpoch = jd - JULIAN_DATE_J2000 + 0.5

        val timeZoneShift = -1 * utcOffsetHours.toDouble() / HOURS_PER_DAY

        val localStellarTime = calculateLocalStellarTime(
            daysSinceEpoch = daysSinceEpoch,
            longitude = coordinate.longitude,
            timeZoneShift = timeZoneShift
        )

        daysSinceEpoch += timeZoneShift

        val lunarToday = calculateMoonPosition(daysSinceEpoch)
        var lunarTomorrow = calculateMoonPosition(daysSinceEpoch + 1)
        lunarTomorrow = adjustForNextAscension(lunarToday, lunarTomorrow)

        val ageInDays = calculateAgeOfMoon(jd + 1.0)
        val illuminationPercent =
            calculateMoonIlluminationPercentage(ageInDays)

        return calculateAstronomicalLunarEvent(
            horizonCorrection = horizonCorrection,
            coordinate = coordinate,
            localSiderealTime = localStellarTime,
            todayPosition = lunarToday,
            tomorrowPosition = lunarTomorrow,
            ageInDays = ageInDays,
            illuminationPercent = illuminationPercent
        )
    }

    /**
     * Calculates rise/set lunar events and additional day characteristics.
     *
     * Performs interpolation of the Moon’s position throughout a sidereal day and applies
     * geometric calculations to detect crossings of the astronomical horizon. Also computes
     * lunar age, illumination, and visibility duration.
     *
     * @param horizonCorrection Correction model for visual and atmospheric adjustments.
     * @param coordinate Observer’s geographic position.
     * @param localSiderealTime Local sidereal time in radians.
     * @param todayPosition Geocentric lunar position for the current date.
     * @param tomorrowPosition Geocentric lunar position for the following date (used for interpolation).
     * @param ageInDays Age of the Moon in days since the last new moon.
     * @param illuminationPercent Illuminated fraction of the Moon (percentage).
     * @return A [LunarEventDay] object with detected lunar events and observational metadata.
     */
    private fun calculateAstronomicalLunarEvent(
        horizonCorrection: HorizonCorrection,
        coordinate: Coordinate,
        localSiderealTime: Double,
        todayPosition: Position,
        tomorrowPosition: Position,
        ageInDays: Double,
        illuminationPercent: Double
    ): LunarEventDay {
        var previousRightAscension = todayPosition.rightAscension
        var previousDeclination = todayPosition.declination

        val deltaRightAscension = tomorrowPosition.rightAscension - todayPosition.rightAscension
        val deltaDeclination = tomorrowPosition.declination - todayPosition.declination

        val hourAngleStart = localSiderealTime - todayPosition.rightAscension
        val zenithDistance =
            RADIANS_PER_DEGREE * (if (horizonCorrection.isAtmosphericRefractionIncluded) 90.833 else 90.0)
        val sinLat = sin(coordinate.latitude * RADIANS_PER_DEGREE)
        val cosLat = cos(coordinate.latitude * RADIANS_PER_DEGREE)
        val adjustedZenith = cos(zenithDistance) + horizonCorrection.angleFromHorizon * RADIANS_PER_DEGREE
        val prevVertical = sinLat * sin(todayPosition.declination) +
                cosLat * cos(todayPosition.declination) * cos(hourAngleStart) -
                adjustedZenith
        var previousVerticalPosition = prevVertical

        val eventResult = mutableListOf<Event>()

        for (hour in 0 until HOURS_PER_DAY) {
            val dayFraction = (hour + 1) / HOURS_PER_DAY.toDouble()
            val currentRightAscension = todayPosition.rightAscension + dayFraction * deltaRightAscension
            val currentDeclination = todayPosition.declination + dayFraction * deltaDeclination

            val (events, verticalPosition) = calculateEventTiming(
                hourOfDay = hour,
                horizonCorrection = horizonCorrection,
                previousRightAscension = previousRightAscension,
                currentRightAscension = currentRightAscension,
                previousDeclination = previousDeclination,
                currentDeclination = currentDeclination,
                previousVerticalPosition = previousVerticalPosition,
                observerCoordinate = coordinate,
                localStellarTime = localSiderealTime
            )

            if (events.isNotEmpty()) {
                eventResult.addAll(events)
            }

            previousRightAscension = currentRightAscension
            previousDeclination = currentDeclination
            previousVerticalPosition = verticalPosition
        }

        val events = if (eventResult.isNotEmpty()) {
            eventResult.sortedBy { it.time.toTotalMilliseconds() }
        } else {
            eventResult
        }

        return LunarEventDay(
            events = events,
            type = eventsToTypeLunarEventDay(
                events = events,
                currentVerticalPosition = previousVerticalPosition
            ),
            ageInDays = ageInDays,
            illuminationPercent = illuminationPercent
        ).apply {
            this.calculationLunarDayAndNightLength()
            this.updateLunarMeridianCrossing()
            this.updateLunarAntimeridianCrossing()
        }
    }

    /**
     * Computes the durations of lunar day and night based on rise/set events.
     *
     * This function analyzes the sequence of [Event]s for a given day, calculates time
     * intervals between them, and determines visible (light) and invisible (dark) periods.
     * In cases of full day/night or lack of events, fallback rules apply.
     */
    private fun LunarEventDay.calculationLunarDayAndNightLength() {
        val minLength = Time(hour = 0, min = 0)
        val maxLength = Time(days = 1, hour = 0, min = 0)

        if (events.isNotEmpty()) {

            var prevTime = events.first().time.toTotalMilliseconds()
            var isStartDay = events.first().type == EventType.RISE

            var (lightMilliseconds, darkMilliseconds) = if (isStartDay) {
                Pair(0L, prevTime)
            } else {
                Pair(prevTime, 0L)
            }

            if (events.size > 1) {
                for (i in 1 until events.size) {
                    val currentEvent = events[i]
                    val currentTime = currentEvent.time.toTotalMilliseconds()
                    val interval = currentTime - prevTime

                    if (isStartDay) {
                        lightMilliseconds += interval
                    } else {
                        darkMilliseconds += interval
                    }

                    isStartDay = !isStartDay
                    prevTime = currentTime
                }
            }

            val lastTime = maxLength.toTotalMilliseconds() - events.last().time.toTotalMilliseconds()
            val isStartNight = events.last().type == EventType.SET

            if (isStartNight) {
                darkMilliseconds += lastTime
            } else {
                lightMilliseconds += lastTime
            }

            visibleLength = Time.fromTotalMilliseconds(lightMilliseconds)
            invisibleLength = Time.fromTotalMilliseconds(darkMilliseconds)

        } else {
            when (type) {
                HorizonCrossingLunarState.FULL_DAY -> {
                    visibleLength = maxLength
                    invisibleLength = minLength
                }

                HorizonCrossingLunarState.FULL_NIGHT -> {
                    visibleLength = minLength
                    invisibleLength = maxLength
                }

                else -> {}
            }
        }
    }

    /**
     * Estimates the moment when the Moon crosses the observer’s meridian (highest altitude).
     *
     * Based on known rise and set times and assuming symmetrical visibility across the sky,
     * the meridian crossing is interpolated at the midpoint of the visible period.
     * Returns null if insufficient or conflicting data is present.
     */
    private fun LunarEventDay.updateLunarMeridianCrossing() {
        meridianCrossing = if (events.size == 2) {

            val prevTime = events.first().time.toTotalMilliseconds()

            if (events.first().type == EventType.RISE &&
                type == HorizonCrossingLunarState.RISEN_AND_SET
            ) {
                visibleLength?.let { visibleLength ->
                    Time.fromTotalMilliseconds(prevTime + visibleLength.toTotalMilliseconds() / 2)
                }
            } else if (events.first().type == EventType.SET &&
                type == HorizonCrossingLunarState.SET_AND_RISEN
            ) {
                visibleLength?.let { visibleLength ->
                    val halfVisibleLength = visibleLength.toTotalMilliseconds() / 2
                    if (prevTime - halfVisibleLength >= 0) {
                        Time.fromTotalMilliseconds(prevTime - halfVisibleLength)
                    } else {
                        Time.fromTotalMilliseconds(
                            Time(
                                days = 1,
                                hour = 0,
                                min = 0
                            ).toTotalMilliseconds() - halfVisibleLength
                        )
                    }
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * Estimates the Moon’s antimeridian crossing (lowest altitude below the horizon).
     *
     * Similar to [updateLunarMeridianCrossing], this function computes the approximate
     * midpoint of the invisible (nighttime) period to estimate when the Moon is directly
     * beneath the observer.
     */
    private fun LunarEventDay.updateLunarAntimeridianCrossing() {
        antimeridianCrossing = if (events.size == 2) {

            val prevTime = events.first().time.toTotalMilliseconds()

            if (events.first().type == EventType.RISE &&
                type == HorizonCrossingLunarState.RISEN_AND_SET
            ) {
                invisibleLength?.let { invisibleLunarLength ->
                    val halfInvisibleLength = invisibleLunarLength.toTotalMilliseconds() / 2
                    if (prevTime - halfInvisibleLength >= 0) {
                        Time.fromTotalMilliseconds(prevTime - halfInvisibleLength)
                    } else {
                        Time.fromTotalMilliseconds(
                            Time(
                                days = 1,
                                hour = 0,
                                min = 0
                            ).toTotalMilliseconds() - halfInvisibleLength
                        )
                    }
                }
            } else if (events.first().type == EventType.SET &&
                type == HorizonCrossingLunarState.SET_AND_RISEN
            ) {
                invisibleLength?.let { invisibleLunarLength ->
                    Time.fromTotalMilliseconds(prevTime + invisibleLunarLength.toTotalMilliseconds() / 2)
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * Interprets the list of rise/set events into a lunar visibility classification.
     *
     * Based on event types and sequence, this method assigns one of the following states:
     * - FULL_DAY: Moon never sets
     * - FULL_NIGHT: Moon never rises
     * - RISEN_AND_SET, SET_AND_RISEN: Normal two-event sequences
     * - ONLY_SET, ONLY_RISEN: One incomplete transition
     * - RISE_SET_RISE, SET_RISE_SET: Three events
     * - ERROR: Unhandled or conflicting case
     *
     * @param events List of rise/set events.
     * @param currentVerticalPosition Vertical position of the Moon at end of day (used for fallback).
     * @return A [HorizonCrossingLunarState] representing the visibility type.
     */
    private fun eventsToTypeLunarEventDay(
        events: List<Event>,
        currentVerticalPosition: Double
    ): HorizonCrossingLunarState {
        return when (events.size) {
            0 -> {
                if (currentVerticalPosition > 0) {
                    HorizonCrossingLunarState.FULL_DAY
                } else if (currentVerticalPosition < 0) {
                    HorizonCrossingLunarState.FULL_NIGHT
                } else {
                    HorizonCrossingLunarState.ERROR
                }
            }

            1 -> {
                when (events[0].type) {
                    EventType.SET -> HorizonCrossingLunarState.ONLY_SET
                    EventType.RISE -> HorizonCrossingLunarState.ONLY_RISEN
                }
            }

            2 -> {
                val (first, second) = events.sortedBy { it.time.toTotalMilliseconds() }
                when {
                    first.type == EventType.RISE && second.type == EventType.SET -> HorizonCrossingLunarState.RISEN_AND_SET
                    first.type == EventType.SET && second.type == EventType.RISE -> HorizonCrossingLunarState.SET_AND_RISEN
                    first.type != second.type -> HorizonCrossingLunarState.RISEN_IS_SET
                    else -> HorizonCrossingLunarState.ERROR
                }
            }

            3 -> {
                val (first, second, third) = events.sortedBy { it.time.toTotalMilliseconds() }

                when {
                    first.type == EventType.RISE && first.type == third.type && first.type != second.type -> HorizonCrossingLunarState.RISE_SET_RISE
                    first.type == EventType.SET && first.type == third.type && first.type != second.type -> HorizonCrossingLunarState.SET_RISE_SET
                    else -> HorizonCrossingLunarState.ERROR
                }
            }

            else -> {
                HorizonCrossingLunarState.ERROR
            }
        }
    }

    /**
     * Computes the illuminated fraction of the Moon based on its age in days.
     *
     * Uses an approximation formula derived from the cosine function to model the lunar
     * illumination curve. Accuracy is approximately ±5%.
     *
     * @param daysSinceNewMoon Number of days since the last new moon.
     * @return Illumination percentage (0–100).
     */
    private fun calculateMoonIlluminationPercentage(daysSinceNewMoon: Double): Double {
        return 0.5 * (1 + cos((Math.round(daysSinceNewMoon) + LUNAR_MONTH_DAYS / 2) / LUNAR_MONTH_DAYS * 2 * Math.PI)) * 100
    }

    /**
     * Calculates the Moon’s age in days based on a given Julian Date.
     *
     * The Moon's age is defined as the number of days elapsed since the last new moon.
     * Normalization ensures the result is always positive within a synodic month.
     *
     * @param julianDate Julian date to evaluate.
     * @return Moon age in days.
     */
    private fun calculateAgeOfMoon(julianDate: Double): Double {
        val normalizedTemp = (julianDate - 2451550.1) / LUNAR_MONTH_DAYS
        var moonAge = normalizedTemp - normalizedTemp.toInt()
        if (moonAge < 0) {
            moonAge++
        }
        moonAge *= LUNAR_MONTH_DAYS
        return moonAge
    }
}