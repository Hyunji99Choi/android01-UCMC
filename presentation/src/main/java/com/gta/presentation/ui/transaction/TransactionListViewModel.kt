package com.gta.presentation.ui.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gta.domain.model.Transaction
import com.gta.domain.usecase.transaction.GetTransactionsUseCase
import com.gta.presentation.model.TransactionState
import com.gta.presentation.model.TransactionUserState
import com.gta.presentation.util.FirebaseUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    args: SavedStateHandle,
    getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    val transaction: StateFlow<List<Transaction>>

    init {
        val userState = args.get<TransactionUserState>(TransactionPagerAdapter.USER_STATE_ARG) ?: TransactionUserState.LENDER
        val transactionState = args.get<TransactionState>(TransactionPagerAdapter.TRANSACTION_STATE_ARG) ?: TransactionState.TRADING

        transaction = getTransactionsUseCase(
            FirebaseUtil.uid,
            userState == TransactionUserState.LENDER,
            transactionState == TransactionState.TRADING
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}