package io.github.msaggik.celestialnavevents.api

import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeShortEvent
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarAbsoluteEventDay
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarEventDay
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarRelativeEventDay
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
 * Interface for calculating lunar astronomical events based on geographic and temporal data.
 *
 * Implementations of this interface are responsible for determining lunar rise and set times,
 * the phase and illumination of the Moon, and other related events for a given location and time.
 *
 * Intended to be used in modules requiring lunar calendar calculations, moon phase tracking,
 * or astronomical timing (e.g., tide predictions, night mode scheduling, etc.).
 */
interface LunarEventsCalculator {

    /**
     * Finds the nearest upcoming lunar event (e.g., moonrise, moonset) in a simplified form.
     *
     * Returns only the type of the event and the time remaining until it occurs,
     * relative to the provided [dateTime].
     *
     * This method is useful when only basic event timing is needed, with minimal overhead.
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime The current local date and time including time zone.
     * @return A [UpcomingRelativeShortEvent] containing the type of the nearest event and time to it,
     * or `null` if no such event is found.
     */
    fun findUpcomingLunarRelativeShortEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): UpcomingRelativeShortEvent?

    /**
     * Finds the next absolute lunar event day that includes at least one valid astronomical event.
     *
     * Events are returned with precise [ZonedDateTime] timestamps and include full
     * metadata about lunar state, visibility, and illumination.
     *
     * This method searches forward from the specified [dateTime] up to a maximum of 365 days.
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime The current local date and time including time zone.
     * @return A [LunarAbsoluteEventDay] containing upcoming absolute lunar events and related information.
     */
    fun findUpcomingLunarAbsoluteEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarAbsoluteEventDay

    /**
     * Finds the next relative lunar event day including time-to-event values.
     *
     * Events are returned as relative intervals (in milliseconds) from the current [dateTime],
     * rather than absolute timestamps. This is particularly useful for scheduling actions
     * relative to the user's current local time.
     *
     * Searches forward up to 365 days until valid lunar events are found.
     *
     * @param latitude Latitude in decimal degrees (positive for north, negative for south).
     * @param longitude Longitude in decimal degrees (positive for east, negative for west).
     * @param dateTime The current local date and time including time zone.
     * @return A [LunarRelativeEventDay] containing upcoming relative lunar events and lunar metadata.
     */
    fun findUpcomingLunarRelativeEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarRelativeEventDay

    /**
     * Calculates all relevant lunar events for a given date and geographic location.
     *
     * The returned [LunarEventDay] contains moonrise, moonset times, lunar illumination percentage,
     * meridian/antimeridian crossings, and duration of visible/invisible lunar presence above horizon.
     *
     * @param latitude Latitude of the observer's location in decimal degrees.
     * @param longitude Longitude of the observer's location in decimal degrees.
     * @param dateTime Local date and time (including time zone) for which the lunar data should be calculated.
     * @return A [LunarEventDay] object containing all computed lunar events for the given day and location.
     */
    fun calculateLunarEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): LunarEventDay
}