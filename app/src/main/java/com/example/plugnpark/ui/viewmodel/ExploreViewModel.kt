package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.data.repository.PlugNParkRepository
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.screens.FilterOptions
import com.example.plugnpark.util.LocationUtils
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

enum class SortOption(val label: String) {
    NEAREST("Nearest"),
    CHEAPEST("Cheapest"),
    TOP_RATED("Top rated")
}

data class ListingWithDistance(
    val listing: Listing,
    val distanceKm: Double,
    val isFavorite: Boolean = false
)

data class SosRecommendation(
    val listing: Listing,
    val distanceKm: Double,
    val travelMinutes: Int,
    val neededKwh: Double,
    val estimatedChargingHours: Double,
    val estimatedCostRupees: Double
)

data class ExploreUiState(
    val searchQuery: String = "",
    val selectedType: ListingType = ListingType.ALL,
    val selectedCity: String = "ALL",
    val sortOption: SortOption = SortOption.NEAREST,
    val isAvailableNowOnly: Boolean = false,
    val isSavedOnly: Boolean = false,
    val isVehicleCompatibleOnly: Boolean = false,
    val defaultVehicle: Vehicle? = null,
    val isMapView: Boolean = false,
    val listingsWithDistance: List<ListingWithDistance> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val unreadNotificationCount: Int = 0,
    val recentSearches: List<String> = listOf("Bandra", "Wakad", "Powai", "Andheri", "Hinjewadi"),
    val selectedListing: Listing? = null,
    val userLat: Double = LocationUtils.MUMBAI_LAT,
    val userLng: Double = LocationUtils.MUMBAI_LNG,
    val filterOptions: FilterOptions = FilterOptions(),
    val isFilterSheetOpen: Boolean = false,
    val isSosDialogOpen: Boolean = false,
    val sosBatteryPct: Float = 8f,
    val sosRecommendations: List<SosRecommendation> = emptyList(),
    val userMessage: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModel(
    private val repository: PlugNParkRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow(ListingType.ALL)
    private val _selectedCity = MutableStateFlow("ALL")
    private val _sortOption = MutableStateFlow(SortOption.NEAREST)
    private val _isAvailableNowOnly = MutableStateFlow(false)
    private val _isSavedOnly = MutableStateFlow(false)
    private val _isVehicleCompatibleOnly = MutableStateFlow(false)
    private val _isMapView = MutableStateFlow(false)
    private val _selectedListing = MutableStateFlow<Listing?>(null)
    private val _userLocation = MutableStateFlow(Pair(LocationUtils.MUMBAI_LAT, LocationUtils.MUMBAI_LNG))
    private val _filterOptions = MutableStateFlow(FilterOptions())
    private val _isFilterSheetOpen = MutableStateFlow(false)
    private val _isSosDialogOpen = MutableStateFlow(false)
    private val _sosBatteryPct = MutableStateFlow(8f)
    private val _isRefreshing = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ExploreUiState> = combine(
        repository.getAllActiveListings(),
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getFavoriteListingIds(session.userId)
        },
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getVehiclesByOwner(session.userId)
        },
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getNotificationsByUser(session.userId)
        },
        _searchQuery,
        _selectedType,
        _selectedCity,
        _sortOption,
        _isAvailableNowOnly,
        _isSavedOnly,
        _isVehicleCompatibleOnly,
        _isMapView,
        _selectedListing,
        _userLocation,
        _filterOptions,
        _isFilterSheetOpen,
        _isSosDialogOpen,
        _sosBatteryPct,
        _isRefreshing,
        _userMessage
    ) { flows ->
        val rawListings = flows[0] as List<Listing>
        val favIds = (flows[1] as List<String>).toSet()
        val userVehicles = flows[2] as List<Vehicle>
        val notifications = flows[3] as List<com.example.plugnpark.data.local.entity.AppNotification>
        val query = flows[4] as String
        val type = flows[5] as ListingType
        val city = flows[6] as String
        val sort = flows[7] as SortOption
        val availableNow = flows[8] as Boolean
        val savedOnly = flows[9] as Boolean
        val compatibleOnly = flows[10] as Boolean
        val isMap = flows[11] as Boolean
        val selected = flows[12] as Listing?
        val (lat, lng) = flows[13] as Pair<Double, Double>
        val filters = flows[14] as FilterOptions
        val isFilterOpen = flows[15] as Boolean
        val isSosOpen = flows[16] as Boolean
        val batteryPct = flows[17] as Float
        val refreshing = flows[18] as Boolean
        val msg = flows[19] as String?

        val unreadCount = notifications.count { !it.isRead }

        val defaultVeh = userVehicles.firstOrNull { it.isDefault } ?: userVehicles.firstOrNull()

        val nowCal = Calendar.getInstance()
        val currentMinuteOfDay = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

        val itemsWithDistance = rawListings.map { listing ->
            val dist = LocationUtils.calculateDistanceKm(lat, lng, listing.lat, listing.lng)
            ListingWithDistance(
                listing = listing,
                distanceKm = dist,
                isFavorite = listing.id in favIds
            )
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
            val matchesDistance = if (filters.maxDistanceKm >= 50f) true else dist <= filters.maxDistanceKm
            val matchesConnector = if (filters.selectedConnector == "ALL" || listing.type != ListingType.EV_CHARGER) true else listing.connectorType.contains(filters.selectedConnector, ignoreCase = true)
            val matchesCovered = if (filters.isCoveredOnly) listing.isCovered else true
            val matchesCCTV = if (filters.hasCCTVOnly) listing.hasCCTV else true
            val matchesVerified = if (filters.isVerifiedOnly) listing.isVerified else true
            val matchesSaved = if (savedOnly) item.isFavorite else true

            val matchesVehicleCompatibility = if (compatibleOnly && defaultVeh != null) {
                if (listing.type == ListingType.EV_CHARGER) {
                    listing.connectorType.contains(defaultVeh.connectorType, ignoreCase = true) ||
                            defaultVeh.connectorType.contains(listing.connectorType, ignoreCase = true)
                } else {
                    listing.vehicleType.contains(defaultVeh.vehicleType, ignoreCase = true) ||
                            defaultVeh.vehicleType.contains(listing.vehicleType, ignoreCase = true)
                }
            } else true

            val matchesAvailableNow = if (availableNow) {
                try {
                    val fromParts = listing.availableFrom.split(":")
                    val toParts = listing.availableTo.split(":")
                    val fromMin = fromParts[0].toInt() * 60 + fromParts[1].toInt()
                    val toMin = toParts[0].toInt() * 60 + toParts[1].toInt()
                    currentMinuteOfDay in fromMin..toMin
                } catch (e: Exception) {
                    true
                }
            } else true

            matchesType && matchesCity && matchesQuery && matchesPrice && matchesDistance &&
                    matchesConnector && matchesCovered && matchesCCTV && matchesVerified &&
                    matchesSaved && matchesVehicleCompatibility && matchesAvailableNow
        }

        val sorted = when (sort) {
            SortOption.NEAREST -> filtered.sortedBy { it.distanceKm }
            SortOption.CHEAPEST -> filtered.sortedBy { it.listing.pricePerHour }
            SortOption.TOP_RATED -> filtered.sortedByDescending { it.listing.rating }
        }

        // Calculate SOS Recommendations (Top 3 nearest compatible chargers)
        val batteryKwh = defaultVeh?.batteryKwh ?: 40.0
        val targetChargeKwh = batteryKwh * ((80.0 - batteryPct) / 100.0).coerceAtLeast(0.1)

        val sosRecs = itemsWithDistance
            .filter { item ->
                val l = item.listing
                l.type == ListingType.EV_CHARGER &&
                        (defaultVeh == null || l.connectorType.contains(defaultVeh.connectorType, ignoreCase = true) || defaultVeh.connectorType.contains(l.connectorType, ignoreCase = true))
            }
            .sortedBy { it.distanceKm }
            .take(3)
            .map { item ->
                val l = item.listing
                val speedKmH = 25.0 // 25 km/h city speed
                val travelMins = ((item.distanceKm / speedKmH) * 60).toInt().coerceAtLeast(2)
                val powerKw = if (l.powerKw > 0) l.powerKw else 7.4
                val chargingHours = targetChargeKwh / powerKw
                val estimatedCost = chargingHours * l.pricePerHour

                SosRecommendation(
                    listing = l,
                    distanceKm = item.distanceKm,
                    travelMinutes = travelMins,
                    neededKwh = targetChargeKwh,
                    estimatedChargingHours = chargingHours,
                    estimatedCostRupees = estimatedCost
                )
            }

        ExploreUiState(
            searchQuery = query,
            selectedType = type,
            selectedCity = city,
            sortOption = sort,
            isAvailableNowOnly = availableNow,
            isSavedOnly = savedOnly,
            isVehicleCompatibleOnly = compatibleOnly,
            defaultVehicle = defaultVeh,
            isMapView = isMap,
            listingsWithDistance = sorted,
            favoriteIds = favIds,
            unreadNotificationCount = unreadCount,
            selectedListing = selected,
            userLat = lat,
            userLng = lng,
            filterOptions = filters,
            isFilterSheetOpen = isFilterOpen,
            isSosDialogOpen = isSosOpen,
            sosBatteryPct = batteryPct,
            sosRecommendations = sosRecs,
            isRefreshing = refreshing,
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

    fun onSortOptionSelect(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleAvailableNow() {
        _isAvailableNowOnly.value = !_isAvailableNowOnly.value
    }

    fun toggleSavedOnly() {
        _isSavedOnly.value = !_isSavedOnly.value
    }

    fun toggleVehicleCompatible() {
        _isVehicleCompatibleOnly.value = !_isVehicleCompatibleOnly.value
    }

    fun openSosDialog() {
        _isSosDialogOpen.value = true
    }

    fun closeSosDialog() {
        _isSosDialogOpen.value = false
    }

    fun setSosBatteryPct(pct: Float) {
        _sosBatteryPct.value = pct
    }

    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.userSessionFlow.first().userId
                repository.toggleFavorite(userId, listingId)
            } catch (e: Exception) {
                _userMessage.value = "Failed to update favorite: ${e.message}"
            }
        }
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedType.value = ListingType.ALL
        _selectedCity.value = "ALL"
        _sortOption.value = SortOption.NEAREST
        _isAvailableNowOnly.value = false
        _isSavedOnly.value = false
        _isVehicleCompatibleOnly.value = false
        _filterOptions.value = FilterOptions()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000)
            _isRefreshing.value = false
            _userMessage.value = "Spots refreshed!"
        }
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
}
