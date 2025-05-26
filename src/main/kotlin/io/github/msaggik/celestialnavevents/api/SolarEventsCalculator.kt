package io.github.msaggik.celestialnavevents.api

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
 * Interface for calculating various solar events (e.g., sunrise, sunset, magic hour, twilight phases)
 * based on geographic coordinates and date-time.
 *
 * Implementations of this interface provide methods for:
 * - Retrieving upcoming solar events with or without time offsets.
 * - Calculating full sets of daily solar events.
 * - Determining specialized light periods such as magic hour and different twilight phases.
 */
interface SolarEventsCalculator {}