package com.example.plugnpark.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.domain.model.ListingType
import org.osmdroid.config.Configuration
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

    // Properly set user agent for osmdroid
    Configuration.getInstance().userAgentValue = context.packageName

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

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
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
}
