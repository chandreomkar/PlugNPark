package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.plugnpark.domain.model.BookingStatus

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey
    val id: String,
    val listingId: String,
    val seekerId: String,
    val startTime: Long,
    val endTime: Long,
    val totalAmount: Double,
    val platformFee: Double,
    val hostEarning: Double,
    val status: BookingStatus,
    val createdAt: Long = System.currentTimeMillis()
)
