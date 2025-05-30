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
 * Represents the next upcoming astronomical event with a relative time offset from the current moment.
 *
 * Includes the local time of the event within the day ([Time]), azimuth angle, and the duration
 * in milliseconds until the event occurs.
 *
 * Suitable for countdowns, visual interfaces, or any scenario where relative timing is needed.
 * Designed for use with any astronomical body such as the Sun or Moon.
 *
 * @property type The type of the astronomical event (e.g., rise or set).
 * @property azimuth The azimuth angle at which the event occurs, in degrees.
 * @property time The local time of day when the event is expected.
 * @property timeToNearestEventMillis Time in milliseconds from the current moment until the event.
 */
data class UpcomingRelativeEvent(
    val type: EventType,
    val azimuth: Double,
    val time: Time,
    val timeToNearestEventMillis: Long
)
