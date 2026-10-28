package com.example.plugnpark

import com.example.plugnpark.util.LocationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationUtilsTest {

    @Test
    fun calculateDistance_BandraToBKC() {
        val bandraLat = 19.0544
        val bandraLng = 72.8318
        
        val bkcLat = 19.0657
        val bkcLng = 72.8680

        val distanceKm = LocationUtils.calculateDistanceKm(bandraLat, bandraLng, bkcLat, bkcLng)

        // The straight line distance between these two coordinates should be around 3.9 - 4.0 km
        assertTrue("Distance should be between 3.5 and 4.5 km, but was $distanceKm", distanceKm > 3.5 && distanceKm < 4.5)
        
        // Exact mathematical haversine value assertion just to be sure
        assertEquals(3.99, distanceKm, 0.1)
    }

    @Test
    fun calculateDistance_SameLocation_ReturnsZero() {
        val bandraLat = 19.0544
        val bandraLng = 72.8318

        val distanceKm = LocationUtils.calculateDistanceKm(bandraLat, bandraLng, bandraLat, bandraLng)
        assertEquals(0.0, distanceKm, 0.0001)
    }
}
