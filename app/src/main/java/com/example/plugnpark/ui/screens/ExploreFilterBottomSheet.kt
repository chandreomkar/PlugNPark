package com.example.plugnpark.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FilterOptions(
    val maxPrice: Float = 300f,
    val maxDistanceKm: Float = 50f,
    val selectedConnector: String = "ALL", // "ALL", "Type 2", "CCS2", "15A 3-Pin"
    val isCoveredOnly: Boolean = false,
    val hasCCTVOnly: Boolean = false,
    val isVerifiedOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreFilterBottomSheet(
    sheetState: SheetState,
    initialFilters: FilterOptions,
    onDismiss: () -> Unit,
    onApplyFilters: (FilterOptions) -> Unit
) {
    var maxPrice by remember { mutableFloatStateOf(initialFilters.maxPrice) }
    var maxDistance by remember { mutableFloatStateOf(initialFilters.maxDistanceKm) }
    var selectedConnector by remember { mutableStateOf(initialFilters.selectedConnector) }
    var isCoveredOnly by remember { mutableStateOf(initialFilters.isCoveredOnly) }
    var hasCCTVOnly by remember { mutableStateOf(initialFilters.hasCCTVOnly) }
    var isVerifiedOnly by remember { mutableStateOf(initialFilters.isVerifiedOnly) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filter Spots & Chargers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Price Range Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Max Price per Hour", fontWeight = FontWeight.SemiBold)
                    Text("₹${maxPrice.toInt()}/hr", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    valueRange = 30f..300f,
                    steps = 27
                )
            }

            // Max Distance Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Max Distance", fontWeight = FontWeight.SemiBold)
                    Text("${maxDistance.toInt()} km", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = maxDistance,
                    onValueChange = { maxDistance = it },
                    valueRange = 1f..50f,
                    steps = 49
                )
            }

            // Connector Filter (for EV Chargers)
            Column {
                Text("EV Connector Type", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val connectors = listOf("ALL", "Type 2", "CCS2", "15A 3-Pin")
                    items(connectors) { connector ->
                        FilterChip(
                            selected = selectedConnector == connector,
                            onClick = { selectedConnector = connector },
                            label = { Text(if (connector == "ALL") "All Connectors" else connector) }
                        )
                    }
                }
            }

            // Amenities Checkboxes
            Column {
                Text("Amenities & Security", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isCoveredOnly, onCheckedChange = { isCoveredOnly = it })
                    Text("Covered / Shaded Parking")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasCCTVOnly, onCheckedChange = { hasCCTVOnly = it })
                    Text("24/7 CCTV Monitored")
                }
            }

            // Verified Only Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Verified Spots Only", fontWeight = FontWeight.SemiBold)
                    Text("Show only host-verified locations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isVerifiedOnly, onCheckedChange = { isVerifiedOnly = it })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        maxPrice = 300f
                        maxDistance = 50f
                        selectedConnector = "ALL"
                        isCoveredOnly = false
                        hasCCTVOnly = false
                        isVerifiedOnly = false
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        onApplyFilters(
                            FilterOptions(
                                maxPrice = maxPrice,
                                maxDistanceKm = maxDistance,
                                selectedConnector = selectedConnector,
                                isCoveredOnly = isCoveredOnly,
                                hasCCTVOnly = hasCCTVOnly,
                                isVerifiedOnly = isVerifiedOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
