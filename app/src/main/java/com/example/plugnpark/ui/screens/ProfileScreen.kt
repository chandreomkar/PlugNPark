package com.example.plugnpark.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plugnpark.domain.model.ConnectorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Card
            val user = uiState.user
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = user?.name ?: "Rahul Sharma",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.openEditNameDialog() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified Member",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileDetailItem(icon = Icons.Default.Phone, title = "Phone", value = user?.phone ?: "+91 98765 43210")
                        ProfileDetailItem(icon = Icons.Default.Email, title = "Email", value = user?.email ?: "rahul@example.com")
                    }
                }
            }

            // Role Switcher Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active App Role", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("BOTH" to "Both Role", "SEEKER" to "Seeker Only", "HOST" to "Host Only").forEach { (roleKey, label) ->
                            FilterChip(
                                selected = uiState.selectedRole == roleKey,
                                onClick = { viewModel.updateRole(roleKey) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // My Vehicles Section (Add / Edit / Delete & Default Vehicle)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("My Vehicles (${uiState.userVehicles.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.openAddVehicleDialog() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Vehicle")
                        }
                    }

                    if (uiState.userVehicles.isEmpty()) {
                        Text("No vehicles added yet. Add your EV or car for quick compatibility checking!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    } else {
                        uiState.userVehicles.forEach { vehicle ->
                            VehicleCardItem(
                                vehicle = vehicle,
                                isDefault = vehicle.id == uiState.defaultVehicle?.id,
                                onEdit = { viewModel.openEditVehicleDialog(vehicle) },
                                onDelete = { viewModel.deleteVehicle(vehicle.id) },
                                onSetDefault = { viewModel.setDefaultVehicle(vehicle.id) }
                            )
                        }
                    }
                }
            }

            // Expandable FAQ Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleFaq() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Frequently Asked Questions (FAQ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Icon(
                            imageVector = if (uiState.isFaqOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle FAQ"
                        )
                    }

                    AnimatedVisibility(visible = uiState.isFaqOpen) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FaqItem("Q: How does home EV charger rental work?", "A: Hosts list their private home EV charger when idle. Seekers reserve a time slot, park, charge, and pay seamlessly through PlugNPark.")
                            FaqItem("Q: Is my private parking spot safe?", "A: Yes! All hosts and seekers undergo phone verification. You set your availability, price, and access instructions.")
                            FaqItem("Q: What are the cancellation and refund rules?", "A: Full refunds are issued if cancelled > 1 hour before the reserved start time.")
                            FaqItem("Q: How are host earnings paid out?", "A: Earnings accumulate in your host dashboard and are automatically disbursed via UPI to your bank account weekly.")
                        }
                    }
                }
            }

            // About PlugNPark Page
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAbout() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("About PlugNPark India", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Icon(
                            imageVector = if (uiState.isAboutOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle About"
                        )
                    }

                    AnimatedVisibility(visible = uiState.isAboutOpen) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "PlugNPark is India's premier peer-to-peer marketplace connecting EV owners with private home chargers and unused parking spots across Mumbai, Pune, and major Indian cities. By unlocking idle residential driveways and wallboxes, PlugNPark makes urban EV charging effortless while helping homeowners earn daily income.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Session", fontWeight = FontWeight.Bold)
            }
        }

        // Edit Name Dialog
        if (uiState.isEditNameDialogOpen) {
            var newName by remember { mutableStateOf(uiState.user?.name ?: "Rahul Sharma") }
            AlertDialog(
                onDismissRequest = { viewModel.closeEditNameDialog() },
                title = { Text("Edit Display Name", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.updateUserName(newName)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeEditNameDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add / Edit Vehicle Dialog
        if (uiState.isVehicleDialogOpen) {
            val vToEdit = uiState.vehicleToEdit
            var make by remember { mutableStateOf(vToEdit?.make ?: "Tata") }
            var model by remember { mutableStateOf(vToEdit?.model ?: "Nexon EV") }
            var type by remember { mutableStateOf(vToEdit?.vehicleType ?: "SUV") }
            var isEv by remember { mutableStateOf(vToEdit?.isEv ?: true) }
            var connector by remember { mutableStateOf(vToEdit?.connectorType ?: ConnectorType.CCS2) }
            var batteryText by remember { mutableStateOf(vToEdit?.batteryKwh?.toString() ?: "40.5") }
            var maxAcText by remember { mutableStateOf(vToEdit?.maxAcChargeKw?.toString() ?: "7.2") }
            var plate by remember { mutableStateOf(vToEdit?.plateNumber ?: "MH 02 FZ 8899") }
            var isDefault by remember { mutableStateOf(vToEdit?.isDefault ?: false) }

            var presetExpanded by remember { mutableStateOf(false) }
            var typeExpanded by remember { mutableStateOf(false) }
            var connectorExpanded by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { viewModel.closeVehicleDialog() },
                title = { Text(if (vToEdit == null) "Add Vehicle" else "Edit Vehicle", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Preset Picker Dropdown
                        ExposedDropdownMenuBox(
                            expanded = presetExpanded,
                            onExpandedChange = { presetExpanded = !presetExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "Select Popular Preset...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("⚡ Preset Picker (Popular Indian EVs)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = presetExpanded,
                                onDismissRequest = { presetExpanded = false }
                            ) {
                                com.example.plugnpark.data.VehiclePresets.INDIAN_EV_PRESETS.forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text("${preset.displayName} (${preset.vehicleType})") },
                                        onClick = {
                                            make = preset.make
                                            model = preset.model
                                            type = preset.vehicleType
                                            isEv = true
                                            connector = preset.connectorType
                                            batteryText = preset.batteryKwh.toString()
                                            maxAcText = preset.maxAcChargeKw.toString()
                                            presetExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = com.example.plugnpark.data.VehiclePresets.DISCLAIMER,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Make") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.weight(1f))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Vehicle Type Dropdown
                            ExposedDropdownMenuBox(
                                expanded = typeExpanded,
                                onExpandedChange = { typeExpanded = !typeExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = type,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Type") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = typeExpanded,
                                    onDismissRequest = { typeExpanded = false }
                                ) {
                                    listOf("CAR", "SUV", "BIKE").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                type = option
                                                typeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Plate No.") }, modifier = Modifier.weight(1f))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isEv, onCheckedChange = { isEv = it })
                            Text("Electric Vehicle (EV)")
                        }

                        if (isEv) {
                            // Connector Dropdown
                            ExposedDropdownMenuBox(
                                expanded = connectorExpanded,
                                onExpandedChange = { connectorExpanded = !connectorExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = connector.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Connector") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = connectorExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = connectorExpanded,
                                    onDismissRequest = { connectorExpanded = false }
                                ) {
                                    ConnectorType.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                connector = option
                                                connectorExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = batteryText,
                                    onValueChange = { batteryText = it },
                                    label = { Text("Battery (kWh)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = maxAcText,
                                    onValueChange = { maxAcText = it },
                                    label = { Text("Max AC (kW)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                            Text("Set as Primary Default Vehicle")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (make.isNotBlank() && model.isNotBlank()) {
                                viewModel.saveVehicle(
                                    id = vToEdit?.id,
                                    make = make,
                                    model = model,
                                    type = type,
                                    isEv = isEv,
                                    connector = connector,
                                    batteryKwh = batteryText.toDoubleOrNull() ?: 40.0,
                                    maxAcChargeKw = maxAcText.toDoubleOrNull() ?: 7.4,
                                    plate = plate,
                                    isDefault = isDefault
                                )
                            }
                        }
                    ) {
                        Text("Save Vehicle")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeVehicleDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun VehicleCardItem(
    vehicle: Vehicle,
    isDefault: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${vehicle.make} ${vehicle.model}", fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDefault) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "✔ Primary",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        TextButton(onClick = onSetDefault) {
                            Text("Set Default", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
                    val connectorsString = if (vehicle.isEv) {
                        "EV (${vehicle.connectorType.label}, ${vehicle.batteryKwh} kWh)"
                    } else {
                        "Petrol/Diesel"
                    }
                    Text(
                        text = "${vehicle.vehicleType} • $connectorsString • ${vehicle.plateNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
        }
    }
}

@Composable
fun ProfileDetailItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Column {
        Text(question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(answer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
