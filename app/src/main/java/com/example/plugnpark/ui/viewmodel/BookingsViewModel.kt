package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.BookingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookingWithListing(
    val booking: Booking,
    val listing: Listing?
)

data class BookingsUiState(
    val bookings: List<BookingWithListing> = emptyList(),
    val selectedTab: Int = 0, // 0 = Active/Upcoming, 1 = Past/Completed, 2 = Cancelled
    val selectedBookingForReview: BookingWithListing? = null,
    val userMessage: String? = null
)

class BookingsViewModel(
    private val repository: PlugNParkRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    private val _selectedBookingForReview = MutableStateFlow<BookingWithListing?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BookingsUiState> = combine(
        repository.getBookingsBySeeker("user_seeker_1"),
        repository.getAllActiveListings(),
        _selectedTab,
        _selectedBookingForReview,
        _userMessage
    ) { bookings, listings, tab, reviewBooking, msg ->

        val listingMap = listings.associateBy { it.id }
        val items = bookings.map { booking ->
            BookingWithListing(booking, listingMap[booking.listingId])
        }

        val filteredItems = items.filter { item ->
            when (tab) {
                0 -> item.booking.status == BookingStatus.CONFIRMED
                1 -> item.booking.status == BookingStatus.COMPLETED
                2 -> item.booking.status == BookingStatus.CANCELLED
                else -> true
            }
        }

        BookingsUiState(
            bookings = filteredItems,
            selectedTab = tab,
            selectedBookingForReview = reviewBooking,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookingsUiState()
    )

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun canCancelBooking(booking: Booking): Boolean {
        if (booking.status != BookingStatus.CONFIRMED) return false
        val oneHourMillis = 3600_000L
        val timeUntilStart = booking.startTime - System.currentTimeMillis()
        return timeUntilStart > oneHourMillis
    }

    fun cancelBookingWithRefund(booking: Booking) {
        viewModelScope.launch {
            try {
                if (canCancelBooking(booking)) {
                    repository.cancelBooking(booking.id)
                    _userMessage.value = "Booking cancelled! Full refund of ₹${booking.totalAmount.toInt()} initiated to original payment method."
                } else {
                    _userMessage.value = "Cancellation failed: Cancellations are only permitted >1 hour before start time."
                }
            } catch (e: Exception) {
                _userMessage.value = "Error cancelling booking: ${e.message}"
            }
        }
    }

    fun openReviewDialog(item: BookingWithListing) {
        _selectedBookingForReview.value = item
    }

    fun closeReviewDialog() {
        _selectedBookingForReview.value = null
    }

    fun submitReview(rating: Float, comment: String) {
        val item = _selectedBookingForReview.value ?: return
        viewModelScope.launch {
            try {
                repository.submitReviewAndUpdateListing(
                    bookingId = item.booking.id,
                    listingId = item.booking.listingId,
                    reviewerId = "user_seeker_1",
                    reviewerName = "Rahul Sharma",
                    rating = rating,
                    comment = comment
                )
                _selectedBookingForReview.value = null
                _userMessage.value = "Thank you! Review submitted and rating updated."
            } catch (e: Exception) {
                _userMessage.value = "Failed to submit review: ${e.message}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
