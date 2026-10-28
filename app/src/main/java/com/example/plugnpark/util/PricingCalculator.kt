package com.example.plugnpark.util

object PricingCalculator {
    data class PriceBreakdown(
        val baseAmount: Double,
        val platformFee: Double,
        val gst: Double,
        val totalAmount: Double,
        val hostEarning: Double
    )

    fun calculatePrice(pricePerHour: Double, startTime: Long, endTime: Long): PriceBreakdown {
        val hours = kotlin.math.max(1.0, (endTime - startTime).toDouble() / (1000 * 3600))
        val baseAmount = pricePerHour * hours
        val platformFee = baseAmount * 0.10
        val gst = platformFee * 0.18
        val totalAmount = baseAmount + platformFee + gst
        val hostEarning = baseAmount

        return PriceBreakdown(
            baseAmount = baseAmount,
            platformFee = platformFee,
            gst = gst,
            totalAmount = totalAmount,
            hostEarning = hostEarning
        )
    }
}
