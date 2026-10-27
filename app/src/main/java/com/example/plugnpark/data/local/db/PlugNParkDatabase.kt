package com.example.plugnpark.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.plugnpark.data.local.dao.BookingDao
import com.example.plugnpark.data.local.dao.ListingDao
import com.example.plugnpark.data.local.dao.ReviewDao
import com.example.plugnpark.data.local.dao.UserDao
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User

@Database(
    entities = [User::class, Listing::class, Booking::class, Review::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlugNParkDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun bookingDao(): BookingDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: PlugNParkDatabase? = null

        fun getDatabase(context: Context): PlugNParkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlugNParkDatabase::class.java,
                    "plug_n_park_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
