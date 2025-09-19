package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.models.UserContribution
import com.icescream.nestpay.data.repository.FirebaseContributionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ==========================================
// UI STATES
// ==========================================

sealed class ContributionUiState {
    object Loading : ContributionUiState()
    data class Success(val contributions: List<UserContribution>) : ContributionUiState()
    data class Error(val message: String) : ContributionUiState()
}

sealed class CreateContributionState {
    object Idle : CreateContributionState()
    object Loading : CreateContributionState()
    object Success : CreateContributionState()
    data class Error(val message: String) : CreateContributionState()
}

// ==========================================
// VIEW MODEL
// ==========================================

class ContributionViewModel(
    private val repository: FirebaseContributionRepository = FirebaseContributionRepository()
) : ViewModel() {

    // Contributions UI State
    private val _uiState = MutableStateFlow<ContributionUiState>(ContributionUiState.Loading)
    val uiState: StateFlow<ContributionUiState> = _uiState.asStateFlow()

    // Create Contribution State
    private val _createContributionState =
        MutableStateFlow<CreateContributionState>(CreateContributionState.Idle)
    val createContributionState: StateFlow<CreateContributionState> =
        _createContributionState.asStateFlow()

    // Currently loaded concept ID
    private val _currentConceptId = MutableStateFlow<String?>(null)

    // ==========================================
    // CONTRIBUTION OPERATIONS
    // ==========================================

    fun loadContributionsForConcept(conceptId: String) {
        viewModelScope.launch {
            _currentConceptId.value = conceptId
            _uiState.value = ContributionUiState.Loading

            repository.getContributionsForConcept(conceptId)
                .onSuccess { contributions ->
                    _uiState.value = ContributionUiState.Success(contributions)
                }
                .onFailure { exception ->
                    _uiState.value = ContributionUiState.Error(
                        exception.message ?: "Error desconocido al cargar contribuciones"
                    )
                }
        }
    }

    fun createContribution(
        conceptId: String,
        communityId: String,
        amount: Double,
        userName: String
    ) {
        viewModelScope.launch {
            println("🔍 ViewModel: Starting contribution creation")
            _createContributionState.value = CreateContributionState.Loading

            repository.createContribution(
                conceptId = conceptId,
                communityId = communityId,
                amount = amount,
                userName = userName
            ).onSuccess {
                println("✅ ViewModel: Contribution created successfully")
                _createContributionState.value = CreateContributionState.Success
                // Recargar las contribuciones para mostrar la nueva
                loadContributionsForConcept(conceptId)
            }.onFailure { exception ->
                println("❌ ViewModel: Failed to create contribution: ${exception.message}")
                _createContributionState.value = CreateContributionState.Error(
                    exception.message ?: "Error al crear la contribución"
                )
            }
        }
    }

    fun resetCreateContributionState() {
        _createContributionState.value = CreateContributionState.Idle
    }

    fun refresh() {
        _currentConceptId.value?.let { conceptId ->
            loadContributionsForConcept(conceptId)
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    fun getCurrentContributions(): List<UserContribution> {
        return when (val currentState = _uiState.value) {
            is ContributionUiState.Success -> currentState.contributions
            else -> emptyList()
        }
    }

    fun getContributionById(id: String): UserContribution? {
        return getCurrentContributions().find { it.id == id }
    }

    fun getTotalContributed(): Double {
        return getCurrentContributions().sumOf { it.amount }
    }

    fun getContributorsCount(): Int {
        return getCurrentContributions().distinctBy { it.userId }.size
    }

    fun getUserContribution(userId: String): UserContribution? {
        return getCurrentContributions().find { it.userId == userId }
    }

    fun hasUserContributed(userId: String): Boolean {
        return getCurrentContributions().any { it.userId == userId }
    }
}