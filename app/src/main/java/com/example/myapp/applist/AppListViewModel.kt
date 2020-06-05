package com.example.myapp.applist

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapp.AppData

class AppListViewModel(private val myapplication: Application) : AndroidViewModel(myapplication) {

    init{
        Log.i("AppListViewModel", "AppListViewModel created")
        setAppList()
    }
    private lateinit var _appDataList : MutableLiveData<MutableList<AppData>>
    val appDataList: LiveData<MutableList<AppData>>
        get() = _appDataList

    private fun setAppList() {
        val packageManager = myapplication.packageManager
        val appList: MutableList<ApplicationInfo> = packageManager.getInstalledApplications(0)
        for (app in appList) {
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                if(this::_appDataList.isInitialized) {
                    _appDataList.value?.add(
                        AppData(
                            app.loadLabel(packageManager).toString(),
                            app.loadIcon(packageManager)
                        )
                    )
                }else{
                    _appDataList = MutableLiveData(
                        mutableListOf(
                        AppData(
                            app.loadLabel(packageManager).toString(),
                            app.loadIcon((packageManager))
                            )
                        )
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("AppListViewModel", "AppListViewModel destroyed!")
    }
}