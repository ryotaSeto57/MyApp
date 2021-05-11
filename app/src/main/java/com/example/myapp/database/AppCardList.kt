package com.example.myapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "app_card_list_table")

data class AppCardList (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name="screen_shot_description")
    var screenShotDescription: String = "",
    @ColumnInfo(name="list_id_on_firebase")
    var listIdOnFirebase: String = "",
    @ColumnInfo(name="shared_date")
    var sharedDate: Date? = null,
    @ColumnInfo(name="list_url")
    var listUrl: String = ""
)