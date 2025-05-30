package io.github.msaggik.celestialnavevents.internal.model.settings

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
 * Data structure representing corrections applied to astronomical calculations
 * related to the rising and setting of celestial bodies (e.g., Sun and Moon).
 *
 * @property angleFromHorizon The angular offset from the horizon in degrees.
 * This value adjusts the calculated position of the celestial body relative to the horizon,
 * where negative values represent positions below the horizon and positive values above it.
 *
 * @property isAtmosphericRefractionIncluded Indicates whether atmospheric refraction
 * should be taken into account when calculating the position of the celestial body.
 */
internal class HorizonCorrection(
    val angleFromHorizon : Double,
    val isAtmosphericRefractionIncluded: Boolean
)