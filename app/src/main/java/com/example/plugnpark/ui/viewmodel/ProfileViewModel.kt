package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.repository.PlugNParkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val selectedRole: String = "BOTH", // "SEEKER", "HOST", "BOTH"
    val vehicleName: String = "Tata Nexon EV Empowered+",
    val connectorType: String = "Type 2 / CCS2",
    val vehicleType: String = "4-Wheeler / SUV",
    val vehicleNumber: String = "MH 02 FZ 8899",
    val isEditNameDialogOpen: Boolean = false,
    val isEditVehicleDialogOpen: Boolean = false,
    val isFaqOpen: Boolean = false,
    val isAboutOpen: Boolean = false,
    val userMessage: String? = null
)

class ProfileViewModel(
    private val repository: PlugNParkRepository
) : ViewModel() {

    private val _selectedRole = MutableStateFlow("BOTH")
    private val _vehicleName = MutableStateFlow("Tata Nexon EV Empowered+")
    private val _connectorType = MutableStateFlow("Type 2 / CCS2")
    private val _vehicleType = MutableStateFlow("4-Wheeler / SUV")
    private val _vehicleNumber = MutableStateFlow("MH 02 FZ 8899")
    private val _isEditNameDialogOpen = MutableStateFlow(false)
    private val _isEditVehicleDialogOpen = MutableStateFlow(false)
    private val _isFaqOpen = MutableStateFlow(false)
    private val _isAboutOpen = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.getUserById("user_seeker_1"),
        _selectedRole,
        _vehicleName,
        _connectorType,
        _vehicleType,
        _vehicleNumber,
        _isEditNameDialogOpen,
        _isEditVehicleDialogOpen,
        _isFaqOpen,
        _isAboutOpen,
        _userMessage
    ) { flows ->
        val user = flows[0] as User?
        val role = flows[1] as String
        val vName = flows[2] as String
        val cType = flows[3] as String
        val vType = flows[4] as String
        val vNum = flows[5] as String
        val editNameOpen = flows[6] as Boolean
        val editVehicleOpen = flows[7] as Boolean
        val faqOpen = flows[8] as Boolean
        val aboutOpen = flows[9] as Boolean
        val msg = flows[10] as String?

        ProfileUiState(
            user = user,
            selectedRole = role,
            vehicleName = vName,
            connectorType = cType,
            vehicleType = vType,
            vehicleNumber = vNum,
            isEditNameDialogOpen = editNameOpen,
            isEditVehicleDialogOpen = editVehicleOpen,
            isFaqOpen = faqOpen,
            isAboutOpen = aboutOpen,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            try {
                val existingUser = repository.getUserById("user_seeker_1").first()
                if (existingUser != null) {
                    val updated = existingUser.copy(name = newName)
                    repository.insertUser(updated)
                    _isEditNameDialogOpen.value = false
                    _userMessage.value = "Name updated to '$newName'"
                }
            } catch (e: Exception) {
                _userMessage.value = "Failed to update name: ${e.message}"
            }
        }
    }

    fun updateRole(newRole: String) {
        _selectedRole.value = newRole
        _userMessage.value = "Switched primary role to ${if (newRole == "BOTH") "Seeker & Host" else newRole}"
    }

    fun updateVehicleDetails(
        name: String,
        connector: String,
        type: String,
        number: String
    ) {
        _vehicleName.value = name
        _connectorType.value = connector
        _vehicleType.value = type
        _vehicleNumber.value = number
        _isEditVehicleDialogOpen.value = false
        _userMessage.value = "Vehicle details updated successfully!"
    }

    fun openEditNameDialog() { _isEditNameDialogOpen.value = true }
    fun closeEditNameDialog() { _isEditNameDialogOpen.value = false }

    fun openEditVehicleDialog() { _isEditVehicleDialogOpen.value = true }
    fun closeEditVehicleDialog() { _isEditVehicleDialogOpen.value = false }

    fun toggleFaq() { _isFaqOpen.value = !_isFaqOpen.value }
    fun toggleAbout() { _isAboutOpen.value = !_isAboutOpen.value }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
