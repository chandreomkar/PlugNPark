package com.example.plugnpark.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Review
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.data.repository.BookingResult
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.util.NotificationHelper
import com.example.plugnpark.util.TimeFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

import com.example.plugnpark.util.CompatibilityChecker
import com.example.plugnpark.util.CompatibilityResult

data class BookingUiState(
    val listing: Listing? = null,
    val host: User? = null,
    val defaultVehicle: Vehicle? = null,
    val hasNoVehicles: Boolean = false,
    val compatibilityResult: CompatibilityResult? = null,
    val isVehicleCompatible: Boolean = true,
    val compatibilityBadgeText: String? = null,
    val effectiveSpeedKw: Double = 0.0,
    val estimatedChargeTimeMins: Int = 0,
    val estimatedChargeCost: Double = 0.0,
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

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModel(
    private val repository: PlugNParkRepository,
    private val sessionManager: UserSessionManager
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
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getVehiclesByOwner(session.userId)
        },
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
        val vehicles = flows[1] as List<Vehicle>
        val dateOffset = flows[2] as Int
        val startHour = flows[3] as Int
        val duration = flows[4] as Int
        val payMethod = flows[5] as String
        val isProcessing = flows[6] as Boolean
        val isSuccess = flows[7] as Boolean
        val confirmed = flows[8] as Booking?
        val msg = flows[9] as String?

        var currentListing: Listing? = null
        var hostUser: User? = null
        var reviewsList = emptyList<Review>()
        var isOverlapping = false
        var overlapErr: String? = null
        var isCompatible = true
        var badgeText: String? = null
        var compatResult: CompatibilityResult? = null
        var effectiveSpeed = 0.0
        var estChargeMins = 0
        var estChargeCost = 0.0

        val defaultVeh = vehicles.firstOrNull { it.isDefault } ?: vehicles.firstOrNull()
        val hasNoVehicles = vehicles.isEmpty()

        if (id != null) {
            currentListing = repository.getListingById(id).first()
            if (currentListing != null) {
                hostUser = repository.getUserById(currentListing.hostId).first()
                reviewsList = repository.getReviewsForListing(id).first()

                // Check compatibility
                if (defaultVeh != null) {
                    compatResult = CompatibilityChecker.check(defaultVeh, currentListing)
                    isCompatible = compatResult.isUsable
                    badgeText = if (compatResult.isUsable) {
                        "✔ ${compatResult.reason}"
                    } else {
                        "⚠️ ${compatResult.reason}"
                    }

                    if (currentListing.type == ListingType.EV_CHARGER && defaultVeh.isEv) {
                        val chargerKw = if (currentListing.powerKw > 0) currentListing.powerKw else 7.4
                        val vehicleMaxAcKw = if (defaultVeh.maxAcChargeKw > 0) defaultVeh.maxAcChargeKw else 7.4
                        effectiveSpeed = kotlin.math.min(chargerKw, vehicleMaxAcKw)

                        // 20% to 80% SoC charge estimate (60% battery capacity)
                        val neededKwh = defaultVeh.batteryKwh * 0.60
                        val chargeHours = if (effectiveSpeed > 0) neededKwh / effectiveSpeed else 1.0
                        estChargeMins = (chargeHours * 60).toInt().coerceAtLeast(1)
                        estChargeCost = chargeHours * currentListing.pricePerHour
                    }
                } else {
                    isCompatible = true // Don't block if user has no vehicle
                    badgeText = "Add your vehicle in Profile for automatic compatibility check"
                }

                // Check overlap
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
            defaultVehicle = defaultVeh,
            hasNoVehicles = hasNoVehicles,
            compatibilityResult = compatResult,
            isVehicleCompatible = isCompatible,
            compatibilityBadgeText = badgeText,
            effectiveSpeedKw = effectiveSpeed,
            estimatedChargeTimeMins = estChargeMins,
            estimatedChargeCost = estChargeCost,
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
            delay(2000)

            val (startTime, endTime) = calculateStartEndMillis(
                currentState.selectedDateOffset,
                currentState.selectedStartHour,
                currentState.durationHours
            )

            val currentUserId = sessionManager.userSessionFlow.first().userId

            when (val result = repository.createBooking(
                listingId = listing.id,
                seekerId = currentUserId,
                startTime = startTime,
                endTime = endTime,
                pricePerHour = listing.pricePerHour
            )) {
                is BookingResult.Success -> {
                    val booking = result.booking
                    _confirmedBooking.value = booking
                    _isPaymentProcessing.value = false
                    _isPaymentSuccess.value = true

                    // System Notification
                    NotificationHelper.showBookingConfirmationNotification(
                        context = context,
                        bookingId = booking.id,
                        listingTitle = listing.title,
                        startTimeFormatted = TimeFormatter.formatDateTime(startTime)
                    )

                    // WorkManager Scheduled 15-Min Start Reminder
                    NotificationHelper.schedule15MinReminderNotification(
                        context = context,
                        bookingId = booking.id,
                        listingTitle = listing.title,
                        userId = currentUserId,
                        startTimeMillis = startTime
                    )

                    // Save Notification into Room AppNotification table
                    repository.sendNotification(
                        userId = currentUserId,
                        title = "Booking Confirmed! 🎉",
                        body = "Your slot at ${listing.title} is reserved for ${TimeFormatter.formatDateTime(startTime)}.",
                        type = "BOOKING"
                    )

                    onSuccess(booking.id)
                }
                is BookingResult.Error -> {
                    _isPaymentProcessing.value = false
                    _userMessage.value = result.message
                }
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
