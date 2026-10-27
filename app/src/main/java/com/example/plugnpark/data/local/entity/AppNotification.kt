package com.example.plugnpark.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_notifications",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class AppNotification(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String = "INFO",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
