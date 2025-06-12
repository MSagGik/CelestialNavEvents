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
 * A helper data class used to pair an event with its event track type.
 *
 * Primarily used to merge and sort events from different horizon offsets (e.g., standard sunrise vs. magic hour).
 *
 * @property typeBufferEventTrack The source or classification of the solar event (e.g., SUNRISE, MAGIC_HOUR).
 * @property event The actual solar event containing time and azimuth data.
 */
internal data class BufferEventTrack(
    val typeBufferEventTrack: TypeBufferEventTrack,
    val event: Event
)
