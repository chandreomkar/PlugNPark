package com.example.plugnpark.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private const val CHANNEL_ID = "plugnpark_bookings_channel"
    private const val CHANNEL_NAME = "PlugNPark Booking Updates"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for EV charger and parking bookings"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showBookingConfirmationNotification(
        context: Context,
        bookingId: String,
        listingTitle: String,
        startTimeFormatted: String
    ) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Booking Confirmed! 🎉")
            .setContentText("Spot booked: $listingTitle ($startTimeFormatted). ID: $bookingId")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(bookingId.hashCode(), builder.build())
    }

    fun show15MinReminderNotification(
        context: Context,
        bookingId: String,
        listingTitle: String
    ) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Booking Starting Soon ⏰")
            .setContentText("Your slot at $listingTitle starts in 15 minutes!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify((bookingId + "_reminder").hashCode(), builder.build())
    }

    fun schedule15MinReminderNotification(
        context: Context,
        bookingId: String,
        listingTitle: String,
        userId: String,
        startTimeMillis: Long
    ) {
        val now = System.currentTimeMillis()
        val reminderTime = startTimeMillis - (15 * 60 * 1000L) // 15 mins before
        val delayMillis = reminderTime - now

        if (delayMillis > 0) {
            val inputData = Data.Builder()
                .putString("booking_id", bookingId)
                .putString("listing_title", listingTitle)
                .putString("user_id", userId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_$bookingId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    fun cancelReminder(context: Context, bookingId: String) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_$bookingId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
