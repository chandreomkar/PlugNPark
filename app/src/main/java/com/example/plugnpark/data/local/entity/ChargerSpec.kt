package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "charger_specs",
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
data class ChargerSpec(
    @PrimaryKey
    val listingId: String,
    val connectorType: String,
    val powerKw: Double,
    val hasEarthing: Boolean = true,
    val hasMcb: Boolean = true,
    val isWeatherproof: Boolean = true
)
