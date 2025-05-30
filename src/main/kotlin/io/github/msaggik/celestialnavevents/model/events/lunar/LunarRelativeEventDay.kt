package io.github.msaggik.celestialnavevents.model.events.lunar

import io.github.msaggik.celestialnavevents.model.events.common.riseset.UpcomingRelativeEvent
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


data class LunarRelativeEventDay(
    val events: List<UpcomingRelativeEvent>,
    val type: HorizonCrossingLunarState? = null,
    val preType: HorizonCrossingLunarState? = null,
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

    companion object {
        fun getDefaultLunarRelativeEventDay(): LunarRelativeEventDay =
            LunarRelativeEventDay(
                events = listOf()
            )
    }
}