package io.github.msaggik.celestialnavevents.model.state

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
 * Represents the possible horizon crossing states of a celestial object (e.g., the Sun)
 * as observed from a specific location over a 24-hour period.
 *
 * This enum describes whether the object rises or sets, remains continuously
 * above or below the horizon, or follows more complex crossing patterns.
 * Such states depend on geographic latitude, date, and seasonal effects,
 * including phenomena typical for polar regions.
 */
enum class HorizonCrossingSolarState {

    /**
     * The object both rises and sets on this day.
     *
     * This is the typical case for most latitudes and seasons.
     */
    RISEN_AND_SET,

    /**
     * The object both sets and rises on this day.
     *
     * For example, this can happen near the boundaries of the polar day,
     * when the Sun sets and rises at the beginning of the polar day.
     */
    SET_AND_RISEN,

    /**
     * The object sets but does not rise on this day.
     *
     * This could happen for an object already above the horizon at 00:00,
     * which sets later, without rising again that same day.
     */
    ONLY_SET,

    /**
     * The object rises but does not set on this day.
     *
     * This may happen when the object is below the horizon at 00:00,
     * rises later, and remains above for the rest of the day.
     */
    ONLY_RISEN,

    /**
     * The object is above the horizon for the entire 24-hour period.
     *
     * Example: The Sun during a polar day at high latitudes near the summer solstice.
     */
    POLAR_DAY,

    /**
     * The object is below the horizon for the entire 24-hour period.
     *
     * Example: The Sun during a polar night at high latitudes near the winter solstice.
     */
    POLAR_NIGHT,

    /**
     * The object sets, rises, and sets again on the same day.
     *
     * This can happen near the edges of polar day when the Sun dips briefly below the horizon.
     */
    SET_RISE_SET,

    /**
     * The object rises, sets, and rises again on the same day.
     *
     * This may occur near the end of a polar night.
     */
    RISE_SET_RISE,

    /**
     * The object’s rising and setting times coincide or overlap,
     * effectively occurring at the same moment.
     *
     * This represents a degenerate or borderline case.
     */
    RISEN_IS_SET,

    /**
     * Undefined or inconsistent horizon crossing state.
     *
     * Represents an error or unexpected condition in computation.
     */
    ERROR
}