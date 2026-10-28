package com.example.plugnpark

import com.example.plugnpark.data.local.entity.Listing
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.domain.model.ConnectorType
import com.example.plugnpark.domain.model.ListingType
import com.example.plugnpark.util.CompatibilityChecker
import com.example.plugnpark.util.CompatibilityResult
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatibilityCheckerTest {

    private fun createVehicle(
        make: String = "Tata",
        model: String = "Nexon EV",
        vehicleType: String = "SUV",
        isEv: Boolean = true,
        connectorType: ConnectorType = ConnectorType.CCS2
    ): Vehicle {
        return Vehicle(
            id = "v1",
            ownerId = "u1",
            make = make,
            model = model,
            vehicleType = vehicleType,
            isEv = isEv,
            connectorType = connectorType,
            batteryKwh = 40.0,
            plateNumber = "MH021234"
        )
    }

    private fun createChargerListing(connectors: List<ConnectorType>): Listing {
        return Listing(
            id = "l1",
            hostId = "h1",
            type = ListingType.EV_CHARGER,
            title = "Test Charger",
            description = "Desc",
            address = "Addr",
            city = "Mumbai",
            area = "Bandra",
            lat = 19.0,
            lng = 72.0,
            pricePerHourPaise = 10000L,
            connectorTypes = connectors
        )
    }

    private fun createParkingListing(supportedVehicleType: String): Listing {
        return Listing(
            id = "l2",
            hostId = "h1",
            type = ListingType.PARKING,
            title = "Test Parking",
            description = "Desc",
            address = "Addr",
            city = "Mumbai",
            area = "Bandra",
            lat = 19.0,
            lng = 72.0,
            pricePerHourPaise = 5000L,
            vehicleType = supportedVehicleType
        )
    }

    @Test
    fun check_CCS2Vehicle_CCS2Charger_ReturnsCompatible() {
        val vehicle = createVehicle(connectorType = ConnectorType.CCS2)
        val listing = createChargerListing(listOf(ConnectorType.CCS2))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.Compatible)
        assertTrue(result.isUsable)
    }

    @Test
    fun check_CCS2Vehicle_Type2Charger_ReturnsCompatibleSlow() {
        val vehicle = createVehicle(connectorType = ConnectorType.CCS2)
        val listing = createChargerListing(listOf(ConnectorType.TYPE_2))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.CompatibleSlow)
        assertTrue(result.isUsable)
    }

    @Test
    fun check_CCS2Vehicle_CHAdeMOCharger_ReturnsNotCompatible() {
        val vehicle = createVehicle(connectorType = ConnectorType.CCS2)
        val listing = createChargerListing(listOf(ConnectorType.CHADEMO))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.NotCompatible)
        assertTrue(!result.isUsable)
    }

    @Test
    fun check_Type2Vehicle_Type2Charger_ReturnsCompatible() {
        val vehicle = createVehicle(connectorType = ConnectorType.TYPE_2)
        val listing = createChargerListing(listOf(ConnectorType.TYPE_2))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.Compatible)
        assertTrue(result.isUsable)
    }

    @Test
    fun check_Type2Vehicle_CHAdeMOCharger_ReturnsNotCompatible() {
        val vehicle = createVehicle(connectorType = ConnectorType.TYPE_2)
        val listing = createChargerListing(listOf(ConnectorType.CHADEMO))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.NotCompatible)
        assertTrue(!result.isUsable)
    }

    @Test
    fun check_CHAdeMOVehicle_CHAdeMOCharger_ReturnsCompatible() {
        val vehicle = createVehicle(connectorType = ConnectorType.CHADEMO)
        val listing = createChargerListing(listOf(ConnectorType.CHADEMO))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.Compatible)
        assertTrue(result.isUsable)
    }

    @Test
    fun check_3PinVehicle_3PinCharger_ReturnsCompatibleSlow() {
        val vehicle = createVehicle(connectorType = ConnectorType.THREE_PIN_15A)
        val listing = createChargerListing(listOf(ConnectorType.THREE_PIN_15A))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.CompatibleSlow)
        assertTrue(result.isUsable)
    }

    @Test
    fun check_NonEVVehicle_ChargerListing_ReturnsNotAnEV() {
        val vehicle = createVehicle(isEv = false)
        val listing = createChargerListing(listOf(ConnectorType.CCS2))

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.NotAnEv)
        assertTrue(!result.isUsable)
    }

    @Test
    fun check_SUVVehicle_SUVParking_ReturnsCompatible() {
        val vehicle = createVehicle(vehicleType = "SUV")
        val listing = createParkingListing("SUV / 4-Wheeler")

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.Compatible)
        assertTrue(result.isUsable)
    }

    @Test
    fun check_SUVVehicle_BikeParking_ReturnsNotCompatible() {
        val vehicle = createVehicle(vehicleType = "SUV")
        val listing = createParkingListing("BIKE ONLY")

        val result = CompatibilityChecker.check(vehicle, listing)

        assertTrue(result is CompatibilityResult.NotCompatible)
        assertTrue(!result.isUsable)
    }
}
