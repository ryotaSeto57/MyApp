package com.example.myapp.applist

import android.app.Application
import android.content.ContentValues.TAG
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapp.database.AppData
import com.example.myapp.database.AppDatabaseDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

class AppListViewModel(private val database: AppDatabaseDao, private val myapplication: Application) : AndroidViewModel(myapplication) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference


    private lateinit var _appDataList: MutableLiveData<MutableList<AppData>>
    val appDataList: LiveData<MutableList<AppData>>
        get() = _appDataList


    init {
        Log.i("AppListViewModel", "AppListViewModel created")
        setAppList()
    }

    private fun setAppList() {
        uiScope.launch {
            val count = countAppList()
            if (count == 0) {
                _appDataList.value = getUserAppList()
            }else{
                _appDataList.value = getAppListFromDatabase()
            }
        }
    }
    private suspend fun getUserAppList(): MutableList<AppData> {
        return withContext(Dispatchers.IO) {
            val packageManager = myapplication.packageManager
            val appList: MutableList<ApplicationInfo> = packageManager.getInstalledApplications(0)
            var userAppList :MutableList<AppData>
            for (app in appList) {
                var initialized :boolean = false
                if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    if (!initialized) {
                        userAppList = mutableListOf(
                                AppData(
                                    app.loadLabel(packageManager).toString(),
                                    app.loadIcon((packageManager)),
                                    "",
                                    app.publicSourceDir
                                )
                            )
                        initialized = true
                        continue
                    }
                    userAppList.add(
                        AppData(
                            app.loadLabel(packageManager).toString(),
                            app.loadIcon(packageManager),
                            "",
                            app.publicSourceDir
                        )
                    )
                }
            }
            userAppList
        }
    }

    private suspend fun getAppListFromDatabase():MutableList<AppData>{
        return withContext(Dispatchers.IO){
            val userAppList = getAppReviewList()
        }
    }

    private suspend fun countAppList(){
        return withContext(Dispatchers.IO){ database.countAppList() }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("AppListViewModel", "AppListViewModel destroyed!")
        viewModelJob.cancel()
    }

    fun removeAppDataFromList(index: Int) {
        _appDataList.value?.removeAt(index)
    }

    fun replaceAppData(index_a: Int, index_b: Int) {

        val appDataA = _appDataList.value?.getOrNull(index_a)
        val appDataB = _appDataList.value?.getOrNull(index_b)

        _appDataList.value!![index_a] = appDataB!!
        _appDataList.value!![index_b] = appDataA!!
    }

    fun uploadUserAppList() {
        if (_appDataList.value!!.lastIndex < 100) {
            saveToFireStorage()
            saveToFireStore()
        }
    }

    private fun saveToFireStorage() {
        var appIcon: Drawable
        var appUid: String

        for (i in _appDataList.value!!.indices) {
            appUid = _appDataList.value?.getOrNull(i)?.sourceDir!!
            appIcon = _appDataList.value?.getOrNull(i)?.icon!!
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

    private fun saveToFireStore() {
        val userApps: MutableMap<String, Any> = mutableMapOf()
        for (i in _appDataList.value!!.indices) {
            val app: MutableMap<String, String> = mutableMapOf(
                "appName" to "",
                "appUid" to "",
                "appReview" to ""
            )
            app["appName"] = _appDataList.value!![i].name
            app["appUid"] = _appDataList.value!![i].pubSouDirAsUid
            app["appReview"] = _appDataList.value!![i].review
            userApps[i.toString()] = app
        }

        db.collection("users")
            .add(userApps)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}