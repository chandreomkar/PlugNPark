package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId")]
)
data class Vehicle(
    @PrimaryKey
    val id: String,
    val ownerId: String,
    val make: String,
    val model: String,
    val vehicleType: String = "CAR", // BIKE, CAR, SUV
    val isEv: Boolean = true,
    val connectorType: String = "Type 2",
    val batteryKwh: Double = 40.0,
    val plateNumber: String = "",
    val isDefault: Boolean = false
)
