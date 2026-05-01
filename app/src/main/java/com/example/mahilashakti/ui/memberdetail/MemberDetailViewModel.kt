package com.example.mahilashakti.ui.memberdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mahilashakti.data.entity.Loan
import com.example.mahilashakti.data.entity.MemberWithLoans
import com.example.mahilashakti.data.entity.MemberWithSavings
import com.example.mahilashakti.data.entity.Savings
import com.example.mahilashakti.data.repository.MahilaRepository
import com.example.mahilashakti.utils.MathUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val repository: MahilaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memberId: Long = checkNotNull(savedStateHandle["memberId"])

    private val _memberWithSavings = MutableStateFlow<MemberWithSavings?>(null)
    val memberWithSavings: StateFlow<MemberWithSavings?> = _memberWithSavings.asStateFlow()

    private val _memberWithLoans = MutableStateFlow<MemberWithLoans?>(null)
    val memberWithLoans: StateFlow<MemberWithLoans?> = _memberWithLoans.asStateFlow()

    private val _loanError = MutableStateFlow<String?>(null)
    val loanError: StateFlow<String?> = _loanError.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMemberWithSavings(memberId).collectLatest { data ->
                _memberWithSavings.value = data
            }
        }
        viewModelScope.launch {
            repository.getMemberWithLoans(memberId).collectLatest { data ->
                _memberWithLoans.value = data
            }
        }
    }

    fun addSavings(amount: Double) {
        viewModelScope.launch {
            val savings = Savings(
                memberId = memberId,
                dateMillis = System.currentTimeMillis(),
                amount = amount,
                isPaid = true
            )
            repository.addSavings(savings)
        }
    }

    fun requestLoan(amount: Double, interestRate: Double, durationMonths: Int) {
        viewModelScope.launch {
            val totalPayable = MathUtils.calculateTotalPayable(amount, interestRate, durationMonths)
            val loan = Loan(
                memberId = memberId,
                amount = amount,
                interestRate = interestRate,
                durationMonths = durationMonths,
                isPaid = false,
                remainingBalance = totalPayable
            )
            val success = repository.requestLoan(loan)
            if (!success) {
                _loanError.value = "Cannot take a new loan. Clear existing unpaid loan first."
            } else {
                _loanError.value = null
            }
        }
    }

    fun clearLoanError() {
        _loanError.value = null
    }

    fun repayLoan(loan: Loan, amount: Double) {
        viewModelScope.launch {
            repository.repayLoan(loan, amount)
        }
    }
}
