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

package com.twoeightnine.root.xvii.chatowner.fragments

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.DrawableRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerViewModel
import com.twoeightnine.root.xvii.chatowner.model.ChatOwner
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatActivity
import com.twoeightnine.root.xvii.extensions.load
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.model.Wrapper
import com.twoeightnine.root.xvii.model.attachments.Photo
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.uikit.Munch
import com.twoeightnine.root.xvii.uikit.paint
import com.twoeightnine.root.xvii.utils.BrowsingUtils
import com.twoeightnine.root.xvii.utils.copyToClip
import com.twoeightnine.root.xvii.utils.showError
import com.twoeightnine.root.xvii.utils.showToast
import com.twoeightnine.root.xvii.views.RateAlertDialog
import global.msnthrp.xvii.uikit.extensions.*
import kotlinx.android.synthetic.main.fragment_chat_owner.ivAvatar
import kotlinx.android.synthetic.main.fragment_chat_owner.nsvContent
import kotlinx.android.synthetic.main.fragment_chat_owner.tvInfo
import kotlinx.android.synthetic.main.fragment_chat_owner.tvTitle
import kotlinx.android.synthetic.main.fragment_chat_owner_user.*
import kotlinx.android.synthetic.main.item_chat_owner_field.view.*

abstract class BaseChatOwnerFragment<T : ChatOwner> : BaseFragment() {

    private val peerId by lazy {
        arguments?.getInt(ARG_PEER_ID) ?: 0
    }
    private var chatOwner: ChatOwner? = null

    protected val viewModel by viewModels<ChatOwnerViewModel>()

    abstract fun getChatOwnerClass(): Class<T>

    abstract fun bindChatOwner(chatOwner: T?)

    abstract fun getBottomPaddableView(): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val behavior = BottomSheetBehavior.from(nsvContent)
        behavior.setBottomSheetCallback(ProfileBottomSheetCallback(activity ?: return))
        fabOpenChat.setOnClickListener {
            chatOwner?.also {
                ChatActivity.launch(context, it)
            }
        }
        ivAvatar?.setOnClickListener(::onAvatarClicked)
        ivAvatarHighRes?.setOnClickListener(::onAvatarClicked)
        context?.let { RateAlertDialog(it).show() }
        ivBack.apply {
            applyTopInsetMargin()
            setOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.apply {
            chatOwner.observe(::onChatOwnerLoaded)
            photos.observe(::onPhotosLoaded)
            alias.observe(::onAliasLoaded)

            loadChatOwner(peerId, getChatOwnerClass())
            loadAlias(peerId)
        }

        setStatusBarLight(isLight = false)

        getBottomPaddableView().applyBottomInsetMargin()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun getChatOwner(): T? = viewModel.chatOwner.value?.data as? T

    @Suppress("UNCHECKED_CAST")
    private fun onChatOwnerLoaded(data: Wrapper<ChatOwner>) {
        if (data.data != null) {
            rlLoader.hide()
            chatOwner = data.data
            chatOwner?.apply {
                ivAvatar?.load(getAvatar())

                val title = getTitle().lowerIf(Prefs.lowerTexts)
                tvTitle.text = title
                xviiToolbar?.title = title

                context?.also {
                    tvInfo.text = getInfoText(it)
                    getPrivacyInfo(it).also { privacyInfo ->
                        ivWarning.setVisible(privacyInfo != null)
                        tvPrivacy.setVisible(privacyInfo != null)
                        privacyInfo?.also { tvPrivacy.text = it }
                    }
                }

                tvInfo.lowerIf(Prefs.lowerTexts)
                swNotifications.isChecked = viewModel.getShowNotifications(getPeerId())
                resetValues()
                bindChatOwner(this as? T)
                viewModel.loadPhotos(getPeerId(), getChatOwnerClass())
            }
        } else {
            showError(context, data.error ?: "")
        }
    }

    private fun onPhotosLoaded(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            ivAvatar?.postDelayed({
                loadHighResWithAnimation(photos[0].getOptimalPhoto()?.url)
            }, 1000L)
        }
    }

    private fun onAliasLoaded(alias: String) {
        tvAlias.show()
        tvAlias.text = getString(R.string.aka_prefix, alias)
    }

    private fun onAvatarClicked(v: View) {
        viewModel.photos.value?.also { photos ->
            if (photos.isNotEmpty()) {
                ImageViewerActivity.viewImages(context, ArrayList(photos))
            }
        }
    }

    private fun resetValues() {
        llContainer.removeAllViews()
    }

    protected fun addValue(
            @DrawableRes icon: Int,
            text: String?,
            onClick: ((String) -> Unit)? = null,
            onLongClick: ((String) -> Unit)? = null
    ) {
        if (text.isNullOrBlank()) return

        with(View.inflate(context, R.layout.item_chat_owner_field, null)) {
            if (icon != 0) {
                ivIcon.setImageResource(icon)
                ivIcon.paint(Munch.color.color)
            }
            tvValue.text = text.toLowerCase()
            onClick?.also {
                rlItem.setOnClickListener { onClick(text) }
            }
            onLongClick?.also {
                rlItem.setOnLongClickListener { onLongClick(text); true }
            }
            llContainer.addView(this)
        }
    }

    protected fun copy(text: String?, title: Int) {
        copyToClip(text ?: return)
        showToast(activity, getString(R.string.copied, getString(title)))
    }

    protected fun goTo(url: String?) {
        BrowsingUtils.openUrl(context, url)
    }

    private fun loadHighResWithAnimation(url: String?) {
        val context = context ?: return
        url ?: return
        SimpleBitmapTarget { bitmap, _ ->
            ivAvatarHighRes?.setImageBitmap(bitmap)
            ivAvatarHighRes?.fadeIn(700L) {
                ivAvatar?.hide()
            }
        }.load(context, url)
    }

    override fun onDestroyView() {
        chatOwner?.getPeerId()?.also { peerId ->
            viewModel.setShowNotifications(peerId, swNotifications.isChecked)
        }
        super.onDestroyView()
    }

    companion object {

        const val ARG_PEER_ID = "peerId"
        const val PARALLAX_COEFF = 0.5f
    }

    private inner class ProfileBottomSheetCallback(activity: Activity) : BottomSheetBehavior.BottomSheetCallback() {

        private var toolbarColored = false
        private val imageHeight = resources.getDimensionPixelSize(R.dimen.profile_avatar_height)
        private val screenHeight = activity.let {
            val displayMetrics = DisplayMetrics()
            it.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }

        override fun onSlide(p0: View, offset: Float) {
            when {
                offset <= 0f -> return
                offset == 1f -> if (!toolbarColored && shouldColorToolbar()) {
                    toolbarColored = true
                    if (Prefs.isLightTheme) {
                        setStatusBarLight(isLight = true)
                    }
                }
                toolbarColored -> {
                    toolbarColored = false
                    setStatusBarLight(isLight = false)
                }
                else -> {
                    val margin = imageHeight * -offset * PARALLAX_COEFF
                    (ivAvatar?.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                        topMargin = margin.toInt()
                        ivAvatar?.layoutParams = this
                    }
                    (ivAvatarHighRes?.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
                        topMargin = margin.toInt()
                        ivAvatarHighRes?.layoutParams = this
                    }
                }
            }
        }

        override fun onStateChanged(p0: View, state: Int) {

        }

        private fun shouldColorToolbar() = xviiToolbar
                ?.let { (screenHeight - it.height + 92) <= cvInfo.height } ?: false
    }
}