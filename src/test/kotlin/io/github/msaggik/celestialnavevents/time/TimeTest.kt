package io.github.msaggik.celestialnavevents.time

import io.github.msaggik.celestialnavevents.model.measurement.Time
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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
 * Unit tests for the [Time] data class and its companion methods,
 * which provide conversion between total minutes and a structured time
 * representation with days, hours, and minutes.
 *
 * The tests cover:
 * - Construction of [Time] instances from positive and negative total minutes,
 *   ensuring correct breakdown into days, hours, and minutes, including negative time handling.
 * - Zero-time case correctness.
 * - Bidirectional consistency between [Time.fromTotalMinutes] and [Time.toTotalMinutes].
 * - Correct conversion of time duration to total milliseconds.
 * - Validation of input values to ensure hours and minutes remain within valid ranges,
 *   throwing [IllegalArgumentException] on invalid inputs.
 *
 * The string representation format tested is:
 * - For zero days: "HH:mm"
 * - For non-zero days: "[+|-]d HH:mm"
 */
internal class TimeTest {

    @Test
    fun `test fromTotalMinutes positive`() {
        val time = Time.fromTotalMinutes(1500) // 1 day, 1:00
        assertEquals(1, time.days)
        assertEquals(1, time.hour)
        assertEquals(0, time.min)
        assertEquals("+1d 01:00", time.toString())
    }

    @Test
    fun `test fromTotalMinutes negative`() {
        val time = Time.fromTotalMinutes(-1) // -1 min = -1d 23:59
        assertEquals(-1, time.days)
        assertEquals(23, time.hour)
        assertEquals(59, time.min)
        assertEquals("-1d 23:59", time.toString())
    }

    @Test
    fun `test zero time`() {
        val time = Time.fromTotalMinutes(0)
        assertEquals(0, time.days)
        assertEquals(0, time.hour)
        assertEquals(0, time.min)
        assertEquals("00:00", time.toString())
    }

    @Test
    fun `test toTotalMinutes and back`() {
        val original = Time(days = 2, hour = 3, min = 45)
        val total = original.toTotalMinutes()
        val converted = Time.fromTotalMinutes(total)
        assertEquals(original, converted)
    }

    @Test
    fun `test toTotalMilliseconds`() {
        val time = Time(days = 1, hour = 0, min = 1) // 1 day + 1 minute = 1441 min
        assertEquals(1441 * 60 * 1000, time.toTotalMilliseconds())
    }

    @Test
    fun `test illegal hour throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            Time(hour = 24, min = 0)
        }
    }

    @Test
    fun `test illegal minute throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            Time(hour = 23, min = 60)
        }
    }
}