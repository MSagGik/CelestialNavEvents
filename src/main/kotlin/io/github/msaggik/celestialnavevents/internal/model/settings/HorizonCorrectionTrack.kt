package io.github.msaggik.celestialnavevents.internal.model.settings

import io.github.msaggik.celestialnavevents.model.events.common.track.TypeEventTrack

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
 * Represents a range of angular corrections used to define the bounds of a solar event interval.
 *
 * Used for calculating intervals like blue hour, twilight phases, or magic hour,
 * where the event is bounded by two angular positions of the Sun relative to the horizon.
 *
 * @property lowerHorizonCorrection The lower bound of the angular range (typically more negative).
 * @property upperHorizonCorrection The upper bound of the angular range (closer to or above horizon).
 * @property typeEventTrack The semantic label indicating the nature of the interval
 * (e.g., CIVIL_TWILIGHT, MAGIC_HOUR).
 */
internal class HorizonCorrectionTrack(
    val lowerHorizonCorrection : HorizonCorrection,
    val upperHorizonCorrection : HorizonCorrection,
    val typeEventTrack: TypeEventTrack
)