package io.github.msaggik.celestialnavevents.position

import io.github.msaggik.celestialnavevents.internal.model.position.Position
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.abs

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
 * Unit tests for verifying the correctness and robustness of the Sun position calculation
 * in the [Position.calculateSunPosition] method.
 *
 * These tests cover a wide range of astronomical and mathematical edge cases:
 * - Known solar events (e.g., solstices and equinoxes)
 * - Long-term stability and periodicity
 * - Validity of returned values (right ascension and declination)
 * - Handling of fractional, negative, and extreme day values
 * - Domain boundaries and continuity
 *
 * The position is calculated based on the number of days since the epoch J1900.0 (Julian Date 2415020.0).
 */
internal class CelestialPositionTest {

    @Test
    fun `Epoch test - daysSinceEpoch = 0`() {
        val pos = Position.calculateSunPosition(0.0)
        assertTrue(pos.rightAscension in 0.0..2 * PI)
        assertTrue(pos.declination in -PI/2..PI/2)
    }

    @Test
    fun `Century forward test - 36525 days`() {
        val pos = Position.calculateSunPosition(36525.0)
        assertTrue(pos.rightAscension in 0.0..(2 * PI))
        assertTrue(pos.declination in -PI/2..PI/2)
    }

    @Test
    fun `Summer solstice 2000 (JD 2451723)`() {
        val days = 2451723.0 - 2415020.0
        val pos = Position.calculateSunPosition(days)
        assertTrue(pos.declination > 0.4) // Высокое Солнце
    }

    @Test
    fun `Winter solstice 2000 (JD 2451909)`() {
        val days = 2451909.0 - 2415020.0
        val pos = Position.calculateSunPosition(days)
        assertTrue(pos.declination < -0.4) // Низкое Солнце
    }

    @Test
    fun `Negative day - 0_1`() {
        val pos = Position.calculateSunPosition(-0.1)
        assertNotNull(pos)
    }

    @Test
    fun `Positive small day offset`() {
        val pos = Position.calculateSunPosition(0.1)
        assertNotNull(pos)
    }

    @Test
    fun `Fractional day input`() {
        val pos = Position.calculateSunPosition(1234.56789)
        assertNotNull(pos)
    }

    @Test
    fun `Very far future test - 50000 days`() {
        val pos = Position.calculateSunPosition(50000.0)
        assertTrue(pos.rightAscension in 0.0..(2 * PI))
    }

    @Test
    fun `Right ascension is in valid range`() {
        val pos = Position.calculateSunPosition(0.0)
        assertTrue(pos.rightAscension in 0.0..(2 * PI))
    }

    @Test
    fun `Declination is in valid range`() {
        val pos = Position.calculateSunPosition(0.0)
        assertTrue(pos.declination in -PI/2..PI/2)
    }

    @Test
    fun `Compare two close days`() {
        val pos1 = Position.calculateSunPosition(1000.0)
        val pos2 = Position.calculateSunPosition(1000.1)
        assertNotEquals(pos1.rightAscension, pos2.rightAscension, "RA should differ")
    }

    @Test
    fun `Periodicity check - one year later`() {
        val pos1 = Position.calculateSunPosition(0.0)
        val pos2 = Position.calculateSunPosition(365.25)
        assertTrue(abs(pos1.rightAscension - pos2.rightAscension) < 0.1)
    }

    @Test
    fun `Leap year date Feb 29 2000 (JD 2451604_5)`() {
        val days = 2451604.5 - 2415020.0
        val pos = Position.calculateSunPosition(days)
        assertNotNull(pos)
    }

    @Test
    fun `Edge case - extremely far future`() {
        val pos = Position.calculateSunPosition(1e6)
        assertFalse(pos.rightAscension.isNaN())
        assertFalse(pos.declination.isNaN())
    }

    @Test
    fun `Edge case - extremely far past`() {
        val pos = Position.calculateSunPosition(-1e6)
        assertFalse(pos.rightAscension.isNaN())
        assertFalse(pos.declination.isNaN())
    }

    @Test
    fun `Zero declination near equinox`() {
        val days = 2451623.0 - 2415020.0 // март 2000 (весеннее равноденствие)
        val pos = Position.calculateSunPosition(days)
        assertTrue(abs(pos.declination) < 0.05)
    }

    @Test
    fun `Extreme declination around solstice`() {
        val days = 2451723.0 - 2415020.0
        val pos = Position.calculateSunPosition(days)
        assertTrue(pos.declination > 0.3)
    }

    @Test
    fun `Right ascension continuity check`() {
        val pos1 = Position.calculateSunPosition(2000.0)
        val pos2 = Position.calculateSunPosition(2000.00001)
        assertTrue(abs(pos1.rightAscension - pos2.rightAscension) < 1e-4)
    }

    @Test
    fun `Declination smooth near 0 days`() {
        val pos1 = Position.calculateSunPosition(0.0)
        val pos2 = Position.calculateSunPosition(0.000001)
        assertTrue(abs(pos1.declination - pos2.declination) < 1e-5)
    }

    @Test
    fun `High precision input days`() {
        val pos = Position.calculateSunPosition(12345.6789012345)
        assertNotNull(pos)
    }

    @Test
    fun `Check symmetry before and after epoch`() {
        val pos1 = Position.calculateSunPosition(100.0)
        val pos2 = Position.calculateSunPosition(-100.0)
        assertNotEquals(pos1.rightAscension, pos2.rightAscension)
    }

    @Test
    fun `Right ascension wraps around correctly`() {
        val pos = Position.calculateSunPosition(99999.0)
        assertTrue(pos.rightAscension in 0.0..2 * PI)
    }

    @Test
    fun `Declination domain edge - near zero orbitalCorrectionFactor`() {
        val pos = Position.calculateSunPosition(-99999.0)
        assertTrue(pos.declination in -PI/2..PI/2)
    }
}