package io.github.msaggik.celestialnavevents.model.events.common.track

import io.github.msaggik.celestialnavevents.model.events.common.riseset.EventType
import java.time.ZonedDateTime

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
 * Represents a single point in a celestial event interval.
 *
 * Typically used as either the start or end of a tracked event.
 *
 * @property type Indicates whether the event is a celestial rise (RISE) or celestial set (SET).
 * @property azimuth Optional azimuth angle of the celestial at this point in degrees.
 * @property dateTime The date and time of the event, including time zone.
 */
data class EventPoint(
    val type: EventType,
    val azimuth: Double? = null,
    val dateTime: ZonedDateTime
)