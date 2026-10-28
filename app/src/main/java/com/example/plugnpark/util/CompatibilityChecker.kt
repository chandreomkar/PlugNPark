package com.example.plugnpark.util

import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.ListingWithDetails
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.domain.model.ConnectorType
import com.example.plugnpark.domain.model.ListingType

sealed class CompatibilityResult(
    val isUsable: Boolean,
    val reason: String
) {
    data class Compatible(val details: String = "Fully compatible with your vehicle") :
        CompatibilityResult(true, details)

    data class CompatibleSlow(val details: String = "Compatible, but charging speed may be limited") :
        CompatibilityResult(true, details)

    data class NotCompatible(val details: String = "Not compatible with your vehicle") :
        CompatibilityResult(false, details)

    data class NotAnEv(val details: String = "Selected vehicle is not an electric vehicle") :
        CompatibilityResult(false, details)
}

object CompatibilityChecker {

    fun check(vehicle: Vehicle?, listing: Listing): CompatibilityResult {
        if (vehicle == null) {
            return CompatibilityResult.Compatible("No vehicle specified")
        }

        if (listing.type == ListingType.PARKING) {
            return checkParking(vehicle, listing.vehicleType)
        }

        if (!vehicle.isEv) {
            return CompatibilityResult.NotAnEv("Your ${vehicle.make} ${vehicle.model} is not an electric vehicle.")
        }

        return checkCharger(vehicle, listing.connectorTypes)
    }

    fun check(vehicle: Vehicle?, listingWithDetails: ListingWithDetails): CompatibilityResult {
        if (vehicle == null) {
            return CompatibilityResult.Compatible("No vehicle specified")
        }

        if (listingWithDetails.listing.type == ListingType.PARKING) {
            val supported = listingWithDetails.parkingSpec?.vehicleTypes ?: "CAR"
            return checkParking(vehicle, supported)
        }

        if (!vehicle.isEv) {
            return CompatibilityResult.NotAnEv("Your ${vehicle.make} ${vehicle.model} is not an electric vehicle.")
        }

        val connectors = listingWithDetails.chargerSpec?.connectorTypes ?: emptyList()
        return checkCharger(vehicle, connectors)
    }

    private fun checkParking(vehicle: Vehicle, supportedTypesString: String): CompatibilityResult {
        val vType = vehicle.vehicleType.uppercase()
        val supportedUpper = supportedTypesString.uppercase()

        val isMatch = when {
            supportedUpper.contains("ALL") -> true
            vType.contains("SUV") && (supportedUpper.contains("SUV") || supportedUpper.contains("4-WHEELER") || supportedUpper.contains("CAR")) -> true
            vType.contains("CAR") && (supportedUpper.contains("CAR") || supportedUpper.contains("4-WHEELER") || supportedUpper.contains("SUV")) -> true
            vType.contains("BIKE") && (supportedUpper.contains("BIKE") || supportedUpper.contains("2-WHEELER")) -> true
            supportedUpper.contains(vType) -> true
            else -> false
        }

        return if (isMatch) {
            CompatibilityResult.Compatible("Spot fits your ${vehicle.make} ${vehicle.model} ($vType)")
        } else {
            CompatibilityResult.NotCompatible("Spot is designated for $supportedTypesString, but your vehicle is $vType")
        }
    }

    private fun checkCharger(vehicle: Vehicle, chargerConnectors: List<ConnectorType>): CompatibilityResult {
        if (chargerConnectors.isEmpty()) {
            return CompatibilityResult.NotCompatible("Charger connector information not available")
        }

        val vConn = vehicle.connectorType

        // Rule 1: CCS2 vehicle
        if (vConn == ConnectorType.CCS2) {
            if (chargerConnectors.contains(ConnectorType.CCS2)) {
                return CompatibilityResult.Compatible("Fast CCS2 DC/AC charging supported for your ${vehicle.make} ${vehicle.model}")
            }
            if (chargerConnectors.contains(ConnectorType.TYPE_2)) {
                return CompatibilityResult.CompatibleSlow("Type 2 AC plug fits your CCS2 inlet (AC charging only)")
            }
            if (chargerConnectors.contains(ConnectorType.THREE_PIN_15A)) {
                return CompatibilityResult.CompatibleSlow("15A 3-pin slow charging supported with portable cable")
            }
            return CompatibilityResult.NotCompatible("Charger offers ${chargerConnectors.joinToString { it.label }}, but your vehicle requires CCS2 or Type 2")
        }

        // Rule 2: Type 2 vehicle
        if (vConn == ConnectorType.TYPE_2) {
            if (chargerConnectors.contains(ConnectorType.TYPE_2)) {
                return CompatibilityResult.Compatible("Type 2 AC charging supported for your ${vehicle.make} ${vehicle.model}")
            }
            if (chargerConnectors.contains(ConnectorType.THREE_PIN_15A)) {
                return CompatibilityResult.CompatibleSlow("15A 3-pin slow charging supported with portable cable")
            }
            return CompatibilityResult.NotCompatible("Charger offers ${chargerConnectors.joinToString { it.label }}, but your vehicle requires Type 2 AC")
        }

        // Rule 3: CHAdeMO vehicle
        if (vConn == ConnectorType.CHADEMO) {
            if (chargerConnectors.contains(ConnectorType.CHADEMO)) {
                return CompatibilityResult.Compatible("CHAdeMO charging supported for your ${vehicle.make} ${vehicle.model}")
            }
            return CompatibilityResult.NotCompatible("Charger does not offer a CHAdeMO plug for your vehicle")
        }

        // Rule 4: 15A 3-Pin vehicle (2W / Small EV)
        if (vConn == ConnectorType.THREE_PIN_15A) {
            if (chargerConnectors.contains(ConnectorType.THREE_PIN_15A)) {
                return CompatibilityResult.CompatibleSlow("15A 3-pin slow socket compatible with your ${vehicle.make} ${vehicle.model}")
            }
            return CompatibilityResult.NotCompatible("Charger does not have a 15A 3-pin socket for your vehicle")
        }

        // Rule 5: Bharat AC001
        if (vConn == ConnectorType.BHARAT_AC001) {
            if (chargerConnectors.contains(ConnectorType.BHARAT_AC001)) {
                return CompatibilityResult.Compatible("Bharat AC-001 supported for your ${vehicle.make} ${vehicle.model}")
            }
            return CompatibilityResult.NotCompatible("Charger does not have a Bharat AC-001 plug")
        }

        // Fallback match
        if (chargerConnectors.contains(vConn)) {
            return CompatibilityResult.Compatible("Plug matches your vehicle connector")
        }

        return CompatibilityResult.NotCompatible("Incompatible connector types")
    }
}
