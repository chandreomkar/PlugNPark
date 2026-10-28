package com.example.plugnpark.util

import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.domain.model.BookingStatus

object BookingOverlapChecker {

    /**
     * Returns true if [newStart] and [newEnd] overlap with any confirmed booking in [existingBookings].
     * Back-to-back bookings (e.g., A ends at 10:00, B starts at 10:00) do NOT overlap.
     */
    fun hasOverlap(newStart: Long, newEnd: Long, existingBookings: List<Booking>): Boolean {
        return existingBookings.any { b ->
            b.status == BookingStatus.CONFIRMED && (newStart < b.endTime && newEnd > b.startTime)
        }
    }
}
