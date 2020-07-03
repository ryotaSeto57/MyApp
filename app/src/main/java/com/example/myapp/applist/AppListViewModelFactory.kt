package com.example.myapp.applist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapp.database.AppDatabaseDao

class AppListViewModelFactory(
    private val dataSource: AppDatabaseDao,
    private val application: Application,
    private val createNewList: Boolean): ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AppListViewModel::class.java)){
            return AppListViewModel(dataSource,application,createNewList) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}