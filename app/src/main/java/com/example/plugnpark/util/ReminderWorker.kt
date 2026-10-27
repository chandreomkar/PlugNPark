package com.example.plugnpark.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.plugnpark.data.local.db.PlugNParkDatabase
import com.example.plugnpark.data.local.entity.AppNotification
import java.util.UUID

class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val bookingId = inputData.getString("booking_id") ?: return Result.failure()
        val listingTitle = inputData.getString("listing_title") ?: "Your Reserved Spot"
        val userId = inputData.getString("user_id") ?: "user_seeker_1"

        // Show System Notification
        NotificationHelper.show15MinReminderNotification(appContext, bookingId, listingTitle)

        // Save Notification to Room AppNotification table
        try {
            val db = PlugNParkDatabase.getDatabase(appContext)
            val notif = AppNotification(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = userId,
                title = "Booking Starting Soon ⏰",
                body = "Your reservation at $listingTitle starts in 15 minutes!",
                type = "CHARGING",
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            db.appNotificationDao().insertNotification(notif)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.success()
    }
}
