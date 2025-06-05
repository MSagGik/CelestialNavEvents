package io.github.msaggik.celestialnavevents.internal.common

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.collections.sumOf
import kotlin.collections.withIndex
import kotlin.math.floor
import kotlin.math.pow

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
 * TimeUtils provides internal support for astronomical time computations,
 * including Julian Date conversion and Delta T estimation.
 *
 * This utility encapsulates:
 * - Conversion of calendar time to Julian Date with support for UT-TT correction;
 * - Estimation of Delta T (TT - UT) from a historical and predictive polynomial model;
 * - Polynomial evaluation for time-related models across large date ranges.
 *
 * Designed for use in astronomical algorithms that require precise time references,
 * especially in celestial mechanics and event prediction based on ephemeris data.
 *
 * References:
 * - Reda & Andreas, NREL (2003): Solar Position Algorithm.
 * - Jean Meeus, "Astronomical Algorithms", 2nd ed. (1998).
 * - Espenak & Meeus (2006), NASA TP-2006-214141 and eclipsewise.com (2014).
 */
internal object TimeUtils {

    /**
     * Calculates the Julian Date (JD) for a given ZonedDateTime, including fractional days.
     *
     * Sources:
     * - Reda & Andreas (2003). Solar Position Algorithm. NREL Report TP-560-34302.
     *   https://www.nrel.gov/docs/fy08osti/34302.pdf
     * - Jean Meeus, Astronomical Algorithms, 2nd ed., 1998.
     *
     * This method converts input time to UTC, then computes the Julian Date, applying
     * the Gregorian calendar correction for dates after 1582-10-15.
     *
     * @param date the ZonedDateTime to convert
     * @param utToTtCorrection difference UT - TT in seconds (default is 0.0)
     * @return Julian Date as Double with fractional day
     */
    fun convertMillisToJulianDate(
        date: ZonedDateTime,
        utToTtCorrection: Double = 0.0
    ): Double {
        val utcDate = date.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

        var year = utcDate.year
        var month = utcDate.monthValue
        val day = utcDate.dayOfMonth +
                (utcDate.hour + (utcDate.minute + utcDate.second / 60.0) / 60.0) / 24.0

        if (month < 3) {
            year -= 1
            month += 12
        }

        val jd = floor(365.25 * (year + 4716.0)) +
                floor(30.6001 * (month + 1)) +
                day - 1524.5

        val century = floor(year / 100.0)
        val calendarReformCorrection = if (jd > 2299160.0) 2.0 - century + floor(century / 4.0) else 0.0

        return jd + calendarReformCorrection + utToTtCorrection / 86400.0
    }

    /**
     * Estimates Delta T (TT - UT) in seconds for a given date.
     *
     * Sources:
     * - Espenak and Meeus, "Five Millennium Canon of Solar Eclipses: -1999 to +3000", NASA/TP-2006-214141.
     * - Updated by Espenak (2014): https://www.eclipsewise.com/help/deltatpoly2014.html
     *
     * @param date LocalDate to estimate Delta T for.
     * @return Estimated Delta T in seconds.
     */
    fun estimateDeltaT(date: LocalDate): Double {
        val decimalYear = date.year + (date.monthValue - 0.5) / 12.0
        return estimateDeltaT(decimalYear)
    }

    /**
     * Estimates Delta T (TT - UT) in seconds for a given decimal year.
     *
     * @param decimalYear Year with fractional component (e.g. 2025.5 for mid-year).
     * @return Estimated Delta T in seconds.
     */
    fun estimateDeltaT(decimalYear: Double): Double {
        val x: Double
        return when {
            decimalYear < -500 -> {
                x = (decimalYear - 1820) / 100.0
                poly(x, -20.0, 0.0, 32.0)
            }
            decimalYear < 500 -> {
                x = decimalYear / 100.0
                poly(x, 10583.6, -1014.41, 33.78311, -5.952053,
                    -0.1798452, 0.022174192, 0.0090316521)
            }
            decimalYear < 1600 -> {
                x = (decimalYear - 1000) / 100.0
                poly(x, 1574.2, -556.01, 71.23472, 0.319781,
                    -0.8503463, -0.005050998, 0.0083572073)
            }
            decimalYear < 1700 -> {
                x = decimalYear - 1600
                poly(x, 120.0, -0.9808, -0.01532, 1.0 / 7129.0)
            }
            decimalYear < 1800 -> {
                x = decimalYear - 1700
                poly(x, 8.83, 0.1603, -0.0059285, 0.00013336, -1.0 / 1174000.0)
            }
            decimalYear < 1860 -> {
                x = decimalYear - 1800
                poly(x, 13.72, -0.332447, 0.0068612, 0.0041116,
                    -0.00037436, 0.0000121272, -0.0000001699, 0.000000000875)
            }
            decimalYear < 1900 -> {
                x = decimalYear - 1860
                poly(x, 7.62, 0.5737, -0.251754, 0.01680668,
                    -0.0004473624, 1.0 / 233174.0)
            }
            decimalYear < 1920 -> {
                x = decimalYear - 1900
                poly(x, -2.79, 1.494119, -0.0598939, 0.0061966, -0.000197)
            }
            decimalYear < 1941 -> {
                x = decimalYear - 1920
                poly(x, 21.20, 0.84493, -0.076100, 0.0020936)
            }
            decimalYear < 1961 -> {
                x = decimalYear - 1950
                poly(x, 29.07, 0.407, -1.0 / 233.0, 1.0 / 2547.0)
            }
            decimalYear < 1986 -> {
                x = decimalYear - 1975
                poly(x, 45.45, 1.067, -1.0 / 260.0, -1.0 / 718.0)
            }
            decimalYear < 2005 -> {
                x = decimalYear - 2000
                poly(x, 63.86, 0.3345, -0.060374, 0.0017275, 0.000651814, 0.00002373599)
            }
            decimalYear < 2015 -> {
                x = decimalYear - 2005
                poly(x, 64.69, 0.2930)
            }
            decimalYear <= 3000 -> {
                x = decimalYear - 2015
                poly(x, 67.62, 0.3645, 0.0039755)
            }
            else -> throw kotlin.IllegalArgumentException("Delta T estimation is not available for years > 3000.")
        }
    }

    /**
     * Evaluates a polynomial for the given x and coefficients.
     *
     * @param x the value at which to evaluate
     * @param coefficients the polynomial coefficients, ordered from degree 0 upwards
     * @return result of the polynomial evaluation
     */
    private fun poly(x: Double, vararg coefficients: Double): Double {
        return coefficients.withIndex().sumOf { (index, coef) -> coef * x.pow(index) }
    }
}