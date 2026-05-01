package com.example.mahilashakti.di

import android.content.Context
import androidx.room.Room
import com.example.mahilashakti.data.local.MahilaDao
import com.example.mahilashakti.data.local.MahilaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMahilaDatabase(
        @ApplicationContext context: Context
    ): MahilaDatabase {
        return Room.databaseBuilder(
            context,
            MahilaDatabase::class.java,
            "mahila_shakti_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMahilaDao(database: MahilaDatabase): MahilaDao {
        return database.mahilaDao()
    }
}
