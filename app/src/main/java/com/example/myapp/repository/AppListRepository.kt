package com.example.myapp.repository

import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppDatabaseDao
import com.example.myapp.database.ReviewList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AppListRepository(
    private val database: AppDatabaseDao, private val pm: PackageManager
) : Repository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    override suspend fun getList(key: Long): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            return@withContext database.getAppCards(key)
        }
    }

    override suspend fun add(card: AppCard) {
        withContext(Dispatchers.IO) {
            database.insert(card)
        }
    }

    override suspend fun getLatestReviewList(): ReviewList? {
        return withContext(Dispatchers.IO) {
            return@withContext database.getLatestReviewList()
        }
    }

    override suspend fun createNewList() {
        withContext(Dispatchers.IO) {
            database.insert(ReviewList(0))
            val listId = getLatestReviewList()?.id ?: 0L
            val userAppInfoList = getUserAppInfo()
            for ((index, appInfo) in userAppInfoList.withIndex()) {
                database.insert(
                    AppCard(
                        id = 0,
                        listId = listId,
                        index = index,
                        packageName = appInfo.packageName
                    )
                )
            }
        }
    }

    override fun getUserAppInfo(): MutableList<ApplicationInfo> {
        val allAppList = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        val selectedList =
            allAppList.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }.toMutableList()
        val regex = Regex("com.example.")
        val finalList = mutableListOf<ApplicationInfo>()
        for (appInfo in selectedList) {
            if (!regex.containsMatchIn(appInfo.packageName)) {
                finalList.add(appInfo)
            }
        }
        return finalList
    }

    override suspend fun save(card: AppCard) {
        withContext(Dispatchers.IO) {
            database.update(card)
        }
    }

    override suspend fun deleteAppCard(appCardId: Long) {
        withContext(Dispatchers.IO) {
            database.deleteAppCard(appCardId)
        }
    }

    override suspend fun shareList(listOfAppCards: MutableList<AppCard>) {
        withContext(Dispatchers.IO) {
            var appIcon: Drawable
            var appUid: String
            for (appCard in listOfAppCards) {
                appUid = appCard.packageName
                val uri = kotlin.runCatching {
                    try {
                        storageRef.child("images/${appUid}").downloadUrl.await()
                    } catch (e: StorageException) {
                        val appInfo = pm.getApplicationInfo(appCard.packageName, 0)
                        appIcon = appInfo.loadIcon(pm)
                        val bitmap = (appIcon as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        storageRef.child("images/$appUid").putBytes(data).await()
                        storageRef.child("images/${appUid}").downloadUrl.await()
                    }
                }
                uri.getOrNull()
                    ?.also { appCard.downloadUrl = it.toString() }
                    ?: Log.w(ContentValues.TAG, "error in uploading $appUid")
            }
            val userAppCards: MutableMap<String, Any> = mutableMapOf()
            for ((index, appCard) in listOfAppCards.withIndex()) {
                val appInfo = pm.getApplicationInfo(appCard.packageName, 0)
                val app: MutableMap<String, String> = mutableMapOf(
                    "appUid" to appCard.packageName,
                    "appReview" to appCard.review,
                    "appName" to appInfo.loadLabel(pm).toString(),
                    "URL" to appCard.downloadUrl
                )
                userAppCards[index.toString()] = app
            }
            try {
                db.collection("users").add(userAppCards).await()
            } catch (e: Exception) {
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
        }
    }
}

