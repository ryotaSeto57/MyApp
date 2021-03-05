package com.example.myapp.repository

import android.content.pm.ApplicationInfo
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

    suspend fun getAppCardLists():MutableList<AppCardList>

    suspend fun getAllAppCards():MutableList<AppCard>


    fun getUserAppInfo(): MutableList<ApplicationInfo>
}