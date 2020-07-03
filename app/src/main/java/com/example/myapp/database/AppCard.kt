package com.example.myapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "app_card_table")

data class AppCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "list_id")
    val listId: Long,
    @ColumnInfo(name="index")
    var index: Int,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "review")
    var review : String = ""
)