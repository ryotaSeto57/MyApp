package com.example.myapp.applist

import android.app.Application
import android.content.ContentValues.TAG
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.*
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppDatabaseDao
import com.example.myapp.database.ReviewList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.Exception

class AppListViewModel(private val database: AppDatabaseDao, private val myapplication: Application,private val createNewList: Boolean) : AndroidViewModel(myapplication) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val pm = myapplication.packageManager
    private val appList: MutableList<ApplicationInfo> = getAppList()

    private fun getAppList():MutableList<ApplicationInfo>{
        val allAppList =  pm.getInstalledApplications(MATCH_UNINSTALLED_PACKAGES)
        return allAppList.filter{it.flags and FLAG_SYSTEM == 0}.toMutableList()
    }

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private val _userAppReviewList = MutableLiveData<MutableList<AppCard>>()
    val userAppReviewList: LiveData<MutableList<AppCard>>
        get() = _userAppReviewList

    val addButtonVisible = Transformations.map(_userAppReviewList){
        View.GONE
    }

    private var userAppReviewListStore : MutableList<AppCard> = mutableListOf()

    init {
        Log.i("AppListViewModel", "AppListViewModel created")
        setUserAppList()
    }

    private fun setUserAppList() {
        uiScope.launch {
            val count = countReviewList()
            if (count == 0 || createNewList) {
                val listId = getNewListId(count)
                createAppCards(listId)
            }
            _userAppReviewList.value = getAppCardsFromDatabase()
        }
    }

    private suspend fun countReviewList():Int{
        return withContext(Dispatchers.IO) {
            val numberOfReviewList =database.countReviewList()
            numberOfReviewList
        }
    }

    private suspend fun getNewListId(numberOfReviewList: Int) :Long{
        return withContext(Dispatchers.IO) {
            createNewListById(numberOfReviewList.toLong())
            numberOfReviewList.toLong()
        }
    }

    private suspend fun createAppCards(listId: Long) {
        withContext(Dispatchers.IO) {
            val listAppCards :MutableList<AppCard> = MutableList(appList.size) { index ->
                AppCard(id =0,listId =  listId,index =  index, packageName = appList[index].packageName)
            }
            for (appCard in listAppCards){
                insert(appCard)
            }
        }
    }

    private suspend fun getAppCardsFromDatabase():MutableList<AppCard>{
        return withContext(Dispatchers.IO){
            val listId = getLatestReviewList()!!.id
            val listAppCards = database.getAppCards(listId).sortedBy{ it.index }.toMutableList()
            listAppCards
        }
    }

    private suspend fun insert(appCard: AppCard){
        withContext(Dispatchers.IO){
            database.insert(appCard)
        }
    }

    private suspend fun createNewListById(listId: Long){
        withContext(Dispatchers.IO){
            database.insert(ReviewList(listId))
        }
    }
    private suspend fun saveAppCards(){
        for ((index,appCard) in _userAppReviewList.value!!.withIndex()) {
            appCard.index = index
            update(appCard)
        }
    }

    private suspend fun update(appCard: AppCard){
        withContext(Dispatchers.IO){
            database.update(appCard)
        }
    }

    private suspend fun getLatestReviewList():ReviewList?{
       return   withContext(Dispatchers.IO){
            val latestReviewList= database.getLatestReviewList()
           latestReviewList
        }
    }

    fun uploadUserAppList() {
        uiScope.launch {
            if (_userAppReviewList.value!!.lastIndex < 300) {
                saveAppCards()
                saveToFireStorage()
                saveToFireStore()
            }
        }
    }

    private suspend fun saveToFireStorage() {
        withContext(Dispatchers.IO) {
            var appIcon: Drawable
            var appUid: String
            for (appCard in _userAppReviewList.value!!) {
                appUid = appCard.packageName
                val uri = kotlin.runCatching {
                    try {
                         storageRef.child("images/${appUid}").downloadUrl.await()
                    } catch(e:StorageException) {
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
                    ?.also{ appCard.downloadUrl = it.toString()}
                    ?:Log.w(TAG,"error in uploading $appUid")
            }
        }
    }

    private suspend fun saveToFireStore() {
        withContext(Dispatchers.IO) {
            val userAppCards: MutableMap<String, Any> = mutableMapOf()
            for ((index, appCard) in _userAppReviewList.value!!.withIndex()) {
                val appInfo = pm.getApplicationInfo(appCard.packageName,0)
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
                Log.w(TAG, "Error adding document", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.launch {
            saveAppCards()
        }
        Log.i("AppListViewModel", "AppListViewModel destroyed!")
        viewModelJob.cancel()
    }

    fun removeAppDataFromList(index: Int,appCardId:Long) {
       uiScope.launch {
           deleteAppCard(appCardId)
           _userAppReviewList.value!!.removeAll{ it.id == appCardId }
           _userAppReviewList.value = _userAppReviewList.value
         }
    }

    fun replaceAppData(indexOfFrom: Int, indexOfTo: Int) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                userAppReviewListStore = _userAppReviewList.value!!
                userAppReviewListStore[indexOfFrom].index = indexOfTo
                if (indexOfFrom < indexOfTo) {
                    for (i in indexOfFrom until indexOfTo) {
                        userAppReviewListStore[i + 1].index -= 1
                    }
                } else if (indexOfFrom > indexOfTo) {
                    for (i in indexOfTo until indexOfFrom) {
                        userAppReviewListStore[i].index += 1
                    }
                }
                userAppReviewListStore.sortBy { it.index }
                _userAppReviewList.postValue(userAppReviewListStore)
            }
        }
    }


    private suspend fun deleteAppCard(appCardId:Long){
        withContext(Dispatchers.IO){
            database.deleteAppCard(appCardId)
        }
    }
}