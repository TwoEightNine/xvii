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

package com.twoeightnine.root.xvii.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.background.longpoll.LongPollStorage
import com.twoeightnine.root.xvii.base.BaseActivity
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.managers.Prefs
import com.twoeightnine.root.xvii.pin.SecurityFragment
import com.twoeightnine.root.xvii.pin.fake.alarm.AlarmActivity
import com.twoeightnine.root.xvii.pin.fake.diagnostics.DiagnosticsActivity
import com.twoeightnine.root.xvii.storage.SessionProvider
import com.twoeightnine.root.xvii.utils.isOnline
import com.twoeightnine.root.xvii.utils.showAlert
import com.twoeightnine.root.xvii.utils.startNotificationService
import global.msnthrp.xvii.uikit.extensions.applyTopInsetMargin
import global.msnthrp.xvii.uikit.extensions.hide
import global.msnthrp.xvii.uikit.extensions.show
import global.msnthrp.xvii.uikit.utils.DisplayUtils
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var longPollStorage: LongPollStorage

    private val viewModel by viewModels<LoginViewModel>()
    private val addNewAccount by lazy {
        intent?.extras?.getBoolean(ARG_NEW_ACCOUNT) == true
    }

    private var isWebViewShown = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        DisplayUtils.init(this)

        viewModel.accountCheckResult.observe(this, Observer(::onAccountChecked))
        viewModel.accountUpdated.observe(this, Observer(::onAccountUpdated))

        if (addNewAccount || !SessionProvider.hasToken()) {
            logIn()
        } else {
            checkTokenAndStart()
        }

        webView?.applyTopInsetMargin()
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.splash_background)

    override fun getNavigationBarColor() = ContextCompat.getColor(this, R.color.splash_background)

    override fun shouldRunService() = false

    private fun checkTokenAndStart() {
        if (isOnline()) {
            viewModel.checkAccount(SessionProvider.token, SessionProvider.userId)
        } else {
            startApp()
        }
    }

    private fun logIn() {
        if (isOnline()) {
            showWebView()
        } else {
            finishWithAlert(getString(R.string.login_no_internet))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebView() {
        with(webView) {
            hide()
            CookieSyncManager.createInstance(this@LoginActivity).sync()
            CookieManager.getInstance().removeAllCookie()
            settings.javaScriptEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            webViewClient = ParsingWebClient { token, userId ->
                viewModel.checkAccount(token, userId)
            }

            loadUrl(LOGIN_URL)
            show()
            isWebViewShown = true
        }
    }

    private fun startApp() {
        App.appComponent?.inject(this)
        longPollStorage.clear()
        startNotificationService(this)
        when (Prefs.fakeAppType) {
            SecurityFragment.FakeAppType.ALARMS ->
                AlarmActivity.launch(this)

            SecurityFragment.FakeAppType.DIAGNOSTICS -> {
                DiagnosticsActivity.launch(this)
            }

            SecurityFragment.FakeAppType.NONE ->
                MainActivity.launch(this)
        }
        this.finish()
    }

    private fun finishWithAlert(text: String) {
        showAlert(this, text) {
            finish()
        }
    }

    private fun onAccountChecked(accountCheckResult: LoginViewModel.AccountCheckResult) {
        val user = accountCheckResult.user
        val token = accountCheckResult.token
        when {
            accountCheckResult.success && user != null && token != null -> {
                viewModel.updateAccount(user, token, isRunning = !addNewAccount)
            }
            SessionProvider.hasToken() -> showWebView()
            else -> finishWithAlert(getString(R.string.login_unable_to_log_in))
        }
    }

    private fun onAccountUpdated(unit: Unit) {
        if (addNewAccount) {
            finish()
        } else {
            startApp()
        }
    }

    override fun getThemeId() = R.style.SplashTheme

    companion object {

        private const val LOGIN_URL = "https://oauth.vk.com/authorize?" +
                "client_id=${App.APP_ID}&" +
                "scope=${App.SCOPE_ALL}&" +
                "redirect_uri=${App.REDIRECT_URL}&" +
                "display=touch&" +
                "v=${App.VERSION}&" +
                "response_type=token"
        // https://oauth.vk.com/authorize?client_id=6079611&scope=&redirect_uri=https://oauth.vk.com/blank.html&display=touch&v=5.95&response_type=token

        private const val ARG_NEW_ACCOUNT = "newAccount"

        fun launchForNewAccount(context: Context?) {
            context?.startActivity(Intent(context, LoginActivity::class.java).apply {
                putExtra(ARG_NEW_ACCOUNT, true)
            })
        }
    }

    /**
     * handles redirect and calls token parsing
     * @param onLoggedIn callback
     */
    private inner class ParsingWebClient(
            private val onLoggedIn: (String, Int) -> Unit
    ) : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            rlLoader.hide()
            if (url.startsWith(App.REDIRECT_URL)) {
                view.hide()
                val token = extract(url, "access_token=(.*?)&")
                val uid = extract(url, "user_id=(\\d*)").toIntOrNull() ?: 0
                onLoggedIn(token, uid)
            }
        }

        private fun extract(from: String, regex: String): String {
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(from)
            if (!matcher.find()) {
                return ""
            }
            return matcher.toMatchResult().group(1)
        }
    }
}