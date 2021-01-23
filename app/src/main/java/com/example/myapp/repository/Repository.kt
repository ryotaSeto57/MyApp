package com.example.myapp.repository

import android.content.pm.ApplicationInfo
import com.example.myapp.database.AppCard
import com.example.myapp.database.ReviewList

interface Repository {

    suspend fun getList(key: Long): MutableList<AppCard>

    suspend fun createNewList()

    suspend fun save(card: AppCard)

    suspend fun add(card: AppCard)

    suspend fun deleteAppCard(appCardId: Long)

    suspend fun shareList(listOfAppCards: MutableList<AppCard>)

    suspend fun getLatestReviewList():ReviewList?

    fun getUserAppInfo(): MutableList<ApplicationInfo>
}