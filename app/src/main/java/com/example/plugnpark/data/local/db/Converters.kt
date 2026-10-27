package com.example.plugnpark.data.local.db

import androidx.room.TypeConverter
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType

class Converters {
    @TypeConverter
    fun fromListingType(value: ListingType): String {
        return value.name
    }

    @TypeConverter
    fun toListingType(value: String): ListingType {
        return try {
            ListingType.valueOf(value)
        } catch (e: Exception) {
            ListingType.ALL
        }
    }

    @TypeConverter
    fun fromBookingStatus(value: BookingStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBookingStatus(value: String): BookingStatus {
        return try {
            BookingStatus.valueOf(value)
        } catch (e: Exception) {
            BookingStatus.CONFIRMED
        }
    }
}
