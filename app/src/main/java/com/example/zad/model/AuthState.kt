package com.example.zad.model

import androidx.annotation.StringRes

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: User) : AuthState
    data object Guest : AuthState
    data class Error(@StringRes val message: Int) : AuthState
}