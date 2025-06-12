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
 * Entry point and factory for creating an instance of the unified astronomical events calculator.
 *
 * This object provides a simplified way to access both solar and lunar event calculators through
 * a single call to [create], following the Factory and Facade design patterns.
 *
 * Example usage:
 * ```
 * val celestial = CelestialNavEvents.create()
 * val sunEvents = celestial.solar().calculateSunEvent(...)
 * val moonEvents = celestial.lunar().calculateMoonEvent(...)
 * ```
 */
object CelestialNavEvents {

    /**
     * Creates a new instance of [CelestialEventsCalculator], which provides access
     * to both solar and lunar astronomical event calculations.
     *
     * @return A [CelestialEventsCalculator] implementation that acts as a unified interface
     *         to all celestial navigation computations.
     */
    fun create(): CelestialEventsCalculator = CelestialEventsCalculatorImpl()
}