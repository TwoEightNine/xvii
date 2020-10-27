package com.twoeightnine.root.xvii.uikit

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.*
import android.view.Menu
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.twoeightnine.root.xvii.R


fun Drawable.paint(color: Int) {
    when (this) {
        is ShapeDrawable -> paint.color = color
        is GradientDrawable -> setColor(color)
        is ColorDrawable -> this.color = color
        is VectorDrawable -> setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}

fun Menu.paint(color: Int) {
    for (i in 0 until size()) {
        getItem(i).icon?.paint(color)
    }
}

fun ImageView.paint(color: Int) {
    drawable.paint(color)
}

fun TextView.paint(color: Int) {
    setTextColor(color)
}

fun CheckBox.paint(color: Int) {
    buttonTintList = ColorStateList(
            arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf()
            ),
            intArrayOf(
                    color,
                    ContextCompat.getColor(context, R.color.minor_text)
            )
    )
}

fun SwitchCompat.paint(color: Munch.ColorScope) {
    thumbDrawable.setColorFilter(color.color, PorterDuff.Mode.SRC_ATOP)
    trackDrawable.setColorFilter(color.color50, PorterDuff.Mode.SRC_ATOP)
}

fun RadioButton.paint(color: Int) {
    val colorStateList = ColorStateList(
            arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(android.R.attr.state_enabled)
            ),
            intArrayOf(
                    ContextCompat.getColor(context, R.color.minor_text),
                    color
            )
    )
    buttonTintList = colorStateList
}