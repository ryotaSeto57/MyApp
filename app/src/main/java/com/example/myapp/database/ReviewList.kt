package com.example.myapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_list_table")


data class ReviewList (
    @PrimaryKey(autoGenerate = false)
    val id :Long
)