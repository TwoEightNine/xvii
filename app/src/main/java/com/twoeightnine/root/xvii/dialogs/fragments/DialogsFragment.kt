package com.twoeightnine.root.xvii.dialogs.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chats.ChatActivity
import com.twoeightnine.root.xvii.chats.attachments.audios.AudiosActivity
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesActivity
import com.twoeightnine.root.xvii.dialogs.adapters.DialogsAdapter
import com.twoeightnine.root.xvii.dialogs.models.Dialog
import com.twoeightnine.root.xvii.dialogs.viewmodels.DialogsViewModel
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.managers.Style
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.search.SearchActivity
import com.twoeightnine.root.xvii.utils.*
import com.twoeightnine.root.xvii.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.fragment_dialogs_new.*
import javax.inject.Inject

open class DialogsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: DialogsViewModel.Factory
    private lateinit var viewModel: DialogsViewModel

    private val adapter by lazy {
        DialogsAdapter(contextOrThrow, ::loadMore, ::onClick, ::onLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_dialogs_new

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DialogsViewModel::class.java]
        viewModel.getDialogs().observe(this, Observer { updateDialogs(it) })
        viewModel.loadDialogs()
        adapter.startLoading()

        progressBar.show()
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDialogs()
            adapter.reset()
            adapter.startLoading()
        }
        Style.forProgressBar(progressBar)
    }

    private fun initRecycler() {
        rvDialogs.layoutManager = LinearLayoutManager(context)
        rvDialogs.adapter = adapter
    }

    private fun updateDialogs(data: Wrapper<ArrayList<Dialog>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadDialogs(offset)
    }

    protected open fun onClick(dialog: Dialog) {
//        rootActivity?.loadFragment(ChatFragment.newInstance(dialog))
        ChatActivity.launch(context, dialog.peerId, dialog.alias ?: dialog.title, dialog.isOnline)
//        rootActivity?.loadFragment(ChatMessagesFragment.newInstance(dialog.peerId))
    }

    protected open fun onLongClick(dialog: Dialog) {
        getContextPopup(context ?: return, R.layout.popup_dialogs) { view ->
            when (view.id) {

                R.id.llPin -> viewModel.pinDialog(dialog)
                R.id.llRead -> viewModel.readDialog(dialog)
                R.id.llMute -> viewModel.muteDialog(dialog)
                R.id.llDelete -> showDeleteDialog(context) {
                    viewModel.deleteDialog(dialog)
                }
                R.id.llAlias -> TextInputAlertDialog(contextOrThrow, getString(R.string.alias),
                        dialog.title, dialog.alias ?: dialog.title, { newAlias ->
                    viewModel.addAlias(dialog, newAlias)
                }).show()
            }
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.dialog_menu, menu)
        menu?.findItem(R.id.menu_music)?.isVisible = equalsDevUids(Session.uid)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_search_users -> {
                SearchActivity.launch(context)
                true
            }
            R.id.important_menu -> {
                StarredMessagesActivity.launch(context)
                true
            }
            R.id.menu_music -> {
                AudiosActivity.launch(context)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance() = DialogsFragment()
    }
}