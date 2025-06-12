package io.github.msaggik.celestialnavevents

import io.github.msaggik.celestialnavevents.events.solar.MagicHourTest
import io.github.msaggik.celestialnavevents.events.solar.SolarAbsoluteEventDayTest
import io.github.msaggik.celestialnavevents.events.solar.SolarEventTest
import io.github.msaggik.celestialnavevents.events.solar.SolarRelativeEventDayTest
import io.github.msaggik.celestialnavevents.events.lunar.LunarRelativeEventDayTest
import io.github.msaggik.celestialnavevents.events.lunar.LunarEventTest
import io.github.msaggik.celestialnavevents.events.solar.SolarRelativeShortEventTest
import io.github.msaggik.celestialnavevents.position.CelestialPositionTest
import io.github.msaggik.celestialnavevents.time.TimeJDTest
import io.github.msaggik.celestialnavevents.time.TimeTest
import io.github.msaggik.celestialnavevents.time.UtToTtCorrection
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

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
 * Test suite that aggregates all core celestial mechanics and solar event test classes.
 *
 * This suite includes tests for:
 * - Solar event detection and classification (e.g., sunrise, sunset).
 * - Astronomical position calculations (e.g., right ascension, declination).
 * - Julian date conversions and time-related utilities.
 * - UT to TT correction estimates and leap second handling.
 *
 * Use this suite to run a complete validation of the solar and astronomical calculation modules.
 *
 * This class is annotated with JUnit Platform's [@Suite] and [@SelectClasses] to ensure grouped execution.
 *
 * Note: Requires JUnit 5 with the `junit-platform-suite` module included as a test dependency.
 *
 * @see SolarEventTest
 * @see CelestialPositionTest
 * @see TimeJDTest
 * @see TimeTest
 * @see UtToTtCorrection
 */
@Suite
@SelectClasses(
    value = [
        SolarEventTest::class,
        SolarRelativeEventDayTest::class,
        SolarRelativeShortEventTest::class,
        SolarAbsoluteEventDayTest::class,
        MagicHourTest::class,
        LunarEventTest::class,
        LunarRelativeEventDayTest::class,
        CelestialPositionTest::class,
        TimeJDTest::class,
        TimeTest::class,
        UtToTtCorrection::class
    ]
)
internal class AllCelestialTests