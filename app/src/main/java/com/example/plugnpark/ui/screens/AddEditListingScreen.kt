package com.example.plugnpark.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SafetyCheck
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.ui.components.OsmdroidLocationPicker
import com.example.plugnpark.ui.viewmodel.HostViewModel
import com.example.plugnpark.util.LocationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditListingScreen(
    viewModel: HostViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("Bandra West, Mumbai") }
    var city by remember { mutableStateOf("Mumbai") }
    var area by remember { mutableStateOf("Bandra") }
    var lat by remember { mutableDoubleStateOf(LocationUtils.MUMBAI_LAT) }
    var lng by remember { mutableDoubleStateOf(LocationUtils.MUMBAI_LNG) }
    var type by remember { mutableStateOf(ListingType.EV_CHARGER) }
    var priceText by remember { mutableStateOf("180") }
    var connectorType by remember { mutableStateOf("Type 2") }
    var powerKwText by remember { mutableStateOf("7.4") }
    var isCovered by remember { mutableStateOf(true) }
    var hasCCTV by remember { mutableStateOf(true) }
    var vehicleType by remember { mutableStateOf("4-Wheeler") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Safety Checklist State
    var isEarthingVerified by remember { mutableStateOf(true) }
    var isMcbPresent by remember { mutableStateOf(true) }
    var isWeatherproof by remember { mutableStateOf(true) }
    var isCertificateUploaded by remember { mutableStateOf(false) }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("List Your Charger / Spot", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Spot Type Choice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Select Listing Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { type = ListingType.EV_CHARGER },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (type == ListingType.EV_CHARGER) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(Icons.Default.EvStation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EV Charger")
                        }

                        Button(
                            onClick = { type = ListingType.PARKING },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (type == ListingType.PARKING) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Icon(Icons.Default.LocalParking, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parking Spot")
                        }
                    }
                }
            }

            // 2. Spot Details & Pricing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("2. Spot Information & Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Spot Title") },
                        placeholder = { Text("e.g. 7.4kW Fast Charger at Hill Road Villa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area / Locality") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price / Hour (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        if (type == ListingType.EV_CHARGER) {
                            OutlinedTextField(
                                value = powerKwText,
                                onValueChange = { powerKwText = it },
                                label = { Text("Output Power (kW)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (type == ListingType.EV_CHARGER) {
                        OutlinedTextField(
                            value = connectorType,
                            onValueChange = { connectorType = it },
                            label = { Text("Connector Type (Type 2 / CCS2 / 15A)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Access Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Location Picker on Osmdroid Map
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
                        Text("3. Tap Map to Set Coordinates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = {
                                lat = LocationUtils.MUMBAI_LAT
                                lng = LocationUtils.MUMBAI_LNG
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Current City")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        OsmdroidLocationPicker(
                            selectedLat = lat,
                            selectedLng = lng,
                            onLocationPicked = { newLat, newLng ->
                                lat = newLat
                                lng = newLng
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 4. Photo Picker from Gallery
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("4. Spot Cover Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (photoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected Cover Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Cover Photo from Gallery")
                        }
                    }
                }
            }

            // 5. Charger Safety Verification Checklist (if EV Charger)
            if (type == ListingType.EV_CHARGER) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SafetyCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("5. Charger Safety Verification", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isEarthingVerified, onCheckedChange = { isEarthingVerified = it })
                            Text("Heavy-Duty Earthing Verified")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isMcbPresent, onCheckedChange = { isMcbPresent = it })
                            Text("Dedicated MCB Circuit Breaker")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isWeatherproof, onCheckedChange = { isWeatherproof = it })
                            Text("IP65 Weatherproof Enclosure")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Electrician Certificate Mock Upload Button
                        if (!isCertificateUploaded) {
                            OutlinedButton(
                                onClick = { isCertificateUploaded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Upload Electrician Safety Certificate")
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Electrician Certificate Attached", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
            }

            // Publish Button
            Button(
                onClick = {
                    if (title.isNotBlank() && address.isNotBlank()) {
                        val price = priceText.toDoubleOrNull() ?: 150.0
                        val power = powerKwText.toDoubleOrNull() ?: 7.4
                        viewModel.addNewListing(
                            title = title,
                            description = description,
                            address = address,
                            city = city,
                            area = area,
                            lat = lat,
                            lng = lng,
                            type = type,
                            pricePerHour = price,
                            connectorType = connectorType,
                            powerKw = power,
                            isCovered = isCovered,
                            hasCCTV = hasCCTV,
                            vehicleType = vehicleType,
                            imageUrl = photoUri?.toString() ?: ""
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Publish Listing Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}
