package io.github.msaggik.celestialnavevents.internal.solar

import io.github.msaggik.celestialnavevents.api.SolarEventsCalculator
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.SUNRISE_OFFSET
import io.github.msaggik.celestialnavevents.internal.common.NUMBER_MILLIS_DAY
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.ASTRONOMICAL_TWILIGHT_TRACK_OFFSET
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.BLUE_HOUR_TRACK_OFFSET
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.CIVIL_TWILIGHT_TRACK_OFFSET
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.MAGIC_HOUR_TRACK_OFFSET
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.NAUTICAL_TWILIGHT_TRACK_OFFSET
import io.github.msaggik.celestialnavevents.internal.solar.SolarUtils.calculateSolarEventCommon
import io.github.msaggik.celestialnavevents.internal.solar.SolarUtils.calculateSolarRingEventDay
import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingAbsoluteEvent
import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeEvent
import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeShortEvent
import io.github.msaggik.celestialnavevents.model.events.solar.SolarAbsoluteEventDay
import io.github.msaggik.celestialnavevents.model.events.solar.SolarEventDay
import io.github.msaggik.celestialnavevents.model.events.solar.SolarRelativeEventDay
import io.github.msaggik.celestialnavevents.model.events.solar.SolarRingEventDay
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingSolarState
import java.time.LocalTime
import java.time.ZonedDateTime

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
 * Internal implementation of the [SolarEventsCalculator] interface responsible for calculating
 * astronomical solar events such as sunrise and sunset based on geographic coordinates and time.
 *
 * This class uses accurate astronomical formulas to compute the position of the Sun and
 * determine event times. It accounts for factors such as atmospheric refraction, observer's
 * location, Earth's rotation, and sidereal time.
 *
 * @see SolarEventDay
 * @see io.github.msaggik.celestialnavevents.model.events.common.riseset.Event
 * @see io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
 * @see HorizonCrossingSolarState
 */
internal class SolarCalculatorImpl : SolarEventsCalculator {

    /**
     * Finds the nearest upcoming solar horizon crossing event (sunrise or sunset)
     * from the given date and geographic location.
     *
     * Internally, this method uses [findUpcomingSolarRelativeEventDay] to locate the next
     * day that contains valid solar events, skipping days with no horizon crossings.
     * Then, it returns the soonest event from that day, along with the time remaining
     * until it occurs relative to the input [dateTime].
     *
     * @param latitude The geographic latitude of the observer in degrees.
     * @param longitude The geographic longitude of the observer in degrees.
     * @param dateTime The starting date and time with time zone information.
     *
     * @return [UpcomingRelativeShortEvent] containing the type and time offset (in milliseconds)
     *         of the nearest solar event, or null if no valid event is found.
     */
    override fun findUpcomingSolarRelativeShortEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): UpcomingRelativeShortEvent? {

        val eventsDaySunWithNearest = findUpcomingSolarRelativeEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )

        return eventsDaySunWithNearest.events.firstOrNull()?.let { event ->
            UpcomingRelativeShortEvent(
                eventType = event.type,
                timestampMillis = event.timeToNearestEventMillis
            )
        }
    }

    /**
     * Calculates solar events starting from the given [dateTime], searching forward up to one year,
     * and returns the first day that contains at least one solar horizon crossing event.
     *
     * Each event within the returned day includes an absolute [ZonedDateTime] corresponding to the
     * event time in the same time zone as the input [dateTime].
     *
     * Additionally, the horizon crossing state for the day prior to the start date is included.
     *
     * @param latitude Latitude of the observation point in degrees.
     * @param longitude Longitude of the observation point in degrees.
     * @param dateTime Starting [ZonedDateTime] from which to begin the search.
     * @return A [SolarAbsoluteEventDay] instance containing:
     *         - events with their absolute zoned date-times,
     *         - the day's horizon crossing state,
     *         - the previous day's horizon crossing state,
     *         - and other solar day parameters (day length, night length, meridian/antimeridian crossings).
     */
    override fun findUpcomingSolarAbsoluteEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarAbsoluteEventDay {

        val currentSunDayEvent = calculateSolarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )
        val defaultSolarAbsoluteEventDay = SolarAbsoluteEventDay.getDefaultSolarAbsoluteEventDay()

        val preHorizonCrossingSolarState: HorizonCrossingSolarState = calculateSolarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime.minusDays(1)
        ).type

        for (i in 0..365) {
            val sunEvent = if (i == 0) {
                currentSunDayEvent
            } else {
                calculateSolarEventDay(
                    latitude = latitude,
                    longitude = longitude,
                    dateTime = dateTime.plusDays(i.toLong())
                )
            }

            if (sunEvent.events.isNotEmpty()) {
                return SolarAbsoluteEventDay(
                    events = sunEvent.events.asSequence()
                        .sortedBy {
                            it.time.toTotalMilliseconds()
                        }
                        .map { event ->
                            UpcomingAbsoluteEvent(
                                type = event.type,
                                azimuth = event.azimuth,
                                dateTime = dateTime
                                    .plusDays(i.toLong() + event.time.days.toLong())
                                    .with(
                                        LocalTime.of(
                                            event.time.hour,
                                            event.time.min,
                                            event.time.sec,
                                            event.time.milliSec * 1_000_000
                                        )
                                    )
                            )
                        }
                        .filter { event ->
                            !event.dateTime.isBefore(dateTime)
                        }.toList(),
                    type = sunEvent.type,
                    preType = preHorizonCrossingSolarState,
                    dayLength = sunEvent.dayLength,
                    nightLength = sunEvent.nightLength,
                    meridianCrossing = sunEvent.meridianCrossing,
                    antimeridianCrossing = sunEvent.antimeridianCrossing
                )
            }
        }

        return defaultSolarAbsoluteEventDay
    }


    /**
     * Calculates solar events starting from the given [dateTime], searching forward up to one year,
     * and returns the first day that contains at least one solar horizon crossing event.
     *
     * Each event within the returned day includes a relative [Time] instance representing
     * the time of the event, plus the number of milliseconds from the start [dateTime]
     * to that event (time offset in milliseconds).
     *
     * Additionally, the horizon crossing state for the day prior to the start date is included.
     *
     * @param latitude Latitude of the observation point in degrees.
     * @param longitude Longitude of the observation point in degrees.
     * @param dateTime Starting [ZonedDateTime] from which to begin the search.
     * @return A [SolarRelativeEventDay] instance containing:
     *         - events with relative times and time offsets in milliseconds,
     *         - the day's horizon crossing state,
     *         - the previous day's horizon crossing state,
     *         - and other solar day parameters (day length, night length, meridian/antimeridian crossings).
     */
    override fun findUpcomingSolarRelativeEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRelativeEventDay {

        val currentSunDayEvent = calculateSolarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )
        val defaultSolarRelativeEventDay = SolarRelativeEventDay.getDefaultSolarRelativeEventDay()

        val preHorizonCrossingSolarState: HorizonCrossingSolarState = calculateSolarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime.minusDays(1)
        ).type

        for (i in 0..365) {
            val sunEvent = if (i == 0) {
                currentSunDayEvent
            } else {
                calculateSolarEventDay(
                    latitude = latitude,
                    longitude = longitude,
                    dateTime = dateTime.plusDays(i.toLong())
                )
            }

            if (sunEvent.events.isNotEmpty()) {
                val calculateSolarRelativeEventDay = SolarRelativeEventDay(
                    events = sunEvent.events.asSequence()
                        .sortedBy {
                            it.time.toTotalMilliseconds()
                        }
                        .map { event ->
                            UpcomingRelativeEvent(
                                type = event.type,
                                azimuth = event.azimuth,
                                time = event.time,
                                timeToNearestEventMillis = i * NUMBER_MILLIS_DAY +
                                        event.time.toTotalMilliseconds() -
                                        dateTime.toLocalTime().toNanoOfDay() / 1_000_000
                            )
                        }
                        .filter { event ->
                            event.timeToNearestEventMillis >= 0
                        }.toList(),
                    type = sunEvent.type,
                    preType = preHorizonCrossingSolarState,
                    dayLength = sunEvent.dayLength,
                    nightLength = sunEvent.nightLength,
                    meridianCrossing = sunEvent.meridianCrossing,
                    antimeridianCrossing = sunEvent.antimeridianCrossing
                )

                if (calculateSolarRelativeEventDay.events.isNotEmpty()) {
                    return calculateSolarRelativeEventDay
                }
            }
        }

        return defaultSolarRelativeEventDay
    }

    /**
     * Calculates solar astronomical events (sunrise and sunset) for the given location and date.
     *
     * The result includes a list of [io.github.msaggik.celestialnavevents.model.events.common.riseset.Event] instances with precise timestamps and azimuths.
     * Additionally, the method evaluates the type of solar day (e.g. polar day, normal day)
     * and computes the lengths of day and night.
     *
     * @param latitude Geographic latitude of the observer in degrees.
     * @param longitude Geographic longitude of the observer in degrees.
     * @param dateTime Date and time for which the events should be calculated.
     * @return [SolarEventDay] object containing sunrise/sunset events, day type, and durations.
     */
    override fun calculateSolarEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarEventDay {
        return calculateSolarEventCommon(
            horizonCorrection = SUNRISE_OFFSET,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )
    }

    /**
     * Calculates the magic hour intervals for the specified date and geographic position.
     *
     * Magic hour is defined as the period when the Sun is between -4.0° and +6.0°
     * relative to the geometric horizon, producing warm, low-angle light optimal for
     * photography and cinematography. Atmospheric refraction is not included in these bounds.
     *
     * Internally delegates to [calculateSolarRingEventDay] using [MAGIC_HOUR_TRACK_OFFSET].
     *
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @param dateTime Zoned date-time for which to compute the magic hour.
     * @return [SolarRingEventDay] containing magic hour segments and remaining daylight/night intervals.
     */
    override fun calculateMagicHourPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay {
        return calculateSolarRingEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            horizonCorrectionTrack = MAGIC_HOUR_TRACK_OFFSET
        )
    }

    /**
     * Calculates the blue hour intervals for the specified date and geographic position.
     *
     * Blue hour is defined as the period when the Sun is between -6.0° and -4.0°
     * below the geometric horizon, generating deep blue tones in the sky before sunrise
     * or after sunset. Atmospheric refraction is excluded from these bounds.
     *
     * Internally delegates to [calculateSolarRingEventDay] using [BLUE_HOUR_TRACK_OFFSET].
     *
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @param dateTime Zoned date-time for which to compute the blue hour.
     * @return [SolarRingEventDay] containing blue hour segments and remaining day/night intervals.
     */
    override fun calculateBlueHourPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay {
        return calculateSolarRingEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            horizonCorrectionTrack = BLUE_HOUR_TRACK_OFFSET
        )
    }

    /**
     * Calculates civil twilight intervals for the specified date and geographic position.
     *
     * Civil twilight occurs when the Sun is between -6.0° and 0.0° relative to the horizon.
     * The upper boundary includes atmospheric refraction, making the Sun appear on the visible horizon.
     * This phase provides sufficient natural light for most outdoor activities without artificial lighting.
     *
     * Internally delegates to [calculateSolarRingEventDay] using [CIVIL_TWILIGHT_TRACK_OFFSET].
     *
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @param dateTime Zoned date-time for which to compute civil twilight.
     * @return [SolarRingEventDay] containing civil twilight segments and residual day/night durations.
     */
    override fun calculateCivilTwilightPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay {
        return calculateSolarRingEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            horizonCorrectionTrack = CIVIL_TWILIGHT_TRACK_OFFSET
        )
    }

    /**
     * Calculates nautical twilight intervals for the specified date and geographic position.
     *
     * Nautical twilight spans the interval when the Sun is between -12.0° and -6.0°
     * below the geometric horizon. During this phase, the horizon is still faintly visible
     * at sea, allowing for traditional marine navigation.
     *
     * Internally delegates to [calculateSolarRingEventDay] using [NAUTICAL_TWILIGHT_TRACK_OFFSET].
     *
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @param dateTime Zoned date-time for which to compute nautical twilight.
     * @return [SolarRingEventDay] containing nautical twilight segments and remaining night/day durations.
     */
    override fun calculateNauticalTwilightPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay {
        return calculateSolarRingEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            horizonCorrectionTrack = NAUTICAL_TWILIGHT_TRACK_OFFSET
        )
    }

    /**
     * Calculates astronomical twilight intervals for the specified date and geographic position.
     *
     * Astronomical twilight occurs when the Sun is between -18.0° and -12.0°
     * below the geometric horizon. It represents the transition between full night
     * and the earliest perceptible sunlight. This period is dark enough for most
     * astronomical observations.
     *
     * Internally delegates to [calculateSolarRingEventDay] using [ASTRONOMICAL_TWILIGHT_TRACK_OFFSET].
     *
     * @param latitude Latitude in decimal degrees.
     * @param longitude Longitude in decimal degrees.
     * @param dateTime Zoned date-time for which to compute astronomical twilight.
     * @return [SolarRingEventDay] containing astronomical twilight segments and remaining night/day durations.
     */
    override fun calculateAstronomicalTwilightPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay {
        return calculateSolarRingEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime,
            horizonCorrectionTrack = ASTRONOMICAL_TWILIGHT_TRACK_OFFSET
        )
    }
}