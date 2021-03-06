package com.on.smartphone.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface AppDatabaseDao {
    @Insert(onConflict =  OnConflictStrategy.REPLACE)
    fun insert(appCard: AppCard)

    @Update
    fun update(appCard: AppCard)

    @Query("SELECT * FROM app_card_table WHERE list_id = :key ORDER BY `index`")
    fun getAppCards(key:Long): MutableList<AppCard>

    @Query("SELECT COUNT( * ) FROM app_card_list_table")
    fun countReviewList() : Int

    @Insert
    fun insert(appCardList: AppCardList)

    @Query("SELECT * FROM app_card_list_table ORDER BY id DESC LIMIT 1")
    fun getLatestReviewList() :AppCardList?

    @Query("DELETE FROM app_card_table WHERE id = :key")
    fun deleteAppCard(key:Long)

    @Query("SELECT * FROM app_card_list_table ORDER BY id")
    fun getAppCardLists(): LiveData<MutableList<AppCardList>>

    @Query("SELECT * FROM app_card_table ")
    fun getAllAppCards(): LiveData<MutableList<AppCard>>

    @Update
    fun update(appCardList: AppCardList)

    @Query("SELECT * FROM app_card_list_table WHERE id = :key")
    fun getAppCardList(key: Long): AppCardList?

    @Query("SELECT * FROM app_card_table WHERE list_id = :listId AND original_index = :originalIndex")
    fun getAppCard(listId: Long, originalIndex: Int): AppCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(screenShot: ScreenShotItem)

    @Query("SELECT * FROM screen_shot_table WHERE list_id = :listId ORDER BY `index`")
    fun getScreenShotItems(listId: Long) :MutableList<ScreenShotItem>

    @Query("DELETE FROM screen_shot_table WHERE list_id =:listId")
    fun deleteScreenShotItems(listId:Long)
}