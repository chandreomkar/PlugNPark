package com.example.plugnpark.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.plugnpark.domain.model.ConnectorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.components.EarningsBarChart
import com.example.plugnpark.ui.viewmodel.BookingWithListing
import com.example.plugnpark.ui.viewmodel.HostViewModel
import com.example.plugnpark.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostScreen(
    viewModel: HostViewModel,
    onNavigateToAddListing: () -> Unit = { viewModel.openAddListingDialog() },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Host Dashboard", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Earnings Summary Cards (Gross, Fee, Net)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Revenue & Fee Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EarningStatPill("Gross Sales", "₹${uiState.grossEarnings.toInt()}")
                            EarningStatPill("PlugNPark Fee (10%)", "-₹${uiState.platformFeeTotal.toInt()}")
                            EarningStatPill("Net Earnings", "₹${uiState.totalEarnings.toInt()}")
                        }
                    }
                }
            }

            // 2. Weekly Earnings Canvas Bar Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Real Weekly Earnings Chart (Mon - Sun)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        EarningsBarChart(weeklyData = uiState.weeklyChartData)
                    }
                }
            }

            // 3. Incoming Booking Requests (InstantBook = False)
            if (uiState.pendingBookingRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Booking Requests (${uiState.pendingBookingRequests.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(uiState.pendingBookingRequests, key = { it.booking.id }) { item ->
                    PendingRequestCard(
                        item = item,
                        onAccept = { viewModel.acceptBookingRequest(item.booking) },
                        onDecline = { viewModel.declineBookingRequest(item.booking) }
                    )
                }
            }

            // 4. Upcoming Host Bookings
            item {
                Text(
                    text = "Upcoming Confirmed Bookings (${uiState.upcomingHostBookings.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.upcomingHostBookings.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming reservations scheduled yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(uiState.upcomingHostBookings, key = { it.booking.id }) { item ->
                    HostBookingItemCard(item = item)
                }
            }

            // 5. Managed Spots & Safety Verification
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Managed Spots (${uiState.hostListings.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onNavigateToAddListing,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Spot")
                    }
                }
            }

            if (uiState.hostListings.isEmpty()) {
                item {
                    // Friendly Empty State
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EvStation, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Spots Listed Yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Share your home EV charger or private driveway to start earning daily passive income!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onNavigateToAddListing, shape = RoundedCornerShape(10.dp)) {
                                Text("List Your Spot Now")
                            }
                        }
                    }
                }
            } else {
                items(uiState.hostListings, key = { it.id }) { listing ->
                    ManagedListingCard(
                        listing = listing,
                        isVerifying = listing.id in uiState.verifyingListingIds,
                        onToggleActive = { viewModel.toggleListingActive(listing) },
                        onVerifyElectrician = { viewModel.verifyElectricianCertificate(listing) },
                        onDelete = { viewModel.confirmDeleteListing(listing) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Delete Listing Confirmation Dialog
        uiState.listingToDelete?.let { listing ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteListing() },
                title = { Text("Delete Listing?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete '${listing.title}'? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.executeDeleteListing() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Listing")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteListing() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add / Edit Listing Dialog with Schedule & Photo Picker
        if (uiState.isAddListingDialogOpen) {
            AddListingDialog(
                onDismiss = { viewModel.closeAddListingDialog() },
                onPublish = { title, desc, addr, city, area, lat, lng, type, price, connectors, power, covered, cctv, vehType, instant, photos, days, startMin, endMin ->
                    viewModel.addNewListingWithPhotosAndRules(
                        title = title, description = desc, address = addr, city = city, area = area,
                        lat = lat, lng = lng, type = type, pricePerHour = price, connectorTypes = connectors,
                        powerKw = power, isCovered = covered, hasCCTV = cctv, vehicleType = vehType,
                        instantBook = instant, photos = photos, activeDays = days, startMinute = startMin, endMinute = endMin
                    )
                }
            )
        }
    }
}

@Composable
fun PendingRequestCard(
    item: BookingWithListing,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Request", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text("₹${item.booking.totalAmount.toInt()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(item.listing?.title ?: "Spot Reservation", fontWeight = FontWeight.SemiBold)
            Text("🕒 ${TimeFormatter.formatDateTime(item.booking.startTime)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun ManagedListingCard(
    listing: Listing,
    isVerifying: Boolean,
    onToggleActive: () -> Unit,
    onVerifyElectrician: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${listing.area}, ${listing.city} • ₹${listing.pricePerHour.toInt()}/hr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pause / Activate Switch
                    Switch(
                        checked = listing.isActive,
                        onCheckedChange = { onToggleActive() }
                    )

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Safety & Electrician Certificate Verification Badge / Action
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (listing.isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (listing.isVerified) Icons.Default.Verified else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (listing.isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (listing.isVerified) "Verified Safety & Wiring" else "Electrician Inspection Pending",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (listing.isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (listing.isVerified) "Earthing & MCB Breaker Certified" else "Upload certificate to get Verified Badge",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!listing.isVerified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onVerifyElectrician,
                            enabled = !isVerifying,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.ElectricMeter, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verify Now", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EarningStatPill(title: String, amount: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun HostBookingItemCard(item: BookingWithListing) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.listing?.title ?: "Spot Booking", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("🕒 ${TimeFormatter.formatDateTime(item.booking.startTime)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("₹${item.booking.hostEarning.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingDialog(
    onDismiss: () -> Unit,
    onPublish: (
        title: String, description: String, address: String, city: String, area: String,
        lat: Double, lng: Double, type: ListingType, price: Double, connectors: List<ConnectorType>,
        power: Double, covered: Boolean, cctv: Boolean, vehType: String, instant: Boolean,
        photos: List<String>, days: Set<Int>, startMin: Int, endMin: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("Fast 11kW Charger at My Courtyard") }
    var description by remember { mutableStateOf("Spacious gated parking with 11kW Type 2 AC fast charger in Bandra.") }
    var address by remember { mutableStateOf("Pali Hill, Bandra West, Mumbai") }
    var city by remember { mutableStateOf("Mumbai") }
    var area by remember { mutableStateOf("Bandra") }
    var priceText by remember { mutableStateOf("150") }
    var isCharger by remember { mutableStateOf(true) }
    var selectedConnectors by remember { mutableStateOf(setOf(ConnectorType.TYPE_2)) }
    var powerText by remember { mutableStateOf("11.0") }
    var isCovered by remember { mutableStateOf(true) }
    var hasCctv by remember { mutableStateOf(true) }
    var vehicleType by remember { mutableStateOf("SUV") }
    var instantBook by remember { mutableStateOf(true) }

    var typeExpanded by remember { mutableStateOf(false) }

    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) } // Mon..Sun
    var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1563720223185-11003d516935") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List Your Charger / Parking Spot", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Spot Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area") }, modifier = Modifier.weight(1f))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (₹/hour)") }, modifier = Modifier.weight(1f))
                    
                    // Vehicle Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = vehicleType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vehicle Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            listOf("CAR", "SUV", "BIKE").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        vehicleType = option
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCharger, onCheckedChange = { isCharger = it })
                    Text("Is EV Charger (Uncheck for Parking only)")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = instantBook, onCheckedChange = { instantBook = it })
                    Text("Enable Instant Booking")
                }

                if (isCharger) {
                    Text("Supported Connectors:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    // Multi-select connector chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ConnectorType.entries.filter { it != ConnectorType.NONE }) { type ->
                            FilterChip(
                                selected = type in selectedConnectors,
                                onClick = {
                                    selectedConnectors = if (type in selectedConnectors) selectedConnectors - type else selectedConnectors + type
                                },
                                label = { Text(type.label) }
                            )
                        }
                    }
                    OutlinedTextField(value = powerText, onValueChange = { powerText = it }, label = { Text("Power Rating (kW)") }, modifier = Modifier.fillMaxWidth())
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCovered, onCheckedChange = { isCovered = it })
                    Text("Shaded / Covered")
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = hasCctv, onCheckedChange = { hasCctv = it })
                    Text("CCTV")
                }

                // Weekly Availability Schedule Editor
                Text("Weekly Available Days:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dayNames = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
                    dayNames.forEach { (dayIdx, label) ->
                        FilterChip(
                            selected = dayIdx in selectedDays,
                            onClick = {
                                selectedDays = if (dayIdx in selectedDays) selectedDays - dayIdx else selectedDays + dayIdx
                            },
                            label = { Text(label) }
                        )
                    }
                }

                // Photo URL
                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text("Photo URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && address.isNotBlank()) {
                        onPublish(
                            title, description, address, city, area,
                            19.0544, 72.8318,
                            if (isCharger) ListingType.EV_CHARGER else ListingType.PARKING,
                            priceText.toDoubleOrNull() ?: 100.0,
                            selectedConnectors.toList(),
                            powerText.toDoubleOrNull() ?: 7.4,
                            isCovered, hasCctv, vehicleType, instantBook,
                            listOf(photoUrl), selectedDays, 360, 1320
                        )
                    }
                }
            ) {
                Text("Publish Spot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
