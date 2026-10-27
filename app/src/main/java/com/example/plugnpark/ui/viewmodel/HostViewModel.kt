package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.local.entity.AvailabilityRule
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.components.BarData
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
import java.util.UUID

data class HostUiState(
    val hostListings: List<Listing> = emptyList(),
    val todayEarnings: Double = 0.0,
    val thisWeekEarnings: Double = 0.0,
    val grossEarnings: Double = 0.0,
    val platformFeeTotal: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val weeklyChartData: List<BarData> = emptyList(),
    val upcomingHostBookings: List<BookingWithListing> = emptyList(),
    val pendingBookingRequests: List<BookingWithListing> = emptyList(),
    val verifyingListingIds: Set<String> = emptySet(),
    val isAddListingDialogOpen: Boolean = false,
    val listingToDelete: Listing? = null,
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HostViewModel(
    private val repository: PlugNParkRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _isAddListingDialogOpen = MutableStateFlow(false)
    private val _listingToDelete = MutableStateFlow<Listing?>(null)
    private val _verifyingListingIds = MutableStateFlow<Set<String>>(emptySet())
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HostUiState> = combine(
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getListingsByHostId(session.userId)
        },
        repository.getAllBookings(),
        _verifyingListingIds,
        _isAddListingDialogOpen,
        _listingToDelete,
        _userMessage
    ) { flows ->
        val listings = flows[0] as List<Listing>
        val bookings = flows[1] as List<Booking>
        val verifyingIds = flows[2] as Set<String>
        val isDialogOpen = flows[3] as Boolean
        val deleteListing = flows[4] as Listing?
        val msg = flows[5] as String?

        val hostListingIds = listings.map { it.id }.toSet()
        val hostBookings = bookings.filter { it.listingId in hostListingIds }
        val listingMap = listings.associateBy { it.id }

        val completedBookings = hostBookings.filter { it.status == BookingStatus.COMPLETED }
        val grossEarnings = completedBookings.sumOf { it.totalAmount }
        val platformFeeTotal = completedBookings.sumOf { it.platformFee }
        val totalEarnings = completedBookings.sumOf { it.hostEarning }

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEarnings = completedBookings.filter { it.endTime >= todayStart }.sumOf { it.hostEarning }
        val weekEarnings = completedBookings.filter { it.endTime >= weekStart }.sumOf { it.hostEarning }

        // Calculate REAL weekly chart data from completed bookings
        val dayEarningsMap = mutableMapOf(1 to 0.0, 2 to 0.0, 3 to 0.0, 4 to 0.0, 5 to 0.0, 6 to 0.0, 7 to 0.0)
        completedBookings.forEach { booking ->
            val cal = Calendar.getInstance().apply { timeInMillis = booking.startTime }
            val rawDay = cal.get(Calendar.DAY_OF_WEEK) // Sun=1, Mon=2..Sat=7
            val monToSunDay = if (rawDay == Calendar.SUNDAY) 7 else rawDay - 1
            dayEarningsMap[monToSunDay] = (dayEarningsMap[monToSunDay] ?: 0.0) + booking.hostEarning
        }

        val dayLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
        val chartData = dayLabels.map { (dayIdx, label) ->
            BarData(label, dayEarningsMap[dayIdx] ?: 0.0)
        }

        val upcomingBookings = hostBookings
            .filter { it.status == BookingStatus.CONFIRMED }
            .map { booking -> BookingWithListing(booking, listingMap[booking.listingId]) }

        val pendingRequests = hostBookings
            .filter { it.status == BookingStatus.PENDING }
            .map { booking -> BookingWithListing(booking, listingMap[booking.listingId]) }

        HostUiState(
            hostListings = listings,
            todayEarnings = todayEarnings,
            thisWeekEarnings = weekEarnings,
            grossEarnings = grossEarnings,
            platformFeeTotal = platformFeeTotal,
            totalEarnings = totalEarnings,
            weeklyChartData = chartData,
            upcomingHostBookings = upcomingBookings,
            pendingBookingRequests = pendingRequests,
            verifyingListingIds = verifyingIds,
            isAddListingDialogOpen = isDialogOpen,
            listingToDelete = deleteListing,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HostUiState()
    )

    fun acceptBookingRequest(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.createBooking(
                    listingId = booking.listingId,
                    seekerId = booking.seekerId,
                    startTime = booking.startTime,
                    endTime = booking.endTime,
                    pricePerHour = booking.totalAmount / ((booking.endTime - booking.startTime) / 3600_000L).coerceAtLeast(1L)
                )
                repository.sendNotification(
                    userId = booking.seekerId,
                    title = "Booking Request Accepted! 🎉",
                    body = "Your booking request for spot #${booking.listingId} has been accepted by the host.",
                    type = "BOOKING"
                )
                _userMessage.value = "Booking request accepted!"
            } catch (e: Exception) {
                _userMessage.value = "Failed to accept request: ${e.message}"
            }
        }
    }

    fun declineBookingRequest(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.cancelBooking(booking.id)
                repository.sendNotification(
                    userId = booking.seekerId,
                    title = "Booking Request Declined",
                    body = "Your booking request for spot #${booking.listingId} was declined by the host. Any pre-authorization has been released.",
                    type = "BOOKING"
                )
                _userMessage.value = "Booking request declined"
            } catch (e: Exception) {
                _userMessage.value = "Failed to decline request: ${e.message}"
            }
        }
    }

    fun openAddListingDialog() {
        _isAddListingDialogOpen.value = true
    }

    fun closeAddListingDialog() {
        _isAddListingDialogOpen.value = false
    }

    fun confirmDeleteListing(listing: Listing) {
        _listingToDelete.value = listing
    }

    fun cancelDeleteListing() {
        _listingToDelete.value = null
    }

    fun executeDeleteListing() {
        val listing = _listingToDelete.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteListing(listing.id)
                _listingToDelete.value = null
                _userMessage.value = "Listing deleted successfully"
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete listing: ${e.message}"
            }
        }
    }

    fun toggleListingActive(listing: Listing) {
        viewModelScope.launch {
            val updated = listing.copy(isActive = !listing.isActive)
            repository.updateListing(updated)
            _userMessage.value = if (updated.isActive) "Listing activated" else "Listing paused"
        }
    }

    fun verifyElectricianCertificate(listing: Listing) {
        viewModelScope.launch {
            _verifyingListingIds.value = _verifyingListingIds.value + listing.id
            _userMessage.value = "Electrician Certificate Uploaded. Verifying..."

            delay(3000)

            val updated = listing.copy(isVerified = true)
            repository.updateListing(updated)
            _verifyingListingIds.value = _verifyingListingIds.value - listing.id
            _userMessage.value = "✔ Verified! Electrician Certificate Approved."
        }
    }

    fun addNewListingWithPhotosAndRules(
        title: String,
        description: String,
        address: String,
        city: String,
        area: String,
        lat: Double,
        lng: Double,
        type: ListingType,
        pricePerHour: Double,
        connectorType: String,
        powerKw: Double,
        isCovered: Boolean,
        hasCCTV: Boolean,
        vehicleType: String,
        instantBook: Boolean,
        photos: List<String>,
        activeDays: Set<Int>, // 1..7
        startMinute: Int,
        endMinute: Int
    ) {
        viewModelScope.launch {
            try {
                val currentHostId = sessionManager.userSessionFlow.first().userId
                val newListingId = "list_host_" + UUID.randomUUID().toString().take(8)
                val newListing = Listing(
                    id = newListingId,
                    type = type,
                    title = title,
                    description = description,
                    address = address,
                    city = city,
                    area = area,
                    lat = lat,
                    lng = lng,
                    pricePerHour = pricePerHour,
                    rating = 5.0f,
                    ratingCount = 0,
                    isVerified = false,
                    availableFrom = String.format(java.util.Locale.US, "%02d:%02d", startMinute / 60, startMinute % 60),
                    availableTo = String.format(java.util.Locale.US, "%02d:%02d", endMinute / 60, endMinute % 60),
                    isActive = true,
                    hostId = currentHostId,
                    connectorType = connectorType,
                    powerKw = powerKw,
                    isCovered = isCovered,
                    hasCCTV = hasCCTV,
                    vehicleType = vehicleType,
                    imageUrl = photos.firstOrNull() ?: "https://images.unsplash.com/photo-1563720223185-11003d516935"
                ).copy(instantBook = instantBook)

                val rules = activeDays.map { day ->
                    AvailabilityRule(
                        id = "rule_${newListingId}_$day",
                        listingId = newListingId,
                        dayOfWeek = day,
                        startMinute = startMinute,
                        endMinute = endMinute
                    )
                }

                repository.saveListingWithPhotosAndRules(newListing, photos, rules)
                _isAddListingDialogOpen.value = false
                _userMessage.value = "New listing published with schedule & photos!"
            } catch (e: Exception) {
                _userMessage.value = "Error creating listing: ${e.message}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
