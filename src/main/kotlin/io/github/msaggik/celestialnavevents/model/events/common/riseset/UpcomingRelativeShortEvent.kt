package io.github.msaggik.celestialnavevents.model.events.common.riseset

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
 * A lightweight representation of the next upcoming astronomical event using relative timing.
 *
 * Provides only the event type and the time offset in milliseconds from the current moment.
 * Ideal for simplified scheduling, reminders, or fast computations where full event context is unnecessary.
 *
 * Can be used for any celestial object such as the Sun, Moon, or other astronomical bodies.
 *
 * @property eventType The type of the upcoming event (e.g., rise or set).
 * @property timestampMillis Time in milliseconds from now until the event occurs.
 */
data class UpcomingRelativeShortEvent(
    val eventType: EventType,
    val timestampMillis: Long
)