package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "availability_rules",
    foreignKeys = [
        ForeignKey(
            entity = Listing::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listingId")]
)
data class AvailabilityRule(
    @PrimaryKey
    val id: String,
    val listingId: String,
    val dayOfWeek: Int,
    val startMinute: Int = 360,
    val endMinute: Int = 1320
)
