package io.github.msaggik.celestialnavevents.events.solar

import io.github.msaggik.celestialnavevents.internal.common.NUMBER_MILLIS_DAY
import io.github.msaggik.celestialnavevents.internal.solar.SolarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.events.solar.SolarRelativeEventDay
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingSolarState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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


private const val AZIMUTH_TOLERANCE = 1.0
private const val TIME_OFFSET_TOLERANCE_MILLIS = 1 * 60 * 1000L // minutes

/**
 * Unit tests for verifying the correctness of [SolarCalculatorImpl.findUpcomingSolarRelativeEventDay]
 * across a wide range of geographic locations, dates, and edge cases.
 *
 * These tests cover:
 * - Equatorial behavior during equinoxes
 * - Polar day and night scenarios at the poles
 * - Mid-latitude sunrise/sunset patterns in summer and winter
 * - Handling of leap years and extreme dates
 * - Azimuth validation for known events at specific coordinates
 * - Validation of event timing within a reasonable tolerance
 *
 * The assertions include both general properties (e.g., event presence, sun state transitions),
 * and precise expectations for time and azimuth accuracy in critical cases.
 *
 * Constants:
 * - [AZIMUTH_TOLERANCE]: Allowed deviation in sunrise/sunset azimuth in degrees.
 * - [TIME_OFFSET_TOLERANCE_MILLIS]: Allowed deviation in sunrise/sunset event timing in milliseconds.
 */
internal class SolarRelativeEventDayTest {

    private val calculator = SolarCalculatorImpl()

    @Test
    fun `test equator on equinox`() {
        val dateTime = ZonedDateTime.of(2023, 3, 20, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertNotNull(result.dayLength)
        assertNotNull(result.nightLength)
    }

    @Test
    fun `test north pole during polar night`() {
        val dateTime = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(90.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertTrue(result.timeToFirstEventMillis() > 0)
    }

    @Test
    fun `test south pole during polar day`() {
        val dateTime = ZonedDateTime.of(2023, 12, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(-90.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `test mid-latitude summer`() {
        val dateTime = ZonedDateTime.of(2023, 6, 21, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(45.0, 0.0, dateTime)

        assertEquals(HorizonCrossingSolarState.RISEN_AND_SET, result.type)
    }

    @Test
    fun `test mid-latitude winter`() {
        val dateTime = ZonedDateTime.of(2023, 12, 21, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(45.0, 0.0, dateTime)

        assertEquals(HorizonCrossingSolarState.RISEN_AND_SET, result.preType)
    }

    @Test
    fun `test equator at equinox`() {
        val dateTime = ZonedDateTime.of(2025, 3, 20, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        val firstEvent = result.events.first()
        assertTrue(firstEvent.timeToNearestEventMillis in 0..NUMBER_MILLIS_DAY)
    }

    @Test
    fun `test north pole after polar night`() {
        val dateTime = ZonedDateTime.of(2025, 3, 15, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(90.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertEquals(EventType.RISE, result.events.first().type)
    }

    @Test
    fun `test south pole before polar night ends`() {
        val dateTime = ZonedDateTime.of(2025, 7, 15, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(-90.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertEquals(EventType.RISE, result.events.first().type)
    }

    @Test
    fun `test azimuth at Tokyo summer sunrise`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 3, 0, 0, 0, ZoneId.of("Asia/Tokyo"))
        val result = calculator.findUpcomingSolarRelativeEventDay(35.6762, 139.6503, dateTime)

        val sunrise = result.events.first { it.type == EventType.RISE }
        assertTrue(sunrise.azimuth in 40.0..70.0)
    }

    @Test
    fun `test Buenos Aires winter sunset`() {
        val dateTime = ZonedDateTime.of(2025, 6, 21, 17, 0, 0, 0, ZoneId.of("America/Argentina/Buenos_Aires"))
        val result = calculator.findUpcomingSolarRelativeEventDay(-34.6037, -58.3816, dateTime)

        val sunset = result.events.first { it.type == EventType.SET }
        assertTrue(sunset.azimuth in 280.0..310.0)
    }

    @Test
    fun `test UTC noon`() {
        val dateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
        assertTrue(result.events.first().timeToNearestEventMillis > 0)
    }

    @Test
    fun `test polar day skip`() {
        val dateTime = ZonedDateTime.of(2025, 6, 20, 0, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(69.6, 18.8, dateTime) // Tromsø

        assertTrue(result.events.isNotEmpty())
        assertTrue(result.events.first().timeToNearestEventMillis > NUMBER_MILLIS_DAY)
    }

    @Test
    fun `test meridian edge longitude`() {
        val dateTime = ZonedDateTime.of(2025, 3, 20, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 180.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `test antimeridian edge longitude`() {
        val dateTime = ZonedDateTime.of(2025, 3, 20, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, -180.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `test leap year February 29`() {
        val dateTime = ZonedDateTime.of(2024, 2, 29, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `test day before leap day`() {
        val dateTime = ZonedDateTime.of(2024, 2, 28, 6, 0, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)

        assertTrue(result.events.isNotEmpty())
    }

    @Test
    fun `sunrise after polar night in Tromsø`() {
        val dateTime = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(1))
        val result = calculator.findUpcomingSolarRelativeEventDay(69.6496, 18.9560, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertTimeOffsetNear(event.timeToNearestEventMillis, ((14 * 24  + 11) * 60 + 45) * 60 * 1000L)
        assertAzimuthNear(event.azimuth, 178.0)
    }

    @Test
    fun `sunset after polar day in Tromsø`() {
        val dateTime = ZonedDateTime.of(2024, 7, 20, 12, 0, 0, 0, ZoneId.of("Europe/Oslo"))
        val result = calculator.findUpcomingSolarRelativeEventDay(69.6496, 18.9560, dateTime)
        val event = result.events.first()

        assertEquals(EventType.SET, event.type)
        assertAzimuthNear(event.azimuth, 353.0)
    }

    @Test
    fun `sunrise after polar night in Barrow Alaska`() {
        val dateTime = ZonedDateTime.of(2025, 1, 5, 12, 0, 0, 0, ZoneOffset.ofHours(-9))
        val result = calculator.findUpcomingSolarRelativeEventDay(71.2906, -156.7886, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertTimeOffsetNear(event.timeToNearestEventMillis, ((16 * 24 + 12 + 13) * 60  + 15) * 60 * 1000L)
        assertAzimuthNear(event.azimuth, 174.0)
    }

    @Test
    fun `sunset after polar day in Barrow Alaska`() {
        val dateTime = ZonedDateTime.of(2025, 8, 1, 12, 0, 0, 0, ZoneId.of("America/Anchorage"))
        val result = calculator.findUpcomingSolarRelativeEventDay(71.2906, -156.7886, dateTime)
        val event = result.events.first()

        assertEquals(EventType.SET, event.type)
        assertAzimuthNear(event.azimuth, 351.0)
    }

    @Test
    fun `sunrise after short polar night in Murmansk`() {
        val dateTime = ZonedDateTime.of(2025, 1, 2, 12, 0, 0, 0, ZoneOffset.ofHours(3))
        val result = calculator.findUpcomingSolarRelativeEventDay(68.9585, 33.0827, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertTimeOffsetNear(event.timeToNearestEventMillis, ((8 * 24 + 12 + 12)* 60 + 27) * 60 * 1000L)
        assertAzimuthNear(event.azimuth, 174.0)
    }

    @Test
    fun `first event near equator in Nairobi before first event`() {
        val dateTime = ZonedDateTime.of(2025, 3, 10, 5, 0, 0, 0, ZoneId.of("Africa/Nairobi"))
        val result = calculator.findUpcomingSolarRelativeEventDay(-1.2921, 36.8219, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertAzimuthNear(event.azimuth, 94.0)
    }

    @Test
    fun `first event near equator in Nairobi after first event`() {
        val dateTime = ZonedDateTime.of(2025, 3, 10, 10, 0, 0, 0, ZoneId.of("Africa/Nairobi"))
        val result = calculator.findUpcomingSolarRelativeEventDay(-1.2921, 36.8219, dateTime)
        val event = result.events.first()

        assertEquals(EventType.SET, event.type)
        assertAzimuthNear(event.azimuth, 266.0)
    }

    @Test
    fun `sunrise after long polar night in Svalbard`() {
        val dateTime = ZonedDateTime.of(2025, 1, 20, 12, 0, 0, 0, ZoneId.of("Arctic/Longyearbyen"))
        val result = calculator.findUpcomingSolarRelativeEventDay(78.2232, 15.6469, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertTimeOffsetNear(event.timeToNearestEventMillis, ((25 * 24 + 12 + 11) * 60 + 41)* 60 * 1000L)
        assertAzimuthNear(event.azimuth, 173.0)
    }

    @Test
    fun `sunset after polar day in Longyearbyen`() {
        val dateTime = ZonedDateTime.of(2025, 7, 25, 12, 0, 0, 0, ZoneId.of("Arctic/Longyearbyen"))
        val result = calculator.findUpcomingSolarRelativeEventDay(78.2232, 15.6469, dateTime)
        val event = result.events.first()

        assertEquals(EventType.SET, event.type)
        assertAzimuthNear(event.azimuth, 350.0)
    }

    @Test
    fun `sunrise in Alert Canada after long polar night`() {
        val dateTime = ZonedDateTime.of(2025, 1, 10, 12, 0, 0, 0, ZoneId.of("America/Iqaluit"))
        val result = calculator.findUpcomingSolarRelativeEventDay(82.5018, -62.3481, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertTimeOffsetNear(event.timeToNearestEventMillis, ((47 * 24 + 12 + 10) * 60 + 23) * 60 * 1000L)
        assertAzimuthNear(event.azimuth, 165.0)
    }

    @Test
    fun `returns nearest event next day if no events today`() {
        val baseDateTime = ZonedDateTime.of(2025, 1, 1, 23, 50, 0, 0, ZoneOffset.ofHours(2))
        val result = calculator.findUpcomingSolarRelativeEventDay(66.5, 25.7, baseDateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertTimeOffsetNear(event.timeToNearestEventMillis, (10 * 60 + 55 + 10) * 60 * 1000L)
        assertAzimuthNear(event.azimuth, 160.0)
    }

    @Test
    fun `sunset just after midnight UTC at equator`() {
        val dateTime = ZonedDateTime.of(2025, 5, 15, 0, 5, 0, 0, ZoneId.of("UTC"))
        val result = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)
        val event = result.events.first()

        assertEquals(EventType.RISE, event.type)
        assertAzimuthNear(event.azimuth, 71.0)
    }

    private fun SolarRelativeEventDay.timeToFirstEventMillis(): Long =
        events.firstOrNull()?.timeToNearestEventMillis?.toLong() ?: -1

    private fun assertAzimuthNear(actual: Double, expected: Double) {
        assertTrue(
            abs(actual - expected) <= AZIMUTH_TOLERANCE,
            "Azimuth $actual not within ±$AZIMUTH_TOLERANCE of $expected"
        )
    }

    private fun assertTimeOffsetNear(actual: Long, expected: Long) {
        assertTrue(
            abs(actual - expected) <= TIME_OFFSET_TOLERANCE_MILLIS,
            "Time offset $actual not within ±$TIME_OFFSET_TOLERANCE_MILLIS of $expected"
        )
    }
}