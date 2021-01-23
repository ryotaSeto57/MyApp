package com.example.myapp.database

import androidx.lifecycle.MutableLiveData

data class AddAppName (val packageName:String, val addOrNot :MutableLiveData<Boolean> = MutableLiveData(false))