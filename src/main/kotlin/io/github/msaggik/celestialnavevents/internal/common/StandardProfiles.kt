package io.github.msaggik.celestialnavevents.internal.common

import io.github.msaggik.celestialnavevents.internal.model.settings.HorizonCorrection
import io.github.msaggik.celestialnavevents.internal.model.settings.HorizonCorrectionTrack
import io.github.msaggik.celestialnavevents.model.events.common.track.TypeEventTrack

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
 * Astronomical constants used throughout the library.
 */

/**
 * Length of a lunar month in days.
 */
internal const val LUNAR_MONTH_DAYS: Double = 29.530588853

/**
 * Julian date corresponding to January 1, 2000 (astronomical epoch standard).
 */
internal const val JULIAN_DATE_J2000: Double = 2451545.0

/**
 * Number of days in a century, including leap days (365 * 100 + 25).
 */
internal const val DAYS_PER_CENTURY: Int = 36525

/**
 * Number of hours in a day.
 */
internal const val HOURS_PER_DAY: Int = 24

/**
 * Number of degrees in one time zone (used for time zone calculations).
 */
internal const val DEGREES_PER_TIME_ZONE: Int = 15

/**
 * Conversion factor from degrees to radians.
 */
internal const val RADIANS_PER_DEGREE: Double = Math.PI / 180.0

/**
 * Correction factor for Earth's rotation relative to celestial objects.
 * The factor 1.0027379 accounts for the difference between solar and sidereal time.
 */
internal const val EARTH_ROTATION_COEFFICIENT: Double = 15.0 * RADIANS_PER_DEGREE * 1.0027379

/**
 * Number of milliseconds in one day.
 */
internal const val NUMBER_MILLIS_DAY: Long = 86_400_000L


/**
 * A collection of standard angular offsets and event tracks used in astronomical
 * and visual light-phase calculations for the Sun and Moon.
 *
 * This object centralizes reusable `HorizonCorrection` and `HorizonCorrectionTrack` values
 * for common photometric intervals such as twilight phases, magic hour, and moonrise/moonset.
 *
 * Offsets are based on typical observational standards and may include or exclude
 * atmospheric refraction depending on the nature of the event.
 *
 * All angles are measured in degrees from the geometric horizon:
 * - Positive values = above horizon
 * - Negative values = below horizon
 */
internal object StandardProfiles {

    /**
     * Horizon correction for sunrise/sunset time calculations.
     * Includes atmospheric refraction (~0.833°) and apparent solar radius.
     */
    val SUNRISE_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = 0.0,
        isAtmosphericRefractionIncluded = true
    )

    /**
     * Lower boundary of the magic hour interval.
     * This offset does not account for atmospheric refraction.
     */
    val MAGIC_HOUR_LOWER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -4.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Upper boundary of the magic hour interval.
     * Typically set to ~6°, corresponding to soft warm lighting before sunset or after sunrise.
     * This value does not include atmospheric refraction.
     */
    val MAGIC_HOUR_UPPER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = 6.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Defines the full angular span of the magic hour interval.
     */
    val MAGIC_HOUR_TRACK_OFFSET: HorizonCorrectionTrack = HorizonCorrectionTrack(
        lowerHorizonCorrection = MAGIC_HOUR_LOWER_OFFSET,
        upperHorizonCorrection = MAGIC_HOUR_UPPER_OFFSET,
        typeEventTrack = TypeEventTrack.MAGIC_HOUR
    )

    /**
     * Lower boundary of the blue hour — occurs before sunrise or after sunset.
     */
    val BLUE_HOUR_LOWER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -6.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Upper boundary of the blue hour — transitions into civil twilight or magic hour.
     */
    val BLUE_HOUR_UPPER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -4.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Defines the angular span of the blue hour interval.
     */
    val BLUE_HOUR_TRACK_OFFSET: HorizonCorrectionTrack = HorizonCorrectionTrack(
        lowerHorizonCorrection = BLUE_HOUR_LOWER_OFFSET,
        upperHorizonCorrection = BLUE_HOUR_UPPER_OFFSET,
        typeEventTrack = TypeEventTrack.BLUE_HOUR
    )

    /**
     * Lower (dark) boundary of civil twilight — when the Sun is approximately 6° below the horizon.
     */
    val CIVIL_TWILIGHT_LOWER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -6.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Upper (bright) boundary of civil twilight — the Sun is at the visible horizon.
     * Includes atmospheric refraction.
     */
    val CIVIL_TWILIGHT_UPPER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = 0.0,
        isAtmosphericRefractionIncluded = true
    )

    /**
     * Defines the angular span of civil twilight.
     */
    val CIVIL_TWILIGHT_TRACK_OFFSET: HorizonCorrectionTrack = HorizonCorrectionTrack(
        lowerHorizonCorrection = CIVIL_TWILIGHT_LOWER_OFFSET,
        upperHorizonCorrection = CIVIL_TWILIGHT_UPPER_OFFSET,
        typeEventTrack = TypeEventTrack.CIVIL_TWILIGHT
    )

    /**
     * Lower boundary of nautical twilight — ends when the Sun is ~12° below the horizon.
     */
    val NAUTICAL_TWILIGHT_LOWER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -12.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Upper boundary of nautical twilight — begins when the Sun is ~6° below the horizon.
     */
    val NAUTICAL_TWILIGHT_UPPER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -6.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Defines the angular span of nautical twilight.
     */
    val NAUTICAL_TWILIGHT_TRACK_OFFSET: HorizonCorrectionTrack = HorizonCorrectionTrack(
        lowerHorizonCorrection = NAUTICAL_TWILIGHT_LOWER_OFFSET,
        upperHorizonCorrection = NAUTICAL_TWILIGHT_UPPER_OFFSET,
        typeEventTrack = TypeEventTrack.NAUTICAL_TWILIGHT
    )

    /**
     * Lower boundary of astronomical twilight — ends when the Sun is ~18° below the horizon,
     * marking the start of full night.
     */
    val ASTRONOMICAL_TWILIGHT_LOWER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -18.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Upper boundary of astronomical twilight — starts when the Sun is ~12° below the horizon.
     */
    val ASTRONOMICAL_TWILIGHT_UPPER_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = -12.0,
        isAtmosphericRefractionIncluded = false
    )

    /**
     * Defines the angular span of astronomical twilight.
     */
    val ASTRONOMICAL_TWILIGHT_TRACK_OFFSET: HorizonCorrectionTrack = HorizonCorrectionTrack(
        lowerHorizonCorrection = ASTRONOMICAL_TWILIGHT_LOWER_OFFSET,
        upperHorizonCorrection = ASTRONOMICAL_TWILIGHT_UPPER_OFFSET,
        typeEventTrack = TypeEventTrack.ASTRONOMICAL_TWILIGHT
    )

    /**
     * Offset used for moonrise and moonset calculations.
     * Does not include atmospheric refraction.
     */
    val MOONRISE_OFFSET: HorizonCorrection = HorizonCorrection(
        angleFromHorizon = 0.0,
        isAtmosphericRefractionIncluded = false
    )
}