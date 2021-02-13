package com.twoeightnine.root.xvii.features

import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProviders
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.BuildConfig
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.fragments.AccountsFragment
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.base.FragmentPlacementActivity.Companion.startFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerActivity
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesFragment
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.features.appearance.AppearanceActivity
import com.twoeightnine.root.xvii.features.general.GeneralFragment
import com.twoeightnine.root.xvii.features.notifications.NotificationsFragment
import com.twoeightnine.root.xvii.journal.JournalFragment
import com.twoeightnine.root.xvii.lg.LgAlertDialog
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.managers.Session
import com.twoeightnine.root.xvii.pin.SecurityFragment
import com.twoeightnine.root.xvii.scheduled.ui.ScheduledMessagesFragment
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.*
import global.msnthrp.xvii.data.accounts.Account
import global.msnthrp.xvii.uikit.extensions.*
import kotlinx.android.synthetic.main.fragment_features.*
import java.util.*
import javax.inject.Inject

class FeaturesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FeaturesViewModel.Factory
    private lateinit var viewModel: FeaturesViewModel

    override fun getLayoutId() = R.layout.fragment_features

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FeaturesViewModel::class.java]

        tvSwitchAccount.paintFlags = tvSwitchAccount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvSwitchAccount.paint(Munch.color.color)

        xiAnalyze.setOnClickListener { showToast(context, R.string.in_future_versions) }
        xiStarred.setOnClickListener { startFragment<StarredMessagesFragment>() }
        xiScheduledMessages.setOnClickListener { startFragment<ScheduledMessagesFragment>() }
        xiJournal.setOnClickListener { startFragment<JournalFragment>() }

        rlAccounts.setOnClickListener { ChatOwnerActivity.launch(context, Session.uid) }
        tvSwitchAccount.setOnClickListener { startFragment<AccountsFragment>() }
        xiGeneral.setOnClickListener {
            startFragment<GeneralFragment>()
            suggestJoin()
        }
        xiNotifications.setOnClickListener {
            startFragment<NotificationsFragment>()
            suggestJoin()
        }
        xiAppearance.setOnClickListener {
            AppearanceActivity.launch(context)
            suggestJoin()
        }
        xiSecurity.setOnClickListener { startFragment<SecurityFragment>() }

        xiSupport.setOnClickListener { ChatActivity.launch(context, -App.GROUP, getString(R.string.app_name)) }
        xiRate.setOnClickListener { context?.also { rate(it) } }
        xiShare.setOnClickListener { share() }
        xiPrivacy.setOnClickListener { resolvePrivacyPolicy() }
        xiSourceCode.setOnClickListener { BrowsingUtils.openUrl(context, GITHUB_URL) }

        tvAbout.text = getString(R.string.aboutbig, BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME)
        tvAbout.setOnClickListener { showLogDialog() }

//        rlRoot.stylizeAll()
        svContent.setOnScrollChangeListener(ContentScrollListener())
        svContent.applyHorizontalInsetPadding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getAccount().observe(viewLifecycleOwner, ::updateAccount)
        viewModel.lastSeen.observe(viewLifecycleOwner) { (isOnline, timeStamp) ->
            tvLastSeen.text = getLastSeenText(resources, isOnline, timeStamp, 0)
        }
        viewModel.loadAccount()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateLastSeen()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun updateAccount(account: Account) {
        civPhoto.load(account.photo)
        account.name?.lowerIf(Prefs.lowerTexts)?.also { userName ->
            tvName.text = userName
            xviiToolbar.title = userName
        }
    }

    private fun showLogDialog() {
        LgAlertDialog(context ?: return).show()
    }

    private fun share() {
        viewModel.shareXvii({
            showToast(context, R.string.shared)
        }, { showError(context, it) })
    }

    private fun suggestJoin() {
        if (time() - Prefs.joinShownLast <= SHOW_JOIN_DELAY) return // one week

        Prefs.joinShownLast = time()
        if (!equalsDevUids(Session.uid)) {
            viewModel.checkMembership { inGroup ->
                if (!inGroup) {
                    val dialog = AlertDialog.Builder(context ?: return@checkMembership)
                            .setMessage(R.string.join_us)
                            .setPositiveButton(R.string.join) { _, _ -> viewModel.joinGroup() }
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                    dialog.show()
                    dialog.stylize()
                }
            }
        }
    }

    private fun resolvePrivacyPolicy() {
        val url = if (Locale.getDefault() == Locale("ru")) {
            PRIVACY_RU
        } else {
            PRIVACY_WORLD
        }
        BrowsingUtils.openUrl(context, url)
    }

    companion object {

        const val PRIVACY_WORLD = "https://github.com/TwoEightNine/XVII/blob/master/privacy.md"
        const val PRIVACY_RU = "https://github.com/TwoEightNine/XVII/blob/master/privacy_ru.md"

        const val GITHUB_URL = "https://github.com/twoeightnine/xvii"

        const val SHOW_JOIN_DELAY = 3600 * 24 * 7 // one week

        fun newInstance() = FeaturesFragment()
    }

    private inner class ContentScrollListener : NestedScrollView.OnScrollChangeListener {

        private val toolbarHeight by lazy {
            xviiToolbar.height
        }
        private val accountsHolderHeight by lazy {
            rlAccounts.height
        }
        private val threshold by lazy { accountsHolderHeight - toolbarHeight }

        private var lastHandledY = 0

        override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
            val shouldShowToolbar = threshold in (lastHandledY + 1) until scrollY
            val shouldHideToolbar = threshold in (scrollY + 1) until lastHandledY

            if (shouldShowToolbar) {
                xviiToolbar.fadeIn(200L)
            } else if (shouldHideToolbar) {
                xviiToolbar.fadeOut(200L)
            }
            lastHandledY = scrollY
        }
    }
}