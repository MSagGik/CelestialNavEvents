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
 * Enumeration representing various types of solar event intervals.
 *
 * These event types describe transitional lighting phases that occur as the Sun moves relative to the horizon.
 * They are commonly used in astronomy, navigation, and photography to determine optimal lighting conditions.
 */
enum class TypeEventTrack {
    /**
     * Magic hour — period shortly after sunrise or before sunset when sunlight is warm and diffused.
     * Often used in landscape and portrait photography due to its visually pleasing quality.
     */
    MAGIC_HOUR,

    /**
     * Blue hour — phase when the Sun is approximately 4–6 degrees below the horizon,
     * resulting in a deep blue hue in the sky. Occurs just before sunrise and just after sunset.
     */
    BLUE_HOUR,

    /**
     * Civil twilight — period when the Sun is between 0° and 6° below the horizon.
     * There is still enough natural light for most outdoor activities without artificial lighting.
     */
    CIVIL_TWILIGHT,

    /**
     * Nautical twilight — occurs when the Sun is between 6° and 12° below the horizon.
     * The horizon remains visible at sea, making it useful for marine navigation.
     */
    NAUTICAL_TWILIGHT,

    /**
     * Astronomical twilight — occurs when the Sun is between 12° and 18° below the horizon.
     * Marks the limit at which the sky is dark enough for astronomical observations.
     */
    ASTRONOMICAL_TWILIGHT,

    /**
     * Composite or user-defined interval, used in custom solar ring calculations.
     * Allows for arbitrary definitions beyond standard twilight phases.
     */
    POLY
}