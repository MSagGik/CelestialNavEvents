package io.github.msaggik.celestialnavevents.time

import io.github.msaggik.celestialnavevents.internal.common.TimeUtils.convertMillisToJulianDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
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

// Tolerance for floating point comparison of Julian Dates, in days (~0.0864 ms)
private const val TOLERANCE = 1e-9

/**
 * Unit tests for the [convertMillisToJulianDate] method, which converts a given [ZonedDateTime]
 * into the corresponding Julian Date (JD) with fractional day precision.
 *
 * Julian Date is a continuous count of days since noon Universal Time on January 1, 4713 BCE
 * (Julian calendar), widely used in astronomy for timekeeping and calculations.
 *
 * These tests verify the correctness of JD calculation for well-known reference dates,
 * including Unix epoch, the Y2K moment, and a leap day, ensuring correct handling
 * of calendar specifics (e.g., leap years) and fractional day computations.
 *
 * The precision tolerance is set extremely low (1e-9) to confirm numerical accuracy
 * of the algorithm implementation.
 */
internal class TimeJDTest {

    @Test
    fun `JD for Unix epoch start 1970-01-01T00Z`() {
        val dt = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2440587.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for Y2K noon 2000-01-01T12Z`() {
        val dt = ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2451545.0
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for leap day 2020-02-29T00Z`() {
        val dt = ZonedDateTime.of(2020, 2, 29, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2458908.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for non-leap year 2019-03-01T00Z`() {
        val dt = ZonedDateTime.of(2019, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2458543.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for date before Unix epoch 1969-12-31T00Z`() {
        val dt = ZonedDateTime.of(1969, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2440586.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for Gregorian calendar start 1582-10-15T00Z`() {
        val dt = ZonedDateTime.of(1582, 10, 15, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2299160.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for Julian calendar last day 1582-10-04T00Z`() {
        val dt = ZonedDateTime.of(1582, 10, 4, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2299159.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for date with time 2023-06-05T18-30-00Z`() {
        val dt = ZonedDateTime.of(2023, 6, 5, 18, 30, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2460101.2708333335
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, 1e-6) // чуть меньше точность из-за дробной части
    }

    @Test
    fun `JD with deltaT correction`() {
        val dt = ZonedDateTime.of(2023, 6, 5, 12, 0, 0, 0, ZoneOffset.UTC)
        val deltaT = 69.0
        val jdWithoutDeltaT = convertMillisToJulianDate(dt, 0.0)
        val jdWithDeltaT = convertMillisToJulianDate(dt, deltaT)
        assertEquals(jdWithoutDeltaT + deltaT / 86400.0, jdWithDeltaT, TOLERANCE)
    }

    @Test
    fun `JD for far future date 3000-01-01T00Z`() {
        val dt = ZonedDateTime.of(3000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedJD = 2816787.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for 2025-01-01T00Z in UTC+9`() {
        val dt = ZonedDateTime.of(2025, 1, 1, 9, 0, 0, 0, ZoneOffset.ofHours(9))
        val expectedJD = 2460676.5
        val actualJD = convertMillisToJulianDate(dt)
        assertEquals(expectedJD, actualJD, TOLERANCE)
    }

    @Test
    fun `JD for 2030-06-15T18-45-00 in UTC-4`() {
        val dt = ZonedDateTime.of(2030, 6, 15, 18, 45, 0, 0, ZoneOffset.ofHours(-4))
        assertEquals(2462668.447916667, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2040-02-29T00Z UTC`() {
        val dt = ZonedDateTime.of(2040, 2, 29, 0, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(2466213.5, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2050-12-31T23-59-59 in UTC+5`() {
        val dt = ZonedDateTime.of(2050, 12, 31, 23, 59, 59, 0, ZoneOffset.ofHours(5))
        assertEquals(2470172.291655093, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2060-07-01T12-30-30 in UTC+3`() {
        val dt = ZonedDateTime.of(2060, 7, 1, 12, 30, 30, 0, ZoneOffset.ofHours(3))
        assertEquals(2473641.896180556, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2075-03-20T06Z in UTC-7`() {
        val dt = ZonedDateTime.of(2075, 3, 20, 6, 0, 0, 0, ZoneOffset.ofHours(-7))
        assertEquals(2479017.041666667, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2088-11-11T11-11-11 in UTC+1`() {
        val dt = ZonedDateTime.of(2088, 11, 11, 11, 11, 11, 0, ZoneOffset.ofHours(1))
        assertEquals(2484001.92443287, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2099-05-05T05-05-05 UTC`() {
        val dt = ZonedDateTime.of(2099, 5, 5, 5, 5, 5, 0, ZoneOffset.UTC)
        assertEquals(2487828.711863426, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2100-01-01T00Z in UTC-5`() {
        val dt = ZonedDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(-5))
        assertEquals(2488069.708333333, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2111-08-08T08-08-08 in UTC+2`() {
        val dt = ZonedDateTime.of(2111, 8, 8, 8, 8, 8, 0, ZoneOffset.ofHours(2))
        assertEquals(2492305.755648148, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2122-09-09T09-09-09 in UTC+9`() {
        val dt = ZonedDateTime.of(2122, 9, 9, 9, 9, 9, 0, ZoneOffset.ofHours(9))
        assertEquals(2496355.506354167, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2133-10-10T10-10-10 in UTC-10`() {
        val dt = ZonedDateTime.of(2133, 10, 10, 10, 10, 10, 0, ZoneOffset.ofHours(-10))
        assertEquals(2500405.340393519, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2144-04-04T04-04-04 in UTC+4`() {
        val dt = ZonedDateTime.of(2144, 4, 4, 4, 4, 4, 0, ZoneOffset.ofHours(4))
        assertEquals(2504233.502824074, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2155-02-28T23-59-59 UTC`() {
        val dt = ZonedDateTime.of(2155, 2, 28, 23, 59, 59, 0, ZoneOffset.UTC)
        assertEquals(2508216.499988426, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2166-06-06T06-06-06 in UTC-6`() {
        val dt = ZonedDateTime.of(2166, 6, 6, 6, 6, 6, 0, ZoneOffset.ofHours(-6))
        assertEquals(2512332.004236111, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2177-07-07T07-07-07 in UTC+7`() {
        val dt = ZonedDateTime.of(2177, 7, 7, 7, 7, 7, 0, ZoneOffset.ofHours(7))
        assertEquals(2516380.50494213, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2188-12-12T12-12-12 in UTC-3`() {
        val dt = ZonedDateTime.of(2188, 12, 12, 12, 12, 12, 0, ZoneOffset.ofHours(-3))
        assertEquals(2520557.133472222, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2200-01-01T00Z UTC`() {
        val dt = ZonedDateTime.of(2200, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(2524593.5, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2250-05-05T05-05-05 in UTC-5`() {
        val dt = ZonedDateTime.of(2250, 5, 5, 5, 5, 5, 0, ZoneOffset.ofHours(-5))
        assertEquals(2542979.920196759, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 2300-10-10T10-10-10 in UTC+10`() {
        val dt = ZonedDateTime.of(2300, 10, 10, 10, 10, 10, 0, ZoneOffset.ofHours(10))
        assertEquals(2561399.507060185, convertMillisToJulianDate(dt), 1e-9)
    }

    @Test
    fun `JD for 3025-01-01T00Z UTC`() {
        val dt = ZonedDateTime.of(3025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(2825918.5, convertMillisToJulianDate(dt), 1e-9)
    }
}