package com.example.myapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_shot_table")
data class ScreenShotItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "list_id")
    val listId: Long,
    @ColumnInfo(name = "uri_string")
    val uriString: String,
    @ColumnInfo(name="index")
    var index: Int
)
