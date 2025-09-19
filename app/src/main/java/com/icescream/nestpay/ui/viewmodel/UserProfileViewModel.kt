package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.models.UserProfile
import com.icescream.nestpay.data.repository.FirebaseUserProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ==========================================
// UI STATES
// ==========================================

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(val profile: UserProfile) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
    object NotFound : UserProfileUiState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

// ==========================================
// VIEW MODEL
// ==========================================

class UserProfileViewModel(
    private val repository: FirebaseUserProfileRepository = FirebaseUserProfileRepository()
) : ViewModel() {

    // User Profile State
    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    // Update Profile State
    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState.asStateFlow()

    // Convenient access to user profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadUserProfile()
    }

    // ==========================================
    // PROFILE OPERATIONS
    // ==========================================

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading

            repository.getUserProfile()
                .onSuccess { profile ->
                    if (profile != null) {
                        _uiState.value = UserProfileUiState.Success(profile)
                        _userProfile.value = profile
                    } else {
                        _uiState.value = UserProfileUiState.NotFound
                        _userProfile.value = null
                    }
                }
                .onFailure { exception ->
                    _uiState.value = UserProfileUiState.Error(
                        exception.message ?: "Error desconocido al cargar perfil"
                    )
                    _userProfile.value = null
                }
        }
    }

    fun updateMainWalletAddress(walletAddress: String) {
        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading

            repository.updateMainWalletAddress(walletAddress)
                .onSuccess { updatedProfile ->
                    _updateProfileState.value = UpdateProfileState.Success
                    _userProfile.value = updatedProfile
                    _uiState.value = UserProfileUiState.Success(updatedProfile)
                }
                .onFailure { exception ->
                    _updateProfileState.value = UpdateProfileState.Error(
                        exception.message ?: "Error al actualizar wallet"
                    )
                }
        }
    }

    fun updateProfile(
        displayName: String? = null,
        email: String? = null,
        mainWalletAddress: String? = null
    ) {
        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading

            repository.createOrUpdateUserProfile(
                displayName = displayName,
                email = email,
                mainWalletAddress = mainWalletAddress
            ).onSuccess { updatedProfile ->
                _updateProfileState.value = UpdateProfileState.Success
                _userProfile.value = updatedProfile
                _uiState.value = UserProfileUiState.Success(updatedProfile)
            }.onFailure { exception ->
                _updateProfileState.value = UpdateProfileState.Error(
                    exception.message ?: "Error al actualizar perfil"
                )
            }
        }
    }

    fun hasMainWalletConfigured(): Boolean {
        return !_userProfile.value?.mainWalletAddress.isNullOrBlank()
    }

    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }

    fun refresh() {
        loadUserProfile()
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    fun getCurrentUserProfile(): UserProfile? {
        return _userProfile.value
    }

    fun getMainWalletAddress(): String? {
        return _userProfile.value?.mainWalletAddress
    }
}