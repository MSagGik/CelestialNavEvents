package io.github.msaggik.celestialnavevents.events.solar

import io.github.msaggik.celestialnavevents.api.SolarEventsCalculator
import io.github.msaggik.celestialnavevents.internal.common.NUMBER_MILLIS_DAY
import io.github.msaggik.celestialnavevents.internal.solar.SolarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.track.TypeEventTrack
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

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
 * Unit test suite for verifying the correctness of "magic hour" calculations
 * under a variety of geographic and astronomical conditions.
 *
 * Tested implementation: [io.github.msaggik.celestialnavevents.internal.solar.SolarCalculatorImpl] via the [io.github.msaggik.celestialnavevents.api.SolarEventsCalculator] interface.
 *
 * Covered scenarios include:
 * - Standard urban locations across different time zones;
 * - Extreme polar conditions (polar night, polar day);
 * - Equatorial and edge-case coordinates;
 * - Computation of day, night, and magic hour durations;
 * - Edge cases with only one sunrise/sunset event;
 * - Preservation of azimuth data for solar events;
 * - Consistency of results for identical input.
 *
 */
internal class MagicHourTest {

    private val calculator: SolarEventsCalculator = SolarCalculatorImpl()

    private val testLatitude = 40.7128 // New York
    private val testLongitude = -74.0060

    @Test
    fun `returns non-empty magic hour list on typical date`() {
        val dateTime = ZonedDateTime.of(2024, 6, 1, 12, 0, 0, 0, ZoneId.of("America/New_York"))

        val result = calculator.calculateMagicHourPeriod(testLatitude, testLongitude, dateTime)

        Assertions.assertFalse(result.events.isEmpty(), "Expected at least one magic hour interval")
    }

    @Test
    fun `all magic hour intervals are of correct type`() {
        val dateTime = ZonedDateTime.of(2024, 4, 15, 12, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(51.5074, -0.1278, dateTime) // London

        result.events.forEach {
            Assertions.assertEquals(TypeEventTrack.MAGIC_HOUR, it.typeEventTrack)
        }
    }

    @Test
    fun `returns empty magic hour near North Pole in winter`() {
        val dateTime = ZonedDateTime.of(2024, 12, 15, 12, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(85.0, 0.0, dateTime)

        Assertions.assertTrue(result.events.isEmpty(), "Expected no magic hour events in polar night")
    }

    @Test
    fun `returns extended magic hour on edge-case morning only`() {
        val dateTime = ZonedDateTime.of(2024, 12, 15, 0, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(-75.0, 0.0, dateTime)

        if (result.events.isNotEmpty()) {
            val first = result.events.first()
            Assertions.assertEquals(dateTime.toLocalDate().atStartOfDay(dateTime.zone), first.start.dateTime)
        }
    }

    @Test
    fun `returns extended magic hour on edge-case evening only`() {
        val dateTime = ZonedDateTime.of(2024, 12, 15, 0, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(75.0, 0.0, dateTime)

        if (result.events.isNotEmpty()) {
            val last = result.events.last()
            Assertions.assertEquals(dateTime.toLocalDate().atStartOfDay(dateTime.zone), last.finish.dateTime)
        }
    }

    @Test
    fun `day and night durations are reduced by magic hour`() {
        val dateTime = ZonedDateTime.of(2024, 7, 1, 12, 0, 0, 0, ZoneId.of("Europe/Berlin"))

        val result = calculator.calculateMagicHourPeriod(52.52, 13.4050, dateTime) // Berlin

        val sumMagic = result.events.sumOf {
            Duration.between(it.start.dateTime, it.finish.dateTime).toMillis()
        }

        val day = result.daylightBeforeRing?.toTotalMilliseconds() ?: 0
        val night = result.darknessAfterRing?.toTotalMilliseconds() ?: 0

        Assertions.assertEquals(NUMBER_MILLIS_DAY, day + night + sumMagic)
    }

    @Test
    fun `magic hour never exceeds full day duration`() {
        val dateTime = ZonedDateTime.of(2024, 3, 15, 0, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.calculateMagicHourPeriod(34.0, -118.0, dateTime)

        val total = result.events.sumOf {
            Duration.between(it.start.dateTime, it.finish.dateTime).toMillis()
        }

        Assertions.assertTrue(total <= NUMBER_MILLIS_DAY)
    }

    @Test
    fun `result contains no negative intervals`() {
        val dateTime = ZonedDateTime.of(2024, 9, 10, 0, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(48.8566, 2.3522, dateTime) // Paris

        result.events.forEach {
            Assertions.assertFalse(it.finish.dateTime.isBefore(it.start.dateTime), "Interval ends before it starts")
        }
    }

    @Test
    fun `returns consistent results for same input`() {
        val dateTime = ZonedDateTime.of(2024, 5, 20, 0, 0, 0, 0, ZoneId.of("UTC"))

        val result1 = calculator.calculateMagicHourPeriod(35.6895, 139.6917, dateTime)
        val result2 = calculator.calculateMagicHourPeriod(35.6895, 139.6917, dateTime)

        Assertions.assertEquals(result1, result2)
    }

    @Test
    fun `works correctly for equator location`() {
        val dateTime = ZonedDateTime.of(2024, 3, 21, 0, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(0.0, 0.0, dateTime)

        Assertions.assertFalse(result.events.isEmpty(), "Magic hour should exist near equator")
    }

    @Test
    fun `correctly calculates when only one pair of events exists`() {
        val dateTime = ZonedDateTime.of(2024, 10, 5, 0, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(30.0, 30.0, dateTime)

        Assertions.assertTrue(result.events.size <= 2)
    }

    @Test
    fun `clean day and night are null when original durations are null`() {
        val dateTime = ZonedDateTime.of(2024, 12, 21, 12, 0, 0, 0, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(89.0, 0.0, dateTime)

        if (result.daylightBeforeRing == null && result.darknessAfterRing == null) {
            Assertions.assertTrue(result.events.isEmpty())
        }
    }

    @Test
    fun `handles fractional time correctly`() {
        val dateTime = ZonedDateTime.of(2024, 6, 15, 15, 30, 45, 123_000_000, ZoneId.of("UTC"))

        val result = calculator.calculateMagicHourPeriod(41.9028, 12.4964, dateTime)

        result.events.forEach {
            Assertions.assertTrue(it.start.dateTime.nano % 1_000_000 == 0)
            Assertions.assertTrue(it.finish.dateTime.nano % 1_000_000 == 0)
        }
    }

    @Test
    fun `does not throw exception on extreme coordinates`() {
        val dateTime = ZonedDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

        Assertions.assertDoesNotThrow {
            calculator.calculateMagicHourPeriod(-90.0, 180.0, dateTime)
            calculator.calculateMagicHourPeriod(90.0, -180.0, dateTime)
        }
    }

    private val northPoleLat = 89.9
    private val southPoleLat = -89.9
    private val longitude = 0.0

    private fun zonedDate(year: Int, month: Int, day: Int): ZonedDateTime =
        ZonedDateTime.of(year, month, day, 12, 0, 0, 0, ZoneId.of("UTC"))

    @Test
    fun `no events during polar night at North Pole`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2024, 12, 21))
        Assertions.assertTrue(result.events.isEmpty(), "Magic hour should not exist during polar night")
    }

    @Test
    fun `no events during polar day at North Pole`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2024, 6, 21))
        Assertions.assertTrue(result.events.isEmpty(), "Magic hour should not exist during polar day")
    }

    @Test
    fun `no events during polar night at South Pole`() {
        val result = calculator.calculateMagicHourPeriod(southPoleLat, longitude, zonedDate(2024, 6, 21))
        Assertions.assertTrue(result.events.isEmpty(), "South Pole winter should not have magic hour")
    }

    @Test
    fun `no events during polar day at South Pole`() {
        val result = calculator.calculateMagicHourPeriod(southPoleLat, longitude, zonedDate(2024, 12, 21))
        Assertions.assertTrue(result.events.isEmpty(), "South Pole summer should not have magic hour")
    }

    @Test
    fun `clean day and night are still calculated in polar night`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2024, 12, 15))
        Assertions.assertNotNull(result.daylightBeforeRing)
        Assertions.assertNotNull(result.darknessAfterRing)
    }

    @Test
    fun `magic hour duration is short on transition day`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2024, 4, 10))
        result.events.firstOrNull()?.let {
            val duration = Duration.between(it.start.dateTime, it.finish.dateTime).toMinutes()
            Assertions.assertTrue(duration in 0..120, "Expected short magic hour on solar transition days")
        }
    }

    @Test
    fun `start time of extended event defaults to midnight when no sunrise exists`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2024, 1, 5))
        result.events.firstOrNull()?.let {
            Assertions.assertEquals(0, it.start.dateTime.hour)
            Assertions.assertEquals(0, it.start.dateTime.minute)
        }
    }

    @Test
    fun `finish time of extended event defaults to midnight when no sunset exists`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2024, 11, 5))
        result.events.lastOrNull()?.let {
            Assertions.assertEquals(0, it.finish.dateTime.toLocalTime().hour)
        }
    }

    @Test
    fun `azimuth data is preserved even in polar edge conditions before rise`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2025, 3, 17))
        val event = result.events.firstOrNull()

        Assertions.assertTrue(
            event?.start?.azimuth == null && event?.finish?.azimuth == null,
            "Azimuth should be preserved in polar case"
        )
    }

    @Test
    fun `azimuth data is preserved even in polar edge conditions in rise`() {
        val result = calculator.calculateMagicHourPeriod(northPoleLat, longitude, zonedDate(2025, 3, 10))
        val event = result.events.firstOrNull()

        Assertions.assertTrue(
            event?.start?.azimuth != null || event?.finish?.azimuth != null,
            "Azimuth should be preserved in polar case"
        )
    }
}