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
import com.twoeightnine.root.xvii.model.WallPost
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Attachment(

        @SerializedName("type")
        @Expose
        var type: String? = null,

        @SerializedName("photo")
        @Expose
        var photo: Photo? = null,

        @SerializedName("sticker")
        @Expose
        val sticker: Sticker? = null,

        @SerializedName("audio")
        @Expose
        var audio: Audio? = null,

        @SerializedName("link")
        @Expose
        var link: Link? = null,

        @SerializedName("video")
        @Expose
        var video: Video? = null,

        @SerializedName("doc")
        @Expose
        var doc: Doc? = null,

        @SerializedName("wall")
        @Expose
        var wall: WallPost? = null,

        @SerializedName("gift")
        @Expose
        var gift: Gift? = null,

        @SerializedName("audio_message")
        @Expose
        var audioMessage: AudioMessage? = null,

        @SerializedName("poll")
        @Expose
        val poll: Poll? = null,

        @SerializedName("graffiti")
        @Expose
        val graffiti: Graffiti? = null,

        @SerializedName("call")
        @Expose
        val call: Call? = null
) : Parcelable {

    val isGraphical: Boolean
        get() {
            val isPureGraphical = listOf(photo, sticker, gift, video, graffiti).any { it != null }
            if (isPureGraphical) {
                return true
            }
            val isGraphicalWallPost = wall?.attachments?.getPhotos()?.isNotEmpty() ?: false
            if (isGraphicalWallPost) {
                return true
            }
            val isGraphicalDoc = doc?.isGif ?: false
            if (isGraphicalDoc) {
                return true
            }
            return false
        }

    constructor(photo: Photo) : this(
            type = TYPE_PHOTO,
            photo = photo
    )

    constructor(audio: Audio) : this(
            type = TYPE_AUDIO,
            audio = audio
    )

    constructor(video: Video) : this(
            type = TYPE_VIDEO,
            video = video
    )

    constructor(doc: Doc) : this(
            doc = doc,
            type = TYPE_DOC
    )

    override fun toString() = (when (type) {
        TYPE_PHOTO -> photo
        TYPE_AUDIO -> audio
        TYPE_VIDEO -> video
        TYPE_DOC -> doc
        TYPE_POLL -> poll
        else -> null
    } as? IdTypeable)?.getId() ?: "null"

    companion object {

        const val TYPE_PHOTO = "photo"
        const val TYPE_STICKER = "sticker"
        const val TYPE_AUDIO = "audio"
        const val TYPE_LINK = "link"
        const val TYPE_VIDEO = "video"
        const val TYPE_DOC = "doc"
        const val TYPE_WALL = "wall"
        const val TYPE_GIFT = "gift"
        const val TYPE_AUDIO_MESSAGE = "audio_message"
        const val TYPE_AUDIO_MSG = "audiomsg"
        const val TYPE_POLL = "poll"
        const val TYPE_GRAFFITI = "graffiti"
        const val TYPE_CALL = "call"
    }
}

fun ArrayList<Attachment>.isSticker() = isNotEmpty() && this[0].sticker != null

fun ArrayList<Attachment>.isGift() = isNotEmpty() && this[0].gift != null

fun List<Attachment>.getPhotos() = ArrayList(this.mapNotNull { it.photo })

fun ArrayList<Attachment>.photosCount() = getPhotos().size

fun ArrayList<Attachment>.getVideos() = ArrayList(this.mapNotNull { it.video })

fun ArrayList<Attachment>.videosCount() = getVideos().size

fun List<Attachment>.getAudios() = filter { it.type == Attachment.TYPE_AUDIO }.map { it.audio }

fun ArrayList<Attachment>.audiosCount() = getAudios().size

fun ArrayList<Attachment>.getDocs() = ArrayList(this.mapNotNull { it.doc })

fun ArrayList<Attachment>.docsCount() = getDocs().size

fun ArrayList<Attachment>.getAudioMessage() = find { it.type == Attachment.TYPE_AUDIO_MESSAGE }?.audioMessage

fun ArrayList<Attachment>.isAudioMessage() = getAudioMessage() != null

fun ArrayList<Attachment>.isGraffiti() = isNotEmpty() && this[0].graffiti != null

fun ArrayList<Attachment>.isPoll() = isNotEmpty() && this[0].poll != null

fun ArrayList<Attachment>.isLink() = isNotEmpty() && this[0].link != null

fun ArrayList<Attachment>.isWallPost() = isNotEmpty() && this[0].wall != null

fun ArrayList<Attachment>.isCall() = isNotEmpty() && this[0].call != null