package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.plugnpark.domain.model.ListingType

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey
    val id: String,
    val type: ListingType,
    val title: String,
    val description: String,
    val address: String,
    val city: String,
    val area: String,
    val lat: Double,
    val lng: Double,
    val pricePerHour: Double,
    val rating: Float,
    val ratingCount: Int,
    val isVerified: Boolean,
    val availableFrom: String,
    val availableTo: String,
    val isActive: Boolean,
    val hostId: String,
    val connectorType: String,
    val powerKw: Double,
    val isCovered: Boolean,
    val hasCCTV: Boolean,
    val vehicleType: String,
    val imageUrl: String = ""
)
