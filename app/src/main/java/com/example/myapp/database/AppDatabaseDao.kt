package com.example.myapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface AppDatabaseDao {

    @Insert
    fun insert(appCard: AppCard)

    @Update
    fun update(appCard: AppCard)

    @Query("SELECT * FROM app_card_table WHERE list_id = :key ORDER BY `index`")
    fun getAppCards(key:Long): MutableList<AppCard>

    @Query("SELECT COUNT( * ) FROM review_list_table")
    fun countReviewList() : Int

    @Insert
    fun insert(reviewList: ReviewList)

    @Query("SELECT * FROM review_list_table ORDER BY id DESC LIMIT 1")
    fun getLatestReviewList() :ReviewList?

    @Query("DELETE FROM app_card_table WHERE id = :key")
    fun deleteAppCard(key:Long)

}