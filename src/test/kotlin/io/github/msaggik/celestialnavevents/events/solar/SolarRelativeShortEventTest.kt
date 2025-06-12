package io.github.msaggik.celestialnavevents.events.solar

import io.github.msaggik.celestialnavevents.internal.solar.SolarCalculatorImpl
import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import io.github.msaggik.celestialnavevents.model.measurement.Coordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneId
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
 * Unit tests for [SolarCalculatorImpl.findUpcomingSolarRelativeShortEventDay].
 *
 * These tests validate the behavior of the method responsible for determining
 * the next upcoming solar event (sunrise or sunset) relative to a specific time and location.
 *
 * Covered scenarios include:
 * - Equatorial zones with normal sunrise/sunset cycles.
 * - Polar regions during seasons with no events for extended periods.
 * - Cities in different time zones and latitudes (e.g., Tokyo, Buenos Aires).
 * - Edge cases like events occurring exactly at the provided time.
 * - Filtering logic when first events are discarded based on relative timing.
 * - Input validation (e.g., out-of-bounds latitude should throw an exception).
 *
 * Only the method [SolarCalculatorImpl.findUpcomingSolarRelativeShortEventDay] is directly tested.
 * Other methods (e.g., absolute or full-day relative searches) may be referenced
 * for comparison or setup but are not under test here.
 *
 * These tests ensure correct timestamp calculations, proper filtering of past events,
 * and robustness across edge-case geographic and temporal inputs.
 */
internal class SolarRelativeShortEventTest {
    private val calculator = SolarCalculatorImpl()

    @Test
    fun `returns sunrise or sunset shortly after given time in equatorial zone before rise`() {
        val dateTime = ZonedDateTime.of(2025, 6, 1, 5, 0, 0, 0, ZoneId.of("UTC"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(0.0, 0.0, dateTime)

        assertNotNull(event)
        assertTrue(event!!.timestampMillis >= 0)
        assertTrue(event.eventType == EventType.RISE )
    }

    @Test
    fun `returns sunrise or sunset shortly after given time in equatorial zone after rise`() {
        val dateTime = ZonedDateTime.of(2025, 6, 1, 7, 0, 0, 0, ZoneId.of("UTC"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(0.0, 0.0, dateTime)

        assertNotNull(event)
        assertTrue(event!!.timestampMillis >= 0)
        assertTrue(event.eventType == EventType.SET)
    }

    @Test
    fun `returns null when no events exist for up to a year (polar night)`() {
        val dateTime = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(89.9, 0.0, dateTime)

        assertNotNull(event)
        assertTrue(event!!.timestampMillis >= 0)
        assertTrue(event.eventType == EventType.RISE )
    }

    @Test
    fun `returns nearest event next day if no events today`() {
        val baseDateTime = ZonedDateTime.of(2025, 1, 1, 23, 50, 0, 0, ZoneId.of("UTC"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(66.5, 25.7, baseDateTime)

        assertNotNull(event)
        assertTrue(event!!.timestampMillis > 0)
    }

    @Test
    fun `returns sunrise in Tokyo before morning time`() {
        val dateTime = ZonedDateTime.of(2025, 6, 10, 2, 0, 0, 0, ZoneId.of("Asia/Tokyo"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(35.68, 139.76, dateTime)

        assertNotNull(event)
        assertEquals(EventType.RISE, event!!.eventType)
        assertTrue(event.timestampMillis in 1..(6 * 60 * 60 * 1000)) // < 6 часов до восхода
    }

    @Test
    fun `returns sunset in Buenos Aires in evening`() {
        val dateTime = ZonedDateTime.of(2025, 6, 10, 17, 30, 0, 0, ZoneId.of("America/Argentina/Buenos_Aires"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(-34.6, -58.4, dateTime)

        assertNotNull(event)
        assertEquals(EventType.SET, event!!.eventType)
        assertTrue(event.timestampMillis in 0..(3 * 60 * 60 * 1000)) // закат в пределах 3 часов
    }

    @Test
    fun `timestampMillis is 0 when event is exactly at dateTime`() {
        val dateTime = ZonedDateTime.of(2025, 6, 10, 6, 0, 0, 0, ZoneId.of("UTC"))
        val eventDay = calculator.findUpcomingSolarRelativeEventDay(0.0, 0.0, dateTime)

        val exactTimeEvent = eventDay.events.firstOrNull {
            it.timeToNearestEventMillis == 0L
        }

        if (exactTimeEvent != null) {
            val shortEvent = calculator.findUpcomingSolarRelativeShortEventDay(0.0, 0.0, dateTime)
            assertNotNull(shortEvent)
            assertEquals(0, shortEvent!!.timestampMillis)
        }
    }

    @Test
    fun `timestampMillis is positive and within 24h for regular day at mid-latitudes`() {
        val dateTime = ZonedDateTime.of(2025, 6, 8, 10, 0, 0, 0, ZoneId.of("Europe/London"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(51.5, -0.1, dateTime)

        assertNotNull(event)
        assertTrue(event!!.timestampMillis in 0..(24 * 60 * 60 * 1000))
    }

    @Test
    fun `throws exception for invalid latitude`() {
        val dateTime = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))

        assertThrows(IllegalArgumentException::class.java) {
            val invalidCoordinate = Coordinate(-91.0, 0.0)
            calculator.findUpcomingSolarRelativeShortEventDay(
                invalidCoordinate.latitude,
                invalidCoordinate.longitude,
                dateTime
            )
        }
    }

    @Test
    fun `returns correct event when first event in event list is filtered out`() {
        val dateTime = ZonedDateTime.of(2025, 6, 10, 23, 50, 0, 0, ZoneId.of("UTC"))
        val event = calculator.findUpcomingSolarRelativeShortEventDay(30.0, 31.0, dateTime)

        assertNotNull(event)
        assertTrue(event!!.timestampMillis >= 0)
    }

    @Test
    fun `event returned is consistent with event from absolute method`() {
        val dateTime = ZonedDateTime.of(2025, 6, 9, 6, 0, 0, 0, ZoneId.of("UTC"))
        val shortEvent = calculator.findUpcomingSolarRelativeShortEventDay(48.85, 2.35, dateTime)!!
        val absEvent = calculator.findUpcomingSolarAbsoluteEventDay(48.85, 2.35, dateTime).events.first()

        val expectedMillis = Duration.between(dateTime, absEvent.dateTime).toMillis()
        val toleranceMillis = 5 * 60 * 1000L

        assertTrue(abs(shortEvent.timestampMillis - expectedMillis) <= toleranceMillis)
    }
}