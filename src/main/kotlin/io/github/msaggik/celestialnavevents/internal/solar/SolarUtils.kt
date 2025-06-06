package io.github.msaggik.celestialnavevents.internal.solar

import io.github.msaggik.celestialnavevents.internal.common.AstronomicalUtility.adjustForNextAscension
import io.github.msaggik.celestialnavevents.internal.common.AstronomicalUtility.calculateEventTiming
import io.github.msaggik.celestialnavevents.internal.common.AstronomicalUtility.calculateLocalStellarTime
import io.github.msaggik.celestialnavevents.model.events.common.track.TypeEventTrack
import io.github.msaggik.celestialnavevents.internal.common.HOURS_PER_DAY
import io.github.msaggik.celestialnavevents.internal.common.JULIAN_DATE_J2000
import io.github.msaggik.celestialnavevents.internal.common.NUMBER_MILLIS_DAY
import io.github.msaggik.celestialnavevents.internal.common.RADIANS_PER_DEGREE
import io.github.msaggik.celestialnavevents.internal.common.TimeUtils
import io.github.msaggik.celestialnavevents.internal.common.TimeUtils.convertMillisToJulianDate
import io.github.msaggik.celestialnavevents.internal.model.events.BufferEventTrack
import io.github.msaggik.celestialnavevents.internal.model.events.TypeBufferEventTrack
import io.github.msaggik.celestialnavevents.internal.model.position.Position
import io.github.msaggik.celestialnavevents.internal.model.position.Position.Companion.calculateSunPosition
import io.github.msaggik.celestialnavevents.internal.model.settings.HorizonCorrection
import io.github.msaggik.celestialnavevents.internal.model.settings.HorizonCorrectionTrack
import io.github.msaggik.celestialnavevents.model.events.solar.SolarEventDay
import io.github.msaggik.celestialnavevents.model.events.common.riseset.Event
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.events.common.track.EventPoint
import io.github.msaggik.celestialnavevents.model.events.common.track.EventTrack
import io.github.msaggik.celestialnavevents.model.events.solar.SolarRingEventDay
import io.github.msaggik.celestialnavevents.model.measurement.Coordinate
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingSolarState
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.indices
import kotlin.collections.isNotEmpty
import kotlin.collections.last
import kotlin.collections.lastOrNull
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.sortedBy
import kotlin.collections.sumOf
import kotlin.let
import kotlin.math.cos
import kotlin.math.sin
import kotlin.ranges.until
import kotlin.run
import kotlin.to

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
 * Contains helper functions specific to solar position and event calculations.
 *
 * This object provides internal utility methods tailored for handling solar-specific
 * astronomical phenomena such as:
 * - Normalization and adjustment of solar coordinates;
 * - Specialized solar interpolation or ephemeris corrections;
 * - Internal constants and calculations related only to solar movement models.
 *
 * Intended for internal use within solar computation modules. Not applicable to general
 * celestial mechanics or other bodies such as the Moon or planets.
 */
internal object SolarUtils {

    /**
     * Calculates a solar interval track (termed "solar ring") for a given day and location,
     * based on upper and lower solar elevation angle thresholds.
     *
     * This method is designed to extract visually meaningful light phases — such as
     * magic hour, blue hour, or twilight — from raw solar elevation data by pairing
     * transition points computed from horizon correction parameters.
     *
     * Internally, it computes two sets of solar events:
     * - One for the lower threshold (e.g., 0° = geometric sunrise/sunset)
     * - One for the upper threshold (e.g., -6° = civil twilight boundary)
     *
     * Each transition (RISE/SET) is timestamped and filtered for the current day.
     * The function then matches these transitions into valid intervals (EventTrack),
     * forming one or more light "ring" segments, sorted chronologically.
     *
     * Special edge conditions are handled:
     * - If only one transition exists (e.g. in polar regions), the interval extends to the full day or night.
     * - Event intervals remain non-overlapping and inside the 24h window.
     *
     * @param latitude Latitude in decimal degrees. Positive = northern hemisphere.
     * @param longitude Longitude in decimal degrees. Positive = east of Greenwich.
     * @param dateTime Reference date and timezone for which the solar track is evaluated.
     * Only events that occur during this local day are considered.
     * @param horizonCorrectionTrack Contains paired horizon corrections (upper and lower thresholds)
     * and the logical type of solar event being calculated (e.g., [TypeEventTrack.MAGIC_HOUR]).
     *
     * @return [SolarRingEventDay] — encapsulates:
     * - A list of [EventTrack] intervals between lower and upper threshold transitions.
     * - The total clean daylight duration excluding ring intervals.
     * - The total clean night duration excluding ring intervals.
     * - The full duration of the ring intervals (i.e., matched segments).
     *
     * @throws IllegalArgumentException if the upper angle is lower than the base angle.
     *
     * Example use cases:
     * - Extracting the magic hour period for photographers.
     * - Computing visibility windows based on light phases for navigation.
     * - Astronomy-aware calendar/timeline generation.
     */
    internal fun calculateSolarRingEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime,
        horizonCorrectionTrack: HorizonCorrectionTrack
    ): SolarRingEventDay {

        require(horizonCorrectionTrack.upperHorizonCorrection.angleFromHorizon >= horizonCorrectionTrack.lowerHorizonCorrection.angleFromHorizon) {
            "Invalid horizon corrections: " +
                    "upperHorizonCorrection.angleFromHorizon (${horizonCorrectionTrack.upperHorizonCorrection.angleFromHorizon}) " +
                    "must be >= lowerHorizonCorrection.angleFromHorizon (${horizonCorrectionTrack.lowerHorizonCorrection.angleFromHorizon})"
        }

        val (lowerSolarEventDay, upperSolarEventDay) = Pair(
            calculateSolarEventCommon(
                horizonCorrection = horizonCorrectionTrack.lowerHorizonCorrection,
                latitude = latitude,
                longitude = longitude,
                dateTime = dateTime
            ),
            calculateSolarEventCommon(
                horizonCorrection = horizonCorrectionTrack.upperHorizonCorrection,
                latitude = latitude,
                longitude = longitude,
                dateTime = dateTime
            )
        )

        val bufferEvents = (
                lowerSolarEventDay.events
                    .map {
                        BufferEventTrack(
                            typeBufferEventTrack = TypeBufferEventTrack.LOWER,
                            event = it
                        )
                    } +
                        upperSolarEventDay.events
                            .map {
                                BufferEventTrack(
                                    typeBufferEventTrack = TypeBufferEventTrack.UPPER,
                                    event = it
                                )
                            }
                )
            .filter { it.event.time.days == 0 }
            .sortedBy { it.event.time.toTotalMilliseconds() }

        val magicEventList = mutableListOf<EventTrack>()

        // edge situations
        bufferEvents.firstOrNull()?.let { first ->
            if ((first.event.type == EventType.SET && first.typeBufferEventTrack == TypeBufferEventTrack.LOWER) ||
                (first.event.type == EventType.RISE && first.typeBufferEventTrack == TypeBufferEventTrack.UPPER)
            ) {
                magicEventList.add(
                    EventTrack(
                        typeEventTrack = horizonCorrectionTrack.typeEventTrack,
                        start = EventPoint(
                            type = EventType.RISE,
                            dateTime = dateTime.toLocalDate().atStartOfDay(dateTime.zone)
                        ),
                        finish = EventPoint(
                            type = EventType.SET,
                            azimuth = first.event.azimuth,
                            dateTime = dateTime
                                .plusDays(first.event.time.days.toLong())
                                .with(
                                    LocalTime.of(
                                        first.event.time.hour,
                                        first.event.time.min,
                                        first.event.time.sec,
                                        first.event.time.milliSec * 1_000_000
                                    )
                                )
                        )
                    )
                )
            }
        }

        bufferEvents.lastOrNull()?.let { last ->
            if ((last.event.type == EventType.RISE && last.typeBufferEventTrack == TypeBufferEventTrack.LOWER) ||
                (last.event.type == EventType.SET && last.typeBufferEventTrack == TypeBufferEventTrack.UPPER)
            ) {
                magicEventList.add(
                    EventTrack(
                        typeEventTrack = horizonCorrectionTrack.typeEventTrack,
                        start = EventPoint(
                            type = EventType.RISE,
                            azimuth = last.event.azimuth,
                            dateTime = dateTime
                                .plusDays(last.event.time.days.toLong())
                                .with(
                                    LocalTime.of(
                                        last.event.time.hour,
                                        last.event.time.min,
                                        last.event.time.sec,
                                        last.event.time.milliSec * 1_000_000
                                    )
                                )
                        ),
                        finish = EventPoint(
                            type = EventType.SET,
                            dateTime = dateTime.toLocalDate().atStartOfDay(dateTime.zone).plusDays(1)
                        )
                    )
                )
            }
        }

        // other situations
        for (i in bufferEvents.indices) {
            if (i + 1 < bufferEvents.size) {
                if ((bufferEvents[i].typeBufferEventTrack == TypeBufferEventTrack.LOWER &&
                            bufferEvents[i].event.type == EventType.RISE &&
                            bufferEvents[i + 1].typeBufferEventTrack == TypeBufferEventTrack.UPPER &&
                            bufferEvents[i + 1].event.type == EventType.RISE) ||
                    (bufferEvents[i].typeBufferEventTrack == TypeBufferEventTrack.UPPER &&
                            bufferEvents[i].event.type == EventType.SET &&
                            bufferEvents[i + 1].typeBufferEventTrack == TypeBufferEventTrack.LOWER &&
                            bufferEvents[i + 1].event.type == EventType.SET) ||
                    (bufferEvents[i].typeBufferEventTrack == TypeBufferEventTrack.LOWER &&
                            bufferEvents[i].event.type == EventType.RISE &&
                            bufferEvents[i + 1].typeBufferEventTrack == TypeBufferEventTrack.LOWER &&
                            bufferEvents[i + 1].event.type == EventType.SET)
                ) {
                    magicEventList.add(
                        EventTrack(
                            typeEventTrack = horizonCorrectionTrack.typeEventTrack,
                            start = EventPoint(
                                type = EventType.RISE,
                                azimuth = bufferEvents[i].event.azimuth,
                                dateTime = dateTime
                                    .plusDays(bufferEvents[i].event.time.days.toLong())
                                    .with(
                                        LocalTime.of(
                                            bufferEvents[i].event.time.hour,
                                            bufferEvents[i].event.time.min,
                                            bufferEvents[i].event.time.sec,
                                            bufferEvents[i].event.time.milliSec * 1_000_000
                                        )
                                    )
                            ),
                            finish = EventPoint(
                                type = EventType.SET,
                                azimuth = bufferEvents[i + 1].event.azimuth,
                                dateTime = dateTime
                                    .plusDays(bufferEvents[i + 1].event.time.days.toLong())
                                    .with(
                                        LocalTime.of(
                                            bufferEvents[i + 1].event.time.hour,
                                            bufferEvents[i + 1].event.time.min,
                                            bufferEvents[i + 1].event.time.sec,
                                            bufferEvents[i + 1].event.time.milliSec * 1_000_000
                                        )
                                    )
                            )
                        )
                    )
                }
            }
        }

        val ringDuration = magicEventList.sumOf {
            Duration.between(it.start.dateTime, it.finish.dateTime).toMillis()
        }

        val (daylightBeforeRing, darknessAfterRing) = if (magicEventList.isEmpty()) {
            lowerSolarEventDay.dayLength to lowerSolarEventDay.nightLength
        } else {
            var cleanDayMillis = 0L
            var cleanNightMillis = 0L

            lowerSolarEventDay.dayLength?.let { dayLength ->
                cleanDayMillis = dayLength.toTotalMilliseconds() - ringDuration
                cleanNightMillis = NUMBER_MILLIS_DAY - dayLength.toTotalMilliseconds()
            } ?: run {
                lowerSolarEventDay.nightLength?.let { nightLength ->
                    cleanNightMillis = nightLength.toTotalMilliseconds()
                    cleanDayMillis = NUMBER_MILLIS_DAY - cleanNightMillis - ringDuration
                }
            }

            if (cleanDayMillis == 0L && cleanNightMillis == 0L) {
                lowerSolarEventDay.dayLength to lowerSolarEventDay.nightLength
            } else {
                Time.fromTotalMilliseconds(cleanDayMillis) to
                        Time.fromTotalMilliseconds(cleanNightMillis)
            }
        }

        return SolarRingEventDay(
            events = magicEventList,
            daylightBeforeRing = daylightBeforeRing,
            ringDuration = Time.fromTotalMilliseconds(ringDuration),
            darknessAfterRing = darknessAfterRing
        )
    }

    /**
     * Calculates solar astronomical events for a given location and time.
     *
     * This function computes the Sun's position and determines key solar phenomena
     * such as sunrise, sunset, twilight phases (civil, nautical, astronomical),
     * magic hour, and meridian transits, depending on the implementation logic.
     *
     * It accounts for the observer's geographic coordinates, time zone offset,
     * and atmospheric correction (via [HorizonCorrection]) to ensure accurate results.
     *
     * The returned [SolarEventDay] contains:
     * - A list of [Event] instances, each describing a specific solar event
     *   with its time and azimuth.
     * - The type of solar day (e.g., normal day, polar night, polar day).
     * - Duration of daylight and nighttime.
     *
     * @param latitude Geographic latitude of the observer in degrees.
     * @param longitude Geographic longitude of the observer in degrees.
     * @param dateTime Date and time (with time zone) for which the solar events are to be calculated.
     * @param horizonCorrection Correction factor for the horizon altitude (used to distinguish different solar phenomena).
     * @return [SolarEventDay] object containing the day's solar events, their classification, and durations.
     */
    internal fun calculateSolarEventCommon(
        horizonCorrection: HorizonCorrection,
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarEventDay {
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

        val sunToday = calculateSunPosition(daysSinceEpoch)
        var sunTomorrow = calculateSunPosition(daysSinceEpoch + 1)
        sunTomorrow = adjustForNextAscension(sunToday, sunTomorrow)

        return calculateAstronomicalSolarEvent(
            horizonCorrection = horizonCorrection,
            coordinate = coordinate,
            localSiderealTime = localStellarTime,
            todayPosition = sunToday,
            tomorrowPosition = sunTomorrow
        )
    }

    /**
     * Core method for computing astronomical events (e.g. sunrise, sunset) based on solar position.
     *
     * Performs interpolation over a 24-hour period to detect vertical position crossings and
     * determines event time using parabolic approximation.
     *
     * @param horizonCorrection Correction for atmospheric refraction and visual horizon offset.
     * @param coordinate Observer's geographic coordinates.
     * @param localSiderealTime Local sidereal time in radians.
     * @param todayPosition Sun position for the current day.
     * @param tomorrowPosition Sun position for the next day (used for interpolation).
     * @return An [SolarEventDay] containing events and day state.
     */
    internal fun calculateAstronomicalSolarEvent(
        horizonCorrection: HorizonCorrection,
        coordinate: Coordinate,
        localSiderealTime: Double,
        todayPosition: Position,
        tomorrowPosition: Position
    ): SolarEventDay {
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

        return SolarEventDay(
            events = events,
            type = eventsToTypeSolarEventDay(
                events = events,
                currentVerticalPosition = previousVerticalPosition
            )
        ).apply {
            this.calculationSolarDayAndNightLength()
            this.updateSolarMeridianCrossing()
            this.updateSolarAntimeridianCrossing()
        }
    }

    /**
     * Calculates the duration of daylight and nighttime based on the sequence of solar events.
     *
     * Partitions the 24-hour period into day and night segments using timestamps of rise and set events.
     * Also accounts for special cases such as polar day and polar night.
     *
     * @receiver The [SolarEventDay] instance to be updated with calculated day and night lengths.
     */
    private fun SolarEventDay.calculationSolarDayAndNightLength() {
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

            dayLength = Time.fromTotalMilliseconds(lightMilliseconds)
            nightLength = Time.fromTotalMilliseconds(darkMilliseconds)

        } else {
            when (type) {
                HorizonCrossingSolarState.POLAR_DAY -> {
                    dayLength = maxLength
                    nightLength = minLength
                }

                HorizonCrossingSolarState.POLAR_NIGHT -> {
                    dayLength = minLength
                    nightLength = maxLength
                }

                else -> {}
            }
        }
    }

    /**
     * Updates the time of meridian crossing, representing the midpoint of the day.
     *
     * This value is computed if exactly two solar events (sunrise and sunset) are available
     * and the current horizon state indicates a valid transition. It provides an approximation
     * of solar noon based on the event timing and day length.
     *
     * @receiver The [SolarEventDay] instance containing the event list and the calculated day length.
     */
    private fun SolarEventDay.updateSolarMeridianCrossing() {
        meridianCrossing = if (events.size == 2) {

            val prevTime = events.first().time.toTotalMilliseconds()

            if (events.first().type == EventType.RISE &&
                type == HorizonCrossingSolarState.RISEN_AND_SET
            ) {
                dayLength?.let { dayLength ->
                    Time.fromTotalMilliseconds(prevTime + dayLength.toTotalMilliseconds() / 2)
                }
            } else if (events.first().type == EventType.SET &&
                type == HorizonCrossingSolarState.SET_AND_RISEN
            ) {
                dayLength?.let { dayLength ->
                    val halfDayLength = dayLength.toTotalMilliseconds() / 2
                    if (prevTime - halfDayLength >= 0) {
                        Time.fromTotalMilliseconds(prevTime - halfDayLength)
                    } else {
                        Time.fromTotalMilliseconds(
                            Time(
                                days = 1,
                                hour = 0,
                                min = 0
                            ).toTotalMilliseconds() - halfDayLength
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
     * Updates the time of antimeridian crossing, representing the midpoint of the night.
     *
     * This value is computed if exactly two solar events (sunrise and sunset) are available
     * and the current horizon state indicates a valid transition. It is useful for determining
     * the center of the nighttime period.
     *
     * @receiver The [SolarEventDay] instance containing the event list and the calculated night length.
     */
    private fun SolarEventDay.updateSolarAntimeridianCrossing() {
        antimeridianCrossing = if (events.size == 2) {

            val prevTime = events.first().time.toTotalMilliseconds()

            if (events.first().type == EventType.RISE &&
                type == HorizonCrossingSolarState.RISEN_AND_SET
            ) {
                nightLength?.let { nightLength ->
                    val halfNightLength = nightLength.toTotalMilliseconds() / 2
                    if (prevTime - halfNightLength >= 0) {
                        Time.fromTotalMilliseconds(prevTime - halfNightLength)
                    } else {
                        Time.fromTotalMilliseconds(
                            Time(
                                days = 1,
                                hour = 0,
                                min = 0
                            ).toTotalMilliseconds() - halfNightLength
                        )
                    }
                }
            } else if (events.first().type == EventType.SET &&
                type == HorizonCrossingSolarState.SET_AND_RISEN
            ) {
                nightLength?.let { nightLength ->
                    Time.fromTotalMilliseconds(prevTime + nightLength.toTotalMilliseconds() / 2)
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    /**
     * Determines the type of solar day (e.g., polar day, normal day, single event)
     * based on the number and type of detected events.
     *
     * @param events List of calculated rise/set events.
     * @param currentVerticalPosition Final vertical position of the Sun, used for edge cases.
     * @return A [HorizonCrossingSolarState] representing the nature of the day.
     */
    private fun eventsToTypeSolarEventDay(
        events: List<Event>,
        currentVerticalPosition: Double
    ): HorizonCrossingSolarState {
        return when (events.size) {
            0 -> {
                if (currentVerticalPosition > 0) {
                    HorizonCrossingSolarState.POLAR_DAY
                } else if (currentVerticalPosition < 0) {
                    HorizonCrossingSolarState.POLAR_NIGHT
                } else {
                    HorizonCrossingSolarState.ERROR
                }
            }

            1 -> {
                when (events[0].type) {
                    EventType.SET -> HorizonCrossingSolarState.ONLY_SET
                    EventType.RISE -> HorizonCrossingSolarState.ONLY_RISEN
                }
            }

            2 -> {
                val (first, second) = events.sortedBy { it.time.toTotalMilliseconds() }
                when {
                    first.type == EventType.RISE && second.type == EventType.SET -> HorizonCrossingSolarState.RISEN_AND_SET
                    first.type == EventType.SET && second.type == EventType.RISE -> HorizonCrossingSolarState.SET_AND_RISEN
                    first.type != second.type -> HorizonCrossingSolarState.RISEN_IS_SET
                    else -> HorizonCrossingSolarState.ERROR
                }
            }

            3 -> {
                val (first, second, third) = events.sortedBy { it.time.toTotalMilliseconds() }

                when {
                    first.type == EventType.RISE && first.type == third.type && first.type != second.type -> HorizonCrossingSolarState.RISE_SET_RISE
                    first.type == EventType.SET && first.type == third.type && first.type != second.type -> HorizonCrossingSolarState.SET_RISE_SET
                    else -> HorizonCrossingSolarState.ERROR
                }
            }

            else -> {
                HorizonCrossingSolarState.ERROR
            }
        }
    }
}