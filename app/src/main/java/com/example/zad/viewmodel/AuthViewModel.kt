package com.example.zad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zad.R
import com.example.zad.model.AuthState
import com.example.zad.model.User
import com.example.zad.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _sessionResolved = MutableStateFlow(false)
    val sessionResolved: StateFlow<Boolean> = _sessionResolved.asStateFlow()

    private val _hasCurrentSession = MutableStateFlow(false)
    val hasCurrentSession: StateFlow<Boolean> = _hasCurrentSession.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uid = repository.currentUid
                _hasCurrentSession.value = uid != null
                _sessionResolved.value = true
                if (uid == null) {
                    _authState.value = AuthState.Idle
                } else {
                    loadProfileOnIo(uid)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _sessionResolved.value = true
                _authState.value = AuthState.Error(R.string.auth_session_error)
            }
        }
    }

    fun signIn(email: String, password: String) {
        runAuthAction(
            errorMessage = R.string.auth_signin_error,
            action = { repository.signIn(email, password) },
            afterAuthenticated = { authUser ->
                try {
                    val profile = repository.fetchUser(authUser.uid, authUser.email)
                    _currentUser.value = profile
                    _authState.value = AuthState.Authenticated(profile)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    Log.w(TAG, "Could not load profile; continuing with Firebase Auth profile", error)
                }
            }
        )
    }

    fun signUp(fullName: String, email: String, major: String, phone: String, password: String) {
        runAuthAction(
            errorMessage = R.string.auth_signup_error,
            action = { repository.signUp(fullName, email, major, phone, password) },
            afterAuthenticated = repository::persistUserProfile
        )
    }

    fun continueAsGuest() {
        _currentUser.value = null
        _hasCurrentSession.value = false
        _sessionResolved.value = true
        _authState.value = AuthState.Guest
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.signOut()
                _currentUser.value = null
                _hasCurrentSession.value = false
                _authState.value = AuthState.Idle
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _authState.value = AuthState.Error(R.string.auth_signout_error)
            }
        }
    }

    fun retryProfileLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            val uid = repository.currentUid
            if (uid != null) loadProfileOnIo(uid)
        }
    }

    private suspend fun loadProfileOnIo(uid: String) {
        _authState.value = AuthState.Loading
        try {
            val user = repository.fetchUser(uid)
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            _authState.value = AuthState.Error(R.string.auth_profile_error)
        }
    }

    private fun runAuthAction(
        errorMessage: Int,
        action: suspend () -> User,
        afterAuthenticated: suspend (User) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = AuthState.Loading
            try {
                val user = action()
                _currentUser.value = user
                _hasCurrentSession.value = true
                _sessionResolved.value = true
                _authState.value = AuthState.Authenticated(user)
                try {
                    afterAuthenticated(user)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (profileError: Exception) {
                    Log.w(TAG, "Authenticated; continuing after Firestore profile sync failure", profileError)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    private companion object {
        const val TAG = "AuthViewModel"
    }
}