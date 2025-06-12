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
 * Represents the types of horizon crossing events.
 *
 * Used to specify whether the event corresponds to a rise (sunrise/dawn) or a set (sunset/dusk).
 */
enum class EventType {
    /**
     * The event is a rise (e.g., sunrise, moonrise).
     */
    RISE,

    /**
     * The event is a set (e.g., sunset, moonset).
     */
    SET
}