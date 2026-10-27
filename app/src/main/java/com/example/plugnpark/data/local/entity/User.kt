package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val avatarUrl: String = "",
    val role: String = "SEEKER",
    val walletBalancePaise: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isHost: Boolean get() = role == "HOST" || role == "BOTH"
    val rating: Float get() = 5.0f
    val reviewCount: Int get() = 0
}
