package com.example.plugnpark.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@Composable
fun OsmdroidLocationPicker(
    selectedLat: Double,
    selectedLng: Double,
    onLocationPicked: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            isTilesScaledToDpi = true
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(selectedLat, selectedLng))
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

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { map ->
            map.overlays.clear()

            // Touch gesture intercept
            map.overlays.add(object : Overlay() {
                override fun onTouchEvent(event: MotionEvent?, mapView: MapView?): Boolean {
                    mapView?.parent?.requestDisallowInterceptTouchEvent(true)
                    return false
                }
            })

            // Map Click Event Overlay
            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    p?.let {
                        onLocationPicked(it.latitude, it.longitude)
                    }
                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    return false
                }
            })
            map.overlays.add(eventsOverlay)

            // Pin Marker
            val marker = Marker(map).apply {
                position = GeoPoint(selectedLat, selectedLng)
                title = "Selected Spot Coordinates"
                snippet = "Lat: ${String.format("%.4f", selectedLat)}, Lng: ${String.format("%.4f", selectedLng)}"
            }
            map.overlays.add(marker)

            map.invalidate()
        }
    )
}
