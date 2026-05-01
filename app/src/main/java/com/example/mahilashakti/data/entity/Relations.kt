package com.example.mahilashakti.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MemberWithSavings(
    @Embedded val member: Member,
    @Relation(
        parentColumn = "id",
        entityColumn = "memberId"
    )
    val savings: List<Savings>
)

data class MemberWithLoans(
    @Embedded val member: Member,
    @Relation(
        parentColumn = "id",
        entityColumn = "memberId"
    )
    val loans: List<Loan>
)
