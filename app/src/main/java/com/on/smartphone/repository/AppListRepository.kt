package com.on.smartphone.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.on.smartphone.database.AppCard
import com.on.smartphone.database.AppDatabaseDao
import com.on.smartphone.database.AppCardList
import com.on.smartphone.database.ScreenShotItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

private const val ERROR_MESSAGE_OF_APP_NAME = "削除されました"

class AppListRepository @Inject constructor(
    private val database: AppDatabaseDao,
    @ApplicationContext appContext: Context
) : Repository {

    private val pm = appContext.packageManager
    private val contentResolver = appContext.contentResolver
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    override suspend fun getCardList(key: Long): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            return@withContext database.getAppCards(key)
        }
    }

    override suspend fun add(card: AppCard) {
        withContext(Dispatchers.IO) {
            database.insert(card)
        }
    }

    override suspend fun getAppCard(listId: Long, originalIndex: Int): AppCard? {
        return withContext(Dispatchers.IO) {
            return@withContext database.getAppCard(listId, originalIndex)
        }
    }

    override suspend fun addAppCards(appCards: MutableList<AppCard>): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            val appCardsFromDatabase: MutableList<AppCard> = mutableListOf()
            for (appCard in appCards) {
                add(appCard)
                val appCardFromDatabase = getAppCard(appCard.listId, appCard.originalIndex)
                if (appCardFromDatabase != null) {
                    appCardsFromDatabase.add(appCardFromDatabase)
                }
            }
            return@withContext appCardsFromDatabase
        }
    }

    override suspend fun getLatestAppCardList(): AppCardList? {
        return withContext(Dispatchers.IO) {
            return@withContext database.getLatestReviewList()
        }
    }

    override suspend fun getAllAppCards(): LiveData<MutableList<AppCard>> {
        return withContext(Dispatchers.IO) {
            return@withContext database.getAllAppCards()
        }
    }

    override suspend fun createNewList() {
        withContext(Dispatchers.IO) {
            database.insert(AppCardList(0))
            val listId = getLatestAppCardList()?.id ?: 0L
            val userAppInfoList = getUserAppInfo()
            for ((index, appInfo) in userAppInfoList.withIndex()) {
                add(
                    AppCard(
                        id = 0,
                        listId = listId,
                        originalIndex = index,
                        index = index,
                        packageName = appInfo.packageName
                    )
                )
            }
        }
    }

    override suspend fun getAppCardLists(): LiveData<MutableList<AppCardList>> {
        return withContext(Dispatchers.IO) {
            return@withContext database.getAppCardLists()
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

    override suspend fun shareList(listOfAppCards: MutableList<AppCard>): String {
        return withContext(Dispatchers.IO) {
            var appIcon: Drawable
            var appUid: String
            var appInfo: ApplicationInfo
            for (appCard in listOfAppCards) {
                appUid = appCard.packageName
                appInfo = try {
                    pm.getApplicationInfo(appCard.packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    continue
                }
                val uri = kotlin.runCatching {
                    try {
                        storageRef.child("images/${appUid}").downloadUrl.await()
                    } catch (e: StorageException) {
                        appIcon = appInfo.loadIcon(pm)
                        val bitmap = (appIcon as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        storageRef.child("images/$appUid").putBytes(data).await()
                        storageRef.child("images/$appUid").downloadUrl.await()
                    }
                }
                uri.getOrNull()
                    ?.also { appCard.downloadUrl = it.toString() }
                    ?: Timber.w("error in uploading $appUid")
            }
            val userAppCards: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
            for ((index, appCard) in listOfAppCards.withIndex()) {
                val appInfo: ApplicationInfo? = try {
                    pm.getApplicationInfo(appCard.packageName, 0)
                } catch (e: Exception) {
                    null
                }
                val appName = appInfo?.loadLabel(pm)?.toString() ?: ERROR_MESSAGE_OF_APP_NAME
                val app: MutableMap<String, String> = mutableMapOf(
                    "appUid" to appCard.packageName,
                    "appReview" to appCard.review,
                    "appName" to appName,
                    "URL" to appCard.downloadUrl,
                )
                userAppCards[index.toString()] = app
            }
            var listUrl = ""
            try {
                db.collection("lists").add(userAppCards).await().also { documentReference ->
                    listUrl = documentReference.id
                }
            } catch (e: Exception) {
                Timber.w("Error adding document")
            }
            return@withContext listUrl
        }
    }

    override suspend fun saveScreenShotItem(uriString: String, index: Int, listId: Long) {
        withContext(Dispatchers.IO) {
            database.insert(
                ScreenShotItem(
                    id = 0,
                    uriString = uriString,
                    index = index,
                    listId = listId
                )
            )
        }
    }

    override suspend fun deleteScreenShotItem(listId: Long) {
        withContext(Dispatchers.IO) {
            database.deleteScreenShotItems(listId)
        }
    }

    override suspend fun getScreenShotItems(listId: Long): MutableList<ScreenShotItem> {
        return withContext(Dispatchers.IO) {
            return@withContext database.getScreenShotItems(listId)
        }
    }

    override suspend fun saveAppCardList(appCardList: AppCardList) {
        withContext(Dispatchers.IO) {
            database.update(appCardList)
        }
    }

    override suspend fun getAppCardList(listId: Long): AppCardList? {
        return withContext(Dispatchers.IO) {
            database.getAppCardList(listId)
        }
    }

    override suspend fun shareScreenShot(
        listOfScreenShotItems: MutableList<ScreenShotItem>,
        userId: String,
        documentId: String,
        screenShotDescription: String
    ): Date? {
        return withContext(Dispatchers.IO) {
            val screenShotUrl: MutableMap<String,String> = mutableMapOf()
            for (i in listOfScreenShotItems) {
                var bitmap: Bitmap
                if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, i.uriString.toUri())
                } else {
                    val source = ImageDecoder.createSource(contentResolver, i.uriString.toUri())
                    bitmap = ImageDecoder.decodeBitmap(source)
                }
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val uri = kotlin.runCatching {
                    try {
                        storageRef.child("screenshots/${userId}/${i.index}").putBytes(data).await()
                        storageRef.child("screenshots/${userId}/${i.index}").downloadUrl.await()
                    } catch (e: Exception) {
                        Timber.w("Error in adding ScreenShot of index ${i.index}")
                    }
                }
                uri.getOrNull()?.also { screenShotUrl[i.index.toString()]= it.toString() }
            }
            val userInfo: MutableMap<String, Any> =
                mutableMapOf(
                    "screenshot_description" to screenShotDescription,
                    "screenshot_url" to screenShotUrl,
                    "user_id" to userId,
                    "time_stamp" to FieldValue.serverTimestamp()
                )
            try {
                db.collection("lists").document(documentId).update(userInfo).await()
            } catch (e: Exception) {
                Timber.w("Error in adding screenshot description.")
            }
            var document: Map<String, Any>? = null
            db.collection("lists").document(documentId).get().await().also { documentSnapshot ->
                documentSnapshot?.let { document = it.data }
            }
            return@withContext document?.let { (it["time_stamp"] as Timestamp).toDate() }
        }
    }
}

private const val USER_LIST_ID_LENGTH = 20
private const val USER_LIST_ID_ALPHABET =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

class CreateUserListId {

    private val rand = Random()

    fun createId(): String {
        val builder = StringBuilder()
        val maxRandom = USER_LIST_ID_ALPHABET.length
        for (i in 0..USER_LIST_ID_LENGTH) {
            builder.append(USER_LIST_ID_ALPHABET[rand.nextInt(maxRandom)])
        }
        return builder.toString()
    }
}

