package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.datastore.UserSession
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.local.entity.User
import com.example.plugnpark.data.local.entity.Vehicle
import com.example.plugnpark.data.repository.PlugNParkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class ProfileUiState(
    val user: User? = null,
    val selectedRole: String = "BOTH", // "SEEKER", "HOST", "BOTH"
    val userVehicles: List<Vehicle> = emptyList(),
    val defaultVehicle: Vehicle? = null,
    val isEditNameDialogOpen: Boolean = false,
    val isVehicleDialogOpen: Boolean = false,
    val vehicleToEdit: Vehicle? = null,
    val isFaqOpen: Boolean = false,
    val isAboutOpen: Boolean = false,
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val repository: PlugNParkRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _isEditNameDialogOpen = MutableStateFlow(false)
    private val _isVehicleDialogOpen = MutableStateFlow(false)
    private val _vehicleToEdit = MutableStateFlow<Vehicle?>(null)
    private val _isFaqOpen = MutableStateFlow(false)
    private val _isAboutOpen = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getUserById(session.userId)
        },
        sessionManager.userSessionFlow.flatMapLatest { session ->
            repository.getVehiclesByOwner(session.userId)
        },
        sessionManager.userSessionFlow,
        _isEditNameDialogOpen,
        _isVehicleDialogOpen,
        _vehicleToEdit,
        _isFaqOpen,
        _isAboutOpen,
        _userMessage
    ) { flows ->
        val user = flows[0] as User?
        val vehicles = flows[1] as List<Vehicle>
        val session = flows[2] as UserSession
        val editNameOpen = flows[3] as Boolean
        val vehicleDialogOpen = flows[4] as Boolean
        val vehicleToEdit = flows[5] as Vehicle?
        val faqOpen = flows[6] as Boolean
        val aboutOpen = flows[7] as Boolean
        val msg = flows[8] as String?

        val defaultVeh = vehicles.firstOrNull { it.isDefault } ?: vehicles.firstOrNull()

        ProfileUiState(
            user = user,
            selectedRole = session.userRole,
            userVehicles = vehicles,
            defaultVehicle = defaultVeh,
            isEditNameDialogOpen = editNameOpen,
            isVehicleDialogOpen = vehicleDialogOpen,
            vehicleToEdit = vehicleToEdit,
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
                val currentUserId = sessionManager.userSessionFlow.first().userId
                val existingUser = repository.getUserById(currentUserId).first()
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
        viewModelScope.launch {
            val session = sessionManager.userSessionFlow.first()
            sessionManager.saveLoginSession(session.userId, session.phoneNumber, newRole)
            _userMessage.value = "Switched primary role to ${if (newRole == "BOTH") "Seeker & Host" else newRole}"
        }
    }

    fun openAddVehicleDialog() {
        _vehicleToEdit.value = null
        _isVehicleDialogOpen.value = true
    }

    fun openEditVehicleDialog(vehicle: Vehicle) {
        _vehicleToEdit.value = vehicle
        _isVehicleDialogOpen.value = true
    }

    fun closeVehicleDialog() {
        _isVehicleDialogOpen.value = false
        _vehicleToEdit.value = null
    }

    fun saveVehicle(
        id: String?,
        make: String,
        model: String,
        type: String,
        isEv: Boolean,
        connector: com.example.plugnpark.domain.model.ConnectorType,
        batteryKwh: Double,
        plate: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            try {
                val currentUserId = sessionManager.userSessionFlow.first().userId
                val vehicleId = id ?: ("veh_" + UUID.randomUUID().toString().take(8))
                val vehicle = Vehicle(
                    id = vehicleId,
                    ownerId = currentUserId,
                    make = make,
                    model = model,
                    vehicleType = type,
                    isEv = isEv,
                    connectorType = connector,
                    batteryKwh = batteryKwh,
                    plateNumber = plate,
                    isDefault = isDefault
                )
                repository.insertVehicle(vehicle)
                closeVehicleDialog()
                _userMessage.value = "Vehicle saved successfully!"
            } catch (e: Exception) {
                _userMessage.value = "Error saving vehicle: ${e.message}"
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                repository.deleteVehicle(vehicleId)
                _userMessage.value = "Vehicle deleted"
            } catch (e: Exception) {
                _userMessage.value = "Error deleting vehicle: ${e.message}"
            }
        }
    }

    fun setDefaultVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = sessionManager.userSessionFlow.first().userId
                repository.setDefaultVehicle(currentUserId, vehicleId)
                _userMessage.value = "Default vehicle updated"
            } catch (e: Exception) {
                _userMessage.value = "Error setting default vehicle: ${e.message}"
            }
        }
    }

    fun openEditNameDialog() { _isEditNameDialogOpen.value = true }
    fun closeEditNameDialog() { _isEditNameDialogOpen.value = false }

    fun toggleFaq() { _isFaqOpen.value = !_isFaqOpen.value }
    fun toggleAbout() { _isAboutOpen.value = !_isAboutOpen.value }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
