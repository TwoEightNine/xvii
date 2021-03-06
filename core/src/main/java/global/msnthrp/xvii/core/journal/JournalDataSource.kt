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

package global.msnthrp.xvii.core.journal

import global.msnthrp.xvii.core.journal.model.JournalEvent

interface JournalDataSource {

    fun addJournalEvent(journalEvent: JournalEvent)

    fun getJournalEvents(): List<JournalEvent>

    fun clearAll()

    /**
     * events with timeStamp < [recentThresholdTimeStamp] should be deleted
     * @param recentThresholdTimeStamp timeStamp of obsolescence threshold
     */
    fun clearAllExceptRecent(recentThresholdTimeStamp: Long)
}