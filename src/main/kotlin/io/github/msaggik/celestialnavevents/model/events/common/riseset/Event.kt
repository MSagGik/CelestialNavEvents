package io.github.msaggik.celestialnavevents.model.events.common.riseset

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
 * Represents a single horizon crossing event for a celestial body.
 *
 * @property type The type of the event: rise or set.
 * @property azimuth The azimuth angle (in degrees) at which the event occurs.
 * @property time The time when the event occurs.
 */
data class Event(
    val type: EventType,
    val azimuth: Double,
    val time: Time
)