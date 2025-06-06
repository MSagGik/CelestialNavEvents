package io.github.msaggik.celestialnavevents.api

import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeShortEvent
import io.github.msaggik.celestialnavevents.model.events.solar.SolarAbsoluteEventDay
import io.github.msaggik.celestialnavevents.model.events.solar.SolarEventDay
import io.github.msaggik.celestialnavevents.model.events.solar.SolarRelativeEventDay
import io.github.msaggik.celestialnavevents.model.events.solar.SolarRingEventDay
import io.github.msaggik.celestialnavevents.model.measurement.Time
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
 * Interface for calculating various solar events (e.g., sunrise, sunset, magic hour, twilight phases)
 * based on geographic coordinates and date-time.
 *
 * Implementations of this interface provide methods for:
 * - Retrieving upcoming solar events with or without time offsets.
 * - Calculating full sets of daily solar events.
 * - Determining specialized light periods such as magic hour and different twilight phases.
 */
interface SolarEventsCalculator {

    /**
     * Finds the nearest upcoming solar horizon crossing event (sunrise or sunset)
     * from the specified date and geographic location.
     *
     * This method searches forward in time (up to one year) and skips days without valid
     * solar events (such as during polar day or polar night), returning the soonest available event.
     *
     * @param latitude The geographic latitude of the observer in degrees.
     * @param longitude The geographic longitude of the observer in degrees.
     * @param dateTime The starting date and time with time zone information.
     *
     * @return [UpcomingRelativeShortEvent] containing the event type and the time offset
     *         in milliseconds from the input time until the event occurs,
     *         or null if no valid event is found within the search window.
     */
    fun findUpcomingSolarRelativeShortEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): UpcomingRelativeShortEvent?

    /**
     * Finds the next day containing valid solar horizon crossing events (e.g., sunrise, sunset),
     * starting from the given [dateTime] and searching forward up to one year.
     *
     * Returns an absolute event day with all events' [ZonedDateTime] values,
     * preserving the input timezone.
     *
     * @param latitude The geographic latitude in degrees.
     * @param longitude The geographic longitude in degrees.
     * @param dateTime The starting date and time with timezone information.
     * @return [SolarAbsoluteEventDay] containing solar events with absolute date-times,
     *         horizon crossing states, and other solar day parameters.
     */
    fun findUpcomingSolarAbsoluteEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarAbsoluteEventDay

    /**
     * Finds the next day containing valid solar horizon crossing events (e.g., sunrise, sunset),
     * starting from the given [dateTime] and searching forward up to one year.
     *
     * Returns a relative event day with events' times as relative [Time] instances
     * and time offsets in milliseconds from the start [dateTime].
     *
     * Useful when the current day has no events (e.g., polar night or day).
     *
     * @param latitude The geographic latitude in degrees.
     * @param longitude The geographic longitude in degrees.
     * @param dateTime The current date and time with timezone information.
     * @return [SolarRelativeEventDay] containing solar events with relative times,
     *         their time offsets in milliseconds, horizon crossing states,
     *         and other solar day parameters.
     */
    fun findUpcomingSolarRelativeEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRelativeEventDay

    /**
     * Calculates all solar events for the given day at a specific geographic location.
     *
     * Events include sunrise, sunset, and optionally meridian crossings.
     * This method does not include time offsets.
     *
     * @param latitude The geographic latitude in degrees.
     * @param longitude The geographic longitude in degrees.
     * @param dateTime The target date and time with timezone information.
     * @return [SolarEventDay] containing sunrise/sunset events and other solar metadata for the day.
     */
    fun calculateSolarEventDay(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarEventDay

    /**
     * Computes the magic hour time intervals for a specific location and date.
     *
     * Magic hour is defined as the period when the Sun is between -4.0° and +6.0°
     * relative to the geometric horizon, producing warm, soft, low-angle sunlight ideal for photography and cinematography.
     * Atmospheric refraction is not included in these bounds.
     *
     * @param latitude Latitude of the location in decimal degrees.
     * @param longitude Longitude of the location in decimal degrees.
     * @param dateTime The target date and time including time zone information.
     * @return [SolarRingEventDay] object containing the computed magic hour intervals and daylight/nighttime durations.
     */
    fun calculateMagicHourPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay

    /**
     * Calculates the blue hour period for the specified date and location.
     *
     * Blue hour is defined as the period when the Sun is between -6.0° and -4.0°
     * below the geometric horizon, creating rich blue tones in the sky shortly before sunrise and after sunset.
     * Atmospheric refraction is not included.
     *
     * @param latitude Geographic latitude in degrees.
     * @param longitude Geographic longitude in degrees.
     * @param dateTime Date and time (with time zone) for which to compute the period.
     * @return [SolarRingEventDay] containing the blue hour intervals and the remaining clean day/night durations.
     */
    fun calculateBlueHourPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay

    /**
     * Calculates the civil twilight period for the given location and date.
     *
     * Civil twilight occurs when the Sun is between -6.0° and 0.0° relative to the horizon.
     * The upper boundary includes atmospheric refraction, making the Sun appear on the visible horizon.
     * This period provides enough natural light for most outdoor activities without artificial lighting.
     *
     * @param latitude Geographic latitude in degrees.
     * @param longitude Geographic longitude in degrees.
     * @param dateTime Date and time (with time zone) for which to compute the period.
     * @return [SolarRingEventDay] with civil twilight intervals and corresponding clean day/night durations.
     */
    fun calculateCivilTwilightPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay

    /**
     * Calculates the nautical twilight period for the given location and date.
     *
     * Nautical twilight spans the time when the Sun is between -12.0° and -6.0°
     * below the geometric horizon. It is dark enough for horizon-based marine navigation
     * while still providing some indirect sunlight.
     *
     * @param latitude Geographic latitude in degrees.
     * @param longitude Geographic longitude in degrees.
     * @param dateTime Date and time (with time zone) for which to compute the period.
     * @return [SolarRingEventDay] with nautical twilight intervals and clean day/night durations.
     */
    fun calculateNauticalTwilightPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay

    /**
     * Calculates the astronomical twilight period for the given date and location.
     *
     * Astronomical twilight occurs when the Sun is between -18.0° and -12.0°
     * below the geometric horizon. This marks the start and end of full night,
     * providing optimal darkness for astronomical observations.
     *
     * @param latitude Geographic latitude in degrees.
     * @param longitude Geographic longitude in degrees.
     * @param dateTime Date and time (with time zone) for which to compute the period.
     * @return [SolarRingEventDay] containing astronomical twilight intervals and clean day/night durations.
     */
    fun calculateAstronomicalTwilightPeriod(
        latitude: Double,
        longitude: Double,
        dateTime: ZonedDateTime
    ): SolarRingEventDay
}