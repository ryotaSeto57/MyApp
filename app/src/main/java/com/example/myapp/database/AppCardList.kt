package com.example.myapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_card_list_table")


data class AppCardList (
    @PrimaryKey(autoGenerate = true)
    val id :Long,
    @ColumnInfo(name="number_of_apps_in_total")
    val numberOfAppsInTotal :Int = 0,
    @ColumnInfo(name="screen_shot_description")
    val screenShotDescription :String = ""
)