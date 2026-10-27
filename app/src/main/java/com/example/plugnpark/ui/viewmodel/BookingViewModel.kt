package com.example.plugnpark.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.util.NotificationHelper
import com.example.plugnpark.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class BookingUiState(
    val listing: Listing? = null,
    val host: User? = null,
    val reviews: List<Review> = emptyList(),
    val selectedDateOffset: Int = 0, // 0 = Today, 1 = Tomorrow, 2 = Day after
    val selectedStartHour: Int = 10, // 10 AM default
    val durationHours: Int = 2,
    val isSlotOverlapping: Boolean = false,
    val overlapErrorMessage: String? = null,
    val paymentMethod: String = "UPI", // "UPI", "CARD", "WALLET"
    val isPaymentProcessing: Boolean = false,
    val isPaymentSuccess: Boolean = false,
    val confirmedBooking: Booking? = null,
    val userMessage: String? = null
)

class BookingViewModel(
    private val repository: PlugNParkRepository
) : ViewModel() {

    private val _listingId = MutableStateFlow<String?>(null)
    private val _selectedDateOffset = MutableStateFlow(0)
    private val _selectedStartHour = MutableStateFlow(10)
    private val _durationHours = MutableStateFlow(2)
    private val _paymentMethod = MutableStateFlow("UPI")
    private val _isPaymentProcessing = MutableStateFlow(false)
    private val _isPaymentSuccess = MutableStateFlow(false)
    private val _confirmedBooking = MutableStateFlow<Booking?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    fun setListingId(id: String) {
        _listingId.value = id
    }

    val uiState: StateFlow<BookingUiState> = combine(
        _listingId,
        _selectedDateOffset,
        _selectedStartHour,
        _durationHours,
        _paymentMethod,
        _isPaymentProcessing,
        _isPaymentSuccess,
        _confirmedBooking,
        _userMessage
    ) { flows ->
        val id = flows[0] as String?
        val dateOffset = flows[1] as Int
        val startHour = flows[2] as Int
        val duration = flows[3] as Int
        val payMethod = flows[4] as String
        val isProcessing = flows[5] as Boolean
        val isSuccess = flows[6] as Boolean
        val confirmed = flows[7] as Booking?
        val msg = flows[8] as String?

        var currentListing: Listing? = null
        var hostUser: User? = null
        var reviewsList = emptyList<Review>()
        var isOverlapping = false
        var overlapErr: String? = null

        if (id != null) {
            currentListing = repository.getListingById(id).first()
            if (currentListing != null) {
                hostUser = repository.getUserById(currentListing.hostId).first()
                reviewsList = repository.getReviewsForListing(id).first()

                // Check overlap against existing Room bookings
                val existingBookings = repository.getBookingsByListing(id).first()
                val (newStart, newEnd) = calculateStartEndMillis(dateOffset, startHour, duration)

                val overlap = existingBookings.any { b ->
                    b.status == BookingStatus.CONFIRMED &&
                            (newStart < b.endTime && newEnd > b.startTime)
                }

                if (overlap) {
                    isOverlapping = true
                    overlapErr = "Selected time slot is already booked for this spot. Please select a different date or time."
                }
            }
        }

        BookingUiState(
            listing = currentListing,
            host = hostUser,
            reviews = reviewsList,
            selectedDateOffset = dateOffset,
            selectedStartHour = startHour,
            durationHours = duration,
            isSlotOverlapping = isOverlapping,
            overlapErrorMessage = overlapErr,
            paymentMethod = payMethod,
            isPaymentProcessing = isProcessing,
            isPaymentSuccess = isSuccess,
            confirmedBooking = confirmed,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookingUiState()
    )

    private fun calculateStartEndMillis(dateOffset: Int, startHour: Int, duration: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dateOffset)
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = startTime + (duration * 3600_000L)
        return Pair(startTime, endTime)
    }

    fun setDateOffset(offset: Int) {
        _selectedDateOffset.value = offset
    }

    fun setStartHour(hour: Int) {
        _selectedStartHour.value = hour
    }

    fun setDuration(hours: Int) {
        _durationHours.value = hours
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun processPaymentAndConfirm(
        context: Context,
        onSuccess: (bookingId: String) -> Unit
    ) {
        val currentState = uiState.value
        val listing = currentState.listing ?: return
        if (currentState.isSlotOverlapping) return

        viewModelScope.launch {
            _isPaymentProcessing.value = true
            delay(2000) // 2-second fake processing state

            val (startTime, endTime) = calculateStartEndMillis(
                currentState.selectedDateOffset,
                currentState.selectedStartHour,
                currentState.durationHours
            )

            val baseAmount = listing.pricePerHour * currentState.durationHours
            val platformFee = baseAmount * 0.10
            val gst = platformFee * 0.18
            val totalAmount = baseAmount + platformFee + gst
            val hostEarning = baseAmount

            val booking = Booking(
                id = "PNP-" + UUID.randomUUID().toString().take(6).uppercase(),
                listingId = listing.id,
                seekerId = "user_seeker_1",
                startTime = startTime,
                endTime = endTime,
                totalAmount = totalAmount,
                platformFee = platformFee,
                hostEarning = hostEarning,
                status = BookingStatus.CONFIRMED,
                createdAt = System.currentTimeMillis()
            )

            // Save in Room
            repository.createBooking(
                listingId = listing.id,
                seekerId = "user_seeker_1",
                startTime = startTime,
                endTime = endTime,
                pricePerHour = listing.pricePerHour
            )

            _confirmedBooking.value = booking
            _isPaymentProcessing.value = false
            _isPaymentSuccess.value = true

            // Trigger Notifications
            NotificationHelper.showBookingConfirmationNotification(
                context = context,
                bookingId = booking.id,
                listingTitle = listing.title,
                startTimeFormatted = TimeFormatter.formatDateTime(startTime)
            )

            NotificationHelper.schedule15MinReminderNotification(
                context = context,
                bookingId = booking.id,
                listingTitle = listing.title,
                startTimeMillis = startTime
            )

            onSuccess(booking.id)
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
