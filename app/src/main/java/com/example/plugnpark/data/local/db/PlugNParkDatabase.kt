package com.example.plugnpark.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.plugnpark.data.local.dao.AppNotificationDao
import com.example.plugnpark.data.local.dao.AvailabilityRuleDao
import com.example.plugnpark.data.local.dao.BookingDao
import com.example.plugnpark.data.local.dao.ChargerSpecDao
import com.example.plugnpark.data.local.dao.FavoriteDao
import com.example.plugnpark.data.local.dao.ListingDao
import com.example.plugnpark.data.local.dao.ListingPhotoDao
import com.example.plugnpark.data.local.dao.ParkingSpecDao
import com.example.plugnpark.data.local.dao.ReviewDao
import com.example.plugnpark.data.local.dao.UserDao
import com.example.plugnpark.data.local.dao.VehicleDao
import com.example.plugnpark.data.local.entity.AppNotification
import com.example.plugnpark.data.local.entity.AvailabilityRule
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.ChargerSpec
import com.example.plugnpark.data.local.entity.Favorite
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.ListingPhoto
import com.example.plugnpark.data.local.entity.ParkingSpec
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.local.entity.Vehicle

@Database(
    entities = [
        User::class,
        Vehicle::class,
        Listing::class,
        ChargerSpec::class,
        ParkingSpec::class,
        ListingPhoto::class,
        AvailabilityRule::class,
        Booking::class,
        Review::class,
        Favorite::class,
        AppNotification::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PlugNParkDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun listingDao(): ListingDao
    abstract fun chargerSpecDao(): ChargerSpecDao
    abstract fun parkingSpecDao(): ParkingSpecDao
    abstract fun listingPhotoDao(): ListingPhotoDao
    abstract fun availabilityRuleDao(): AvailabilityRuleDao
    abstract fun bookingDao(): BookingDao
    abstract fun reviewDao(): ReviewDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun appNotificationDao(): AppNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: PlugNParkDatabase? = null

        fun getDatabase(context: Context): PlugNParkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlugNParkDatabase::class.java,
                    "plug_n_park_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
