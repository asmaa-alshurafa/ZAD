package com.example.zad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zad.R
import com.example.zad.model.AppNotification
import com.example.zad.repository.NotificationRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    private var currentUid = ""
    private var listener: ListenerRegistration? = null

    fun setCurrentUser(uid: String?) {
        val nextUid = uid.orEmpty().takeUnless { it == "guest" }.orEmpty()
        if (nextUid == currentUid) return
        listener?.remove()
        listener = null
        currentUid = nextUid
        _notifications.value = emptyList()
        if (nextUid.isNotBlank()) {
            listener = repository.listenToUserNotifications(
                uid = nextUid,
                onNotificationsChanged = { _notifications.value = it },
                onError = { _errorMessage.value = R.string.app_error_notifications }
            )
        }
    }

    fun markAsRead(notificationId: String) {
        if (currentUid.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.markAsRead(currentUid, notificationId)
                _errorMessage.value = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _errorMessage.value = R.string.app_error_notifications
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        listener?.remove()
    }
}