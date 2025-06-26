package com.youcef_bounaas.athlo.presentation.state

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    object NeedsUserInfo : AuthState()
}
