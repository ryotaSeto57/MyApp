package com.example.myapp.applist

import android.app.Application
import android.content.ContentValues.TAG
import android.content.pm.ApplicationInfo
import android.content.pm.ApplicationInfo.*
import android.content.pm.PackageManager.MATCH_SYSTEM_ONLY
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppDatabaseDao
import com.example.myapp.database.ReviewList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

class AppListViewModel(private val database: AppDatabaseDao, private val myapplication: Application) : AndroidViewModel(myapplication) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val pm = myapplication.packageManager
    private val appList: MutableList<ApplicationInfo> = getAppList()

    private fun getAppList():MutableList<ApplicationInfo>{
        val allAppList =  pm.getInstalledApplications(MATCH_UNINSTALLED_PACKAGES)
        return allAppList.filter{it.flags and MATCH_SYSTEM_ONLY == 0}.toMutableList()
    }


    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private val _userAppReviewList = MutableLiveData<MutableList<AppCard>>()
    val userAppReviewList: LiveData<MutableList<AppCard>>
        get() = _userAppReviewList

    init {
        Log.i("AppListViewModel", "AppListViewModel created")
        setUserAppList()
    }

    private fun setUserAppList() {
        uiScope.launch {
            val listId = createNewListId()
            createAppCards(listId)
            _userAppReviewList.value = getAppCardsFromDatabase(listId)
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

    private suspend fun getAppCardsFromDatabase(listId: Long):MutableList<AppCard>{
        return withContext(Dispatchers.IO){
            val listAppCards = database.getAppCards(listId)
            listAppCards
        }
    }

    private suspend fun insert(appCard: AppCard){
        withContext(Dispatchers.IO){
            database.insert(appCard)
        }
    }

    private suspend fun insert(listId: Long){
        withContext(Dispatchers.IO){
            database.insert(ReviewList(listId))
        }
    }

    private suspend fun update(appCard: AppCard){
        withContext(Dispatchers.IO){
            database.update(appCard)
        }
    }

    private suspend fun createNewListId() :Long{
        return withContext(Dispatchers.IO) {
            val numberOfReviewList = database.countReviewList()
            insert(numberOfReviewList.toLong())
            numberOfReviewList.toLong()
        }
    }

    fun uploadUserAppList() {
        uiScope.launch {
            if (_userAppReviewList.value!!.lastIndex < 100) {
                for ((index,appCard) in _userAppReviewList.value!!.withIndex()){
                    appCard.index = index
                    update(appCard)
                }
                saveToFireStorage()
                saveToFireStore()
            }
        }
    }

    private suspend fun saveToFireStorage() {
        var appIcon: Drawable
        var appUid: String
        for (appCard in _userAppReviewList.value!!) {
            appUid = appCard.packageName
            val appInfo = pm.getApplicationInfo(appCard.packageName,0)
            appIcon = appInfo.loadIcon(pm)
            val bitmap = (appIcon as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val checkIconNewOnCloud = storageRef.child("images/${appUid}").downloadUrl
            checkIconNewOnCloud.addOnFailureListener {
                storageRef.child("images/${appUid}").putBytes(data)
            }
        }
    }

    private suspend fun saveToFireStore() {
        val userAppCards: MutableMap<Int, Any> = mutableMapOf()
        for ((index,appCard) in _userAppReviewList.value!!.withIndex()) {
            val app: MutableMap<String, String> = mutableMapOf(
                "appUid" to appCard.packageName,
                "appReview" to appCard.review
            )
            userAppCards[index] = app
        }

        db.collection("users")
            .add(userAppCards)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("AppListViewModel", "AppListViewModel destroyed!")
        viewModelJob.cancel()
    }

    fun removeAppDataFromList(index: Int) {
        _userAppReviewList.value?.removeAt(index)
    }

    fun replaceAppData(index_a: Int, index_b: Int) {

        val appDataA = _userAppReviewList.value!![index_a]
        val appDataB = _userAppReviewList.value!![index_b]

        _userAppReviewList.value!![index_a] = appDataB
        _userAppReviewList.value!![index_b] = appDataA
    }
}