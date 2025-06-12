package io.github.msaggik.celestialnavevents.model.events.solar

import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingAbsoluteEvent
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
 * Represents a day containing absolute astronomical events for a celestial body,
 * where each event includes a concrete date and time.
 *
 * Suitable for use cases requiring time references for future horizon crossings,
 * such as sunrise or sunset.
 *
 * @property events A list of horizon crossing events (e.g., rise/set) with absolute date and time.
 * @property type The overall horizon crossing state for this day
 *                (e.g., polar day, polar night, normal cycle).
 * @property preType The horizon crossing state of the previous day, useful for context (e.g., transition from polar night).
 * @property dayLength The total duration of daylight, if applicable.
 * @property nightLength The total duration of nighttime, if applicable.
 * @property meridianCrossing The time when the celestial body crosses the local meridian (culmination).
 * @property antimeridianCrossing The time when the body crosses the antimeridian (lowest point).
 */
data class SolarAbsoluteEventDay(
    val events: List<UpcomingAbsoluteEvent>,
    val type: HorizonCrossingSolarState? = null,
    val preType: HorizonCrossingSolarState? = null,
    val dayLength: Time? = null,
    val nightLength: Time? = null,
    val meridianCrossing: Time? = null,
    val antimeridianCrossing: Time? = null
) {
    companion object {
        fun getDefaultSolarAbsoluteEventDay(): SolarAbsoluteEventDay =
            SolarAbsoluteEventDay(
                events = listOf()
            )
    }
}
