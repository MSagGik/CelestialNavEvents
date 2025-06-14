package io.github.msaggik.celestialnavevents

import io.github.msaggik.celestialnavevents.api.CelestialEventsCalculator
import io.github.msaggik.celestialnavevents.api.impl.CelestialEventsCalculatorImpl

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
 * Entry point and factory for accessing unified solar and lunar astronomical event calculations.
 *
 * This class acts as a facade and factory for accessing [CelestialEventsCalculator],
 * which provides methods to compute solar and lunar events.
 *
 * ## Design Patterns
 * - **Factory Pattern**: Instantiates and returns a unified calculator.
 * - **Facade Pattern**: Hides the complexity of the underlying implementations.
 *
 * ## Example
 * ```kotlin
 * val celestial = CelestialNavEvents().provide()
 * val sunEvent = celestial.solar().calculateSolarEventDay(...)
 * val moonEvent = celestial.lunar().calculateLunarEventDay(...)
 * ```
 */
class CelestialNavEvents {

    private val calculator: CelestialEventsCalculator by lazy {
        CelestialEventsCalculatorImpl()
    }

    /**
     * Provides a singleton instance of [CelestialEventsCalculator], which allows access to both
     * solar and lunar astronomical event calculations.
     *
     * @return A unified [CelestialEventsCalculator] implementation.
     */
    fun provide(): CelestialEventsCalculator = calculator
}