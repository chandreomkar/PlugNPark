package com.example.plugnpark.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.domain.model.ListingType
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@Composable
fun OsmdroidMapView(
    listings: List<Listing>,
    selectedListing: Listing?,
    onListingSelect: (Listing) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)

            val defaultCenter = if (selectedListing != null) {
                GeoPoint(selectedListing.lat, selectedListing.lng)
            } else if (listings.isNotEmpty()) {
                GeoPoint(listings.first().lat, listings.first().lng)
            } else {
                GeoPoint(19.0760, 72.8777) // Mumbai
            }
            controller.setCenter(defaultCenter)
        }
    }

    // Lifecycle binding
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Network connectivity check
    val isOnline = remember(context) {
        checkNetworkOnline(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.clear()

                // Touch overlay to prevent gesture parent interception
                map.overlays.add(object : Overlay() {
                    override fun onTouchEvent(event: MotionEvent?, mapView: MapView?): Boolean {
                        mapView?.parent?.requestDisallowInterceptTouchEvent(true)
                        return false
                    }
                })

                listings.forEach { listing ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(listing.lat, listing.lng)
                        title = listing.title
                        snippet = "${if (listing.type == ListingType.EV_CHARGER) "⚡ EV Charger" else "🅿️ Parking Spot"} • ₹${listing.pricePerHour.toInt()}/hr"
                        subDescription = "${listing.area}, ${listing.city}"

                        setOnMarkerClickListener { _, _ ->
                            onListingSelect(listing)
                            showInfoWindow()
                            true
                        }
                    }
                    map.overlays.add(marker)
                }

                if (selectedListing != null) {
                    map.controller.animateTo(GeoPoint(selectedListing.lat, selectedListing.lng))
                }

                map.invalidate()
            }
        )

        // Offline Banner
        if (!isOnline) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Map needs internet connection to load tiles",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Floating Map Controls (My Location + Zoom In / Zoom Out)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // My Location Button
            SmallFloatingActionButton(
                onClick = {
                    val center = if (selectedListing != null) {
                        GeoPoint(selectedListing.lat, selectedListing.lng)
                    } else if (listings.isNotEmpty()) {
                        GeoPoint(listings.first().lat, listings.first().lng)
                    } else {
                        GeoPoint(19.0760, 72.8777)
                    }
                    mapView.controller.animateTo(center)
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Zoom In Button
            SmallFloatingActionButton(
                onClick = { mapView.controller.zoomIn() },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Zoom Out Button
            SmallFloatingActionButton(
                onClick = { mapView.controller.zoomOut() },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun checkNetworkOnline(context: Context): Boolean {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    } catch (e: Exception) {
        true
    }
}
