package com.example.plugnpark.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plugnpark.data.local.datastore.UserSessionManager
import com.example.plugnpark.data.local.entity.AppNotification
import com.example.plugnpark.data.repository.PlugNParkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModel(
    private val repository: PlugNParkRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    val uiState: StateFlow<NotificationsUiState> = sessionManager.userSessionFlow.flatMapLatest { session ->
        repository.getNotificationsByUser(session.userId).map { list ->
            NotificationsUiState(
                notifications = list,
                unreadCount = list.count { !it.isRead }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationsUiState()
    )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = sessionManager.userSessionFlow.first().userId
            repository.markAllNotificationsAsRead(userId)
        }
    }
}
