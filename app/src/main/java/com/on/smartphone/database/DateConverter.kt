package com.on.smartphone.database

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun fromTimeStamp(value: Long): Date?{
        if(value == 0L){
            return null
        }
        return  Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long{
        return date?.time ?: 0L
    }

}