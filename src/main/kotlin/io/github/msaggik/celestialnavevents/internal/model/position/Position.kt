package io.github.msaggik.celestialnavevents.internal.model.position

import io.github.msaggik.celestialnavevents.internal.common.DAYS_PER_CENTURY
import io.github.msaggik.celestialnavevents.internal.common.DEGREES_PER_TIME_ZONE
import io.github.msaggik.celestialnavevents.internal.common.RADIANS_PER_DEGREE
import java.util.Locale
import kotlin.math.asin
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
 * Represents a celestial object's position using equatorial coordinates:
 * right ascension and declination, both in radians.
 *
 * Right ascension is expressed in hours (HMS), declination in degrees (DMS).
 *
 * @param rightAscension Right ascension in radians.
 * @param declination Declination in radians.
 */
internal class Position(
    val rightAscension: Double,
    val declination: Double
) {
    override fun toString(): String {
        val raHms = toHMS(rightAscension / RADIANS_PER_DEGREE / DEGREES_PER_TIME_ZONE)
        val decDms = toDMS(declination / RADIANS_PER_DEGREE)
        return "Position (RA: $raHms, Dec: $decDms)"
    }

    private fun toHMS(decimalHours: Double): String {
        val h = decimalHours.toInt()
        val m = ((decimalHours - h) * 60).toInt()
        val s = ((decimalHours - h) * 60 - m) * 60
        return String.format(Locale.US, "%dh %dm %.2fs", h, m, s)
    }

    private fun toDMS(decimalDegrees: Double): String {
        val d = decimalDegrees.toInt()
        val m = ((decimalDegrees - d) * 60).toInt()
        val s = ((decimalDegrees - d) * 60 - m) * 60
        return String.format(Locale.US, "%d° %d′ %.2f″", d, m, s)
    }

    /**
     * Companion object containing low-precision analytical models for computing the geocentric
     * ecliptic positions of the Sun and the Moon, intended for efficient astronomical approximations.
     *
     * These formulas provide sufficient accuracy (typically within 1°) for general-purpose applications
     * such as ephemerides, calendars, and astronomical visualizations.
     */
    companion object {

        /**
         * Calculates the low-precision **geocentric ecliptic longitude and distance of the Sun**
         * for a given number of days since the reference epoch (Julian Day 2415020.0).
         *
         * This method models the apparent position of the Sun as seen from Earth by computing
         * the Earth's heliocentric position and then projecting it into the geocentric frame.
         *
         * The implementation uses trigonometric series that account for the Earth's orbital motion
         * and includes perturbative terms primarily due to Venus, Mars, Jupiter, and the Moon.
         *
         * ### Reference:
         * van Flandern, T.C., & Pulkkinen, K.F. (1979).
         * "Low-precision formulae for planetary positions", *Astrophysical Journal Supplement Series*, 41, 391–411.
         *
         * @param daysSinceEpoch Number of days since the reference epoch (JD 2415020.0).
         * @return [Position] object containing the Sun’s geocentric ecliptic longitude (in radians)
         *         and distance (in astronomical units, AU).
         *
         * @see calculateMoonPosition for the Moon’s position.
         */
        internal fun calculateSunPosition(daysSinceEpoch: Double): Position {
            val numCenturiesSince1900 = daysSinceEpoch / DAYS_PER_CENTURY + 1
            val meanLongitudeOfSun =
                normalizeToRadians(.779072 + .00273790931 * daysSinceEpoch)
            val meanAnomalyOfSun = normalizeToRadians(.993126 + .00273777850 * daysSinceEpoch)
            val meanLongitudeOfMoon =
                normalizeToRadians(.606434 + .03660110129 * daysSinceEpoch)
            val longitudeOfLunarAscendingNode =
                normalizeToRadians(.347343 - .00014709391 * daysSinceEpoch)
            val meanAnomalyOfVenus =
                normalizeToRadians(.140023 + .00445036173 * daysSinceEpoch)
            val meanAnomalyOfMars = normalizeToRadians(.053856 + .00145561327 * daysSinceEpoch)
            val meanAnomalyOfJupiter =
                normalizeToRadians(.056531 + .00023080893 * daysSinceEpoch)
            val solarDistanceAdjustment = .39785 * sin(meanLongitudeOfSun) -
                    .01000 * sin(meanLongitudeOfSun - meanAnomalyOfSun) +
                    .00333 * sin(meanLongitudeOfSun + meanAnomalyOfSun) -
                    .00021 * numCenturiesSince1900 * sin(meanLongitudeOfSun) +
                    .00004 * sin(meanLongitudeOfSun + 2 * meanAnomalyOfSun) -
                    .00004 * cos(meanLongitudeOfSun) -
                    .00004 * sin(longitudeOfLunarAscendingNode - meanLongitudeOfSun) +
                    .00003 * numCenturiesSince1900 * sin(meanLongitudeOfSun - meanAnomalyOfSun)
            val orbitalCorrectionFactor = 1 - .03349 * cos(meanAnomalyOfSun) -
                    .00014 * cos(2 * meanLongitudeOfSun) +
                    .00008 * cos(meanLongitudeOfSun) -
                    .00003 * sin(meanAnomalyOfSun - meanAnomalyOfJupiter)
            val angularVelocityAdjustment = -.04129 * sin(2 * meanLongitudeOfSun) +
                    .03211 * sin(meanAnomalyOfSun) +
                    .00104 * sin(2 * meanLongitudeOfSun - meanAnomalyOfSun) -
                    .00035 * sin(2 * meanLongitudeOfSun + meanAnomalyOfSun) -
                    .00010 -
                    .00008 * numCenturiesSince1900 * sin(meanAnomalyOfSun) -
                    .00008 * sin(longitudeOfLunarAscendingNode) +
                    .00007 * sin(2 * meanAnomalyOfSun) +
                    .00005 * numCenturiesSince1900 * sin(2 * meanLongitudeOfSun) +
                    .00003 * sin(meanLongitudeOfMoon - meanLongitudeOfSun) -
                    .00002 * cos(meanAnomalyOfSun - meanAnomalyOfJupiter) +
                    .00002 * sin(4 * meanAnomalyOfSun - 8 * meanAnomalyOfMars + 3 * meanAnomalyOfJupiter) -
                    .00002 * sin(meanAnomalyOfSun - meanAnomalyOfVenus) -
                    .00002 * cos(2 * meanAnomalyOfSun - 2 * meanAnomalyOfVenus)

            return calculatePosition(
                meanLongitudeOfSun,
                orbitalCorrectionFactor,
                solarDistanceAdjustment,
                angularVelocityAdjustment
            )
        }

        /**
         * Calculates the low-precision **geocentric ecliptic longitude and distance of the Moon**
         * for a given number of days since the reference epoch (Julian Day 2415020.0).
         *
         * This method is based on simplified lunar theory, incorporating the Moon’s mean anomaly,
         * elongation from the Sun, and node longitude, along with perturbative terms caused by
         * the Sun and planets (notably Venus).
         *
         * The resulting position is suitable for approximate Moon tracking and astronomical event prediction,
         * with typical accuracy within ~1°.
         *
         * ### Reference:
         * van Flandern, T.C., & Pulkkinen, K.F. (1979).
         * "Low-precision formulae for planetary positions", *Astrophysical Journal Supplement Series*, 41, 391–411.
         *
         * @param daysSinceEpoch Number of days since the reference epoch (JD 2415020.0).
         * @return [Position] object containing the Moon’s geocentric ecliptic longitude (in radians)
         *         and distance (in Earth radii).
         *
         * @see calculateSunPosition for the Sun’s position.
         */
        internal fun calculateMoonPosition(daysSinceEpoch: Double): Position {
            val numCenturiesSince1900 = daysSinceEpoch / DAYS_PER_CENTURY + 1
            val meanLongitudeOfMoon =
                normalizeToRadians(.606434 + .03660110129 * daysSinceEpoch)
            val moonAnomaly =
                normalizeToRadians(.374897 + .03629164709 * daysSinceEpoch)
            val moonLatitude =
                normalizeToRadians(.259091 + .03674819520 * daysSinceEpoch)
            val moonElongation =
                normalizeToRadians(.827362 + .03386319198 * daysSinceEpoch)
            val lunarNodeLongitude =
                normalizeToRadians(.347343 - .00014709391 * daysSinceEpoch)
            val sunLongitude =
                normalizeToRadians(.779072 + .00273790931 * daysSinceEpoch)
            val sunAnomaly =
                normalizeToRadians(.993126 + .00273777850 * daysSinceEpoch)
            val venusLongitude =
                normalizeToRadians(0.505498 + .00445046867 * daysSinceEpoch)

            val solarDistanceAdjustment = .39558 * sin(moonLatitude + lunarNodeLongitude) +
                    .08200 * sin(moonLatitude) +
                    .03257 * sin(moonAnomaly - moonLatitude - lunarNodeLongitude) +
                    .01092 * sin(moonAnomaly + moonLatitude + lunarNodeLongitude) +
                    .00666 * sin(moonAnomaly - moonLatitude) -
                    .00644 * sin(moonAnomaly + moonLatitude - 2 * moonElongation + lunarNodeLongitude) -
                    .00331 * sin(moonLatitude - 2 * moonElongation + lunarNodeLongitude) -
                    .00304 * sin(moonLatitude - 2 * moonElongation) -
                    .00240 * sin(moonAnomaly - moonLatitude - 2 * moonElongation - lunarNodeLongitude) +
                    .00226 * sin(moonAnomaly + moonLatitude) -
                    .00108 * sin(moonAnomaly + moonLatitude - 2 * moonElongation) -
                    .00079 * sin(moonLatitude - lunarNodeLongitude) +
                    .00078 * sin(moonLatitude + 2 * moonElongation + lunarNodeLongitude) +
                    .00066 * sin(moonLatitude + lunarNodeLongitude - sunAnomaly) -
                    .00062 * sin(moonLatitude + lunarNodeLongitude + sunAnomaly) -
                    .00050 * sin(moonAnomaly - moonLatitude - 2 * moonElongation) +
                    .00045 * sin(2 * moonAnomaly + moonLatitude + lunarNodeLongitude) -
                    .00031 * sin(2 * moonAnomaly + moonLatitude - 2 * moonElongation + lunarNodeLongitude) -
                    .00027 * sin(moonAnomaly + moonLatitude - 2 * moonElongation + lunarNodeLongitude + sunAnomaly) -
                    .00024 * sin(moonLatitude - 2 * moonElongation + lunarNodeLongitude + sunAnomaly) -
                    .00021 * numCenturiesSince1900 * sin(moonLatitude + lunarNodeLongitude) +
                    .00018 * sin(moonLatitude - moonElongation + lunarNodeLongitude) +
                    .00016 * sin(moonLatitude + 2 * moonElongation) +
                    .00016 * sin(moonAnomaly - moonLatitude - lunarNodeLongitude - sunAnomaly) -
                    .00016 * sin(2 * moonAnomaly - moonLatitude - lunarNodeLongitude) -
                    .00015 * sin(moonLatitude - 2 * moonElongation + sunAnomaly) -
                    .00012 * sin(moonAnomaly - moonLatitude - 2 * moonElongation - lunarNodeLongitude + sunAnomaly) -
                    .00011 * sin(moonAnomaly - moonLatitude - lunarNodeLongitude + sunAnomaly) +
                    .00009 * sin(moonAnomaly + moonLatitude + lunarNodeLongitude - sunAnomaly) +
                    .00009 * sin(2 * moonAnomaly + moonLatitude) +
                    .00008 * sin(2 * moonAnomaly - moonLatitude) +
                    .00008 * sin(moonAnomaly + moonLatitude + 2 * moonElongation + lunarNodeLongitude) -
                    .00008 * sin(3 * moonLatitude - 2 * moonElongation + lunarNodeLongitude) +
                    .00007 * sin(moonAnomaly - moonLatitude + 2 * moonElongation) -
                    .00007 * sin(2 * moonAnomaly - moonLatitude - 2 * moonElongation - lunarNodeLongitude) -
                    .00007 * sin(moonAnomaly + moonLatitude + lunarNodeLongitude + sunAnomaly) -
                    .00006 * sin(moonLatitude + moonElongation + lunarNodeLongitude) +
                    .00006 * sin(moonLatitude - 2 * moonElongation - sunAnomaly) +
                    .00006 * sin(moonAnomaly - moonLatitude + lunarNodeLongitude) +
                    .00006 * sin(moonLatitude + 2 * moonElongation + lunarNodeLongitude - sunAnomaly) -
                    .00005 * sin(moonAnomaly + moonLatitude - 2 * moonElongation + sunAnomaly) -
                    .00004 * sin(2 * moonAnomaly + moonLatitude - 2 * moonElongation) +
                    .00004 * sin(moonAnomaly - 3 * moonLatitude - lunarNodeLongitude) +
                    .00004 * sin(moonAnomaly - moonLatitude - sunAnomaly) -
                    .00003 * sin(moonAnomaly - moonLatitude + sunAnomaly) +
                    .00003 * sin(moonLatitude - moonElongation) +
                    .00003 * sin(moonLatitude - 2 * moonElongation + lunarNodeLongitude - sunAnomaly) -
                    .00003 * sin(moonLatitude - 2 * moonElongation - lunarNodeLongitude) +
                    .00003 * sin(moonAnomaly + moonLatitude - 2 * moonElongation + lunarNodeLongitude - sunAnomaly) +
                    .00003 * sin(moonLatitude - sunAnomaly) -
                    .00003 * sin(moonLatitude - moonElongation + lunarNodeLongitude - sunAnomaly) -
                    .00002 * sin(moonAnomaly - moonLatitude - 2 * moonElongation + sunAnomaly) -
                    .00002 * sin(moonLatitude + sunAnomaly) +
                    .00002 * sin(moonAnomaly + moonLatitude - moonElongation + lunarNodeLongitude) -
                    .00002 * sin(moonAnomaly + moonLatitude - lunarNodeLongitude) +
                    .00002 * sin(3 * moonAnomaly + moonLatitude + lunarNodeLongitude) -
                    .00002 * sin(2 * moonAnomaly - moonLatitude - 4 * moonElongation - lunarNodeLongitude) +
                    .00002 * sin(moonAnomaly - moonLatitude - 2 * moonElongation - lunarNodeLongitude - sunAnomaly) -
                    .00002 * numCenturiesSince1900 * sin(moonAnomaly - moonLatitude - lunarNodeLongitude) -
                    .00002 * sin(moonAnomaly - moonLatitude - 4 * moonElongation - lunarNodeLongitude) -
                    .00002 * sin(moonAnomaly + moonLatitude - 4 * moonElongation) -
                    .00002 * sin(2 * moonAnomaly - moonLatitude - 2 * moonElongation) +
                    .00002 * sin(moonAnomaly + moonLatitude + 2 * moonElongation) +
                    .00002 * sin(moonAnomaly + moonLatitude - sunAnomaly)
            val orbitalCorrectionFactor = 1 -
                    .10828 * cos(moonAnomaly) -
                    .01880 * cos(moonAnomaly - 2 * moonElongation) -
                    .01479 * cos(2 * moonElongation) +
                    .00181 * cos(2 * moonAnomaly - 2 * moonElongation) -
                    .00147 * cos(2 * moonAnomaly) -
                    .00105 * cos(2 * moonElongation - sunAnomaly) -
                    .00075 * cos(moonAnomaly - 2 * moonElongation + sunAnomaly) -
                    .00067 * cos(moonAnomaly - sunAnomaly) +
                    .00057 * cos(moonElongation) +
                    .00055 * cos(moonAnomaly + sunAnomaly) -
                    .00046 * cos(moonAnomaly + 2 * moonElongation) +
                    .00041 * cos(moonAnomaly - 2 * moonLatitude) +
                    .00024 * cos(sunAnomaly) +
                    .00017 * cos(2 * moonElongation + sunAnomaly) -
                    .00013 * cos(moonAnomaly - 2 * moonElongation - sunAnomaly) -
                    .00010 * cos(moonAnomaly - 4 * moonElongation) -
                    .00009 * cos(moonElongation + sunAnomaly) +
                    .00007 * cos(2 * moonAnomaly - 2 * moonElongation + sunAnomaly) +
                    .00006 * cos(3 * moonAnomaly - 2 * moonElongation) +
                    .00006 * cos(2 * moonLatitude - 2 * moonElongation) -
                    .00005 * cos(2 * moonElongation - 2 * sunAnomaly) -
                    .00005 * cos(2 * moonAnomaly - 4 * moonElongation) +
                    .00005 * cos(moonAnomaly + 2 * moonLatitude - 2 * moonElongation) -
                    .00005 * cos(moonAnomaly - moonElongation) -
                    .00004 * cos(moonAnomaly + 2 * moonElongation - sunAnomaly) -
                    .00004 * cos(3 * moonAnomaly) -
                    .00003 * cos(moonAnomaly - 4 * moonElongation + sunAnomaly) -
                    .00003 * cos(2 * moonAnomaly - 2 * moonLatitude) -
                    .00003 * cos(2 * moonLatitude)
            val angularVelocityAdjustment = .10478 * sin(moonAnomaly) -
                    .04105 * sin(2 * moonLatitude + 2 * lunarNodeLongitude) -
                    .02130 * sin(moonAnomaly - 2 * moonElongation) -
                    .01779 * sin(2 * moonLatitude + lunarNodeLongitude) +
                    .01774 * sin(lunarNodeLongitude) +
                    .00987 * sin(2 * moonElongation) -
                    .00338 * sin(moonAnomaly - 2 * moonLatitude - 2 * lunarNodeLongitude) -
                    .00309 * sin(sunAnomaly) -
                    .00190 * sin(2 * moonLatitude) -
                    .00144 * sin(moonAnomaly + lunarNodeLongitude) -
                    .00144 * sin(moonAnomaly - 2 * moonLatitude - lunarNodeLongitude) -
                    .00113 * sin(moonAnomaly + 2 * moonLatitude + 2 * lunarNodeLongitude) -
                    .00094 * sin(moonAnomaly - 2 * moonElongation + sunAnomaly) -
                    .00092 * sin(2 * moonAnomaly - 2 * moonElongation) +
                    .00071 * sin(2 * moonElongation - sunAnomaly) +
                    .00070 * sin(2 * moonAnomaly) +
                    .00067 * sin(moonAnomaly + 2 * moonLatitude - 2 * moonElongation + 2 * lunarNodeLongitude) +
                    .00066 * sin(2 * moonLatitude - 2 * moonElongation + lunarNodeLongitude) -
                    .00066 * sin(2 * moonElongation + lunarNodeLongitude) +
                    .00061 * sin(moonAnomaly - sunAnomaly) -
                    .00058 * sin(moonElongation) -
                    .00049 * sin(moonAnomaly + 2 * moonLatitude + lunarNodeLongitude) -
                    .00049 * sin(moonAnomaly - lunarNodeLongitude) -
                    .00042 * sin(moonAnomaly + sunAnomaly) +
                    .00034 * sin(2 * moonLatitude - 2 * moonElongation + 2 * lunarNodeLongitude) -
                    .00026 * sin(2 * moonLatitude - 2 * moonElongation) +
                    .00025 * sin(moonAnomaly - 2 * moonLatitude - 2 * moonElongation - 2 * lunarNodeLongitude) +
                    .00024 * sin(moonAnomaly - 2 * moonLatitude) +
                    .00023 * sin(moonAnomaly + 2 * moonLatitude - 2 * moonElongation + lunarNodeLongitude) +
                    .00023 * sin(moonAnomaly - 2 * moonElongation - lunarNodeLongitude) +
                    .00019 * sin(moonAnomaly + 2 * moonElongation) +
                    .00012 * sin(moonAnomaly - 2 * moonElongation - sunAnomaly) +
                    .00011 * sin(moonAnomaly - 2 * moonElongation + lunarNodeLongitude) +
                    .00011 * sin(moonAnomaly - 2 * moonLatitude - 2 * moonElongation - lunarNodeLongitude) -
                    .00010 * sin(2 * moonElongation + sunAnomaly) +
                    .00009 * sin(moonAnomaly - moonElongation) +
                    .00008 * sin(moonElongation + sunAnomaly) -
                    .00008 * sin(2 * moonLatitude + 2 * moonElongation + 2 * lunarNodeLongitude) -
                    .00008 * sin(2 * lunarNodeLongitude) -
                    .00007 * sin(2 * moonLatitude + 2 * lunarNodeLongitude - sunAnomaly) +
                    .00006 * sin(2 * moonLatitude + 2 * lunarNodeLongitude + sunAnomaly) -
                    .00005 * sin(moonAnomaly + 2 * moonLatitude) +
                    .00005 * sin(3 * moonAnomaly) -
                    .00005 * sin(moonAnomaly + 16 * sunLongitude - 18 * venusLongitude) -
                    .00005 * sin(2 * moonAnomaly + 2 * moonLatitude + 2 * lunarNodeLongitude) +
                    .00004 * numCenturiesSince1900 * sin(2 * moonLatitude + 2 * lunarNodeLongitude) +
                    .00004 * cos(moonAnomaly + 16 * sunLongitude - 18 * venusLongitude) -
                    .00004 * sin(moonAnomaly - 2 * moonLatitude + 2 * moonElongation) -
                    .00004 * sin(moonAnomaly - 4 * moonElongation) -
                    .00004 * sin(3 * moonAnomaly - 2 * moonElongation) -
                    .00004 * sin(2 * moonLatitude + 2 * moonElongation + lunarNodeLongitude) -
                    .00004 * sin(2 * moonElongation - lunarNodeLongitude) -
                    .00003 * sin(2 * sunAnomaly) -
                    .00003 * sin(moonAnomaly - 2 * moonElongation + 2 * sunAnomaly) +
                    .00003 * sin(2 * moonLatitude - 2 * moonElongation + lunarNodeLongitude + sunAnomaly) -
                    .00003 * sin(2 * moonElongation + lunarNodeLongitude - sunAnomaly) +
                    .00003 * sin(2 * moonAnomaly + 2 * moonLatitude - 2 * moonElongation + 2 * lunarNodeLongitude) +
                    .00003 * sin(2 * moonElongation - 2 * sunAnomaly) -
                    .00003 * sin(2 * moonAnomaly - 2 * moonElongation + sunAnomaly) +
                    .00003 * sin(moonAnomaly + 2 * moonLatitude - 2 * moonElongation + 2 * lunarNodeLongitude + sunAnomaly) -
                    .00003 * sin(2 * moonAnomaly - 4 * moonElongation) +
                    .00002 * sin(2 * moonLatitude - 2 * moonElongation + 2 * lunarNodeLongitude + sunAnomaly) -
                    .00002 * sin(2 * moonAnomaly + 2 * moonLatitude + lunarNodeLongitude) -
                    .00002 * sin(2 * moonAnomaly - lunarNodeLongitude) +
                    .00002 * numCenturiesSince1900 * cos(moonAnomaly + 16 * sunLongitude - 18 * venusLongitude) +
                    .00002 * sin(4 * moonElongation) -
                    .00002 * sin(2 * moonLatitude - moonElongation + 2 * lunarNodeLongitude) -
                    .00002 * sin(moonAnomaly + 2 * moonLatitude - 2 * moonElongation) -
                    .00002 * sin(2 * moonAnomaly + lunarNodeLongitude) -
                    .00002 * sin(2 * moonAnomaly - 2 * moonLatitude - lunarNodeLongitude) +
                    .00002 * sin(moonAnomaly + 2 * moonElongation - sunAnomaly) +
                    .00002 * sin(2 * moonAnomaly - sunAnomaly) -
                    .00002 * sin(moonAnomaly - 4 * moonElongation + sunAnomaly) +
                    .00002 * numCenturiesSince1900 * sin(moonAnomaly + 16 * sunLongitude - 18 * venusLongitude) -
                    .00002 * sin(moonAnomaly - 2 * moonLatitude - 2 * lunarNodeLongitude - sunAnomaly) +
                    .00002 * sin(2 * moonAnomaly - 2 * moonLatitude - 2 * lunarNodeLongitude) -
                    .00002 * sin(moonAnomaly + 2 * moonElongation + lunarNodeLongitude) -
                    .00002 * sin(moonAnomaly - 2 * moonLatitude + 2 * moonElongation - lunarNodeLongitude)

            return calculatePosition(
                meanLongitudeOfMoon,
                orbitalCorrectionFactor,
                solarDistanceAdjustment,
                angularVelocityAdjustment
            )
        }

        /**
         * Computes the celestial position in equatorial coordinates — specifically, the
         * **right ascension** and **declination** — based on intermediate orbital parameters.
         *
         * This method is used as part of analytical models to convert ecliptic orbital parameters
         * into equatorial coordinates suitable for observational astronomy.
         *
         * The algorithm accounts for orbital eccentricity and inclination to provide
         * an approximate position on the celestial sphere.
         *
         * @param meanLongitude The mean longitude (in radians), representing the uncorrected position.
         * @param orbitalCorrectionFactor The combined orbital correction term (unitless), typically involving distance and geometry.
         * @param verticalOffset A vertical adjustment term (in same units as orbital radius), representing projection onto the celestial sphere.
         * @param angularOffset Angular adjustment term (unitless), related to orbital velocity or perturbation.
         *
         * @return [Position] containing the computed right ascension and declination, in radians.
         */
        private fun calculatePosition(
            meanLongitude: Double,
            orbitalCorrectionFactor: Double,
            verticalOffset: Double,
            angularOffset: Double
        ): Position {
            val rightAscension =
                meanLongitude + asin(angularOffset / sqrt(orbitalCorrectionFactor - verticalOffset * verticalOffset))
            val declination = asin(verticalOffset / sqrt(orbitalCorrectionFactor))
            return Position(rightAscension, declination)
        }

        /**
         * Normalizes a fractional angle value (expressed in turns) to the **[0, 2π)** interval in radians.
         *
         * This method is used to convert values like mean anomalies or longitudes expressed as
         * fractional turns (e.g., 1.25 representing 450°) into standard angular values in radians.
         *
         * @param angle Fractional angle in turns, where 1.0 = full circle = 2π radians.
         * @return Normalized angle in radians within the range [0, 2π).
         */
        private fun normalizeToRadians(angle: Double): Double {
            return (angle % 1) * 2 * Math.PI
        }
    }
}