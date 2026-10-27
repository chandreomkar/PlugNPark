package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = Booking::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Listing::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["reviewerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bookingId", unique = true),
        Index("listingId"),
        Index("reviewerId")
    ]
)
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
