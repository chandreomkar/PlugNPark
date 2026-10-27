package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.entity.Booking
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.BookingStatus
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.components.BarData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class HostUiState(
    val hostListings: List<Listing> = emptyList(),
    val todayEarnings: Double = 0.0,
    val thisWeekEarnings: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val weeklyChartData: List<BarData> = emptyList(),
    val upcomingHostBookings: List<BookingWithListing> = emptyList(),
    val verifyingListingIds: Set<String> = emptySet(),
    val isAddListingDialogOpen: Boolean = false,
    val userMessage: String? = null
)

class HostViewModel(
    private val repository: PlugNParkRepository
) : ViewModel() {

    private val hostId = "host_mumbai_1"
    private val _isAddListingDialogOpen = MutableStateFlow(false)
    private val _verifyingListingIds = MutableStateFlow<Set<String>>(emptySet())
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HostUiState> = combine(
        repository.getListingsByHostId(hostId),
        repository.getAllBookings(),
        _verifyingListingIds,
        _isAddListingDialogOpen,
        _userMessage
    ) { flows ->
        val listings = flows[0] as List<Listing>
        val bookings = flows[1] as List<Booking>
        val verifyingIds = flows[2] as Set<String>
        val isDialogOpen = flows[3] as Boolean
        val msg = flows[4] as String?

        val hostListingIds = listings.map { it.id }.toSet()
        val hostBookings = bookings.filter { it.listingId in hostListingIds }
        val listingMap = listings.associateBy { it.id }

        val completedBookings = hostBookings.filter { it.status == BookingStatus.COMPLETED }
        val totalEarnings = completedBookings.sumOf { it.hostEarning }

        val now = Calendar.getInstance()
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

        val chartData = listOf(
            BarData("M", 240.0),
            BarData("T", 450.0),
            BarData("W", 320.0),
            BarData("T", 600.0),
            BarData("F", 850.0),
            BarData("S", 1200.0),
            BarData("S", 950.0)
        )

        val upcomingBookings = hostBookings
            .filter { it.status == BookingStatus.CONFIRMED }
            .map { booking -> BookingWithListing(booking, listingMap[booking.listingId]) }

        HostUiState(
            hostListings = listings,
            todayEarnings = todayEarnings,
            thisWeekEarnings = weekEarnings,
            totalEarnings = totalEarnings,
            weeklyChartData = chartData,
            upcomingHostBookings = upcomingBookings,
            verifyingListingIds = verifyingIds,
            isAddListingDialogOpen = isDialogOpen,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HostUiState()
    )

    fun openAddListingDialog() {
        _isAddListingDialogOpen.value = true
    }

    fun closeAddListingDialog() {
        _isAddListingDialogOpen.value = false
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

            delay(3000) // 3-second verification processing state

            val updated = listing.copy(isVerified = true)
            repository.updateListing(updated)
            _verifyingListingIds.value = _verifyingListingIds.value - listing.id
            _userMessage.value = "✔ Verified! Electrician Certificate Approved."
        }
    }

    fun addNewListing(
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
        imageUrl: String
    ) {
        viewModelScope.launch {
            try {
                val newListing = Listing(
                    id = "list_host_" + UUID.randomUUID().toString().take(8),
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
                    availableFrom = "06:00",
                    availableTo = "22:00",
                    isActive = true,
                    hostId = hostId,
                    connectorType = connectorType,
                    powerKw = powerKw,
                    isCovered = isCovered,
                    hasCCTV = hasCCTV,
                    vehicleType = vehicleType,
                    imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1563720223185-11003d516935" }
                )
                repository.insertListing(newListing)
                _isAddListingDialogOpen.value = false
                _userMessage.value = "New listing published successfully!"
            } catch (e: Exception) {
                _userMessage.value = "Error creating listing: ${e.message}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
