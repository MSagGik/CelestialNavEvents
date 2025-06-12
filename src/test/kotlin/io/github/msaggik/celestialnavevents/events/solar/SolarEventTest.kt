package io.github.msaggik.celestialnavevents.events.solar

import io.github.msaggik.celestialnavevents.api.SolarEventsCalculator
import io.github.msaggik.celestialnavevents.internal.solar.SolarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingSolarState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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

/**
 * Tolerance in minutes for validating sunrise and sunset times.
 * This value defines the acceptable deviation (in minutes) when comparing
 * calculated solar event times against expected values in unit tests.
 *
 * Due to minor variations in astronomical algorithms and numerical precision
 * (e.g., atmospheric refraction or ΔT), exact matches are not always guaranteed.
 */
private const val TIME_TOLERANCE_MINUTES = 2.5

/**
 * Unit tests for validating solar event calculations across various geographic locations
 * and dates, including equinoxes, solstices, and polar day/night transitions.
 *
 * This test suite ensures correctness of sunrise/sunset calculations and polar state
 * detection (e.g., Polar Day, Polar Night) based on given latitude, longitude,
 * and ZonedDateTime input.
 *
 * Scenarios tested include:
 * - Equatorial behavior at equinoxes
 * - High-latitude conditions (e.g., Murmansk, Barrow) in summer and winter
 * - Time zone and hemisphere correctness
 * - Day/night duration verification
 */
internal class SolarEventTest {

    private val solarEventsCalculator: SolarEventsCalculator = SolarCalculatorImpl()

    private fun timeCloseEnough(expected: Time, actual: Time, toleranceMinutes: Double = TIME_TOLERANCE_MINUTES) {
        val diff = abs(expected.toTotalMilliseconds() - actual.toTotalMilliseconds())/(1000.0 * 60.0)

        assertTrue(diff <= toleranceMinutes, "Expected time $expected but was $actual, diff=$diff minutes > tolerance=$toleranceMinutes")
    }

    @Test
    fun `Equator sunrise and sunset`() {
        val dt = ZonedDateTime.of(2025, 3, 20, 6, 0, 0, 0, ZoneOffset.UTC)
        val res = solarEventsCalculator.calculateSolarEventDay(0.0, 0.0, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }
        val sunset = res.events.find { it.type == EventType.SET }
        assertTrue(sunrise != null)
        assertTrue(sunset != null)

        timeCloseEnough(Time(6, 4), sunrise!!.time)
        timeCloseEnough(Time(18, 10), sunset!!.time)
    }

    @Test
    fun `New York sunrise and sunset in summer`() {
        val dt = ZonedDateTime.of(2025, 6, 21, 12, 0, 0, 0, ZoneOffset.ofHours(-4)) // EDT
        val res = solarEventsCalculator.calculateSolarEventDay(40.7128, -74.0060, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }!!
        val sunset = res.events.find { it.type == EventType.SET }!!

        timeCloseEnough(Time(5, 25), sunrise.time)
        timeCloseEnough(Time(20, 30), sunset.time)
    }

    @Test
    fun `Oslo sunrise and sunset in winter`() {
        val dt = ZonedDateTime.of(2025, 12, 21, 12, 0, 0, 0, ZoneOffset.ofHours(1)) // CET
        val res = solarEventsCalculator.calculateSolarEventDay(59.9139, 10.7522, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }
        val sunset = res.events.find { it.type == EventType.SET }
        if (res.type == HorizonCrossingSolarState.POLAR_NIGHT) {
            assertTrue(sunrise == null)
            assertTrue(sunset == null)
        } else {
            sunrise?.let { timeCloseEnough(Time(9, 20), it.time) }
            sunset?.let { timeCloseEnough(Time(15, 10), it.time) }
        }
    }

    @Test
    fun `Murmansk polar day`() {
        val dt = ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(3))
        val res = solarEventsCalculator.calculateSolarEventDay(68.9585, 33.0827, dt)

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, res.type)
        assertTrue(res.events.isEmpty())
        assertTrue(res.dayLength != null && res.dayLength!!.toTotalMinutes() > 0)
        assertTrue(res.nightLength == null || res.nightLength!!.toTotalMinutes() == 0)
    }

    @Test
    fun `Murmansk polar night`() {
        val dt = ZonedDateTime.of(2025, 12, 21, 0, 0, 0, 0, ZoneOffset.ofHours(3))
        val res = solarEventsCalculator.calculateSolarEventDay(68.9585, 33.0827, dt)

        assertEquals(HorizonCrossingSolarState.POLAR_NIGHT, res.type)
        assertTrue(res.events.isEmpty())
        assertTrue(res.dayLength == null || res.dayLength!!.toTotalMinutes() == 0)
        assertTrue(res.nightLength != null && res.nightLength!!.toTotalMinutes() > 0)
    }

    @Test
    fun `Sydney sunrise and sunset summer`() {
        val dt = ZonedDateTime.of(2025, 12, 21, 0, 0, 0, 0, ZoneOffset.ofHours(11))
        val res = solarEventsCalculator.calculateSolarEventDay(-33.8688, 151.2093, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }!!
        val sunset = res.events.find { it.type == EventType.SET }!!

        timeCloseEnough(Time(5, 40), sunrise.time)
        timeCloseEnough(Time(20, 5), sunset.time)
    }

    @Test
    fun `Sao Paulo sunrise and sunset autumn`() {
        val dt = ZonedDateTime.of(2025, 3, 21, 12, 0, 0, 0, ZoneOffset.ofHours(-3))
        val res = solarEventsCalculator.calculateSolarEventDay(-23.5505, -46.6333, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }!!
        val sunset = res.events.find { it.type == EventType.SET }!!

        timeCloseEnough(Time(6, 10), sunrise.time)
        timeCloseEnough(Time(18, 16), sunset.time)
    }

    @Test
    fun `Reykjavik sunrise and sunset spring`() {
        val dt = ZonedDateTime.of(2025, 4, 20, 12, 0, 0, 0, ZoneOffset.ofHours(0))
        val res = solarEventsCalculator.calculateSolarEventDay(64.1355, -21.8954, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }!!
        val sunset = res.events.find { it.type == EventType.SET }!!

        timeCloseEnough(Time(5, 37), sunrise.time)
        timeCloseEnough(Time(21, 17), sunset.time)
    }

    @Test
    fun `Trollheimen Norway polar regions`() {
        val dt = ZonedDateTime.of(2025, 7, 15, 12, 0, 0, 0, ZoneOffset.ofHours(2))
        val res = solarEventsCalculator.calculateSolarEventDay(62.5, 8.5, dt)
        if (res.type == HorizonCrossingSolarState.POLAR_DAY) {
            assertTrue(res.events.isEmpty())
        } else {
            val sunrise = res.events.find { it.type == EventType.RISE }
            val sunset = res.events.find { it.type == EventType.SET }
            sunrise?.let { timeCloseEnough(Time(4, 3), it.time) }
            sunset?.let { timeCloseEnough(Time(22, 59), it.time) }
        }
    }

    @Test
    fun `Tokyo sunrise and sunset autumn`() {
        val dt = ZonedDateTime.of(2025, 9, 23, 12, 0, 0, 0, ZoneOffset.ofHours(9))
        val res = solarEventsCalculator.calculateSolarEventDay(35.6895, 139.6917, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }!!
        val sunset = res.events.find { it.type == EventType.SET }!!

        timeCloseEnough(Time(5, 29), sunrise.time)
        timeCloseEnough(Time(17, 36), sunset.time)
    }

    @Test
    fun `Cape Town sunrise and sunset winter`() {
        val dt = ZonedDateTime.of(2025, 6, 21, 12, 0, 0, 0, ZoneOffset.ofHours(2))
        val res = solarEventsCalculator.calculateSolarEventDay(-33.9249, 18.4241, dt)
        val sunrise = res.events.find { it.type == EventType.RISE }!!
        val sunset = res.events.find { it.type == EventType.SET }!!

        timeCloseEnough(Time(7, 50), sunrise.time)
        timeCloseEnough(Time(17, 44), sunset.time)
    }

    @Test
    fun `Barrow Alaska polar day`() {
        val dt = ZonedDateTime.of(2025, 7, 1, 12, 0, 0, 0, ZoneOffset.ofHours(-9))
        val res = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, dt)

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, res.type)
        assertTrue(res.events.isEmpty())
        assertTrue(res.dayLength != null && res.dayLength!!.toTotalMinutes() > 0)
    }

    @Test
    fun `Barrow Alaska polar night`() {
        val dt = ZonedDateTime.of(2025, 12, 1, 12, 0, 0, 0, ZoneOffset.ofHours(-9))
        val res = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, dt)

        assertEquals(HorizonCrossingSolarState.POLAR_NIGHT, res.type)
        assertTrue(res.events.isEmpty())
        assertTrue(res.nightLength != null && res.nightLength!!.toTotalMinutes() > 0)
    }


    @Test
    fun `Sunrise and Sunset for Tashkent in equinox (2024-03-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            latitude = 41.3111,
            longitude = 69.2797,
            dateTime = ZonedDateTime.of(2024, 3, 20, 0, 0, 0, 0, ZoneOffset.ofHours(5))
        )

        assertEquals(HorizonCrossingSolarState.RISEN_AND_SET, result.type)
        assertEquals(2, result.events.size)
        assertTrue(result.events.any { it.type == EventType.RISE })
        assertTrue(result.events.any { it.type == EventType.SET })

        val sorted = result.events.sortedBy { it.time.toTotalMinutes() }
        assertTrue(sorted[0].type == EventType.RISE)
        assertTrue(sorted[1].type == EventType.SET)

        val dayLengthMinutes = result.dayLength?.toTotalMinutes() ?: 0
        assertTrue(dayLengthMinutes in 700..740, "Day length should be close to 720 minutes (12h)")
    }

    @Test
    fun `Polar night in Murmansk (2024-12-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            latitude = 68.9585,
            longitude = 33.0827,
            dateTime = ZonedDateTime.of(2024, 12, 21, 0, 0, 0, 0, ZoneOffset.ofHours(3))
        )

        assertEquals(HorizonCrossingSolarState.POLAR_NIGHT, result.type)
        assertTrue(result.events.isEmpty())
        assertTrue(result.dayLength == null || result.dayLength?.toTotalMinutes() == 0)
        assertTrue(result.nightLength != null && (result.nightLength?.toTotalMinutes() ?: -1) > 0)
    }

    @Test
    fun `Polar day in Murmansk (2024-06-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            latitude = 68.9585,
            longitude = 33.0827,
            dateTime = ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(3))
        )

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, result.type)
        assertTrue(result.events.isEmpty())
        assertTrue(result.dayLength != null && (result.dayLength?.toTotalMinutes() ?: -1) > 0)
        assertTrue(result.nightLength == null || result.nightLength?.toTotalMinutes() == 0)
    }

    @Test
    fun `Sun behavior in Quito (Equator) on Equinox (2024-03-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            latitude = -0.1807,
            longitude = -78.4678,
            dateTime = ZonedDateTime.of(2024, 3, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-5))
        )

        assertEquals(HorizonCrossingSolarState.RISEN_AND_SET, result.type)
        val dayLength = result.dayLength?.toTotalMinutes() ?: 0
        assertTrue(dayLength in 700..740)
    }

    @Test
    fun `Polar day in Antarctica (Amundsen-Scott station) on Southern Solstice (2024-12-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            latitude = -90.0,
            longitude = 0.0,
            dateTime = ZonedDateTime.of(2024, 12, 21, 0, 0, 0, 0, ZoneOffset.UTC)
        )

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, result.type)
        assertTrue(result.events.isEmpty())
    }

    @Test
    fun testQuitoEquinox() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            latitude = -0.1807,
            longitude = -78.4678,
            dateTime = ZonedDateTime.of(2024, 3, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-5))
        )

        assertNotNull(result)
    }

    @Test
    fun testMurmanskPolarDay() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            68.9585, 33.0827,
            ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(3))
        )

        assert(result.type == HorizonCrossingSolarState.POLAR_DAY)
    }

    @Test
    fun testMurmanskPolarNight() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            68.9585, 33.0827,
            ZonedDateTime.of(2024, 12, 21, 0, 0, 0, 0, ZoneOffset.ofHours(3))
        )

        assert(result.type == HorizonCrossingSolarState.POLAR_NIGHT)
    }

    @Test
    fun testAmundsenScottStation() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            -90.0, 0.0,
            ZonedDateTime.of(2024, 12, 21, 0, 0, 0, 0, ZoneOffset.UTC)
        )

        assertNotNull(result)
    }

    @Test
    fun testBarrowPolarDay() {
        val result = solarEventsCalculator.calculateSolarEventDay(
            71.2906, -156.7886,
            ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(-8))
        )

        assert(result.type == HorizonCrossingSolarState.POLAR_DAY)
    }

    @Test
    fun `Murmansk last day before polar night (2024-12-01)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(68.9585, 33.0827, ZonedDateTime.of(2024, 12, 1, 0, 0, 0, 0, ZoneOffset.ofHours(3)))

        assertTrue(result.type == HorizonCrossingSolarState.POLAR_NIGHT)
    }

    @Test
    fun `Murmansk first day after polar night (2025-01-12)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(68.9585, 33.0827, ZonedDateTime.of(2025, 1, 12, 0, 0, 0, 0, ZoneOffset.ofHours(3)))

        assertTrue(result.type != HorizonCrossingSolarState.POLAR_NIGHT)
        assertTrue(result.events.any { it.type == EventType.RISE })
    }

    @Test
    fun `Barrow last day before polar day (2024-05-09)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, ZonedDateTime.of(2024, 5, 9, 0, 0, 0, 0, ZoneOffset.ofHours(-8)))

        assertTrue(result.type != HorizonCrossingSolarState.POLAR_DAY)
    }

    @Test
    fun `Barrow first day of polar day (2024-05-10)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, ZonedDateTime.of(2024, 5, 10, 0, 0, 0, 0, ZoneOffset.ofHours(-8)))

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, result.type)
    }

    @Test
    fun `Barrow last day of polar day (2024-07-31)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, ZonedDateTime.of(2024, 7, 31, 0, 0, 0, 0, ZoneOffset.ofHours(-8)))

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, result.type)
    }

    @Test
    fun `Barrow first day after polar day (2024-08-01)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, ZonedDateTime.of(2024, 8, 1, 0, 0, 0, 0, ZoneId.of("America/Anchorage")))
        println(result)
//        assertTrue(result.type != HorizonCrossingSolarState.POLAR_DAY)
    }

    @Test
    fun `Tromsø near last sunrise before polar night`() {
        val result = solarEventsCalculator.calculateSolarEventDay(69.6496, 18.9560, ZonedDateTime.of(2024, 11, 25, 0, 0, 0, 0, ZoneOffset.ofHours(1)))

        assertTrue(result.type != HorizonCrossingSolarState.POLAR_NIGHT)
    }

    @Test
    fun `Tromsø first sunrise after polar night`() {
        val result = solarEventsCalculator.calculateSolarEventDay(69.6496, 18.9560, ZonedDateTime.of(2025, 1, 15, 0, 0, 0, 0, ZoneOffset.ofHours(1)))

        assertTrue(result.events.any { it.type == EventType.RISE })
    }

    @Test
    fun `Antarctica transition to polar day (Amundsen-Scott)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(-90.0, 0.0, ZonedDateTime.of(2024, 10, 20, 0, 0, 0, 0, ZoneOffset.UTC))

        assertEquals(HorizonCrossingSolarState.POLAR_DAY, result.type)
    }

    @Test
    fun `Antarctica transition from polar day to polar night (Amundsen-Scott)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(-90.0, 0.0, ZonedDateTime.of(2024, 7, 20, 23, 0, 0, 0, ZoneOffset.UTC))

        assertEquals(HorizonCrossingSolarState.POLAR_NIGHT, result.type)
    }

    @Test
    fun `Moscow sunrise azimuth at equinox (2025-03-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(55.75, 37.62, ZonedDateTime.of(2025, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC))
        val sunrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunrise")

        assertEquals(89.0, sunrise.azimuth, 1.0) // Вблизи востока
    }

    @Test
    fun `Moscow sunset azimuth at equinox (2025-03-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(55.75, 37.62, ZonedDateTime.of(2025, 3, 20, 0, 0, 0, 0, ZoneOffset.UTC))
        val sunset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No sunset")

        assertEquals(271.0, sunset.azimuth, 1.0) // Вблизи запада
    }

    @Test
    fun `Tromsø sunrise azimuth before polar night (2024-11-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(69.65, 18.95, ZonedDateTime.of(2024, 11, 20, 0, 0, 0, 0, ZoneOffset.ofHours(1)))
        val sunrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunrise")

        assertEquals(159.0, sunrise.azimuth, 1.0) // Юго-восток
    }

    @Test
    fun `Tromsø sunset azimuth before polar night (2024-11-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(69.65, 18.95, ZonedDateTime.of(2024, 11, 20, 0, 0, 0, 0, ZoneOffset.ofHours(1)))
        val sunset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No sunset")

        assertEquals(200.0, sunset.azimuth, 1.0) // Юго-запад
    }

    @Test
    fun `Quito sunrise azimuth at equinox (2025-03-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(-0.18, -78.47, ZonedDateTime.of(2025, 3, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-5)))
        val sunrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunrise")

        assertEquals(90.0, sunrise.azimuth, 1.0)
    }

    @Test
    fun `Quito sunset azimuth at equinox (2025-03-20)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(-0.18, -78.47, ZonedDateTime.of(2025, 3, 20, 0, 0, 0, 0, ZoneOffset.ofHours(-5)))
        val sunset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No sunset")

        assertEquals(270.0, sunset.azimuth, 1.0)
    }

    @Test
    fun `New York sunrise azimuth at summer solstice (2025-06-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(40.71, -74.01, ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(-4)))
        val sunrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunrise")

        assertEquals(58.0, sunrise.azimuth, 1.0) // Сев-восток
    }

    @Test
    fun `New York sunset azimuth at summer solstice (2025-06-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(40.71, -74.01, ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(-4)))
        val sunset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No sunset")

        assertEquals(302.0, sunset.azimuth, 1.0) // Сев-запад
    }

    @Test
    fun `Cape Town sunrise azimuth at winter solstice (2025-06-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(-33.92, 18.42, ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(2)))
        val sunrise = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunrise")

        assertEquals(62.0, sunrise.azimuth, 1.0)
    }

    @Test
    fun `Cape Town sunset azimuth at winter solstice (2025-06-21)`() {
        val result = solarEventsCalculator.calculateSolarEventDay(-33.92, 18.42, ZonedDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneOffset.ofHours(2)))
        val sunset = result.events.firstOrNull { it.type == EventType.SET } ?: fail("No sunset")

        assertEquals(298.0, sunset.azimuth, 1.0)
    }

    @Test
    fun `sunrise after polar night in Barrow Alaska`() {
        val result = solarEventsCalculator.calculateSolarEventDay(71.2906, -156.7886, ZonedDateTime.of(2024, 1, 23, 0, 0, 0, 0, ZoneOffset.ofHours(-9)))
        val sunset = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunset")

        assertEquals(173.0, sunset.azimuth, 2.5)
        assertEquals(Time(hour = 13, min = 9).toTotalMilliseconds().toDouble(), sunset.time.toTotalMilliseconds().toDouble(), 8.5*1000*60)
    }

    @Test
    fun `sunrise in Alert Canada after long polar night`() {
        val result = solarEventsCalculator.calculateSolarEventDay(82.5018, -62.3481, ZonedDateTime.of(2024, 2, 28, 12, 0, 0, 0,  ZoneOffset.ofHours(-5)))
        val sunset = result.events.firstOrNull { it.type == EventType.RISE } ?: fail("No sunset")

        assertEquals(163.0, sunset.azimuth, 2.5)
        assertEquals(Time(hour = 10, min = 13).toTotalMilliseconds().toDouble(), sunset.time.toTotalMilliseconds().toDouble(), 8.5*1000*60)
    }
}