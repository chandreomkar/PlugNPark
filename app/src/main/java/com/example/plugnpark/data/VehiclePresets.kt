package com.example.plugnpark.data

import com.example.plugnpark.domain.model.ConnectorType

data class VehiclePreset(
    val make: String,
    val model: String,
    val vehicleType: String, // "SUV", "CAR", "BIKE"
    val connectorType: ConnectorType,
    val batteryKwh: Double,
    val maxAcChargeKw: Double
) {
    val displayName: String get() = "$make $model"
}

object VehiclePresets {
    const val DISCLAIMER = "Approximate specs from public data. You can edit any value below."

    val INDIAN_EV_PRESETS = listOf(
        VehiclePreset("Tata", "Nexon EV Empowered+", "SUV", ConnectorType.CCS2, 40.5, 7.2),
        VehiclePreset("Tata", "Tiago EV", "CAR", ConnectorType.CCS2, 24.0, 7.2),
        VehiclePreset("Tata", "Punch EV", "SUV", ConnectorType.CCS2, 35.0, 7.2),
        VehiclePreset("Tata", "Tigor EV", "CAR", ConnectorType.CCS2, 26.0, 7.2),
        VehiclePreset("MG", "ZS EV", "SUV", ConnectorType.CCS2, 50.3, 7.4),
        VehiclePreset("MG", "Comet EV", "CAR", ConnectorType.TYPE_2, 17.3, 3.3),
        VehiclePreset("Mahindra", "XUV400 EV", "SUV", ConnectorType.CCS2, 39.4, 7.2),
        VehiclePreset("Hyundai", "Kona Electric", "SUV", ConnectorType.CCS2, 39.2, 7.2),
        VehiclePreset("BYD", "Atto 3", "SUV", ConnectorType.CCS2, 60.48, 7.0),
        VehiclePreset("Ather", "450X Gen 3", "BIKE", ConnectorType.THREE_PIN_15A, 3.7, 1.2),
        VehiclePreset("Ola", "S1 Pro Gen 2", "BIKE", ConnectorType.THREE_PIN_15A, 4.0, 1.0),
        VehiclePreset("TVS", "iQube ST", "BIKE", ConnectorType.THREE_PIN_15A, 5.1, 0.95),
        VehiclePreset("Bajaj", "Chetak 2901", "BIKE", ConnectorType.THREE_PIN_15A, 2.9, 0.8)
    )
}
