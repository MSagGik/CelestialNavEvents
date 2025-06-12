package io.github.msaggik.celestialnavevents.api

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
 * Facade interface that provides access to both solar and lunar astronomical event calculators.
 *
 * This abstraction allows users to work with a unified entry point for computing
 * celestial events, such as sunrises, moon phases, twilights, and more.
 */
interface CelestialEventsCalculator {

    /**
     * Provides access to solar event calculations, including sunrise, sunset,
     * twilight periods, magic hour, and related phenomena.
     *
     * @return A [SolarEventsCalculator] instance capable of computing solar astronomical data.
     */
    fun solar(): SolarEventsCalculator

    /**
     * Provides access to lunar event calculations, including moonrise, moonset,
     * moon phases, illumination, and related phenomena.
     *
     * @return A [LunarEventsCalculator] instance capable of computing lunar astronomical data.
     */
    fun lunar(): LunarEventsCalculator
}