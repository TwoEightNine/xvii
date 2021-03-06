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

package global.msnthrp.xvii.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import global.msnthrp.xvii.core.session.SessionProvider
import kotlin.reflect.KProperty

/**
 * to be removed a little after release
 */
class EncryptedCompatSessionProvider(context: Context) : SessionProvider {

    private val encryptedSessionProvider = EncryptedSessionProvider(context)

    private val prefsCompat: SharedPreferences by lazy {
        context.getSharedPreferences(NAME_SESSION, Context.MODE_PRIVATE)
    }
    private val pinPrefsCompat: SharedPreferences by lazy {
        context.getSharedPreferences(NAME_PREF, Context.MODE_PRIVATE)
    }

    override var token: String? by CompatDelegate(
            getterPrimary = { encryptedSessionProvider.token },
            getterCompat = { prefsCompat.getString(TOKEN, "") ?: "" },
            setterPrimary = { encryptedSessionProvider.token = it },
            isEmpty = { isNullOrBlank() }
    )

    override var userId: Int by CompatDelegate(
            getterPrimary = { encryptedSessionProvider.userId },
            getterCompat = { prefsCompat.getInt(UID, 0) },
            setterPrimary = { encryptedSessionProvider.userId = it ?: 0 },
            isEmpty = { this == null || this == 0 }
    )

    override var fullName: String? by CompatDelegate(
            getterPrimary = { encryptedSessionProvider.fullName },
            getterCompat = { prefsCompat.getString(FULL_NAME, "") ?: "" },
            setterPrimary = { encryptedSessionProvider.fullName = it },
            isEmpty = { isNullOrBlank() }
    )

    override var photo: String? by CompatDelegate(
            getterPrimary = { encryptedSessionProvider.photo },
            getterCompat = { prefsCompat.getString(PHOTO, "") ?: "" },
            setterPrimary = { encryptedSessionProvider.photo = it },
            isEmpty = { isNullOrBlank() }
    )

    override var pin: String? by CompatDelegate(
            getterPrimary = { encryptedSessionProvider.pin },
            getterCompat = { pinPrefsCompat.getString(PIN, "") ?: "" },
            setterPrimary = { encryptedSessionProvider.pin = it },
            isEmpty = { isNullOrBlank() }
    )

    override val encryptionKey256: ByteArray
        get() = encryptedSessionProvider.encryptionKey256

    override fun clearAll() {
        encryptedSessionProvider.clearAll()
        prefsCompat.edit {
            listOf(TOKEN, UID, FULL_NAME, PHOTO).forEach(::remove)
        }
        pinPrefsCompat.edit {
            remove(PIN)
        }
    }

    companion object {
        private const val NAME_SESSION = "sessionPref"
        private const val NAME_PREF = "prefPref"

        private const val TOKEN = "token"
        private const val UID = "uid"
        private const val FULL_NAME = "fullname"
        private const val PHOTO = "photo"
        private const val PIN = "pin"
    }

    private inner class CompatDelegate<T>(
            private val getterPrimary: () -> T?,
            private val getterCompat: () -> T,
            private val setterPrimary: (T?) -> Unit,
            private val isEmpty: T?.() -> Boolean
    ) {

        operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
            var value = getterPrimary()
            if (value.isEmpty()) {
                value = getterCompat()
                if (!value.isEmpty()) {
                    setterPrimary(value)
                }
            }
            return value!!
        }

        operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: T?) {
            setterPrimary(value)
        }
    }

}