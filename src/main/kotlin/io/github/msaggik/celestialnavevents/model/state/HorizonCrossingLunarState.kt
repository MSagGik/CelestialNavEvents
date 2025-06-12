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
 * Represents the possible types of horizon crossing states for a celestial object
 * as observed from a given location on a given date.
 *
 * This enum is typically used to describe whether an object
 * rises or sets during a 24-hour period, or if it remains above or below the horizon
 * for the entire day due to geographic latitude or season (e.g., polar regions).
 */
enum class HorizonCrossingLunarState {

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
     */
    FULL_DAY,

    /**
     * The object is below the horizon for the entire 24-hour period.
     */
    FULL_NIGHT,

    /**
     * The object sets, rises, and sets again on the same day.
     *
     * This can happen near under special lunar geometry.
     */
    SET_RISE_SET,

    /**
     * The object rises, sets, and rises again on the same day.
     *
     * This may occur for the Moon near the poles.
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