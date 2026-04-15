package com.ncwd.porkstreams.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ncwd.porkstreams.data.model.WatchLaterItem
import com.ncwd.porkstreams.data.model.WatchProgress

@Database(entities = [WatchProgress::class, WatchLaterItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun watchLaterDao(): WatchLaterDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "porkstreams.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
