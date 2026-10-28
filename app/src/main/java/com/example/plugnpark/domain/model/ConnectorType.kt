package com.example.plugnpark.domain.model

enum class ConnectorType(val label: String) {
    TYPE_2("Type 2 (AC)"),
    CCS2("CCS2 (AC + DC fast)"),
    CHADEMO("CHAdeMO"),
    BHARAT_AC001("Bharat AC-001"),
    THREE_PIN_15A("15A 3-Pin (2W / slow)"),
    NONE("Not an EV")
}
