package io.github.msaggik.celestialnavevents.internal.lunar

import io.github.msaggik.celestialnavevents.api.LunarEventsCalculator
import io.github.msaggik.celestialnavevents.internal.common.NUMBER_MILLIS_DAY
import io.github.msaggik.celestialnavevents.internal.common.StandardProfiles.MOONRISE_OFFSET
import io.github.msaggik.celestialnavevents.internal.lunar.LunarUtils.calculateLunarEventCommon
import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingAbsoluteEvent
import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeEvent
import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeShortEvent
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarAbsoluteEventDay
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarEventDay
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarRelativeEventDay
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingLunarState
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
 * Default implementation of the [LunarEventsCalculator] interface.
 *
 * Uses internal lunar algorithms and astronomical models to determine moonrise, moonset,
 * lunar phase, and illumination based on the observer's location and the provided date.
 *
 * This implementation applies a predefined [MOONRISE_OFFSET] to correct the visual horizon for
 * atmospheric refraction and apparent lunar position at the horizon.
 *
 * Should be used in applications requiring detailed and accurate lunar event calculations,
 * such as calendars, night mode planning, or astronomy apps.
 */
internal class LunarCalculatorImpl : LunarEventsCalculator {

    /**
     * Finds the nearest upcoming lunar event (such as moonrise, moonset, etc.) in a simplified form.
     *
     * This method is a lightweight version of [findUpcomingLunarRelativeEventDay], returning only
     * the event type and the time remaining until the event occurs in milliseconds.
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime The current date and time with time zone, used as a reference point.
     * @return [UpcomingRelativeShortEvent] containing the nearest event type and time to it, or `null` if no event is found.
     */
    override fun findUpcomingLunarRelativeShortEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): UpcomingRelativeShortEvent? {

        val eventsDayLunarWithNearest = findUpcomingLunarRelativeEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )

        return eventsDayLunarWithNearest.events.firstOrNull()?.let { event ->
            UpcomingRelativeShortEvent(
                eventType = event.type,
                timestampMillis = event.timeToNearestEventMillis
            )
        }
    }

    /**
     * Finds the next available absolute lunar event day starting from the given date.
     *
     * This method searches forward up to 365 days to find the next day that contains
     * one or more absolute lunar events (e.g., moonrise, culmination). Each event includes
     * a precise [ZonedDateTime] timestamp.
     *
     * The method also provides lunar visibility information, illumination percentage,
     * age of the Moon in days, and pre-event lunar state (from the previous day).
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime The current date and time with time zone, used as a reference point.
     * @return [LunarAbsoluteEventDay] containing the list of upcoming absolute lunar events and metadata.
     * If no event is found within 365 days, a default object is returned.
     */
    override fun findUpcomingLunarAbsoluteEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarAbsoluteEventDay {

        val currentLunarDayEvent = calculateLunarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )
        val defaultLunarAbsoluteEventDay = LunarAbsoluteEventDay.getDefaultLunarAbsoluteEventDay()

        val preHorizonCrossingLunarState: HorizonCrossingLunarState = calculateLunarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime.minusDays(1)
        ).type

        for (i in 0..365) {
            val lunarEvent = if (i == 0) {
                currentLunarDayEvent
            } else {
                calculateLunarEventDay(
                    latitude = latitude,
                    longitude = longitude,
                    dateTime = dateTime.plusDays(i.toLong())
                )
            }

            if (lunarEvent.events.isNotEmpty()) {
                val calculateLunarAbsoluteEventDay = LunarAbsoluteEventDay(
                    events = lunarEvent.events.asSequence()
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
                    type = lunarEvent.type,
                    preType = preHorizonCrossingLunarState,
                    visibleLength = lunarEvent.visibleLength,
                    invisibleLength = lunarEvent.invisibleLength,
                    meridianCrossing = lunarEvent.meridianCrossing,
                    antimeridianCrossing = lunarEvent.antimeridianCrossing,
                    ageInDays = lunarEvent.ageInDays,
                    illuminationPercent = lunarEvent.illuminationPercent
                )

                if (calculateLunarAbsoluteEventDay.events.isNotEmpty()) {
                    return calculateLunarAbsoluteEventDay
                }
            }
        }

        return defaultLunarAbsoluteEventDay
    }

    /**
     * Finds the next available relative lunar event day starting from the given date.
     *
     * Similar to [findUpcomingLunarAbsoluteEventDay], but returns time-to-event
     * information relative to the provided [dateTime], instead of absolute timestamps.
     *
     * This method is useful when needing to know how much time remains until the next
     * visible lunar event (e.g., moonrise in 5 hours).
     *
     * Also includes lunar visibility length, illumination percentage, age in days, and
     * the lunar state from the previous day to determine horizon crossing transitions.
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime The current date and time with time zone, used as a reference point.
     * @return [LunarRelativeEventDay] containing the list of upcoming relative lunar events and metadata.
     * If no event is found within 365 days, a default object is returned.
     */
    override fun findUpcomingLunarRelativeEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarRelativeEventDay {

        val currentLunarDayEvent = calculateLunarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )

        val defaultLunarRelativeEventDay = LunarRelativeEventDay.getDefaultLunarRelativeEventDay()

        val preHorizonCrossingLunarState: HorizonCrossingLunarState = calculateLunarEventDay(
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime.minusDays(1)
        ).type

        for (i in 0..365) {
            val lunarEvent = if (i == 0) {
                currentLunarDayEvent
            } else {
                calculateLunarEventDay(
                    latitude = latitude,
                    longitude = longitude,
                    dateTime = dateTime.plusDays(i.toLong())
                )
            }

            if (lunarEvent.events.isNotEmpty()) {
                val calculateLunarRelativeEventDay = LunarRelativeEventDay(
                    events = lunarEvent.events.asSequence()
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
                    type = lunarEvent.type,
                    preType = preHorizonCrossingLunarState,
                    visibleLength = lunarEvent.visibleLength,
                    invisibleLength = lunarEvent.invisibleLength,
                    meridianCrossing = lunarEvent.meridianCrossing,
                    antimeridianCrossing = lunarEvent.antimeridianCrossing,
                    ageInDays = lunarEvent.ageInDays,
                    illuminationPercent = lunarEvent.illuminationPercent
                )

                if (calculateLunarRelativeEventDay.events.isNotEmpty()) {
                    return calculateLunarRelativeEventDay
                }
            }
        }

        return defaultLunarRelativeEventDay
    }

    /**
     * Computes lunar events for a specific geographic location and date.
     *
     * Applies a visual correction constant ([MOONRISE_OFFSET]) to adjust for atmospheric refraction.
     * Delegates actual computation to [calculateLunarEventCommon], which handles astronomical math.
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime ZonedDateTime instance representing the local time and time zone.
     * @return [LunarEventDay] containing lunar event data for the given parameters.
     */
    override fun calculateLunarEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarEventDay {
        return calculateLunarEventCommon(
            horizonCorrection = MOONRISE_OFFSET,
            latitude = latitude,
            longitude = longitude,
            dateTime = dateTime
        )
    }
}