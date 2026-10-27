package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String = "",
    val isHost: Boolean = false,
    val rating: Float = 5.0f,
    val reviewCount: Int = 0
)
