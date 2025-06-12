package io.github.msaggik.celestialnavevents.internal.common

import io.github.msaggik.celestialnavevents.internal.model.events.AstronomicalEventResult
import io.github.msaggik.celestialnavevents.internal.model.position.Position
import io.github.msaggik.celestialnavevents.internal.model.settings.HorizonCorrection
import io.github.msaggik.celestialnavevents.model.events.common.riseset.Event
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.measurement.Coordinate
import io.github.msaggik.celestialnavevents.model.measurement.Time
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
 * Provides utility functions for astronomical computations involving celestial object positioning
 * and event detection such as rise and set times.
 *
 * This object encapsulates reusable algorithms for:
 * - Correcting discontinuities in right ascension across days;
 * - Calculating local sidereal time based on observer location and time zone;
 * - Interpolating rise/set events within hourly intervals using vertical position analysis;
 * - Determining horizon crossings using trigonometric models.
 *
 * The methods are designed to be applicable to any celestial object (e.g., Sun, Moon) given its
 * ephemeris data and are intended to support high-precision astronomical event timing.
 */
object AstronomicalUtility {

    /**
     * Adjusts the right ascension value for the next day to ensure continuity across celestial cycles.
     *
     * This is particularly important when interpolating positions over time, as right ascension
     * values reset after a full 24-hour rotation (2π radians). This method compensates for
     * discontinuity to maintain smooth interpolation.
     *
     * @param today The celestial object's position on the current day.
     * @param tomorrow The celestial object's position on the following day.
     * @return A [Position] with corrected right ascension if wrapping occurred.
     */
    internal fun adjustForNextAscension(today: Position, tomorrow: Position): Position {
        val adjustedAscension = if (tomorrow.rightAscension < today.rightAscension) {
            tomorrow.rightAscension + 2 * Math.PI
        } else {
            tomorrow.rightAscension
        }
        return Position(adjustedAscension, tomorrow.declination)
    }

    /**
     * Computes the local sidereal time at midnight for a given observer.
     *
     * Sidereal time aligns Earth's rotation with celestial coordinates and is used
     * as a fundamental reference in determining the hour angle of celestial objects.
     *
     * @param daysSinceEpoch Number of days since the J2000.0 epoch.
     * @param longitude Observer's longitude in degrees (positive eastward).
     * @param timeZoneShift Time zone offset from UTC.
     * @return Local sidereal time in radians.
     */
    internal fun calculateLocalStellarTime(
        daysSinceEpoch: Double,
        longitude: Double,
        timeZoneShift: Double
    ): Double {
        val longitudeFraction = longitude / 360.0

        var siderealTime = 24110.5 +
                8640184.813 * (daysSinceEpoch / 36525.0) +
                86636.6 * timeZoneShift +
                86400.0 * longitudeFraction

        siderealTime /= 86400.0
        siderealTime -= siderealTime.toInt()

        return siderealTime * 360.0 * RADIANS_PER_DEGREE
    }

    /**
     * Estimates celestial event times (e.g. rise or set) within a given hour interval using interpolation.
     *
     * Applies a parabolic model to the vertical position of the object to determine if, and when,
     * it crosses the observer’s horizon. Suitable for use with any celestial body whose
     * ephemeris and apparent position are known.
     *
     * @param hourOfDay The hour of the day (0–23) for the evaluated time interval.
     * @param horizonCorrection Correction for refraction and apparent horizon displacement.
     * @param previousRightAscension Right ascension at the start of the interval.
     * @param currentRightAscension Right ascension at the end of the interval.
     * @param previousDeclination Declination at the start of the interval.
     * @param currentDeclination Declination at the end of the interval.
     * @param previousVerticalPosition Vertical position (height above horizon) at interval start.
     * @param observerCoordinate Observer's geographic coordinates.
     * @param localStellarTime Local sidereal time in radians at midnight.
     * @return [AstronomicalEventResult] containing any detected horizon crossings and the final vertical position.
     */
    internal fun calculateEventTiming(
        hourOfDay: Int,
        horizonCorrection: HorizonCorrection,
        previousRightAscension: Double,
        currentRightAscension: Double,
        previousDeclination: Double,
        currentDeclination: Double,
        previousVerticalPosition: Double,
        observerCoordinate: Coordinate,
        localStellarTime: Double
    ): AstronomicalEventResult {
        var prevVerticalPosition = previousVerticalPosition
        val events: MutableList<Event> = mutableListOf()

        val zenithDistance =
            RADIANS_PER_DEGREE * (if (horizonCorrection.isAtmosphericRefractionIncluded) 90.833 else 90.0)

        val sinLatitude = sin(observerCoordinate.latitude * RADIANS_PER_DEGREE)
        val cosLatitude = cos(observerCoordinate.latitude * RADIANS_PER_DEGREE)
        val adjustedZenith =
            cos(zenithDistance) + horizonCorrection.angleFromHorizon * RADIANS_PER_DEGREE

        val localSiderealTime = localStellarTime + hourOfDay * EARTH_ROTATION_COEFFICIENT
        val nextSiderealTime = localStellarTime + (hourOfDay + 1) * EARTH_ROTATION_COEFFICIENT

        val hourAngleStart = localSiderealTime - previousRightAscension
        val hourAngleEnd = nextSiderealTime - currentRightAscension

        val averageHourAngle = (hourAngleEnd + hourAngleStart) / 2.0
        val averageDeclination = (currentDeclination + previousDeclination) / 2.0

        if (hourOfDay == 0) {
            prevVerticalPosition =
                sinLatitude * sin(previousDeclination) +
                        cosLatitude * cos(previousDeclination) * cos(hourAngleStart) -
                        adjustedZenith
        }

        val verticalPosition =
            sinLatitude * sin(currentDeclination) +
                    cosLatitude * cos(currentDeclination) * cos(hourAngleEnd) -
                    adjustedZenith

        if (hasObjectCrossedHorizon(prevVerticalPosition, verticalPosition)) {
            val verticalPositionAverage =
                sinLatitude * sin(averageDeclination) +
                        cosLatitude * cos(averageDeclination) * cos(averageHourAngle) -
                        adjustedZenith

            val verticalPositionCoefficient =
                2 * verticalPosition -
                        4 * verticalPositionAverage +
                        2 * prevVerticalPosition
            val verticalPositionAdjustment =
                4 * verticalPositionAverage -
                        3 * prevVerticalPosition -
                        verticalPosition
            var discriminant =
                verticalPositionAdjustment * verticalPositionAdjustment -
                        4 * verticalPositionCoefficient * prevVerticalPosition

            if (discriminant >= 0) {
                discriminant = sqrt(discriminant)

                var eventParameter =
                    (-verticalPositionAdjustment + discriminant) / (2 * verticalPositionCoefficient)
                if (eventParameter > 1 || eventParameter < 0) {
                    eventParameter =
                        (-verticalPositionAdjustment - discriminant) / (2 * verticalPositionCoefficient)
                }

                val hourAngleAtEvent =
                    hourAngleStart + eventParameter * (hourAngleEnd - hourAngleStart)
                val northComponent = -1 * cos(averageDeclination) * sin(hourAngleAtEvent)
                val denominator =
                    cosLatitude * sin(averageDeclination) -
                            sinLatitude * cos(averageDeclination) * cos(hourAngleAtEvent)
                var azimuth = atan(northComponent / denominator) / RADIANS_PER_DEGREE

                if (denominator < 0) {
                    azimuth += 180
                }

                if (azimuth < 0) {
                    azimuth += 360
                }

                if (azimuth > 360) {
                    azimuth -= 360
                }

                val roundedEventTime = hourOfDay + eventParameter
                val time = Time.fromTotalMilliseconds((roundedEventTime * 60.0 * 60.0 * 1000.0).toLong())

                if (prevVerticalPosition < 0 && verticalPosition > 0) {
                    val eventRise = Event(
                        type = EventType.RISE,
                        time = time,
                        azimuth = azimuth
                    )
                    events.add(eventRise)
                }

                if (prevVerticalPosition > 0 && verticalPosition < 0) {
                    val eventRise = Event(
                        type = EventType.SET,
                        time = time,
                        azimuth = azimuth
                    )
                    events.add(eventRise)
                }
            }
        }

        return AstronomicalEventResult(
            events = events,
            verticalPosition = verticalPosition
        )
    }

    /**
     * Checks whether a celestial object crossed the observer’s horizon during a time interval.
     *
     * Detects a transition between above-horizon and below-horizon states based on
     * changes in vertical position.
     *
     * @param previousValue Vertical position at the start of the interval.
     * @param currentValue Vertical position at the end of the interval.
     * @return True if the object crossed the horizon; otherwise, false.
     */
    private fun hasObjectCrossedHorizon(previousValue: Double, currentValue: Double): Boolean {
        return (previousValue > 0) != (currentValue > 0)
    }
}