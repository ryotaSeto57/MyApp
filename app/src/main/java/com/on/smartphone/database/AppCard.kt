package com.on.smartphone.database

import androidx.room.*

@Entity(tableName = "app_card_table")

data class AppCard constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "list_id")
    val listId: Long,
    @ColumnInfo(name = "original_index")
    var originalIndex: Int,
    @ColumnInfo(name="index")
    var index: Int,
    @ColumnInfo(name = "package_name")
    val packageName: String
){
    @ColumnInfo(name = "review")
    var review : String = ""
    @ColumnInfo(name="download_url")
    var downloadUrl : String = ""
}