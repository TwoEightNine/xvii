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

package com.twoeightnine.root.xvii.uikit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseActivity
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import kotlinx.android.synthetic.main.fragment_ui_kit.*

class UiKitFragment : Fragment() {

//    override fun getLayoutId(): Int = R.layout.fragment_ui_kit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ui_kit, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        addText("range color")
//        addColor(Munch.prevRangeColor)
//
//        addText("similar color")
//        addColor(Munch.prevSimilarColor)
//
//        addText("near color")
//        addColor(Munch.prevNearColor)
//
//        addText("main color")
//        addColor(Munch.color.color)
//
//        addText("near color")
//        addColor(Munch.nextNearColor)
//
//        addText("similar color")
//        addColor(Munch.nextSimilarColor)
//
//        addText("range color")
//        addColor(Munch.nextRangeColor)
//
//        addText("extra color")
//        addColor(Munch.extraColor)

        (activity as? BaseActivity)?.also(xviiToolbar::setupWith)
        svContent.applyBottomInsetPadding()
    }

//    private fun addColor(colorScope: Munch.ColorScope) {
//        val container = LinearLayout(context)
//        container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
//        container.orientation = LinearLayout.HORIZONTAL
//        for (color in colorScope.toList()) {
//            val view = View(context)
//            view.layoutParams = LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    1f
//            )
//            view.setBackgroundColor(color)
//            container.addView(view)
//        }
//        llColors.addView(container)
//    }

    private fun addText(text: String) {
        val view = TextView(context)
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.text = text
        llColors.addView(view)
    }
}