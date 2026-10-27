package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.plugnpark.domain.model.ListingType

@Entity(
    tableName = "listings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["hostId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("type", "city"),
        Index("hostId")
    ]
)
data class Listing(
    @PrimaryKey
    val id: String,
    val hostId: String,
    val type: ListingType,
    val title: String,
    val description: String,
    val address: String,
    val city: String,
    val area: String,
    val lat: Double,
    val lng: Double,
    val pricePerHourPaise: Long,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val instantBook: Boolean = true,
    val avgRating: Float = 5.0f,
    val ratingCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),

    @Ignore val connectorType: String = "Type 2",
    @Ignore val powerKw: Double = 7.4,
    @Ignore val isCovered: Boolean = true,
    @Ignore val hasCCTV: Boolean = true,
    @Ignore val vehicleType: String = "4-Wheeler",
    @Ignore val imageUrl: String = "",
    @Ignore val availableFrom: String = "06:00",
    @Ignore val availableTo: String = "22:00"
) {
    // Primary DB constructor used by Room
    constructor(
        id: String,
        hostId: String,
        type: ListingType,
        title: String,
        description: String,
        address: String,
        city: String,
        area: String,
        lat: Double,
        lng: Double,
        pricePerHourPaise: Long,
        isVerified: Boolean,
        isActive: Boolean,
        instantBook: Boolean,
        avgRating: Float,
        ratingCount: Int,
        createdAt: Long
    ) : this(
        id = id,
        hostId = hostId,
        type = type,
        title = title,
        description = description,
        address = address,
        city = city,
        area = area,
        lat = lat,
        lng = lng,
        pricePerHourPaise = pricePerHourPaise,
        isVerified = isVerified,
        isActive = isActive,
        instantBook = instantBook,
        avgRating = avgRating,
        ratingCount = ratingCount,
        createdAt = createdAt,
        connectorType = "Type 2",
        powerKw = 7.4,
        isCovered = true,
        hasCCTV = true,
        vehicleType = "4-Wheeler",
        imageUrl = "",
        availableFrom = "06:00",
        availableTo = "22:00"
    )

    // Secondary constructor accepting pricePerHour: Double
    constructor(
        id: String,
        type: ListingType,
        title: String,
        description: String,
        address: String,
        city: String,
        area: String,
        lat: Double,
        lng: Double,
        pricePerHour: Double,
        rating: Float = 5.0f,
        ratingCount: Int = 0,
        isVerified: Boolean = false,
        availableFrom: String = "06:00",
        availableTo: String = "22:00",
        isActive: Boolean = true,
        hostId: String,
        connectorType: String = "Type 2",
        powerKw: Double = 7.4,
        isCovered: Boolean = true,
        hasCCTV: Boolean = true,
        vehicleType: String = "4-Wheeler",
        imageUrl: String = ""
    ) : this(
        id = id,
        hostId = hostId,
        type = type,
        title = title,
        description = description,
        address = address,
        city = city,
        area = area,
        lat = lat,
        lng = lng,
        pricePerHourPaise = (pricePerHour * 100).toLong(),
        isVerified = isVerified,
        isActive = isActive,
        instantBook = true,
        avgRating = rating,
        ratingCount = ratingCount,
        createdAt = System.currentTimeMillis(),
        connectorType = connectorType,
        powerKw = powerKw,
        isCovered = isCovered,
        hasCCTV = hasCCTV,
        vehicleType = vehicleType,
        imageUrl = imageUrl,
        availableFrom = availableFrom,
        availableTo = availableTo
    )

    val pricePerHour: Double get() = pricePerHourPaise / 100.0
    val rating: Float get() = avgRating
}
