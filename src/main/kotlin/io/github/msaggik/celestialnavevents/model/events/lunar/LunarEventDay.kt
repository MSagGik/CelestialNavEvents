package io.github.msaggik.celestialnavevents.model.events.lunar

import io.github.msaggik.celestialnavevents.model.events.common.riseset.Event
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingLunarState

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
 * Represents a complete set of lunar horizon crossing events and related properties
 * for a specific calendar day.
 *
 * In addition to rise and set times, includes moon-specific characteristics such as
 * phase progression and illumination, making it suitable for both astronomical
 * analysis and visual planning.
 *
 * @property events A list of lunar horizon crossing events (e.g., moonrise and moonset).
 * @property type The overall horizon crossing state for the Moon on this day
 *                (e.g., always above horizon, always below, or standard rise/set).
 * @property visibleLength The total duration during which the Moon is above the horizon.
 * @property invisibleLength The total duration during which the Moon is below the horizon.
 * @property meridianCrossing The time when the Moon crosses the local meridian (culmination).
 * @property antimeridianCrossing The time when the Moon crosses the antimeridian (lowest point).
 * @property ageInDays The number of days since the last new moon (lunar age).
 * @property illuminationPercent The percentage of the Moon’s visible surface that is illuminated (0..100).
 *
 * @throws IllegalArgumentException if [illuminationPercent] is not in the range 0..100 inclusive.
 */
data class LunarEventDay(
    val events: List<Event>,
    val type: HorizonCrossingLunarState,
    var visibleLength: Time? = null,
    var invisibleLength: Time? = null,
    var meridianCrossing: Time? = null,
    var antimeridianCrossing: Time? = null,
    val ageInDays: Double = 0.0,
    val illuminationPercent: Double = 0.0
) {
    init {
        require(illuminationPercent in 0.0..100.0) {
            "Illumination percent must be between 0 and 100 inclusive."
        }
    }
}