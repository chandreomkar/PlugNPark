package com.example.plugnpark.data.local.db

import androidx.room.TypeConverter
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ConnectorType
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

    @TypeConverter
    fun fromConnectorType(type: ConnectorType): String = type.name

    @TypeConverter
    fun toConnectorType(name: String): ConnectorType {
        return try {
            ConnectorType.valueOf(name)
        } catch (e: Exception) {
            ConnectorType.NONE
        }
    }

    @TypeConverter
    fun fromConnectorTypeList(list: List<ConnectorType>): String = list.joinToString(",") { it.name }

    @TypeConverter
    fun toConnectorTypeList(data: String): List<ConnectorType> {
        if (data.isBlank()) return emptyList()
        return data.split(",").mapNotNull { 
            try { ConnectorType.valueOf(it) } catch (e: Exception) { null } 
        }
    }
}
