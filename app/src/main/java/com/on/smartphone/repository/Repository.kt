package com.on.smartphone.repository

import android.content.pm.ApplicationInfo
import androidx.lifecycle.LiveData
import com.on.smartphone.database.AppCard
import com.on.smartphone.database.AppCardList
import com.on.smartphone.database.ScreenShotItem
import java.util.*

interface Repository {

    suspend fun getCardList(key: Long): MutableList<AppCard>

    suspend fun createNewList()

    suspend fun save(card: AppCard)

    suspend fun add(card: AppCard)

    suspend fun deleteAppCard(appCardId: Long)

    suspend fun shareList(listOfAppCards: MutableList<AppCard>): String

    suspend fun getLatestAppCardList():AppCardList?

    suspend fun getAppCardLists():LiveData<MutableList<AppCardList>>

    suspend fun getAllAppCards(): LiveData<MutableList<AppCard>>

    fun getUserAppInfo(): MutableList<ApplicationInfo>

    suspend fun getAppCard(listId: Long,originalIndex: Int): AppCard?

    suspend fun addAppCards(appCards: MutableList<AppCard>): MutableList<AppCard>

    suspend fun saveScreenShotItem(uriString: String,index: Int,listId: Long)

    suspend fun deleteScreenShotItem(listId: Long)

    suspend fun getScreenShotItems(listId :Long):MutableList<ScreenShotItem>

    suspend fun saveAppCardList(appCardList: AppCardList)

    suspend fun getAppCardList(listId: Long):AppCardList?

    suspend fun shareScreenShot(
        listOfScreenShotItems: MutableList<ScreenShotItem>,
        userId: String,
        documentId:String,
        screenShotDescription: String
    ): Date?
}