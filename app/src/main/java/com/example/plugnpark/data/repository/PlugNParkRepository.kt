package com.example.plugnpark.data.repository

import com.example.plugnpark.data.local.dao.BookingDao
import com.example.plugnpark.data.local.dao.ListingDao
import com.example.plugnpark.data.local.dao.ReviewDao
import com.example.plugnpark.data.local.dao.UserDao
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class PlugNParkRepository(
    private val userDao: UserDao,
    private val listingDao: ListingDao,
    private val bookingDao: BookingDao,
    private val reviewDao: ReviewDao
) {
    // LISTINGS
    fun getAllActiveListings(): Flow<List<Listing>> = listingDao.getAllActiveListings()

    fun getListingById(id: String): Flow<Listing?> = listingDao.getListingById(id)

    fun getListingsByType(type: ListingType): Flow<List<Listing>> = listingDao.getListingsByType(type)

    fun getListingsByCity(city: String): Flow<List<Listing>> = listingDao.getListingsByCity(city)

    fun getListingsByHostId(hostId: String): Flow<List<Listing>> = listingDao.getListingsByHostId(hostId)

    suspend fun insertListing(listing: Listing) {
        listingDao.insertListing(listing)
    }

    suspend fun updateListing(listing: Listing) {
        listingDao.updateListing(listing)
    }

    // BOOKINGS
    fun getAllBookings(): Flow<List<Booking>> = bookingDao.getAllBookings()

    fun getBookingsBySeeker(seekerId: String): Flow<List<Booking>> = bookingDao.getBookingsBySeeker(seekerId)

    fun getBookingsByListing(listingId: String): Flow<List<Booking>> = bookingDao.getBookingsByListing(listingId)

    fun getBookingById(id: String): Flow<Booking?> = bookingDao.getBookingById(id)

    suspend fun createBooking(
        listingId: String,
        seekerId: String,
        startTime: Long,
        endTime: Long,
        pricePerHour: Double
    ): Booking {
        val hours = kotlin.math.max(1.0, (endTime - startTime).toDouble() / (1000 * 3600))
        val rawTotal = pricePerHour * hours
        val platformFee = rawTotal * 0.10 // 10% platform fee
        val totalAmount = rawTotal + platformFee
        val hostEarning = rawTotal

        val booking = Booking(
            id = "book_" + UUID.randomUUID().toString().take(8),
            listingId = listingId,
            seekerId = seekerId,
            startTime = startTime,
            endTime = endTime,
            totalAmount = totalAmount,
            platformFee = platformFee,
            hostEarning = hostEarning,
            status = BookingStatus.CONFIRMED,
            createdAt = System.currentTimeMillis()
        )

        bookingDao.insertBooking(booking)
        return booking
    }

    suspend fun cancelBooking(bookingId: String) {
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
    }

    // USERS
    fun getUserById(id: String): Flow<User?> = userDao.getUserById(id)

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    // REVIEWS
    fun getReviewsForListing(listingId: String): Flow<List<Review>> = reviewDao.getReviewsForListing(listingId)

    suspend fun insertReview(review: Review) {
        reviewDao.insertReview(review)
    }

    suspend fun submitReviewAndUpdateListing(
        bookingId: String,
        listingId: String,
        reviewerId: String,
        reviewerName: String,
        rating: Float,
        comment: String
    ) {
        val review = Review(
            id = "rev_" + UUID.randomUUID().toString().take(8),
            bookingId = bookingId,
            listingId = listingId,
            reviewerId = reviewerId,
            reviewerName = reviewerName,
            rating = rating,
            comment = comment,
            createdAt = System.currentTimeMillis()
        )
        reviewDao.insertReview(review)

        val existingListing = listingDao.getListingById(listingId).first()
        if (existingListing != null) {
            val newCount = existingListing.ratingCount + 1
            val newAvg = ((existingListing.rating * existingListing.ratingCount) + rating) / newCount
            val updated = existingListing.copy(
                rating = String.format(java.util.Locale.US, "%.1f", newAvg).toFloat(),
                ratingCount = newCount
            )
            listingDao.updateListing(updated)
        }
    }
}
