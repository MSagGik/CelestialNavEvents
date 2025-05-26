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
 * Interface for calculating lunar astronomical events based on geographic and temporal data.
 *
 * Implementations of this interface are responsible for determining lunar rise and set times,
 * the phase and illumination of the Moon, and other related events for a given location and time.
 *
 * Intended to be used in modules requiring lunar calendar calculations, moon phase tracking,
 * or astronomical timing (e.g., tide predictions, night mode scheduling, etc.).
 */
interface LunarEventsCalculator {}