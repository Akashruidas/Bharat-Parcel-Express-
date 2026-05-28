package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ParcelBooking
import com.example.data.model.User

@Database(entities = [ParcelBooking::class, User::class], version = 1, exportSchema = false)
abstract class ParcelDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: ParcelDatabase? = null

        fun getDatabase(context: Context): ParcelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParcelDatabase::class.java,
                    "parcel_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
