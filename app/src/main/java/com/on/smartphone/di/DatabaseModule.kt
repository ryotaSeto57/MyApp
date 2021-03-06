package com.on.smartphone.di

import android.content.Context
import com.on.smartphone.database.AppDatabase
import com.on.smartphone.database.AppDatabaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    fun provideAppDatabaseDao(@ApplicationContext appContext: Context): AppDatabaseDao{
        return AppDatabase.getInstance(appContext).appDatabaseDao
    }
}