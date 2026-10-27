package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String,
    val bookingId: String,
    val listingId: String,
    val reviewerId: String,
    val reviewerName: String,
    val rating: Float,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)
