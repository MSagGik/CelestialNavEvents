package io.github.msaggik.celestialnavevents.time

import io.github.msaggik.celestialnavevents.internal.common.TimeUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.math.pow
import kotlin.test.assertFailsWith

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


// Allowable delta in seconds
private const val TOLERANCE = 0.1

/**
 * Unit tests for verifying the correctness of Delta T estimation methods in [TimeUtils].
 *
 * These tests cover a broad range of historical, modern, and future years to ensure the polynomial
 * approximations for Delta T values are accurate within defined tolerances.
 *
 * Delta T (ΔT) represents the difference between Terrestrial Time (TT) and Universal Time (UT).
 * It is a critical parameter in astronomical calculations, affecting the precision of celestial
 * event timings such as eclipses and planetary positions.
 *
 * The polynomial coefficients and piecewise approximations used are based on the works of
 * Espenak and Meeus, particularly the "Five Millennium Canon of Solar Eclipses: -1999 to +3000"
 * (NASA/TP-2006-214141) and updates published at <a href="https://www.eclipsewise.com/help/deltatpoly2014.html">
 * Eclipsewise</a>.
 *
 * The tests also validate boundary conditions, fractional years, and ensure proper exceptions are
 * thrown for unsupported input ranges.
 *
 * Tolerances vary depending on the historical period and inherent uncertainties of the polynomial
 * models.
 */
internal class UtToTtCorrection {

    private fun polynomial(x: Double, vararg coefficients: Double): Double {
        return coefficients.withIndex().sumOf { (i, c) -> c * x.pow(i) }
    }

    @Test fun `estimate BC year -1000`() {
        val deltaT = TimeUtils.estimateDeltaT(-1000.0)
        assertEquals(-20.0 + 32.0 * ((-1000.0 - 1820.0) / 100).pow(2), deltaT, TOLERANCE)
    }

    @Test fun `estimate year 0`() {
        val deltaT = TimeUtils.estimateDeltaT(0.0)
        assertEquals(10583.6, deltaT, 500.0) // Loose tolerance, legacy model
    }

    @Test fun `estimate year 500`() {
        val u = (500.0 - 1000.0) / 100.0
        val expected = polynomial(u, 1574.2, -556.01, 71.23472, 0.319781, -0.8503463, -0.005050998, 0.0083572073)
        val deltaT = TimeUtils.estimateDeltaT(500.0)
        assertEquals(expected, deltaT, 0.001)
    }

    @Test fun `estimate year 1000`() {
        val deltaT = TimeUtils.estimateDeltaT(1000.0)
        assertEquals(1574.2, deltaT, 500.0)
    }

    @Test fun `estimate year 1600`() {
        val deltaT = TimeUtils.estimateDeltaT(1600.0)
        assertEquals(120.0, deltaT, 5.0)
    }

    @Test fun `estimate year 1700`() {
        val deltaT = TimeUtils.estimateDeltaT(1700.0)
        assertEquals(8.83, deltaT, 5.0)
    }

    @Test fun `estimate year 1800`() {
        val deltaT = TimeUtils.estimateDeltaT(1800.0)
        assertEquals(13.72, deltaT, 1.0)
    }

    @Test fun `estimate year 1860`() {
        val deltaT = TimeUtils.estimateDeltaT(1860.0)
        assertEquals(7.62, deltaT, 0.5)
    }

    @Test fun `estimate year 1900`() {
        val deltaT = TimeUtils.estimateDeltaT(1900.0)
        assertEquals(-2.79, deltaT, 1.0)
    }

    @Test fun `estimate year 1920`() {
        val deltaT = TimeUtils.estimateDeltaT(1920.0)
        assertEquals(21.20, deltaT, 1.0)
    }

    @Test fun `estimate year 1941`() {
        val t = 1941.0 - 1920.0
        val expected = polynomial(t, 21.20, 0.84493, -0.076100, 0.0020936)
        val deltaT = TimeUtils.estimateDeltaT(1941.0)
        assertEquals(expected, deltaT, 0.001)
    }

    @Test fun `estimate year 1960_999`() {
        val t = 1960.999 - 1950.0
        val expected = polynomial(t, 29.07, 0.407, -1.0 / 233.0, 1.0 / 2547.0)
        val deltaT = TimeUtils.estimateDeltaT(1960.999)
        assertEquals(expected, deltaT, 0.001)
    }

    @Test fun `estimate year 1985_999`() {
        val t = 1985.999 - 1975.0
        val expected = polynomial(t, 45.45, 1.067, -1.0 / 260.0, -1.0 / 718.0)
        val deltaT = TimeUtils.estimateDeltaT(1985.999)
        assertEquals(expected, deltaT, 0.001)
    }

    @Test fun `estimate year 2005`() {
        val deltaT = TimeUtils.estimateDeltaT(2005.0)
        assertEquals(64.69, deltaT, 1.0)
    }

    @Test fun `estimate year 2015`() {
        val deltaT = TimeUtils.estimateDeltaT(2015.0)
        assertEquals(67.62, deltaT, 1.0)
    }

    @Test fun `estimate year 2025`() {
        val deltaT = TimeUtils.estimateDeltaT(2025.0)
        assertEquals(67.62 + 0.3645 * 10 + 0.0039755 * 100, deltaT, 1.0)
    }

    @Test fun `estimate near 3000`() {
        val deltaT = TimeUtils.estimateDeltaT(2999.99)
        assertEquals(true, deltaT > 100.0)
    }

    @Test fun `estimate for LocalDate at 2000-06-01`() {
        val date = LocalDate.of(2000, 6, 1)
        val deltaT1 = TimeUtils.estimateDeltaT(date)
        val deltaT2 = TimeUtils.estimateDeltaT(2000.458333) // approx mid-year
        assertEquals(deltaT2, deltaT1, 0.5)
    }

    @Test fun `estimate for LocalDate at 1900-01-01`() {
        val deltaT = TimeUtils.estimateDeltaT(LocalDate.of(1900, 1, 1))
        assertEquals(-2.79, deltaT, 2.0)
    }

    @Test fun `estimate for LocalDate at 2024-12-31`() {
        val deltaT = TimeUtils.estimateDeltaT(LocalDate.of(2024, 12, 31))
        assertEquals(true, deltaT > 70.0)
    }

    @Test fun `estimate future year 2500`() {
        val deltaT = TimeUtils.estimateDeltaT(2500.0)
        assertEquals(true, deltaT > 90.0)
    }

    @Test fun `estimate decimal year mid 1995`() {
        val deltaT = TimeUtils.estimateDeltaT(1995.5)
        assertEquals(true, deltaT in 58.0..63.0)
    }

    @Test fun `estimate with fractional date 1800_5`() {
        val t = 1800.5 - 1800.0
        val expected = polynomial(
            t, 13.72, -0.332447, 0.0068612, 0.0041116,
            -0.00037436, 0.0000121272, -0.0000001699, 0.000000000875
        )
        val deltaT = TimeUtils.estimateDeltaT(1800.5)
        assertEquals(expected, deltaT, 0.001)
    }

    @Test fun `estimate with fractional date 1860_25`() {
        val deltaT = TimeUtils.estimateDeltaT(1860.25)
        assertEquals(true, deltaT in 7.0..8.5)
    }

    @Test fun `estimate throws for year above 3000`() {
        assertFailsWith<IllegalArgumentException> {
            TimeUtils.estimateDeltaT(3000.1)
        }
    }

    @Test
    fun `estimate throws for year far future`() {
        assertThrows<IllegalArgumentException> {
            TimeUtils.estimateDeltaT(4000.0)
        }
    }
}