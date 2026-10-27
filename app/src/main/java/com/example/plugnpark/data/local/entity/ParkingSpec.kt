package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parking_specs",
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
data class ParkingSpec(
    @PrimaryKey
    val listingId: String,
    val vehicleTypes: String,
    val isCovered: Boolean = true,
    val hasCctv: Boolean = true,
    val hasGuard: Boolean = false,
    val totalSlots: Int = 1,
    val accessNotes: String = ""
)
