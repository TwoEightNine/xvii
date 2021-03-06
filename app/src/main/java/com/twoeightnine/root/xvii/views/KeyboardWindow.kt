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

package com.twoeightnine.root.xvii.views

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import com.twoeightnine.root.xvii.managers.Prefs

abstract class KeyboardWindow(
        private val rootView: View,
        protected val context: Context,
        protected val onKeyboardClosed: () -> Unit = {}
) : PopupWindow(context) {

    private var keyBoardHeight = 0
    private var pendingOpen = false

    private var maxBottom = 0

    private val usableScreenHeight: Int
        get() {
            val metrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(metrics)
            return metrics.heightPixels
        }

    var isKeyBoardOpen = false
        private set

    init {
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        setSize(WindowManager.LayoutParams.MATCH_PARENT, 255)
        Handler(Looper.getMainLooper()).post {
            contentView = createView()
            setBackgroundDrawable(null)
            onViewCreated()
        }
    }

    abstract fun createView(): View

    abstract fun getAdditionalHeight(): Int

    open fun onViewCreated() {
        setSizeForSoftKeyboard()
    }

    /**
     * Use this function to show the popup.
     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
     * library needs you to open the soft keyboard at least once before calling this function.
     * If that is not possible see showAtBottomPending() function.

     */
    private fun showAtBottom() {
        val y = if (Prefs.liftKeyboard) BOTTOM_MARGIN else BOTTOM_MARGIN / 2
        showAtLocation(rootView, Gravity.BOTTOM, 0, y)
    }

    /**
     * Use this function when the soft keyboard has not been opened yet. This
     * will show the popup after the keyboard is up next time.
     * Generally, you will be calling InputMethodManager.showSoftInput function after
     * calling this function.
     */
    private fun showAtBottomPending() {
        if (isKeyBoardOpen) {
            showAtBottom()
        } else {
            pendingOpen = true
        }
    }

    fun showWithRequest(editText: EditText) {
        if (!isKeyBoardOpen) {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        }
        showAtBottomPending()
    }

    /**
     * Call this function to resize the popup according to your soft keyboard size
     */
    fun setSizeForSoftKeyboard() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            if (r.bottom > maxBottom) {
                maxBottom = r.bottom
            }
            val heightDifference = maxBottom - r.bottom
            if (heightDifference > 0) {
                keyBoardHeight = heightDifference - BOTTOM_MARGIN / 2
                setSize(WindowManager.LayoutParams.MATCH_PARENT,
                        keyBoardHeight + getAdditionalHeight())
                isKeyBoardOpen = true
                if (pendingOpen) {
                    showAtBottom()
                    pendingOpen = false
                }
            } else {
                isKeyBoardOpen = false
                onKeyboardClosed()
            }
        }
    }

    /**
     * Manually set the popup window size
     * @param width Width of the popup
     * *
     * @param height Height of the popup
     */
    fun setSize(width: Int, height: Int) {
        setWidth(width)
        setHeight(height)
    }

    companion object {
        const val BOTTOM_MARGIN = 90
    }
}