package com.example.plugnpark.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.components.ListingSkeletonCard
import com.example.plugnpark.ui.components.OsmdroidMapView
import com.example.plugnpark.ui.viewmodel.ExploreViewModel
import com.example.plugnpark.ui.viewmodel.ListingWithDistance
import com.example.plugnpark.ui.viewmodel.SortOption
import com.example.plugnpark.ui.viewmodel.SosRecommendation
import com.example.plugnpark.util.LocationProvider
import com.example.plugnpark.util.LocationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onListingClick: (listingId: String) -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSortMenuOpen by remember { mutableStateOf(false) }

    val fetchAndApplyLocation: suspend () -> Unit = {
        val location = LocationProvider.getCurrentLocation(context)
        if (location != null) {
            val isWithinIndia = location.latitude in 8.0..37.0 && location.longitude in 68.0..97.0
            if (isWithinIndia) {
                viewModel.updateUserLocation(location.latitude, location.longitude)
                snackbarHostState.showSnackbar("Location updated! Showing spots near you")
            } else {
                viewModel.updateUserLocation(LocationUtils.MUMBAI_LAT, LocationUtils.MUMBAI_LNG)
                snackbarHostState.showSnackbar("Showing results near Mumbai (Emulator location detected)")
            }
        } else {
            viewModel.updateUserLocation(LocationUtils.MUMBAI_LAT, LocationUtils.MUMBAI_LNG)
            snackbarHostState.showSnackbar("Showing results near Mumbai")
        }
    }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        coroutineScope.launch {
            if (fineGranted || coarseGranted) {
                fetchAndApplyLocation()
            } else {
                viewModel.updateUserLocation(LocationUtils.MUMBAI_LAT, LocationUtils.MUMBAI_LNG)
                snackbarHostState.showSnackbar("Showing results near Mumbai")
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fetchAndApplyLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Search Bar & Red SOS Button Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search Bandra, Wakad, Powai...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.openFilterSheet() }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Red SOS Button
                        Button(
                            onClick = { viewModel.openSosDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(Icons.Default.BatteryAlert, contentDescription = "SOS", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SOS", fontWeight = FontWeight.ExtraBold)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Bell Icon with Unread Badge
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        ) {
                            Box {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (uiState.unreadNotificationCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Red,
                                        modifier = Modifier
                                            .size(10.dp)
                                            .align(Alignment.TopEnd)
                                    ) {}
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Toggle Map / List
                        IconButton(
                            onClick = { viewModel.toggleMapView() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (uiState.isMapView) Icons.Default.ViewList else Icons.Default.Map,
                                contentDescription = "Toggle View",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Recent Search Chips
                    if (uiState.searchQuery.isNotBlank() || uiState.recentSearches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                Text(
                                    text = "Recent:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            items(uiState.recentSearches) { recent ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.clickable { viewModel.onSearchQueryChange(recent) }
                                ) {
                                    Text(
                                        text = recent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips Bar: Sort, My Vehicle, Available Now, Saved, Category & City
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // My Vehicle Filter Chip
                        item {
                            val defaultVeh = uiState.defaultVehicle
                            FilterChip(
                                selected = uiState.isVehicleCompatibleOnly,
                                onClick = { viewModel.toggleVehicleCompatible() },
                                label = { Text(if (defaultVeh != null) "Compatible (${defaultVeh.model})" else "My Vehicle") },
                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        // Sort Dropdown Chip
                        item {
                            Box {
                                FilterChip(
                                    selected = true,
                                    onClick = { isSortMenuOpen = true },
                                    label = { Text("Sort: ${uiState.sortOption.label}") },
                                    leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                DropdownMenu(
                                    expanded = isSortMenuOpen,
                                    onDismissRequest = { isSortMenuOpen = false }
                                ) {
                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                viewModel.onSortOptionSelect(option)
                                                isSortMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Available Now Chip
                        item {
                            FilterChip(
                                selected = uiState.isAvailableNowOnly,
                                onClick = { viewModel.toggleAvailableNow() },
                                label = { Text("Available now") },
                                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }

                        // Saved Filter Chip
                        item {
                            FilterChip(
                                selected = uiState.isSavedOnly,
                                onClick = { viewModel.toggleSavedOnly() },
                                label = { Text("Saved") },
                                leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        }

                        // Near Me Chip
                        item {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    coroutineScope.launch {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (!hasPermission) {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } else {
                                            fetchAndApplyLocation()
                                        }
                                    }
                                },
                                label = { Text("Near me") },
                                leadingIcon = { Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }

                        // Category Chips
                        item {
                            FilterChip(
                                selected = uiState.selectedType == ListingType.ALL,
                                onClick = { viewModel.onTypeSelect(ListingType.ALL) },
                                label = { Text("All Spots") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedType == ListingType.EV_CHARGER,
                                onClick = { viewModel.onTypeSelect(ListingType.EV_CHARGER) },
                                label = { Text("⚡ EV Chargers") },
                                leadingIcon = { Icon(Icons.Default.EvStation, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = uiState.selectedType == ListingType.PARKING,
                                onClick = { viewModel.onTypeSelect(ListingType.PARKING) },
                                label = { Text("🅿️ Parking") },
                                leadingIcon = { Icon(Icons.Default.LocalParking, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }

                        // City Filters
                        listOf("ALL", "Mumbai", "Pune").forEach { city ->
                            item {
                                FilterChip(
                                    selected = uiState.selectedCity.equals(city, ignoreCase = true),
                                    onClick = { viewModel.onCitySelect(city) },
                                    label = { Text(if (city == "ALL") "All Cities" else city) }
                                )
                            }
                        }
                    }
                }
            }

            // Body Content: Map View or Pull-to-Refresh List View
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isMapView) {
                    OsmdroidMapView(
                        listings = uiState.listingsWithDistance.map { it.listing },
                        selectedListing = uiState.selectedListing,
                        userLat = uiState.userLat,
                        userLng = uiState.userLng,
                        onListingSelect = { listing ->
                            viewModel.selectListing(listing)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Map Preview Card
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.selectedListing != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        uiState.selectedListing?.let { selected ->
                            val item = uiState.listingsWithDistance.firstOrNull { it.listing.id == selected.id }
                            val distKm = item?.distanceKm ?: 1.0

                            MapListingPreviewCard(
                                listing = selected,
                                distanceKm = distKm,
                                onClose = { viewModel.selectListing(null) },
                                onBook = { onListingClick(selected.id) }
                            )
                        }
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.isLoading) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                items(3) { ListingSkeletonCard() }
                            }
                        } else if (uiState.listingsWithDistance.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No spots found matching your search or active filters.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.clearAllFilters() },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Clear Filters & Reset")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }

                                items(uiState.listingsWithDistance, key = { it.listing.id }) { item ->
                                    EnhancedListingCard(
                                        item = item,
                                        onClick = { onListingClick(item.listing.id) },
                                        onToggleFavorite = { viewModel.toggleFavorite(item.listing.id) }
                                    )
                                }

                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }

        // Filter Bottom Sheet
        if (uiState.isFilterSheetOpen) {
            ExploreFilterBottomSheet(
                sheetState = sheetState,
                initialFilters = uiState.filterOptions,
                onDismiss = { viewModel.closeFilterSheet() },
                onApplyFilters = { newFilters -> viewModel.applyFilters(newFilters) }
            )
        }

        // SOS Low Battery Dialog
        if (uiState.isSosDialogOpen) {
            SosLowBatteryDialog(
                batteryPct = uiState.sosBatteryPct,
                recommendations = uiState.sosRecommendations,
                onBatteryPctChange = { viewModel.setSosBatteryPct(it) },
                onDismiss = { viewModel.closeSosDialog() },
                onBookSpot = { listingId ->
                    viewModel.closeSosDialog()
                    onListingClick(listingId)
                }
            )
        }
    }
}

@Composable
fun SosLowBatteryDialog(
    batteryPct: Float,
    recommendations: List<SosRecommendation>,
    onBatteryPctChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onBookSpot: (listingId: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SOS Emergency Charging", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current EV Battery %: ${batteryPct.toInt()}%", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                Slider(
                    value = batteryPct,
                    onValueChange = onBatteryPctChange,
                    valueRange = 1f..20f,
                    steps = 18
                )

                Text("Nearest 3 Compatible Chargers (25 km/h traffic est.):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (recommendations.isEmpty()) {
                    Text("No compatible chargers found nearby.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    recommendations.forEach { rec ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(rec.listing.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "📍 ${LocationUtils.formatDistance(rec.distanceKm)} • ⏱ ~${rec.travelMinutes} mins to reach",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "⚡ Est. ~₹${rec.estimatedCostRupees.toInt()} to charge to 80%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onBookSpot(rec.listing.id) },
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Book Now")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EnhancedListingCard(
    item: ListingWithDistance,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    val listing = item.listing
    val distanceKm = item.distanceKm
    val isFavorite = item.isFavorite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Photo Header via Coil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (listing.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(listing.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = listing.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Type Badge Overlay
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (listing.type == ListingType.EV_CHARGER)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (listing.type == ListingType.EV_CHARGER) Icons.Default.Bolt else Icons.Default.LocalParking,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (listing.type == ListingType.EV_CHARGER)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (listing.type == ListingType.EV_CHARGER) "${listing.powerKw} kW • ${listing.connectorTypes.joinToString("/") { it.name }}" else "Parking Spot",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favorite Heart & Verified Badges
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (listing.isVerified) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00C853)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Favorite Heart Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite Spot",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Card Body Info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFB300)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${listing.rating} (${listing.ratingCount})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${listing.area}, ${listing.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Haversine Distance Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.NearMe,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = LocationUtils.formatDistance(distanceKm),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${listing.pricePerHour.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / hr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Book Spot")
                    }
                }
            }
        }
    }
}

@Composable
fun MapListingPreviewCard(
    listing: Listing,
    distanceKm: Double,
    onClose: () -> Unit,
    onBook: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            val speedKmH = 25.0
            val travelMins = ((distanceKm / speedKmH) * 60).toInt().coerceAtLeast(2)

            Text(
                text = "${listing.address} • ${LocationUtils.formatDistance(distanceKm)} (~$travelMins min drive)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${listing.pricePerHour.toInt()} / hr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Navigate Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val geoUri = Uri.parse("geo:${listing.lat},${listing.lng}?q=${listing.lat},${listing.lng}(${Uri.encode(listing.title)})")
                                val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                context.startActivity(geoIntent)
                            } catch (e: Exception) {
                                try {
                                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${listing.lat},${listing.lng}")
                                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                                    context.startActivity(browserIntent)
                                } catch (e2: Exception) {
                                    android.widget.Toast.makeText(context, "No maps app found", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Navigate")
                    }

                    // Book Button
                    Button(
                        onClick = onBook,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Book Spot")
                    }
                }
            }
        }
    }
}
