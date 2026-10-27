package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.screens.FilterOptions
import com.example.plugnpark.util.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ListingWithDistance(
    val listing: Listing,
    val distanceKm: Double
)

data class ExploreUiState(
    val searchQuery: String = "",
    val selectedType: ListingType = ListingType.ALL,
    val selectedCity: String = "ALL",
    val isMapView: Boolean = false,
    val listingsWithDistance: List<ListingWithDistance> = emptyList(),
    val selectedListing: Listing? = null,
    val userLat: Double = LocationUtils.MUMBAI_LAT,
    val userLng: Double = LocationUtils.MUMBAI_LNG,
    val filterOptions: FilterOptions = FilterOptions(),
    val isFilterSheetOpen: Boolean = false,
    val userMessage: String? = null,
    val isLoading: Boolean = false
)

class ExploreViewModel(
    private val repository: PlugNParkRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow(ListingType.ALL)
    private val _selectedCity = MutableStateFlow("ALL")
    private val _isMapView = MutableStateFlow(false)
    private val _selectedListing = MutableStateFlow<Listing?>(null)
    private val _userLocation = MutableStateFlow(Pair(LocationUtils.MUMBAI_LAT, LocationUtils.MUMBAI_LNG))
    private val _filterOptions = MutableStateFlow(FilterOptions())
    private val _isFilterSheetOpen = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ExploreUiState> = combine(
        repository.getAllActiveListings(),
        _searchQuery,
        _selectedType,
        _selectedCity,
        _isMapView,
        _selectedListing,
        _userLocation,
        _filterOptions,
        _isFilterSheetOpen,
        _userMessage
    ) { flows ->
        val rawListings = flows[0] as List<Listing>
        val query = flows[1] as String
        val type = flows[2] as ListingType
        val city = flows[3] as String
        val isMap = flows[4] as Boolean
        val selected = flows[5] as Listing?
        val (lat, lng) = flows[6] as Pair<Double, Double>
        val filters = flows[7] as FilterOptions
        val isFilterOpen = flows[8] as Boolean
        val msg = flows[9] as String?

        val itemsWithDistance = rawListings.map { listing ->
            val dist = LocationUtils.calculateDistanceKm(lat, lng, listing.lat, listing.lng)
            ListingWithDistance(listing, dist)
        }

        val filtered = itemsWithDistance.filter { item ->
            val listing = item.listing
            val dist = item.distanceKm

            val matchesType = when (type) {
                ListingType.ALL -> true
                ListingType.EV_CHARGER -> listing.type == ListingType.EV_CHARGER
                ListingType.PARKING -> listing.type == ListingType.PARKING
            }
            val matchesCity = if (city == "ALL") true else listing.city.equals(city, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.area.contains(query, ignoreCase = true) ||
                    listing.address.contains(query, ignoreCase = true)

            val matchesPrice = listing.pricePerHour <= filters.maxPrice
            val matchesDistance = dist <= filters.maxDistanceKm
            val matchesConnector = if (filters.selectedConnector == "ALL" || listing.type != ListingType.EV_CHARGER) true else listing.connectorType.contains(filters.selectedConnector, ignoreCase = true)
            val matchesCovered = if (filters.isCoveredOnly) listing.isCovered else true
            val matchesCCTV = if (filters.hasCCTVOnly) listing.hasCCTV else true
            val matchesVerified = if (filters.isVerifiedOnly) listing.isVerified else true

            matchesType && matchesCity && matchesQuery && matchesPrice && matchesDistance && matchesConnector && matchesCovered && matchesCCTV && matchesVerified
        }.sortedBy { it.distanceKm } // Sort by distance (Haversine)

        ExploreUiState(
            searchQuery = query,
            selectedType = type,
            selectedCity = city,
            isMapView = isMap,
            listingsWithDistance = filtered,
            selectedListing = selected,
            userLat = lat,
            userLng = lng,
            filterOptions = filters,
            isFilterSheetOpen = isFilterOpen,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState()
    )

    fun updateUserLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onTypeSelect(type: ListingType) {
        _selectedType.value = type
    }

    fun onCitySelect(city: String) {
        _selectedCity.value = city
    }

    fun toggleMapView() {
        _isMapView.value = !_isMapView.value
    }

    fun selectListing(listing: Listing?) {
        _selectedListing.value = listing
    }

    fun openFilterSheet() {
        _isFilterSheetOpen.value = true
    }

    fun closeFilterSheet() {
        _isFilterSheetOpen.value = false
    }

    fun applyFilters(newFilters: FilterOptions) {
        _filterOptions.value = newFilters
        _isFilterSheetOpen.value = false
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun createBooking(listing: Listing, hours: Int) {
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis() + (30 * 60 * 1000) // 30 mins from now
                val endTime = startTime + (hours * 3600_000L)
                val booking = repository.createBooking(
                    listingId = listing.id,
                    seekerId = "user_seeker_1",
                    startTime = startTime,
                    endTime = endTime,
                    pricePerHour = listing.pricePerHour
                )
                _selectedListing.value = null
                _userMessage.value = "Booking confirmed! Total: ₹${String.format("%.0f", booking.totalAmount)}"
            } catch (e: Exception) {
                _userMessage.value = "Failed to create booking: ${e.message}"
            }
        }
    }
}
