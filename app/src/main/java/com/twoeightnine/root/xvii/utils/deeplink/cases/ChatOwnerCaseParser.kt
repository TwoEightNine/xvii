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

package com.twoeightnine.root.xvii.utils.deeplink.cases

import android.content.Intent
import com.twoeightnine.root.xvii.chatowner.fragments.BaseChatOwnerFragment
import com.twoeightnine.root.xvii.extensions.getIntOrNull
import com.twoeightnine.root.xvii.utils.deeplink.DeepLinkParser

object ChatOwnerCaseParser : DeepLinkParser.CaseParser {

    private const val PATH_PREFIX_USER = "id"

    override fun getResult(intent: Intent): DeepLinkParser.Result =
            when (val peerId = parseIntent(intent)) {
                null -> DeepLinkParser.Result.Unknown
                else -> DeepLinkParser.Result.ChatOwner(peerId)
            }

    private fun parseIntent(intent: Intent): Int? {
        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.lastPathSegment?.let { lastPath ->
                    val isUser = PATH_PREFIX_USER in lastPath

                    getChatOwnerIdFromLastPath(lastPath)?.let { chatOwnerId ->
                        when {
                            isUser -> chatOwnerId
                            else -> -chatOwnerId
                        }
                    }
                }
            }
            else -> intent.extras?.getIntOrNull(BaseChatOwnerFragment.ARG_PEER_ID)
        }
    }

    private fun getChatOwnerIdFromLastPath(lastPath: String): Int? {
        return lastPath
                .replace(PATH_PREFIX_USER, "")
                .replace("club", "")
                .replace("public", "")
                .toIntOrNull()
    }
}