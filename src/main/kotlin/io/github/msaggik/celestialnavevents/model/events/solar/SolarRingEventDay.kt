package io.github.msaggik.celestialnavevents.model.events.solar

import io.github.msaggik.celestialnavevents.model.events.common.track.EventTrack
import io.github.msaggik.celestialnavevents.model.measurement.Time

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
 * Represents solar time segments for a given day associated with a specific solar ring event,
 * such as the magic hour, blue hour, or twilight phases.
 *
 * This class provides both the main event intervals (e.g., twilight periods) and
 * aggregate durations for the remaining parts of the day outside of those intervals.
 *
 * @property events A list of time intervals representing the target solar event (e.g., blue hour, magic hour).
 * @property daylightBeforeRing Total duration of daylight before the first event interval begins.
 * @property ringDuration Total duration of all event intervals combined (e.g., sum of morning and evening blue hour).
 * @property darknessAfterRing Total duration of nighttime after the last event interval ends.
 */
data class SolarRingEventDay(
    val events: List<EventTrack>,
    var daylightBeforeRing: Time? = null,
    var ringDuration: Time? = null,
    var darknessAfterRing: Time? = null
)