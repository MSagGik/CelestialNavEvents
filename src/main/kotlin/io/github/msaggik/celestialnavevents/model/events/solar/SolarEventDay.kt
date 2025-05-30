package io.github.msaggik.celestialnavevents.model.events.solar

import io.github.msaggik.celestialnavevents.model.events.common.riseset.Event
import io.github.msaggik.celestialnavevents.model.measurement.Time
import io.github.msaggik.celestialnavevents.model.state.HorizonCrossingSolarState

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
 * Represents a basic set of horizon crossing events for a celestial body
 * occurring on a specific day, without relative or absolute context.
 *
 * Suitable as a raw or intermediate structure for solar event modeling.
 *
 * @property events A list of rise and set events for the day.
 * @property type The overall horizon crossing state (e.g., normal day, polar night).
 * @property dayLength The duration of visible time, if applicable.
 * @property nightLength The duration of invisible time, if applicable.
 * @property meridianCrossing The time when the body crosses the local meridian.
 * @property antimeridianCrossing The time when the body crosses the antimeridian.
 */
data class SolarEventDay(
    val events: List<Event>,
    val type: HorizonCrossingSolarState,
    var dayLength: Time? = null,
    var nightLength: Time? = null,
    var meridianCrossing: Time? = null,
    var antimeridianCrossing: Time? = null
)