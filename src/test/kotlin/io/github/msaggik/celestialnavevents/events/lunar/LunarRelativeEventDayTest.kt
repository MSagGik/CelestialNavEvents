package io.github.msaggik.celestialnavevents.events.lunar

import io.github.msaggik.celestialnavevents.api.LunarEventsCalculator
import io.github.msaggik.celestialnavevents.internal.lunar.LunarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingLunarState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
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

private const val TIME_TOLERANCE_MS = 4.5 * 60.0 * 1000L
private const val AZIMUTH_TOLERANCE = 2.0

/**
 * Unit tests for verifying the correctness of [LunarCalculatorImpl.findUpcomingLunarRelativeEventDay]
 * across a wide range of geographic locations, dates, and edge cases.
 *
 * These tests cover:
 * - Lunar rise/set behavior at various latitudes and longitudes.
 * - Validation of azimuth and time for lunar events.
 * - Handling edge cases such as full-day lunar visibility in Antarctica.
 * - Testing of different dates throughout the year to ensure accuracy across lunar cycles.
 * - Tolerance-based assertions for time and azimuth to account for calculation variations.
 *
 * The tests use specific dates and locations to validate that the lunar event calculations are accurate.
 * Assertions are made on both the azimuth (direction) and time of the lunar events.
 * The tests also verify the [HorizonCrossingLunarState] for specific locations and dates.
 *
 * Constants:
 * - [TIME_TOLERANCE_MS]: Allowed deviation in event timing in milliseconds.
 * - [AZIMUTH_TOLERANCE]: Allowed deviation in lunar azimuth in degrees.
 */
class LunarRelativeEventDayTest {
    private val calculator: LunarEventsCalculator = LunarCalculatorImpl() // заменить на реализацию

    @Test
    fun greenland_2025_01_05() {
        val result = calculator.findUpcomingLunarRelativeEventDay(66.5, -40.0, ZonedDateTime.parse("2025-01-02T12:00:00-03:00"))
        val event = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 217.0)
        assertEqualsWithTolerance(Time(16, 57), event.time)
    }

    @Test
    fun equator_2025_03_21() {
        val result = calculator.findUpcomingLunarRelativeEventDay(0.0, 0.0, ZonedDateTime.parse("2025-03-21T12:00:00+00:00"))
        val event = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 119.0)
        assertEqualsWithTolerance(Time(23, 43), event.time)
    }

    @Test
    fun sydney_2025_06_03() {
        val result = calculator.findUpcomingLunarRelativeEventDay(-33.87, 151.21, ZonedDateTime.parse("2025-06-03T14:00:00+10:00"))
        val event = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 276.0)
        assertEqualsWithTolerance(Time(0, 8), event.time)
    }

    @Test
    fun oslo_2025_07_01_before() {
        val result = calculator.findUpcomingLunarRelativeEventDay(59.91, 10.75, ZonedDateTime.parse("2025-07-01T12:02:30+02:00"))
        val event = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 86.0)
        assertEqualsWithTolerance(Time(12, 3), event.time)
    }

    @Test
    fun oslo_2025_07_01_after() {
        val result = calculator.findUpcomingLunarRelativeEventDay(59.91, 10.75, ZonedDateTime.parse("2025-07-01T12:02:50+02:00"))
        val event = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 268.0)
        assertEqualsWithTolerance(Time(0, 29), event.time)
    }

    @Test
    fun antarctica_full_day_2025_12_15() {
        val result = calculator.findUpcomingLunarRelativeEventDay(-75.0, 0.0, ZonedDateTime.parse("2025-12-15T12:00:00+00:00"))
        val event = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 215.0)
        assertEqualsWithTolerance(Time(1, 30), event.time)
        assertEqualsWithTolerance(Time(13, 30, days = 9), Time.fromTotalMilliseconds(event.timeToNearestEventMillis))
    }

    @Test
    fun moscow_2025_02_01() {
        val result = calculator.findUpcomingLunarRelativeEventDay(55.75, 37.61, ZonedDateTime.parse("2025-02-01T12:00:00+03:00"))
        val event = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 266.0)
        assertEqualsWithTolerance(Time(21, 13), event.time)
        assertEqualsWithTolerance(Time(9, 13), Time.fromTotalMilliseconds(event.timeToNearestEventMillis))
    }

    @Test
    fun rio_2025_04_11() {
        val result = calculator.findUpcomingLunarRelativeEventDay(-22.91, -43.17, ZonedDateTime.parse("2025-04-11T12:30:00-03:00"))
        val event = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 95.0)
        assertEqualsWithTolerance(Time(16, 56), event.time)
        assertEqualsWithTolerance(Time(4, 26), Time.fromTotalMilliseconds(event.timeToNearestEventMillis))
    }

    @Test
    fun tokyo_2025_05_20() {
        val result = calculator.findUpcomingLunarRelativeEventDay(35.68, 139.69, ZonedDateTime.parse("2025-05-20T12:00:00+09:00"))
        val event = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 106.0)
        assertEqualsWithTolerance(Time(0, 43), event.time)
        assertEqualsWithTolerance(Time(12, 43), Time.fromTotalMilliseconds(event.timeToNearestEventMillis))
    }

    @Test
    fun alaska_2025_06_30() {
        val result = calculator.findUpcomingLunarRelativeEventDay(64.2, -149.5, ZonedDateTime.parse("2025-06-30T12:00:00-08:00"))
        val event = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 275.0)
        assertEqualsWithTolerance(Time(1, 13), event.time)
        assertEqualsWithTolerance(Time(13, 13), Time.fromTotalMilliseconds(event.timeToNearestEventMillis))
    }

    @Test
    fun beijing_2025_08_01() {
        val result = calculator.findUpcomingLunarRelativeEventDay(39.90, 116.40, ZonedDateTime.parse("2025-08-01T12:00:00+08:00"))
        val event = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonset event")
        assertAzimuthAround(event.azimuth, 112.0)
        assertEqualsWithTolerance(Time(12, 41), event.time)
        assertEqualsWithTolerance(Time(0, 41), Time.fromTotalMilliseconds(event.timeToNearestEventMillis))
    }

    @Test
    fun perth_set_and_risen_2025_05_12() {
        val result = calculator.findUpcomingLunarRelativeEventDay(
            -31.95, 115.86, ZonedDateTime.parse("2025-05-12T12:00:00+08:00")
        )
        assertEquals(HorizonCrossingLunarState.SET_AND_RISEN, result.type)
    }


    private fun assertEqualsWithTolerance(expected: Time, actual: Time) {
        val diff = abs(expected.toTotalMilliseconds() - actual.toTotalMilliseconds())
        assertTrue(diff <= TIME_TOLERANCE_MS, "Expected $expected, got $actual (diff: ${diff / 60000} minutes)")
    }

    private fun assertAzimuthAround(actual: Double, expected: Double) {
        val diff = abs(expected - actual)
        assertTrue(diff <= AZIMUTH_TOLERANCE, "Expected azimuth ~$expected°, got $actual°")
    }
}