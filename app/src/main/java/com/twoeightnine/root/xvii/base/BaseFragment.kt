package com.twoeightnine.root.xvii.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.activities.RootActivity
import kotlinx.android.synthetic.main.toolbar.*

abstract class BaseFragment : Fragment() {

    abstract fun getLayoutId(): Int

    protected val rootActivity
        get() = activity as? RootActivity

    protected val contextOrThrow: Context
        get() = context ?: throw IllegalStateException("Context has leaked away!")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) = View.inflate(activity, getLayoutId(), null)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar()
    }

    protected fun initToolbar() {
        if (toolbar != null) {
            rootActivity?.setSupportActionBar(toolbar)
            val actionBar = rootActivity?.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setDisplayUseLogoEnabled(false)
                context?.let {
                    toolbar?.setTitleTextColor(ContextCompat.getColor(it, R.color.toolbar_text))
                    toolbar?.setSubtitleTextColor(ContextCompat.getColor(it, R.color.toolbar_subtext))
                }
            }

        }
    }

    fun setTitle(title: CharSequence) {
        toolbar?.title = title
        val actionBar = rootActivity?.supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    fun setSubtitle(subtitle: CharSequence) {
        rootActivity?.supportActionBar?.subtitle = subtitle
    }

    fun updateTitle(title: String = "", subtitle: String = "") {
        rootActivity?.supportActionBar?.title = title
        rootActivity?.supportActionBar?.subtitle = subtitle
    }
}