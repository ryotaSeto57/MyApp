package com.example.myapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface AppDatabaseDao {

    @Insert
    fun insert(appReviewList: AppReviewList)

    @Update
    fun update(appReviewList: AppReviewList)

    @Query("SELECT * FROM app_review_list_table WHERE listId = :key")
    fun getAppReviewList(key:Long): LiveData<AppReviewList>?

    @Query("SELECT COUNT(*) FROM app_review_list_table")
    fun countAppList(): Int
}