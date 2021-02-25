package com.example.myapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_card_list_table")


data class AppCardList (
    @PrimaryKey(autoGenerate = true)
    val id :Long
)