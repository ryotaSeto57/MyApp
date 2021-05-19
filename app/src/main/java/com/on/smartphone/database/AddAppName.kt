package com.on.smartphone.database

import androidx.lifecycle.MutableLiveData

data class AddAppName (
    val id:Long,val packageName:String,
    val addOrNot :MutableLiveData<Boolean> = MutableLiveData(false)
)