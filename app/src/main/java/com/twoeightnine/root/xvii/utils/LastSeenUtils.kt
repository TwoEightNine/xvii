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

package com.twoeightnine.root.xvii.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint


object LastSeenUtils {

    const val SPAN_TEMPLATE = "\u2004 "

    private const val SPAN_SIZE_FACTOR = 0.8f
    private const val TEXT_SIZE_DEFAULT = 28

    fun getFull(
            context: Context?,
            isOnline: Boolean,
            timeStamp: Int,
            deviceCode: Int,
            textSizePx: Int = TEXT_SIZE_DEFAULT,
            withSeconds: Boolean = Prefs.showSeconds
    ): CharSequence {
        if (context == null) return ""

        val deviceIconSpan = createDeviceIconSpan(context, deviceCode, textSizePx)

        val time = when (timeStamp) {
            0 -> time() - (if (isOnline) 0 else 300)
            else -> timeStamp
        }
        val stringRes = if (isOnline) R.string.online_seen else R.string.last_seen
        val lastSeen = context.getString(stringRes, getTime(time, withSeconds = withSeconds))

        return if (deviceIconSpan != null) {
            val text = "$lastSeen$SPAN_TEMPLATE"
            val position = text.length - 1
            getSpannedWithDeviceIcon(context, text, position, deviceCode, textSizePx)
        } else {
            lastSeen
        }
    }

    fun getSpannedWithDeviceIcon(
            context: Context,
            text: CharSequence,
            position: Int,
            deviceCode: Int,
            textSizePx: Int = TEXT_SIZE_DEFAULT
    ): CharSequence {
        val deviceIconSpan = createDeviceIconSpan(context, deviceCode, textSizePx)
        return SpannableStringBuilder()
                .append(text)
                .apply {
                    setSpan(deviceIconSpan, position, position + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

    }

    private fun createDeviceIconSpan(context: Context, deviceCode: Int, textSizePx: Int): ImageSpan? {
        val height = (textSizePx * SPAN_SIZE_FACTOR).toInt()
        return getDeviceIconRes(deviceCode)
                .takeIf { it != 0 }
                ?.let { ContextCompat.getDrawable(context, it) }
                ?.apply { setBounds(0, 0, height, height) }
                ?.apply { paint(Munch.color.color50) }
                ?.let { ImageSpan(it, ImageSpan.ALIGN_BASELINE) }
    }

    @DrawableRes
    private fun getDeviceIconRes(deviceCode: Int): Int {
        if (deviceCode !in 1..7) return 0

        return when(deviceCode) {
            1 -> R.drawable.ic_mobile
            2, 3 -> R.drawable.ic_apple
            4 -> R.drawable.ic_android
            5, 6 -> R.drawable.ic_windows
            else -> R.drawable.ic_desktop
        }
    }
}