package io.github.msaggik.celestialnavevents.model.events.common.track

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
 * Represents a continuous time interval between two celestial events (start and end).
 *
 * Each track is associated with a type (e.g., MAGIC_HOUR) and is used to group related celestial phenomena.
 *
 * @property typeEventTrack Classification of the event interval (e.g., MAGIC_HOUR, SUNRISE).
 * @property start The start point of the event interval.
 * @property finish The end point of the event interval.
 */
data class EventTrack(
    val typeEventTrack: TypeEventTrack,
    val start: EventPoint,
    val finish: EventPoint
)
