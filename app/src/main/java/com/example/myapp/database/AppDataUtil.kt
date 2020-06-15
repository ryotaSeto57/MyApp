package com.example.myapp.database

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class AppData(
    val name:String,
    val icon: Drawable,
    var review: String,
    val pubSouDirAsUid: String
)

@Entity(tableName = "app_review_list_table")
data class AppReviewList(
    @PrimaryKey(autoGenerate = true)
    var listId: Long = 0L,
    @ColumnInfo(name="app_review_list")
    var appUidToReviewList: MutableList<Pair<String,String>>,
    @ColumnInfo(name = "isStoredInFireStore")
    var isInFireStore: boolean = false
)
