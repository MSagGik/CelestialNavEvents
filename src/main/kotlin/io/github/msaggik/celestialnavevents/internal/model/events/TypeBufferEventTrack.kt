package io.github.msaggik.celestialnavevents.internal.model.events

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
 * Enum representing the buffer track type for solar elevation thresholds in event detection.
 *
 * - [LOWER]: Events at the lower solar elevation threshold (e.g., horizon crossings).
 * - [UPPER]: Events at the upper solar elevation threshold (e.g., civil twilight).
 *
 * Used to differentiate and pair events from different elevation levels when building solar event intervals.
 */
internal enum class TypeBufferEventTrack{
    UPPER,
    LOWER
}