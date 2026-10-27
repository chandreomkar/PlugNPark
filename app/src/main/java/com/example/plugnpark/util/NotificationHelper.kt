package com.example.plugnpark.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

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

    fun schedule15MinReminderNotification(
        context: Context,
        bookingId: String,
        listingTitle: String,
        startTimeMillis: Long
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
}
