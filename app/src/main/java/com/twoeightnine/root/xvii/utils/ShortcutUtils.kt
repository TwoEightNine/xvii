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
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import global.msnthrp.xvii.data.dialogs.Dialog
import global.msnthrp.xvii.uikit.extensions.SimpleBitmapTarget
import global.msnthrp.xvii.uikit.extensions.load

object ShortcutUtils {

    fun createShortcut(context: Context, dialog: Dialog, onAdded: () -> Unit) {
        val title = dialog.aliasOrTitle

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra(ChatActivity.PEER_ID, dialog.peerId)
            putExtra(ChatActivity.TITLE, title)
            putExtra(ChatActivity.AVATAR, dialog.photo)
            flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        SimpleBitmapTarget(tag = "shortcut") { bitmap, _ ->

            if (Build.VERSION.SDK_INT >= 25) {
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                intent.action = Intent.ACTION_VIEW

                val icon = when {
                    bitmap != null -> Icon.createWithBitmap(bitmap)
                    else -> Icon.createWithResource(context, R.drawable.xvii_logo_128_circle)
                }
                val shortcutInfo = ShortcutInfo.Builder(context, dialog.peerId.toString())
                        .setShortLabel(title)
                        .setIcon(icon)
                        .setIntent(intent)
                        .build()
                shortcutManager?.addDynamicShortcuts(arrayListOf(shortcutInfo))
                if (Build.VERSION.SDK_INT >= 26) {
                    shortcutManager?.requestPinShortcut(shortcutInfo, null)
                }
            } else {
                context.sendBroadcast(Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, dialog)
                    if (bitmap != null) {
                        putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
                    } else {
                        putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource.fromContext(context, R.drawable.xvii_logo_128_circle))
                    }
                    putExtra("duplicate", false)
                    action = "com.android.launcher.action.INSTALL_SHORTCUT"
                })
            }
            onAdded()
        }.load(context, dialog.photo ?: "") { circleCrop() }
    }



}