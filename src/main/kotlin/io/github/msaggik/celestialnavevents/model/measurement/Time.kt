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
 * Represents a time of day with an optional day offset.
 *
 * @property days Number of full days offset (can be negative or positive).
 * @property hour Hour of the day (0..23).
 * @property min Minute of the hour (0..59).
 * @property sec Second of the minute (0..59).
 * @property milliSec Millisecond of the second (0..999).
 *
 * @throws IllegalArgumentException if [hour], [min], [sec], or [milliSec] is out of valid range.
 */
data class Time(
    val hour: Int,
    val min: Int,
    val sec: Int = 0,
    val milliSec: Int = 0,
    var days: Int = 0,
) {

    init {
        require(hour in 0..23) { "Hour must be between 0 and 23 inclusive." }
        require(min in 0..59) { "Minute must be between 0 and 59 inclusive." }
        require(sec in 0..59) { "Second must be between 0 and 59 inclusive." }
        require(milliSec in 0..999) { "Millisecond must be between 0 and 999 inclusive." }
    }

    /**
     * Returns time as a string formatted as "HH:mm" or with day offset as "+Nd HH:mm".
     */
    override fun toString(): String {
        return if (days == 0) {
            String.format(Locale.US, "%02d:%02d", hour, min)
        } else {
            val sign = if (days > 0) "+" else "-"
            String.format(Locale.US, "%s%dd %02d:%02d", sign, kotlin.math.abs(days), hour, min)
        }
    }

    /**
     * Converts the current time into the total number of minutes.
     *
     * @return Total minutes, including day offset.
     */
    fun toTotalMinutes(): Int = days * 1440 + hour * 60 + min

    /**
     * Converts the current time into the total number of milliseconds.
     *
     * @return Total milliseconds, including day offset.
     */
    fun toTotalMilliseconds(): Long = (((days * 1440 + hour * 60 + min) * 60 + sec) * 1000 + milliSec).toLong()

    companion object {
        /**
         * Creates a [Time] instance from total minutes, normalizing to valid hour and minute values.
         *
         * @param totalMinutes Total number of minutes (can be negative).
         * @return A normalized [Time] instance.
         */
        fun fromTotalMinutes(totalMinutes: Int): Time {
            var minutes = totalMinutes % 60
            var totalHour = totalMinutes / 60

            if (minutes < 0) {
                minutes += 60
                totalHour -= 1
            }

            var hour = totalHour % 24
            var totalDays = totalHour / 24

            if (hour < 0) {
                hour += 24
                totalDays -= 1
            }
            return Time(days = totalDays, hour = hour, min = minutes)
        }

        /**
         * Creates a [Time] instance from total milliseconds, normalizing all components.
         *
         * @param totalMilliseconds Total number of milliseconds (can be negative).
         * @return A normalized [Time] instance.
         */
        fun fromTotalMilliseconds(totalMilliseconds: Long): Time {
            var milliseconds = totalMilliseconds % 1000
            var totalSeconds = totalMilliseconds / 1000

            if (milliseconds < 0) {
                milliseconds += 1000
                totalSeconds -= 1
            }

            var seconds = totalSeconds % 60
            var totalMinutes = totalSeconds / 60

            if (seconds < 0) {
                seconds += 60
                totalMinutes -= 1
            }

            var minutes = totalMinutes % 60
            var totalHour = totalMinutes / 60

            if (minutes < 0) {
                minutes += 60
                totalHour -= 1
            }

            var hour = totalHour % 24
            var totalDays = totalHour / 24

            if (hour < 0) {
                hour += 24
                totalDays -= 1
            }

            return Time(
                days = totalDays.toInt(),
                hour = hour.toInt(),
                min = minutes.toInt(),
                sec = seconds.toInt(),
                milliSec = milliseconds.toInt()
            )
        }
    }
}