package io.github.msaggik.celestialnavevents.model.events.common.riseset

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
 * Represents the next upcoming astronomical event with an absolute timestamp.
 *
 * Provides full event details including the date and time ([ZonedDateTime]),
 * the azimuth angle, and the event type (e.g., rise, set).
 *
 * Applicable to any celestial object such as the Sun, Moon, or planets.
 * Useful for calendar integration, logging, or time-based scheduling.
 *
 * @property type The type of the astronomical event (e.g., rise or set).
 * @property azimuth The azimuth angle of the celestial object at the time of the event, in degrees.
 * @property dateTime The date and time of the event with time zone information.
 */
data class UpcomingAbsoluteEvent(
    val type: EventType,
    val azimuth: Double,
    val dateTime: ZonedDateTime
)
