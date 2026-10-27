package com.example.plugnpark.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plugnpark.ui.viewmodel.BookingViewModel
import com.example.plugnpark.util.QrCodeGenerator
import com.example.plugnpark.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    bookingId: String,
    viewModel: BookingViewModel,
    onNavigateToBookings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listing = uiState.listing
    val booking = uiState.confirmedBooking
    val context = LocalContext.current

    val qrBitmap = QrCodeGenerator.generateQrCodeBitmap(bookingId, size = 400)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Booking Confirmation", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Header Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFD1FADF), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = Color(0xFF00C853)
                )
            }

            Text(
                text = "Spot Booked Successfully!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Show this QR code upon arrival at the location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // QR Code Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "Booking QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(160.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Booking ID: $bookingId",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Spot Summary
            if (listing != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(listing.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (booking != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${TimeFormatter.formatDateTime(booking.startTime)} - ${TimeFormatter.formatTimeOnly(booking.endTime)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (listing != null) {
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
                                e2.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigate in Google Maps", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = onNavigateToBookings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.BookOnline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View My Bookings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
