package com.example.mahilashakti.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mahilashakti.data.entity.Loan
import com.example.mahilashakti.data.entity.Member
import com.example.mahilashakti.data.entity.Savings

@Database(
    entities = [Member::class, Savings::class, Loan::class],
    version = 1,
    exportSchema = false
)
abstract class MahilaDatabase : RoomDatabase() {
    abstract fun mahilaDao(): MahilaDao
}
