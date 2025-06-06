package io.github.msaggik.celestialnavevents.events.solar

import io.github.msaggik.celestialnavevents.internal.solar.SolarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
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


private const val AZIMUTH_TOLERANCE = 1.0
private const val TIME_OFFSET_TOLERANCE = 1L // minutes

/**
 * Unit tests for validating the SolarCalculatorImpl’s method
 * `findUpcomingSolarAbsoluteEventDay` with respect to solar event timing
 * and azimuth accuracy.
 *
 * The tests check solar event calculations (sunrise, sunset) across diverse
 * geographic coordinates and dates, including edge cases like polar night/day,
 * equinoxes, solstices, leap years, and time zones.
 *
 * Verification is performed with tolerances defined by:
 * - AZIMUTH_TOLERANCE: maximum allowable deviation in azimuth angle (degrees).
 * - TIME_OFFSET_TOLERANCE: maximum allowable deviation in event time (minutes).
 *
 * Tests ensure:
 * - Event presence and correctness relative to the input date/time.
 * - Azimuth values fall within ±AZIMUTH_TOLERANCE degrees of expected values.
 * - Event times are within ±TIME_OFFSET_TOLERANCE minutes of expected local times.
 * - Correct timezone handling of event timestamps.
 * - Proper behavior during daylight saving time changes and special calendar days.
 *
 * These tests guarantee robust and precise solar event computations suitable
 * for applications relying on accurate solar position data.
 */
internal class SolarAbsoluteEventDayTest {
    private val calculator = SolarCalculatorImpl()

    @Test
    fun `test equator on equinox`() {
        val dateTime = ZonedDateTime.of(2023, 3, 20, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertNotNull(result.dayLength)
        assertNotNull(result.nightLength)
    }

    @Test
    fun `test north pole during polar night`() {
        val dateTime = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(90.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertTrue(result.events.first().dateTime.isAfter(dateTime))
    }

    @Test
    fun `test south pole during polar day`() {
        val dateTime = ZonedDateTime.of(2023, 12, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(-90.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `test azimuth and time for Tokyo sunrise in summer`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 3, 0, 0, 0, ZoneId.of("Asia/Tokyo"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(35.6762, 139.6503, dateTime)

        val sunrise = result.events.first { it.type == EventType.RISE }
        assertAzimuthNear(sunrise.azimuth, 60.0)
        assertTrue(sunrise.dateTime.isAfter(dateTime))
    }

    @Test
    fun `test Buenos Aires winter sunset azimuth before rise`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 5, 0, 0, 0, ZoneId.of("America/Argentina/Buenos_Aires"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(-34.6037, -58.3816, dateTime)
        val sunset = result.events.first { it.type == EventType.RISE }

        assertAzimuthNear(sunset.azimuth, 61.0)
    }

    @Test
    fun `test Buenos Aires winter sunset azimuth after rise`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 17, 0, 0, 0, ZoneId.of("America/Argentina/Buenos_Aires"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(-34.6037, -58.3816, dateTime)
        val sunset = result.events.first { it.type == EventType.SET }

        assertAzimuthNear(sunset.azimuth, 298.0)
    }

    @Test
    fun `test leap year February 29`() {
        val dateTime = ZonedDateTime.of(2024, 2, 29, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `test meridian edge longitude`() {
        val dateTime = ZonedDateTime.of(2025, 3, 20, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(0.0, 180.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `sunrise after polar night in Tromsø`() {
        val dateTime = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(1))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(69.6496, 18.9560, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertAzimuthNear(event.azimuth, 178.0)
        assertTrue(event.dateTime.isAfter(dateTime))
    }

    @Test
    fun `sunset after polar day in Tromsø`() {
        val dateTime = ZonedDateTime.of(2024, 7, 20, 12, 0, 0, 0, ZoneId.of("Europe/Oslo"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(69.6496, 18.9560, dateTime)
        val event = result.events.first()

        assertEquals(EventType.SET, event.type)
        assertAzimuthNear(event.azimuth, 353.0)
    }

    @Test
    fun `test event time is ZonedDateTime and matches timezone`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 6, 0, 0, 0, ZoneId.of("Asia/Tokyo"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(35.6762, 139.6503, dateTime)

        result.events.forEach {
            assertEquals(dateTime.zone, it.dateTime.zone)
        }
    }

    @Test
    fun `sunrise in Quito on equinox is around 6am local time`() {
        val dateTime = ZonedDateTime.of(2025, 3, 20, 0, 0, 0, 0, ZoneId.of("America/Guayaquil"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(-0.1807, -78.4678, dateTime)

        val sunrise = result.events.first { it.type == EventType.RISE }
        assertTimeNear(sunrise.dateTime.toLocalTime(), LocalTime.of(6, 17))
    }

    @Test
    fun `sunset in Tokyo on summer solstice is around 7pm local time`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 12, 0, 0, 0, ZoneId.of("Asia/Tokyo"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(35.6762, 139.6503, dateTime)

        val sunset = result.events.first { it.type == EventType.SET }
        assertTimeNear(sunset.dateTime.toLocalTime(), LocalTime.of(19, 0))
    }

    @Test
    fun `sunrise in New York on winter solstice is after 7am local time`() {
        val dateTime = ZonedDateTime.of(2025, 12, 21, 0, 0, 0, 0, ZoneId.of("America/New_York"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(40.7128, -74.0060, dateTime)

        val sunrise = result.events.first { it.type == EventType.RISE }
        assertTrue(sunrise.dateTime.toLocalTime().isAfter(LocalTime.of(7, 0)))
    }

    @Test
    fun `sunset in New York on winter solstice is before 5pm local time`() {
        val dateTime = ZonedDateTime.of(2025, 12, 21, 12, 0, 0, 0, ZoneId.of("America/New_York"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(40.7128, -74.0060, dateTime)

        val sunset = result.events.first { it.type == EventType.SET }
        assertTrue(sunset.dateTime.toLocalTime().isBefore(LocalTime.of(17, 0)))
    }

    @Test
    fun `sunrise in London during DST`() {
        val dateTime = ZonedDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneId.of("Europe/London"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(51.5074, -0.1278, dateTime)

        val sunrise = result.events.first { it.type == EventType.RISE }
        assertTimeNear(sunrise.dateTime.toLocalTime(), LocalTime.of(4, 47))
    }

    @Test
    fun `sunrise and sunset in Reykjavik during polar twilight`() {
        val dateTime = ZonedDateTime.of(2025, 1, 10, 0, 0, 0, 0, ZoneId.of("Atlantic/Reykjavik"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(64.1265, -21.8174, dateTime)

        val rise = result.events.firstOrNull { it.type == EventType.RISE }
        val set = result.events.firstOrNull { it.type == EventType.SET }

        assertNotNull(rise)
        assertNotNull(set)
        assertTrue(rise!!.dateTime.toLocalTime().isBefore(set!!.dateTime.toLocalTime()))
    }

    @Test
    fun `sunset in Sydney on summer solstice is around 8pm local time`() {
        val dateTime = ZonedDateTime.of(2025, 12, 21, 12, 0, 0, 0, ZoneId.of("Australia/Sydney"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(-33.8688, 151.2093, dateTime)

        val sunset = result.events.first { it.type == EventType.SET }
        assertTimeNear(sunset.dateTime.toLocalTime(), LocalTime.of(20, 5))
    }

    @Test
    fun `sunrise in Moscow in October after DST ends`() {
        val dateTime = ZonedDateTime.of(2025, 10, 30, 0, 0, 0, 0, ZoneId.of("Europe/Moscow"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(55.7558, 37.6173, dateTime)

        val sunrise = result.events.first { it.type == EventType.RISE }
        assertTrue(sunrise.dateTime.toLocalTime().isAfter(LocalTime.of(7, 30)))
    }

    @Test
    fun `test UTC consistency in Nairobi`() {
        val dateTime = ZonedDateTime.of(2025, 6, 15, 0, 0, 0, 0, ZoneId.of("Africa/Nairobi"))
        val result = calculator.findUpcomingSolarAbsoluteEventDay(-1.2921, 36.8219, dateTime)

        result.events.forEach {
            assertEquals(dateTime.zone, it.dateTime.zone)
        }
    }

    @Test
    fun `sunrise in Cape Town is earlier in summer than winter`() {
        val summerDate = ZonedDateTime.of(2025, 12, 21, 0, 0, 0, 0, ZoneId.of("Africa/Johannesburg"))
        val winterDate = ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneId.of("Africa/Johannesburg"))

        val summerResult = calculator.findUpcomingSolarAbsoluteEventDay(-33.9249, 18.4241, summerDate)
        val winterResult = calculator.findUpcomingSolarAbsoluteEventDay(-33.9249, 18.4241, winterDate)

        val summerSunrise = summerResult.events.first { it.type == EventType.RISE }
        val winterSunrise = winterResult.events.first { it.type == EventType.RISE }

        assertTrue(summerSunrise.dateTime.toLocalTime().isBefore(winterSunrise.dateTime.toLocalTime()))
    }

    private fun assertAzimuthNear(actual: Double, expected: Double) {
        assertTrue(
            abs(actual - expected) <= AZIMUTH_TOLERANCE,
            "Azimuth $actual not within ±$AZIMUTH_TOLERANCE of $expected"
        )
    }

    private fun assertTimeNear(actual: LocalTime, expected: LocalTime, toleranceMinutes: Long = TIME_OFFSET_TOLERANCE) {
        val diff = abs(Duration.between(actual, expected).toMinutes())
        assertTrue(diff <= toleranceMinutes, "Time $actual not within ±$toleranceMinutes minutes of $expected")
    }
}