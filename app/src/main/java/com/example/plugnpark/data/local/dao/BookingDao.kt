package com.example.plugnpark.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.domain.model.BookingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY startTime DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE seekerId = :seekerId ORDER BY startTime DESC")
    fun getBookingsBySeeker(seekerId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE listingId = :listingId ORDER BY startTime DESC")
    fun getBookingsByListing(listingId: String): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: String): Flow<Booking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<Booking>)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun getBookingCount(): Int
}
