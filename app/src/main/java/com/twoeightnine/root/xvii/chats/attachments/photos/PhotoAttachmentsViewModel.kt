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

package com.twoeightnine.root.xvii.chats.attachments.photos

import com.twoeightnine.root.xvii.chats.attachments.base.BaseAttachmentsViewModel
import com.twoeightnine.root.xvii.model.attachments.Attachment
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.network.ApiService

class PhotoAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Photo>(api) {

    override val mediaType = "photo"

    override fun convert(attachment: Attachment?) = attachment?.photo
}