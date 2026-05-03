package com.example.mahilashakti.data.repository

import com.example.mahilashakti.data.entity.Loan
import com.example.mahilashakti.data.entity.Member
import com.example.mahilashakti.data.entity.MemberWithLoans
import com.example.mahilashakti.data.entity.MemberWithSavings
import com.example.mahilashakti.data.entity.Savings
import com.example.mahilashakti.data.local.MahilaDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MahilaRepository @Inject constructor(
    private val dao: MahilaDao
) {
    // --- Members ---
    val allMembers: Flow<List<Member>> = dao.getAllMembers()

    suspend fun insertMember(member: Member) {
        dao.insertMember(member)
    }

    suspend fun deleteMember(member: Member) {
        dao.deleteMember(member)
    }

    suspend fun getMemberById(id: Long): Member? {
        return dao.getMemberById(id)
    }

    // --- Savings ---
    val totalGroupSavings: Flow<Double?> = dao.getTotalGroupSavings()

    fun getTotalSavingsForMember(memberId: Long): Flow<Double?> {
        return dao.getTotalSavingsForMember(memberId)
    }

    fun getMemberWithSavings(memberId: Long): Flow<MemberWithSavings> {
        return dao.getMemberWithSavings(memberId)
    }

    suspend fun addSavings(savings: Savings) {
        dao.insertSavings(savings)
    }

    suspend fun updateSavingsStatus(memberId: Long, isPaid: Boolean, targetAmount: Double = 150.0) {
        val (start, end) = getCurrentWeekRange()
        if (isPaid) {
            val currentWeekSavings = dao.getSavingsForMemberInPeriodList(memberId, start, end)
            val currentTotal = currentWeekSavings.sumOf { it.amount }
            if (currentTotal < targetAmount) {
                val needed = targetAmount - currentTotal
                dao.insertSavings(
                    Savings(
                        memberId = memberId,
                        dateMillis = System.currentTimeMillis(),
                        amount = needed,
                        isPaid = true
                    )
                )
            }
        } else {
            // When turning off, we remove all savings for the current week
            dao.deleteSavingsForMemberInPeriod(memberId, start, end)
        }
    }

    fun getWeeklySavings(): Flow<List<Savings>> {
        val (start, end) = getCurrentWeekRange()
        return dao.getSavingsInPeriod(start, end)
    }

    private fun getCurrentWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        // Start of current week (Sunday or Monday depending on locale)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        // End of current week
        val endCalendar = calendar.clone() as Calendar
        endCalendar.add(Calendar.DAY_OF_YEAR, 7)
        val end = endCalendar.timeInMillis
        return Pair(start, end)
    }

    // --- Loans ---
    fun getMemberWithLoans(memberId: Long): Flow<MemberWithLoans> {
        return dao.getMemberWithLoans(memberId)
    }

    suspend fun hasUnpaidLoan(memberId: Long): Boolean {
        val unpaidLoans = dao.getUnpaidLoansForMember(memberId)
        return unpaidLoans.isNotEmpty()
    }

    fun getAllUnpaidLoans(): Flow<List<Loan>> {
        return dao.getAllUnpaidLoans()
    }

    suspend fun requestLoan(loan: Loan): Boolean {
        if (hasUnpaidLoan(loan.memberId)) {
            // Cannot take a new loan if there's an unpaid one
            return false
        }
        dao.insertLoan(loan)
        return true
    }

    suspend fun repayLoan(loan: Loan, amount: Double) {
        val newBalance = loan.remainingBalance - amount
        val updatedLoan = loan.copy(
            remainingBalance = if (newBalance < 0) 0.0 else newBalance,
            isPaid = newBalance <= 0
        )
        dao.updateLoan(updatedLoan)
    }
}
