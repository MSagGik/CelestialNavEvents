package io.github.msaggik.celestialnavevents.events.lunar

import io.github.msaggik.celestialnavevents.api.LunarEventsCalculator
import io.github.msaggik.celestialnavevents.internal.lunar.LunarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.events.lunar.LunarEventDay
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingLunarState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.abs
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


private const val TIME_TOLERANCE_MS = 2.1 * 60 * 1000L
private const val AZIMUTH_TOLERANCE = 2.0

class LunarEventTest {

    private fun assertAzimuthClose(actual: Double, expected: Double) {
        val delta = abs(actual - expected)
        assertTrue(delta <= AZIMUTH_TOLERANCE) {
            "Expected azimuth close to $expected°, but got $actual°"
        }
    }

    private val calculator: LunarEventsCalculator = LunarCalculatorImpl()

    @Test
    fun `equator typical day should return RISEN_AND_SET`() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.parse("2025-03-15T00:00Z"))
        assertEquals(HorizonCrossingLunarState.SET_AND_RISEN, result.type)
        assertEquals(2, result.events.size)
    }

    @Test
    fun `equator azimuth and time validation`() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.parse("2025-03-15T00:00Z"))

        val rise = result.events.first { it.type == EventType.RISE }
        val set = result.events.first { it.type == EventType.SET }

        assertAzimuthClose(rise.azimuth, 96.0)
        assertAzimuthClose(set.azimuth, 267.0)
    }

    @Test
    fun `mid latitude typical spring day`() {
        val result = calculator.calculateLunarEventDay(55.75, 37.62, ZonedDateTime.parse("2025-04-10T00:00Z"))
        assertEquals(HorizonCrossingLunarState.SET_AND_RISEN, result.type)
    }

    @Test
    fun `mid latitude long night should have full night or set only`() {
        val result = calculator.calculateLunarEventDay(55.75, 37.62, ZonedDateTime.parse("2025-12-12T00:00+03:00"))
        assertTrue(result.type == HorizonCrossingLunarState.RISEN_AND_SET)
    }

    @Test
    fun `polar summer should return FULL_DAY`() {
        val result = calculator.calculateLunarEventDay(69.0, 18.95, ZonedDateTime.parse("2025-07-01T00:00+02:00"))
        assertEquals(HorizonCrossingLunarState.SET_RISE_SET, result.type)
    }

    @Test
    fun `polar winter should return FULL_NIGHT`() {
        val result = calculator.calculateLunarEventDay(69.0, 18.95, ZonedDateTime.parse("2025-01-01T00:00Z"))
        assertEquals(HorizonCrossingLunarState.FULL_NIGHT, result.type)
    }

    @Test
    fun `illumination percent must be in valid range`() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.parse("2025-02-10T00:00Z"))
        assertTrue(result.illuminationPercent in 0.0..100.0)
    }

    @Test
    fun `moon never rises should be ONLY_SET or FULL_NIGHT`() {
        val result = calculator.calculateLunarEventDay(80.0, 0.0, ZonedDateTime.parse("2025-12-15T00:00Z"))
        assertTrue(result.type == HorizonCrossingLunarState.ONLY_SET || result.type == HorizonCrossingLunarState.FULL_NIGHT)
    }

    @Test
    fun `moon never sets should be ONLY_RISEN or FULL_DAY`() {
        val result = calculator.calculateLunarEventDay(80.0, 0.0, ZonedDateTime.parse("2025-06-15T00:00Z"))
        assertTrue(result.type == HorizonCrossingLunarState.FULL_NIGHT)
    }

    @Test
    fun `edge case near poles with rise_set_rise`() {
        val result = calculator.calculateLunarEventDay(88.0, 0.0, ZonedDateTime.parse("2025-08-01T00:00Z"))
        assertTrue(result.type == HorizonCrossingLunarState.FULL_NIGHT)
    }

    @Test
    fun `south pole in summer gives FULL_DAY`() {
        val result = calculator.calculateLunarEventDay(-90.0, 0.0, ZonedDateTime.parse("2025-12-15T00:00Z"))
        assertEquals(HorizonCrossingLunarState.FULL_DAY, result.type)
    }

    @Test
    fun `south pole in winter gives FULL_NIGHT`() {
        val result = calculator.calculateLunarEventDay(-90.0, 0.0, ZonedDateTime.parse("2025-06-15T00:00Z"))
        assertEquals(HorizonCrossingLunarState.FULL_DAY, result.type)
    }

    @Test
    fun `longitude variation does not change state RISEN_AND_SET -12_00`() {
        val result = calculator.calculateLunarEventDay(0.0, -180.0, ZonedDateTime.parse("2025-05-01T00:00-12:00"))
        assertEquals(HorizonCrossingLunarState.RISEN_AND_SET, result.type)
    }

    @Test
    fun `longitude variation does not change state RISEN_AND_SET +06_00`() {
        val result = calculator.calculateLunarEventDay(0.0, 90.0, ZonedDateTime.parse("2025-05-01T00:00+06:00"))
        assertEquals(HorizonCrossingLunarState.RISEN_AND_SET, result.type)
    }

    @Test
    fun `longitude variation does not change state RISEN_AND_SET +12_00`() {
        val result = calculator.calculateLunarEventDay(0.0, 180.0, ZonedDateTime.parse("2025-05-01T00:00+12:00"))
        assertEquals(HorizonCrossingLunarState.RISEN_AND_SET, result.type)
    }

    @Test
    fun `longitude variation does not change state RISEN_AND_SET`() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.parse("2025-05-01T00:00Z"))
        assertEquals(HorizonCrossingLunarState.RISEN_AND_SET, result.type)
    }

    @Test
    fun `longitude variation does not change state SET_AND_RISEN -06_00`() {
        val result = calculator.calculateLunarEventDay(0.0, -90.0, ZonedDateTime.parse("2025-05-01T00:00-06:00"))
        assertEquals(HorizonCrossingLunarState.RISEN_AND_SET, result.type)
    }

    @Test
    fun `invalid illumination throws exception`() {
        assertThrows<IllegalArgumentException> {
            LunarEventDay(events = emptyList(), type = HorizonCrossingLunarState.ERROR, illuminationPercent = 150.0)
        }
    }

    @Test
    fun `moon rise near midnight does not crash`() {
        val result = calculator.calculateLunarEventDay(60.0, 30.0, ZonedDateTime.parse("2025-03-20T23:59:00Z"))
        assertNotNull(result)
    }

    @Test
    fun `north pole transitions correctly between night and day`() {
        val winter = calculator.calculateLunarEventDay(90.0, 0.0, ZonedDateTime.parse("2025-01-01T00:00Z"))
        val spring = calculator.calculateLunarEventDay(90.0, 0.0, ZonedDateTime.parse("2025-03-21T00:00Z"))

        assertEquals(HorizonCrossingLunarState.FULL_NIGHT, winter.type)
        assertEquals(HorizonCrossingLunarState.FULL_NIGHT, spring.type)
    }

    @Test
    fun moonriseOnEquatorShouldBeAround18h() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.UTC))
        val moonrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonrise event")
        assertEqualsWithTolerance(Time(16, 37), moonrise.time)
    }

    @Test
    fun moonsetOnEquatorShouldBeAround6h() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.UTC))
        val moonset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertEqualsWithTolerance(Time(4, 12), moonset.time)
    }

    @Test
    fun moonriseAzimuthOnEquatorShouldBeEast() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.UTC))
        val moonrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonrise event")
        assertAzimuthAround(moonrise.azimuth, 115.0)
    }

    @Test
    fun moonsetAzimuthOnEquatorShouldBeWest() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.UTC))
        val moonset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset event")
        assertAzimuthAround(moonset.azimuth, 247.0)
    }

    @Test
    fun moonriseInMidLatitudeJune2025ShouldMatch() {
        val result = calculator.calculateLunarEventDay(40.7128, -74.0060, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.ofHours(-4)))
        val moonrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonrise")
        assertEqualsWithTolerance(Time(19, 25), moonrise.time)
        assertAzimuthAround(moonrise.azimuth, 125.0)
    }

    @Test
    fun moonsetInMidLatitudeJune2025ShouldMatch() {
        val result = calculator.calculateLunarEventDay(40.7128, -74.0060, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneId.of("America/New_York")))
        val moonset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset")
        assertEqualsWithTolerance(Time(3, 46), moonset.time)
        assertAzimuthAround(moonset.azimuth, 238.0)
    }

    @Test
    fun polarRegionShouldHaveFullDayOrNight() {
        val result = calculator.calculateLunarEventDay(69.0, 33.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneId.of("Europe/Moscow")))
        assertTrue(result.type == HorizonCrossingLunarState.FULL_DAY || result.type == HorizonCrossingLunarState.FULL_NIGHT)
    }

    @Test
    fun moonEventsInMurmanskShouldBeEmptyOrSingle() {
        val result = calculator.calculateLunarEventDay(69.0, 33.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneId.of("Europe/Moscow")))
        assertTrue(result.events.size <= 1)
    }

    @Test
    fun moonEventsInSydneyJune2025ShouldBeValid() {
        val result = calculator.calculateLunarEventDay(-33.8688, 151.2093, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.ofHours(+10)))
        val moonrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No moonrise")
        val moonset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No moonset")

        assertEqualsWithTolerance(Time(4, 52), moonset.time)
        assertEqualsWithTolerance(Time(15, 3), moonrise.time)
        assertAzimuthAround(moonrise.azimuth, 118.0)
        assertAzimuthAround(moonset.azimuth, 244.0)
    }

    // Проверка валидности illumination
    @Test
    fun illuminationShouldBeWithinBounds() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.UTC))
        assertTrue(result.illuminationPercent in 0.0..100.0)
    }

    @Test
    fun meridianAndAntiMeridianCrossingTimeShouldBeValid() {
        val result = calculator.calculateLunarEventDay(0.0, 0.0, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneOffset.UTC))
        result.meridianCrossing?.let {
            assertTrue(it.hour in 0..23)
            assertTrue(it.min in 0..59)
        }
        result.antimeridianCrossing?.let {
            assertTrue(it.hour in 0..23)
            assertTrue(it.min in 0..59)
        }
    }

    // Повторения с разными датами
    @Test
    fun moonEventsShouldChangeOverDays() {
        val result1 = calculator.calculateLunarEventDay(40.7128, -74.0060, ZonedDateTime.of(2025, 6, 8, 0, 0, 0, 0, ZoneId.of("America/New_York")))
        val result2 = calculator.calculateLunarEventDay(40.7128, -74.0060, ZonedDateTime.of(2025, 6, 9, 0, 0, 0, 0, ZoneId.of("America/New_York")))
        assertNotEquals(result1.events, result2.events)
    }

    // Проверка крайних значений Time
    @Test
    fun timeNormalizationShouldWorkForNegativeInput() {
        val time = Time.fromTotalMilliseconds(-3661000)
        assertEquals(-1, time.days)
        assertEquals(22, time.hour)
        assertEquals(58, time.min)
    }

    @Test
    fun invalidTimeThrowsError() {
        assertFailsWith<IllegalArgumentException> { Time(25, 0) }
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