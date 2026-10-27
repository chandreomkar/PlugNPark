package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.repository.PlugNParkRepository

class ViewModelFactory(
    private val repository: PlugNParkRepository,
    private val sessionManager: UserSessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                ExploreViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(BookingsViewModel::class.java) -> {
                BookingsViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(HostViewModel::class.java) -> {
                HostViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                BookingViewModel(repository, sessionManager) as T
            }
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                NotificationsViewModel(repository, sessionManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
