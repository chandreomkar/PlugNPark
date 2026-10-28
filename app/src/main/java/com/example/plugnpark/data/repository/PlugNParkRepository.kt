package com.example.plugnpark.data.repository

import androidx.room.withTransaction
import com.example.plugnpark.data.local.db.PlugNParkDatabase
import com.example.plugnpark.data.local.entity.AvailabilityRule
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Locale
import java.util.UUID

sealed class BookingResult {
    data class Success(val booking: Booking) : BookingResult()
    data class Error(val message: String) : BookingResult()
}

class PlugNParkRepository(
    private val database: PlugNParkDatabase
) {
    private val userDao = database.userDao()
    private val listingDao = database.listingDao()
    private val bookingDao = database.bookingDao()
    private val reviewDao = database.reviewDao()
    private val availabilityRuleDao = database.availabilityRuleDao()
    private val favoriteDao = database.favoriteDao()
    private val vehicleDao = database.vehicleDao()
    private val listingPhotoDao = database.listingPhotoDao()
    private val appNotificationDao = database.appNotificationDao()

    // VEHICLES
    fun getVehiclesByOwner(ownerId: String): Flow<List<Vehicle>> = vehicleDao.getVehiclesByOwner(ownerId)

    suspend fun insertVehicle(vehicle: Vehicle) {
        if (vehicle.isDefault) {
            vehicleDao.clearDefaultVehicle(vehicle.ownerId)
        }
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun deleteVehicle(id: String) {
        vehicleDao.deleteVehicleById(id)
    }

    suspend fun setDefaultVehicle(ownerId: String, vehicleId: String) {
        database.withTransaction {
            vehicleDao.clearDefaultVehicle(ownerId)
            val vehicle = vehicleDao.getVehicleById(vehicleId).first()
            if (vehicle != null) {
                vehicleDao.insertVehicle(vehicle.copy(isDefault = true))
            }
        }
    }

    // FAVORITES
    fun getFavoriteListingIds(userId: String): Flow<List<String>> =
        favoriteDao.getFavoritesByUser(userId).map { favorites -> favorites.map { it.listingId } }

    suspend fun toggleFavorite(userId: String, listingId: String) {
        val current = favoriteDao.getFavoritesByUser(userId).first()
        val exists = current.any { it.listingId == listingId }
        if (exists) {
            favoriteDao.deleteFavorite(userId, listingId)
        } else {
            favoriteDao.insertFavorite(com.example.plugnpark.data.local.entity.Favorite(userId = userId, listingId = listingId))
        }
    }

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

    suspend fun deleteListing(listingId: String) {
        database.withTransaction {
            listingPhotoDao.deletePhotosByListing(listingId)
            availabilityRuleDao.deleteRulesByListing(listingId)
            listingDao.deleteListingById(listingId)
        }
    }

    suspend fun sendNotification(userId: String, title: String, body: String, type: String = "BOOKING") {
        val notif = com.example.plugnpark.data.local.entity.AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            title = title,
            body = body,
            type = type,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        appNotificationDao.insertNotification(notif)
    }

    fun getNotificationsByUser(userId: String): Flow<List<com.example.plugnpark.data.local.entity.AppNotification>> =
        appNotificationDao.getNotificationsByUser(userId)

    suspend fun markNotificationAsRead(id: String) {
        appNotificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        appNotificationDao.markAllAsRead(userId)
    }

    suspend fun saveListingWithPhotosAndRules(
        listing: Listing,
        photos: List<String>,
        rules: List<AvailabilityRule>
    ) {
        database.withTransaction {
            listingDao.insertListing(listing)

            if (photos.isNotEmpty()) {
                listingPhotoDao.deletePhotosByListing(listing.id)
                val photoEntities = photos.mapIndexed { idx, url ->
                    com.example.plugnpark.data.local.entity.ListingPhoto(
                        id = "photo_${listing.id}_${idx + 1}",
                        listingId = listing.id,
                        url = url,
                        sortOrder = idx
                    )
                }
                listingPhotoDao.insertPhotos(photoEntities)
            }

            if (rules.isNotEmpty()) {
                availabilityRuleDao.deleteRulesByListing(listing.id)
                availabilityRuleDao.insertRules(rules)
            }
        }
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
    ): BookingResult {
        return database.withTransaction {
            val now = System.currentTimeMillis()

            // 1. Reject start times in the past
            if (startTime < now - 120_000L) {
                return@withTransaction BookingResult.Error("Cannot book a time slot in the past.")
            }

            // 2. Re-check overlap for CONFIRMED/active bookings
            val existingBookings = bookingDao.getBookingsByListing(listingId).first()
            val overlap = com.example.plugnpark.util.BookingOverlapChecker.hasOverlap(startTime, endTime, existingBookings)
            if (overlap) {
                return@withTransaction BookingResult.Error("Selected time slot is already booked for this spot.")
            }

            // 3. Reject times outside listing's AvailabilityRules
            val rules = availabilityRuleDao.getRulesForListing(listingId).first()
            if (rules.isNotEmpty()) {
                val cal = Calendar.getInstance().apply { timeInMillis = startTime }
                val rawDay = cal.get(Calendar.DAY_OF_WEEK)
                val dayOfWeek = if (rawDay == Calendar.SUNDAY) 7 else rawDay - 1

                val startMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                val endCal = Calendar.getInstance().apply { timeInMillis = endTime }
                val endMin = if (endCal.get(Calendar.DAY_OF_YEAR) != cal.get(Calendar.DAY_OF_YEAR)) {
                    1439
                } else {
                    endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)
                }

                val ruleForDay = rules.find { it.dayOfWeek == dayOfWeek }
                if (ruleForDay != null) {
                    if (startMin < ruleForDay.startMinute || endMin > ruleForDay.endMinute) {
                        val fromStr = String.format(Locale.getDefault(), "%02d:%02d", ruleForDay.startMinute / 60, ruleForDay.startMinute % 60)
                        val toStr = String.format(Locale.getDefault(), "%02d:%02d", ruleForDay.endMinute / 60, ruleForDay.endMinute % 60)
                        return@withTransaction BookingResult.Error("Spot is only available between $fromStr and $toStr.")
                    }
                }
            }

            // Valid: Create single Booking object with exact GST calculation
            val priceBreakdown = com.example.plugnpark.util.PricingCalculator.calculatePrice(pricePerHour, startTime, endTime)

            val booking = Booking(
                id = "PNP-" + UUID.randomUUID().toString().take(6).uppercase(),
                listingId = listingId,
                seekerId = seekerId,
                startTime = startTime,
                endTime = endTime,
                totalAmount = priceBreakdown.totalAmount,
                platformFee = priceBreakdown.platformFee,
                hostEarning = priceBreakdown.hostEarning,
                status = BookingStatus.CONFIRMED,
                createdAt = now
            )

            bookingDao.insertBooking(booking)
            BookingResult.Success(booking)
        }
    }

    suspend fun cancelBooking(bookingId: String, context: android.content.Context? = null) {
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
        if (context != null) {
            com.example.plugnpark.util.NotificationHelper.cancelReminder(context, bookingId)
        }
    }

    // USERS
    fun getUserById(id: String): Flow<User?> = userDao.getUserById(id)

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getOrCreateUserByPhone(phone: String, role: String): User {
        val existing = userDao.getUserByPhone(phone)
        if (existing != null) {
            val updated = existing.copy(role = role)
            userDao.insertUser(updated)
            return updated
        }
        val newUser = User(
            id = "user_" + UUID.randomUUID().toString().take(8),
            name = "User ${phone.takeLast(4)}",
            phone = phone,
            email = "user${phone.takeLast(4)}@plugnpark.in",
            role = role,
            walletBalancePaise = 100000L
        )
        userDao.insertUser(newUser)
        return newUser
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
                avgRating = String.format(Locale.US, "%.1f", newAvg).toFloat(),
                ratingCount = newCount
            )
            listingDao.updateListing(updated)
        }
    }
}
