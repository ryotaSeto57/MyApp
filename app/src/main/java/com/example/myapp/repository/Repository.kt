package com.example.myapp.repository

import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppCardList

interface Repository {

    suspend fun getCardList(key: Long): MutableList<AppCard>

    suspend fun createNewList()

    suspend fun save(card: AppCard)

    suspend fun add(card: AppCard)

    suspend fun deleteAppCard(appCardId: Long)

    suspend fun shareList(listOfAppCards: MutableList<AppCard>)

    suspend fun getLatestAppCardList():AppCardList?

    suspend fun getAppCardLists():LiveData<MutableList<AppCardList>>

    suspend fun getAllAppCards(): LiveData<MutableList<AppCard>>

    fun getUserAppInfo(): MutableList<ApplicationInfo>

    suspend fun plusNumberOfAppsInTotal(listId: Long, numberOfAddedApps: Int)

    suspend fun getNumberOfPastAppCardsInTotal(listId: Long):Int

    suspend fun getAppCard(listId: Long,originalIndex: Int): AppCard?

    suspend fun addAppCards(appCards: MutableList<AppCard>): MutableList<AppCard>
}