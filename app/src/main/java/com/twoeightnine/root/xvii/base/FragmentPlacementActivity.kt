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

package com.twoeightnine.root.xvii.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment

class FragmentPlacementActivity : ContentActivity() {

    private val fragmentClassName by lazy {
        intent?.extras?.getString(ARG_FRAGMENT_CLASS)
    }
    private val fragmentArgs by lazy {
        intent?.extras?.getBundle(ARG_FRAGMENT_ARGS) ?: Bundle()
    }

    override fun createFragment(intent: Intent?): Fragment =
            Fragment.instantiate(this, fragmentClassName ?: "", fragmentArgs)

    companion object {

        private const val ARG_FRAGMENT_CLASS = "fragmentClass"
        private const val ARG_FRAGMENT_ARGS = "fragmentArgs"

        fun launch(context: Context?, fragmentClass: Class<*>, fragmentArgs: Bundle?) {
            context?.startActivity(createIntent(context, fragmentClass, fragmentArgs))
        }

        fun launchForResult(activity: Activity?, fragmentClass: Class<*>, fragmentArgs: Bundle?, requestCode: Int) {
            activity?.startActivityForResult(createIntent(activity, fragmentClass, fragmentArgs), requestCode)
        }

        fun launchForResult(fragment: Fragment?, fragmentClass: Class<*>, fragmentArgs: Bundle?, requestCode: Int) {
            fragment?.startActivityForResult(createIntent(fragment.context, fragmentClass, fragmentArgs), requestCode)
        }

        private fun createIntent(context: Context?, fragmentClass: Class<*>, fragmentArgs: Bundle?) =
                Intent(context, FragmentPlacementActivity::class.java).apply {
                    putExtra(ARG_FRAGMENT_CLASS, fragmentClass.name)
                    putExtra(ARG_FRAGMENT_ARGS, fragmentArgs)
                }

        inline fun <reified T : Fragment> Context?.startFragment(fragmentArgs: Bundle? = null) {
            launch(this, T::class.java, fragmentArgs)
        }

        inline fun <reified T : Fragment> Fragment.startFragment(fragmentArgs: Bundle? = null) {
            launch(context, T::class.java, fragmentArgs)
        }

        inline fun <reified T : Fragment> Activity?.startFragmentForResult(requestCode: Int, fragmentArgs: Bundle? = null) {
            launchForResult(this, T::class.java, fragmentArgs, requestCode)
        }

        inline fun <reified T : Fragment> Fragment.startFragmentForResult(requestCode: Int, fragmentArgs: Bundle? = null) {
            launchForResult(this, T::class.java, fragmentArgs, requestCode)
        }
    }
}