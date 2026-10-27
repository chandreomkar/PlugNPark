package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.plugnpark.domain.model.BookingStatus

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = Listing::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["seekerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("listingId", "startTime"),
        Index("seekerId"),
        Index("vehicleId")
    ]
)
data class Booking(
    @PrimaryKey
    val id: String,
    val listingId: String,
    val seekerId: String,
    val vehicleId: String? = null,
    val startTime: Long,
    val endTime: Long,
    val baseAmountPaise: Long,
    val platformFeePaise: Long,
    val gstPaise: Long = 0L,
    val totalPaise: Long,
    val hostEarningPaise: Long,
    val status: BookingStatus = BookingStatus.CONFIRMED,
    val paymentMethod: String = "UPI",
    val paymentStatus: String = "COMPLETED",
    val createdAt: Long = System.currentTimeMillis(),
    val cancelledAt: Long? = null,
    val refundPaise: Long = 0L
) {
    // Secondary constructor for convenience with Double values
    constructor(
        id: String,
        listingId: String,
        seekerId: String,
        startTime: Long,
        endTime: Long,
        totalAmount: Double,
        platformFee: Double,
        hostEarning: Double,
        status: BookingStatus = BookingStatus.CONFIRMED,
        createdAt: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        listingId = listingId,
        seekerId = seekerId,
        vehicleId = null,
        startTime = startTime,
        endTime = endTime,
        baseAmountPaise = ((totalAmount - platformFee) * 100).toLong(),
        platformFeePaise = (platformFee * 100).toLong(),
        gstPaise = ((platformFee * 0.18) * 100).toLong(),
        totalPaise = (totalAmount * 100).toLong(),
        hostEarningPaise = (hostEarning * 100).toLong(),
        status = status,
        paymentMethod = "UPI",
        paymentStatus = "COMPLETED",
        createdAt = createdAt,
        cancelledAt = if (status == BookingStatus.CANCELLED) System.currentTimeMillis() else null,
        refundPaise = if (status == BookingStatus.CANCELLED) (totalAmount * 100).toLong() else 0L
    )

    val totalAmount: Double get() = totalPaise / 100.0
    val platformFee: Double get() = platformFeePaise / 100.0
    val hostEarning: Double get() = hostEarningPaise / 100.0
}
