package com.example.plugnpark

import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.util.BookingOverlapChecker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingOverlapCheckerTest {

    private fun createBooking(startHour: Long, endHour: Long, status: BookingStatus = BookingStatus.CONFIRMED): Booking {
        val hourMillis = 3600_000L
        return Booking(
            id = "test",
            listingId = "list_1",
            seekerId = "user_1",
            vehicleId = null,
            startTime = startHour * hourMillis,
            endTime = endHour * hourMillis,
            baseAmountPaise = 0,
            platformFeePaise = 0,
            gstPaise = 0,
            totalPaise = 0,
            hostEarningPaise = 0,
            status = status
        )
    }

    @Test
    fun overlapChecker_BackToBack_DoesNotOverlap() {
        val existing = listOf(
            createBooking(10, 12) // 10:00 to 12:00
        )
        
        // New starts exactly when old ends
        val hourMillis = 3600_000L
        val hasOverlap = BookingOverlapChecker.hasOverlap(12 * hourMillis, 14 * hourMillis, existing)
        
        assertFalse(hasOverlap)
    }

    @Test
    fun overlapChecker_PartialOverlap_ReturnsTrue() {
        val existing = listOf(
            createBooking(10, 12) // 10:00 to 12:00
        )
        
        val hourMillis = 3600_000L
        // 11:00 to 13:00 overlaps
        val hasOverlap = BookingOverlapChecker.hasOverlap(11 * hourMillis, 13 * hourMillis, existing)
        
        assertTrue(hasOverlap)
    }

    @Test
    fun overlapChecker_NestedOverlap_ReturnsTrue() {
        val existing = listOf(
            createBooking(10, 14) // 10:00 to 14:00
        )
        
        val hourMillis = 3600_000L
        // 11:00 to 12:00 overlaps inside
        val hasOverlap = BookingOverlapChecker.hasOverlap(11 * hourMillis, 12 * hourMillis, existing)
        
        assertTrue(hasOverlap)
    }

    @Test
    fun overlapChecker_CancelledBooking_Ignored() {
        val existing = listOf(
            createBooking(10, 12, BookingStatus.CANCELLED)
        )
        
        val hourMillis = 3600_000L
        // 11:00 to 13:00 overlaps mathematically but status is CANCELLED
        val hasOverlap = BookingOverlapChecker.hasOverlap(11 * hourMillis, 13 * hourMillis, existing)
        
        assertFalse(hasOverlap)
    }
}
