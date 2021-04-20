package com.twoeightnine.root.xvii.utils

import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.data.dialogs.Dialog

object FakeData {

    val name = "din läkare"
    val avatar = "https://www.fondren.com/images/uday-v-doctor-md-profile.jpg"

    val dialogs = arrayListOf(Dialog(
            title = "joe omaewamo",
            timeStamp = time() - 863,
            photo = "https://i.pinimg.com/474x/e0/9c/04/e09c04bceeff6f2ff2f7797186ceb704.jpg",
            text = "пока искусство будет ориентироваться на зрителя, оно будет в жопе",
            isOut = true,
            isOnline = true,
            isPinned = true
    ), Dialog(
            title = "lil' sandra go brrr \uD83D\uDE0F",
            timeStamp = time() - 163,
            photo = "https://upload.wikimedia.org/wikipedia/commons/5/58/Sandra_Palermo.jpg",
            text = "стикер",
            isOut = false,
            isOnline = true,
            isPinned = false
    ), Dialog(
            title = "kevin mittagessen",
            timeStamp = time() - 666,
            photo = "https://vk.com/images/camera_400.png",
            text = "ничего нового, в общем-то",
            isOut = true,
            isOnline = true,
            isPinned = false
    ), Dialog(
            title = "fresno yummy",
            timeStamp = time() - 1000,
            photo = "https://cf-images.us-east-1.prod.boltdns.net/v1/static/5596404782001/2f9f5225-e3d4-4f07-8d01-fd37440cd1ef/0676f989-2150-4292-8050-bb460dd77843/1280x720/match/image.jpg",
            text = "эй, ты чего?",
            unreadCount = 3,
            isRead = false,
            isMute = true,
            isOut = false,
            isOnline = false,
            isPinned = false
    ), Dialog(
            title = "fkdplc сходка #289",
            timeStamp = time() - 1112,
            text = "bien sûr ахахахха",
            unreadCount = 17,
            isRead = false,
            isMute = true,
            isOut = false,
            isOnline = false,
            isPinned = false
    ), Dialog(
            title = "yela tricks",
            timeStamp = time() - 1313,
            photo = "https://upload.wikimedia.org/wikipedia/commons/5/52/Madame_Fortuna.png",
            text = "большое спасибо тебе!",
            isRead = false,
            isOut = true,
            isOnline = false,
            isPinned = false
    ), Dialog(
            title = "alan smithee",
            timeStamp = time() - 2411,
            photo = "https://you-anime.ru/anime-images/characters/106157.jpg",
            text = "да я так и подумал, хорошо",
            isRead = true,
            isOut = false,
            isOnline = true,
            isPinned = false
    )
    )

    val accounts = arrayListOf(Account(
            userId = 1753175317,
            token = "stub",
            name = "fresno yummy",
            photo = "https://cf-images.us-east-1.prod.boltdns.net/v1/static/5596404782001/2f9f5225-e3d4-4f07-8d01-fd37440cd1ef/0676f989-2150-4292-8050-bb460dd77843/1280x720/match/image.jpg",
            isActive = true
    ), Account(
            userId = 1753171753,
            token = "stub",
            name = "kriepie fank",
            photo = "https://www.shared.com/content/images/2018/09/CCTV.jpg",
            isActive = false
    ), Account(
            userId = 1717531753,
            name = "pierre gardin",
            token = "stub",
            photo = "https://upload.wikimedia.org/wikipedia/commons/f/f6/Colour_grid_showing_256_color_image_composed_of_true_color_values.png",
            isActive = false
    ))
}