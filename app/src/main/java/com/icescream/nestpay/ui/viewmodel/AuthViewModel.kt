package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.auth.AuthService
import com.icescream.nestpay.data.auth.UserInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userInfo: UserInfo) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authService: AuthService = AuthService()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val isLoggedIn = authService.isUserLoggedIn()
        _isLoggedIn.value = isLoggedIn

        if (isLoggedIn) {
            val userInfo = authService.getUserInfo()
            if (userInfo != null) {
                _authState.value = AuthState.Success(userInfo)
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            authService.signInAnonymously()
                .onSuccess { userId ->
                    val userInfo = authService.getUserInfo()
                    if (userInfo != null) {
                        _authState.value = AuthState.Success(userInfo)
                        _isLoggedIn.value = true
                    } else {
                        _authState.value =
                            AuthState.Error("Error al obtener información del usuario")
                    }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Error desconocido al iniciar sesión"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
                .onSuccess {
                    _authState.value = AuthState.Idle
                    _isLoggedIn.value = false
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(
                        exception.message ?: "Error al cerrar sesión"
                    )
                }
        }
    }

    fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }

    fun isAnonymousUser(): Boolean {
        return authService.isAnonymousUser()
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}