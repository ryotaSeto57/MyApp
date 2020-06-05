package com.example.myapp.applist

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapp.AppData

class AppListViewModelFactory(private val application: Application): ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AppListViewModel::class.java)){
            return AppListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}