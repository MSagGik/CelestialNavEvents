package io.github.msaggik.celestialnavevents.model.measurement

import java.util.Locale

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
 * Represents geographical coordinates on Earth.
 *
 * @property latitude Latitude in degrees. Valid range: [-90.0, 90.0].
 * Positive values indicate north of the equator, negative values indicate south.
 *
 * @property longitude Longitude in degrees. Valid range: [-180.0, 180.0].
 * Positive values indicate east of the Prime Meridian, negative values indicate west.
 *
 * @throws IllegalArgumentException if latitude or longitude is out of valid range.
 */
class Coordinate(
    val latitude : Double,
    val longitude : Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90.0 and 90.0 degrees inclusive."
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180.0 and 180.0 degrees inclusive."
        }
    }

    override fun toString(): String {
        return String.format(Locale.US, "lat=%.8f, lon=%.8f", latitude, longitude)
    }
}