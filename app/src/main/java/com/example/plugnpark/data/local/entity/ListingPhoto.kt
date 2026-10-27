package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listing_photos",
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
data class ListingPhoto(
    @PrimaryKey
    val id: String,
    val listingId: String,
    val url: String,
    val sortOrder: Int = 0
)
