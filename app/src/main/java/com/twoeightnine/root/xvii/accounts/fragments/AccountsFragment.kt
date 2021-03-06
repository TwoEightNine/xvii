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

package com.twoeightnine.root.xvii.accounts.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.twoeightnine.root.xvii.App
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.accounts.adapters.AccountsAdapter
import com.twoeightnine.root.xvii.accounts.viewmodel.AccountsViewModel
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationService
import com.twoeightnine.root.xvii.base.BaseFragment
import com.twoeightnine.root.xvii.chatowner.ChatOwnerFactory
import com.twoeightnine.root.xvii.login.LoginActivity
import com.twoeightnine.root.xvii.utils.FakeData
import com.twoeightnine.root.xvii.utils.restartApp
import com.twoeightnine.root.xvii.utils.showDeleteDialog
import com.twoeightnine.root.xvii.utils.showWarnConfirm
import global.msnthrp.xvii.core.accounts.model.Account
import global.msnthrp.xvii.uikit.extensions.applyBottomInsetPadding
import global.msnthrp.xvii.uikit.extensions.fadeIn
import global.msnthrp.xvii.uikit.extensions.show
import kotlinx.android.synthetic.main.fragment_accounts.*
import javax.inject.Inject

class AccountsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: AccountsViewModel.Factory
    private lateinit var viewModel: AccountsViewModel

    private val adapter by lazy {
        AccountsAdapter(requireContext(), ::onDeleteClick, ::onViewClick, ::onActivateClick)
    }

    override fun getLayoutId() = R.layout.fragment_accounts

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initRecyclerView()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AccountsViewModel::class.java]

        btnAddAccount.setOnClickListener {
            LoginActivity.launchForNewAccount(context)
        }
        btnLogOutAll.setOnClickListener { onLogOutAll() }
        nsvContent.applyBottomInsetPadding()
    }

    private fun updateAccounts(accounts: List<Account>) {
        if (FakeData.ENABLED) {
            adapter.update(FakeData.accounts)
        } else {
            adapter.update(accounts)
        }
        llContent.show()
        llContent.fadeIn(200L)
    }

    private fun initRecyclerView() {
        rvAccounts.layoutManager = LinearLayoutManager(context)
        rvAccounts.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getAccounts().observe(::updateAccounts)
        viewModel.getAccountSwitched().observe {
            restartApp(context, getString(R.string.restart_app))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAccounts()
    }

    private fun onDeleteClick(account: Account) {
        if (account.isActive) return

        showDeleteDialog(context, getString(R.string.this_account)) { viewModel.deleteAccount(account) }
    }

    private fun onViewClick(account: Account) {
        if (account.isActive) return

        ChatOwnerFactory.launch(context, account.userId)
    }

    private fun onActivateClick(account: Account) {
        if (account.isActive) return

        viewModel.switchTo(account)
    }

    private fun onLogOutAll() {
        showWarnConfirm(context, getString(R.string.accounts_log_out_all_accounts), getString(R.string.logout)) { logout ->
            if (logout) {
                NotificationService.stop(context)
                viewModel.logOutAll()
                restartApp(context, getString(R.string.restart_app))
            }
        }
    }

    companion object {
        fun newInstance() = AccountsFragment()
    }
}