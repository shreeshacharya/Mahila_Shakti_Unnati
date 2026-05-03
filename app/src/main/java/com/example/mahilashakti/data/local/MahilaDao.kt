package com.example.mahilashakti.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.mahilashakti.data.entity.Loan
import com.example.mahilashakti.data.entity.Member
import com.example.mahilashakti.data.entity.MemberWithLoans
import com.example.mahilashakti.data.entity.MemberWithSavings
import com.example.mahilashakti.data.entity.Savings
import kotlinx.coroutines.flow.Flow

@Dao
interface MahilaDao {

    // Member Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :memberId")
    suspend fun getMemberById(memberId: Long): Member?

    // Savings Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: Savings): Long

    @Update
    suspend fun updateSavings(savings: Savings)

    @Query("SELECT SUM(amount) FROM savings WHERE isPaid = 1")
    fun getTotalGroupSavings(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM savings WHERE memberId = :memberId AND isPaid = 1")
    fun getTotalSavingsForMember(memberId: Long): Flow<Double?>

    @Query("SELECT * FROM savings WHERE memberId = :memberId AND dateMillis >= :startMillis AND dateMillis < :endMillis LIMIT 1")
    suspend fun getSavingsForMemberInPeriod(memberId: Long, startMillis: Long, endMillis: Long): Savings?

    @Query("SELECT * FROM savings WHERE memberId = :memberId AND dateMillis >= :startMillis AND dateMillis < :endMillis")
    suspend fun getSavingsForMemberInPeriodList(memberId: Long, startMillis: Long, endMillis: Long): List<Savings>

    @Query("DELETE FROM savings WHERE memberId = :memberId AND dateMillis >= :startMillis AND dateMillis < :endMillis")
    suspend fun deleteSavingsForMemberInPeriod(memberId: Long, startMillis: Long, endMillis: Long)

    @Query("SELECT * FROM savings WHERE dateMillis >= :startMillis AND dateMillis < :endMillis")
    fun getSavingsInPeriod(startMillis: Long, endMillis: Long): Flow<List<Savings>>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :memberId")
    fun getMemberWithSavings(memberId: Long): Flow<MemberWithSavings>

    // Loan Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Query("SELECT * FROM loans WHERE memberId = :memberId AND isPaid = 0")
    suspend fun getUnpaidLoansForMember(memberId: Long): List<Loan>

    @Query("SELECT * FROM loans WHERE isPaid = 0")
    fun getAllUnpaidLoans(): Flow<List<Loan>>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :memberId")
    fun getMemberWithLoans(memberId: Long): Flow<MemberWithLoans>
}
