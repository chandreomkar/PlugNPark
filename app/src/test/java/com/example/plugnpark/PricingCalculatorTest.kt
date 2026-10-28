package com.example.plugnpark

import com.example.plugnpark.util.PricingCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class PricingCalculatorTest {

    @Test
    fun calculatePrice_1Hour() {
        val pricePerHour = 100.0
        val startTime = 1000L
        val endTime = startTime + (3600_000L) // 1 hour

        val breakdown = PricingCalculator.calculatePrice(pricePerHour, startTime, endTime)

        assertEquals(100.0, breakdown.baseAmount, 0.01)
        assertEquals(10.0, breakdown.platformFee, 0.01) // 10%
        assertEquals(1.8, breakdown.gst, 0.01) // 18% of platform fee
        assertEquals(111.8, breakdown.totalAmount, 0.01) // 100 + 10 + 1.8
        assertEquals(100.0, breakdown.hostEarning, 0.01)
    }

    @Test
    fun calculatePrice_12Hours() {
        val pricePerHour = 100.0
        val startTime = 1000L
        val endTime = startTime + (12 * 3600_000L) // 12 hours

        val breakdown = PricingCalculator.calculatePrice(pricePerHour, startTime, endTime)

        assertEquals(1200.0, breakdown.baseAmount, 0.01)
        assertEquals(120.0, breakdown.platformFee, 0.01) // 10%
        assertEquals(21.6, breakdown.gst, 0.01) // 18% of 120
        assertEquals(1341.6, breakdown.totalAmount, 0.01) // 1200 + 120 + 21.6
        assertEquals(1200.0, breakdown.hostEarning, 0.01)
    }

    @Test
    fun calculatePrice_LessThan1Hour_DefaultsTo1Hour() {
        val pricePerHour = 150.0
        val startTime = 1000L
        val endTime = startTime + (1800_000L) // 30 minutes

        val breakdown = PricingCalculator.calculatePrice(pricePerHour, startTime, endTime)

        // The logic maxes out to 1 hour minimum
        assertEquals(150.0, breakdown.baseAmount, 0.01)
        assertEquals(15.0, breakdown.platformFee, 0.01)
        assertEquals(2.7, breakdown.gst, 0.01)
        assertEquals(167.7, breakdown.totalAmount, 0.01)
        assertEquals(150.0, breakdown.hostEarning, 0.01)
    }
}
