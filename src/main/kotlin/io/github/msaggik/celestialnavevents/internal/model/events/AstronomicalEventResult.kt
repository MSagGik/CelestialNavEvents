package io.github.msaggik.celestialnavevents.internal.model.events

import io.github.msaggik.celestialnavevents.model.events.common.riseset.Event

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
 * Container for the result of an astronomical event computation.
 *
 * This data class holds any detected celestial events (e.g., rise or set)
 * during a given interval, along with the object's final vertical position
 * relative to the observer’s corrected horizon.
 *
 * @property events List of detected astronomical events (e.g., sunrise, moonset).
 * @property verticalPosition Final vertical position of the celestial body
 *           at the end of the evaluated time interval. Used for continuity in
 *           subsequent calculations.
 */
internal data class AstronomicalEventResult(
    val events: List<Event>,
    var verticalPosition: Double = 0.0
)