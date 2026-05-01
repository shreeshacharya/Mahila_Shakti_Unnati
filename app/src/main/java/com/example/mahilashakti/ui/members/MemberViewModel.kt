package com.example.mahilashakti.ui.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mahilashakti.data.entity.Member
import com.example.mahilashakti.data.entity.Savings
import com.example.mahilashakti.data.repository.MahilaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberViewModel @Inject constructor(
    private val repository: MahilaRepository
) : ViewModel() {

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _totalGroupSavings = MutableStateFlow(0.0)
    val totalGroupSavings: StateFlow<Double> = _totalGroupSavings.asStateFlow()

    private val _weeklySavings = MutableStateFlow<List<Savings>>(emptyList())
    val weeklySavings: StateFlow<List<Savings>> = _weeklySavings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allMembers.collectLatest { memberList ->
                _members.value = memberList
            }
        }
        viewModelScope.launch {
            repository.totalGroupSavings.collectLatest { total ->
                _totalGroupSavings.value = total ?: 0.0
            }
        }
        viewModelScope.launch {
            repository.getWeeklySavings().collectLatest { savingsList ->
                _weeklySavings.value = savingsList
            }
        }
    }

    fun addMember(name: String, phone: String, photoUri: String? = null) {
        viewModelScope.launch {
            repository.insertMember(Member(name = name, phoneNumber = phone, photoUri = photoUri))
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun toggleWeeklySavings(memberId: Long, isPaid: Boolean, amount: Double = 150.0) {
        viewModelScope.launch {
            repository.updateSavingsStatus(memberId, isPaid, amount)
        }
    }
}
