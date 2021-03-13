package com.example.myapp.repository

import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppCardList

interface Repository {

    suspend fun getList(key: Long): MutableList<AppCard>

    suspend fun createNewList()

    suspend fun save(card: AppCard)

    suspend fun add(card: AppCard)

    suspend fun deleteAppCard(appCardId: Long)

    suspend fun shareList(listOfAppCards: MutableList<AppCard>)

    suspend fun getLatestAppCardList():AppCardList?

    suspend fun getAppCardLists():LiveData<MutableList<AppCardList>>

    suspend fun getAllAppCards(): LiveData<MutableList<AppCard>>

    fun getUserAppInfo(): MutableList<ApplicationInfo>
}