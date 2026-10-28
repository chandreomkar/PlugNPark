package com.example.plugnpark.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.MotionEvent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.domain.model.ListingType
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline

val CARTO_VOYAGER = XYTileSource(
    "CARTO_Voyager",
    4, 19, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
    )
)

@Composable
fun rememberNetworkConnectivityState(): State<Boolean> {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val initialState = try {
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    } catch (e: Exception) {
        true
    }
    
    val isOnline = remember { mutableStateOf(initialState) }
    
    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isOnline.value = true }
            override fun onLost(network: Network) { isOnline.value = false }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // Ignore
        }
        
        onDispose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    return isOnline
}

@Composable
fun OsmdroidMapView(
    listings: List<Listing>,
    selectedListing: Listing?,
    userLat: Double,
    userLng: Double,
    onListingSelect: (Listing) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val listingsOverlayFolder = remember { FolderOverlay() }
    val userOverlayFolder = remember { FolderOverlay() }
    val routeOverlayFolder = remember { FolderOverlay() }

    val isOnline by rememberNetworkConnectivityState()
    var isTilesFailed by remember { mutableStateOf(false) }
    var usingFallback by remember { mutableStateOf(false) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            isTilesScaledToDpi = true
            minZoomLevel = 4.0
            maxZoomLevel = 19.0

            controller.setZoom(14.0)
            controller.setCenter(GeoPoint(userLat, userLng))

            overlays.add(object : Overlay() {
                override fun onTouchEvent(event: MotionEvent?, mapView: MapView?): Boolean {
                    mapView?.parent?.requestDisallowInterceptTouchEvent(true)
                    return false
                }
            })

            overlays.add(routeOverlayFolder)
            overlays.add(userOverlayFolder)
            overlays.add(listingsOverlayFolder)
        }
    }

    // Tile Fallback Timeout
    LaunchedEffect(usingFallback, isOnline) {
        if (!usingFallback && isOnline) {
            delay(8000)
            try {
                // Simplified failure check: assuming failure if timeout is reached
                isTilesFailed = true
                usingFallback = true
            } catch (e: Exception) {
                isTilesFailed = true
                usingFallback = true
            }
        }
    }

    // Apply Tile Source
    LaunchedEffect(usingFallback) {
        if (usingFallback) {
            mapView.setTileSource(CARTO_VOYAGER)
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK)
        }
        mapView.invalidate()
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

    // Update User Location Marker
    LaunchedEffect(userLat, userLng) {
        userOverlayFolder.items.clear()
        val userMarker = Marker(mapView).apply {
            position = GeoPoint(userLat, userLng)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = createUserLocationDrawable(context)
            setOnMarkerClickListener { _, _ -> true } // Disable click
        }
        userOverlayFolder.add(userMarker)
        mapView.invalidate()
    }

    // Update Listings Markers
    LaunchedEffect(listings) {
        listingsOverlayFolder.items.clear()
        listings.forEach { listing ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(listing.lat, listing.lng)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = createListingDrawable(context, listing.type)

                setOnMarkerClickListener { _, _ ->
                    onListingSelect(listing)
                    true // Do not show default info window
                }
            }
            listingsOverlayFolder.add(marker)
        }
        mapView.invalidate()
    }

    // Animate to Selected Listing & Draw Route
    LaunchedEffect(selectedListing?.id) {
        routeOverlayFolder.items.clear()
        
        selectedListing?.let {
            // Draw Dashed Polyline
            val line = Polyline(mapView).apply {
                addPoint(GeoPoint(userLat, userLng))
                addPoint(GeoPoint(it.lat, it.lng))
                outlinePaint.color = android.graphics.Color.parseColor("#4285F4")
                outlinePaint.strokeWidth = 8f
                outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(20f, 20f), 0f)
            }
            routeOverlayFolder.add(line)
            
            mapView.controller.animateTo(GeoPoint(it.lat, it.lng))
        }
        
        mapView.invalidate()
    }

    // Network connectivity check
    // val isOnline = remember(context) { checkNetworkOnline(context) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = {
                // Do nothing on recomposition, state handled by LaunchedEffects
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

        // Retry Chip & Attribution Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isTilesFailed) {
                    Surface(
                        onClick = {
                            isTilesFailed = false
                            usingFallback = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Retry Map Connection",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                if (usingFallback) {
                    Text(
                        text = "© OpenStreetMap contributors © CARTO",
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = Color.Black
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
                    mapView.controller.animateTo(GeoPoint(userLat, userLng))
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

private fun createUserLocationDrawable(context: Context): Drawable {
    val size = 60
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Translucent accuracy circle
    paint.color = android.graphics.Color.parseColor("#334285F4")
    paint.style = Paint.Style.FILL
    canvas.drawCircle(30f, 30f, 30f, paint)
    
    // Solid inner dot
    paint.color = android.graphics.Color.parseColor("#4285F4")
    canvas.drawCircle(30f, 30f, 15f, paint)
    
    // White border for dot
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    canvas.drawCircle(30f, 30f, 15f, paint)
    
    return BitmapDrawable(context.resources, bitmap)
}

private fun createListingDrawable(context: Context, type: ListingType): Drawable {
    val size = 60
    val bitmap = Bitmap.createBitmap(size, size + 20, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    val color = if (type == ListingType.EV_CHARGER) android.graphics.Color.parseColor("#00C853") else android.graphics.Color.parseColor("#FF9800")
    
    // Draw Pin shape
    paint.color = color
    paint.style = Paint.Style.FILL
    
    val path = android.graphics.Path()
    path.moveTo(30f, size + 15f)
    path.lineTo(15f, 45f)
    path.arcTo(0f, 0f, 60f, 60f, 150f, 240f, false)
    path.lineTo(30f, size + 15f)
    path.close()
    
    canvas.drawPath(path, paint)
    
    // White border
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f
    canvas.drawPath(path, paint)
    
    // Inner dot
    paint.style = Paint.Style.FILL
    canvas.drawCircle(30f, 30f, 10f, paint)
    
    return BitmapDrawable(context.resources, bitmap)
}


