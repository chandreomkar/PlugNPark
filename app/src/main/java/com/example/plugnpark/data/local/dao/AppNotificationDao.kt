package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.AppNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUser(userId: String): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}
