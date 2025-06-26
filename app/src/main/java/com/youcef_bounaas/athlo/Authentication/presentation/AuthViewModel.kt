package com.youcef_bounaas.athlo.Authentication.presentation

import androidx.lifecycle.ViewModel
import com.youcef_bounaas.athlo.Authentication.domain.use_case.SignOutUseCase
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.youcef_bounaas.athlo.Authentication.domain.use_case.SignInUseCase
import com.youcef_bounaas.athlo.Authentication.domain.use_case.SignUpUseCase



class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _signinResult = MutableStateFlow<Result<Unit>?>(null)
    val signinResult: StateFlow<Result<Unit>?> = _signinResult.asStateFlow()

    private val _signupResult = MutableStateFlow<Result<Unit>?>(null)
    val signupResult: StateFlow<Result<Unit>?> = _signupResult.asStateFlow()

    private val _signoutState = MutableStateFlow<Result<Unit>?>(null)
    val signoutState: StateFlow<Result<Unit>?> = _signoutState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _signinResult.value = signInUseCase(email, password)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _signupResult.value = signUpUseCase(email, password)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _signoutState.value = signOutUseCase()
        }
    }

    fun clearLoginResult() {
        _signinResult.value = null
    }

    fun clearSignupResult() {
        _signupResult.value = null
    }

    fun clearLogoutResult() {
        _signoutState.value = null
    }
}