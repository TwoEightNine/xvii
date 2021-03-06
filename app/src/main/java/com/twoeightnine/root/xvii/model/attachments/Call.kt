/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.twoeightnine.root.xvii.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Call (

    @SerializedName("state")
    @Expose
    private val stateInternal: String,

    @SerializedName("duration")
    @Expose
    val duration: Int
) : Parcelable {

    val state: State
        get() = when (stateInternal) {
            "reached" -> State.REACHED
            "canceled_by_receiver" -> State.DECLINED
            "canceled_by_initiator" -> State.MISSED
            else -> State.UNKNOWN
        }

    enum class State {
        REACHED,
        DECLINED,
        MISSED,

        UNKNOWN
    }
}