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
 * Aggregated test suite for CelestialNavEvents core astronomical calculations.
 *
 * This suite runs comprehensive tests covering all critical modules of the library:
 * - **Solar event detection and classification:** Verifies sunrise, sunset, twilight phases, magic/blue hour, and horizon events.
 * - **Lunar event calculations:** Tests moonrise, moonset, phase determination (in days), and illumination percentage.
 * - **Astronomical position algorithms:** Checks right ascension, declination, and related celestial coordinates.
 * - **Time and date utilities:** Validates Julian date conversions, time system corrections (UT to TT), and leap second handling.
 *
 * Use this suite to ensure the accuracy and stability of all solar and lunar event computations, position solvers, and time conversion helpers.
 *
 * Test coverage includes:
 * - SolarEventTest: Sunrise, sunset, twilight, and event classification
 * - SolarRelativeEventDayTest: Relative solar event intervals and periods
 * - SolarRelativeShortEventTest: Short-duration solar events
 * - SolarAbsoluteEventDayTest: Absolute time-based solar events
 * - MagicHourTest: Magic hour and blue hour detection
 * - LunarEventTest: Moonrise, moonset, and lunar phase (in days) calculations
 * - LunarRelativeEventDayTest: Relative lunar event intervals and periods
 * - CelestialPositionTest: Astronomical coordinate algorithms
 * - TimeJDTest: Julian date conversions
 * - TimeTest: General time utilities
 * - UtToTtCorrection: UT-TT corrections and leap second handling
 *
 * Use this suite to run a complete validation of the solar and astronomical calculation modules.
 *
 * This class is annotated with JUnit Platform's [@Suite] and [@SelectClasses] to ensure grouped execution.
 *
 * Note: Requires JUnit 5 with the `junit-platform-suite` module included as a test dependency.
 *
 * @see SolarEventTest
 * @see SolarRelativeEventDayTest
 * @see SolarRelativeShortEventTest
 * @see SolarAbsoluteEventDayTest
 * @see MagicHourTest
 * @see LunarEventTest
 * @see LunarRelativeEventDayTest
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