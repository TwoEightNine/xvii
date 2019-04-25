package com.twoeightnine.root.xvii.features.appearance

import android.content.Context
import android.os.Bundle
import com.twoeightnine.root.xvii.activities.ContentActivity
import com.twoeightnine.root.xvii.utils.launchActivity

class AppearanceActivity : ContentActivity() {

    override fun getFragment(args: Bundle?) = AppearanceFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, AppearanceActivity::class.java)
        }
    }
}