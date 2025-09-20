package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.models.UserContribution
import com.icescream.nestpay.data.repository.FirebaseContributionRepository
import com.icescream.nestpay.data.repository.PaymentRepository
import com.icescream.nestpay.network.ApiResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

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
    private val repository: FirebaseContributionRepository = FirebaseContributionRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "ContributionViewModel"
    }

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

    /**
     * Create contribution using the new payment API flow with backend
     */
    fun createContribution(
        conceptId: String,
        communityId: String,
        amount: Double,
        userName: String
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🔍 Iniciando pago via backend local")
            _createContributionState.value = CreateContributionState.Loading

            // Use the new payment repository method for community payments
            when (val result = paymentRepository.initiateCommunityPayment(
                amount = amount.toString(),
                communityId = communityId,
                conceptId = conceptId,
                description = "Aporte de $userName a concepto $conceptId"
            )) {
                is ApiResult.Success -> {
                    Log.d(TAG, "✅ Pago iniciado exitosamente: ${result.data}")
                    _createContributionState.value = CreateContributionState.Success
                    // Recargar las contribuciones para mostrar la nueva
                    loadContributionsForConcept(conceptId)
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "❌ Error al iniciar pago: ${result.message}")
                    _createContributionState.value = CreateContributionState.Error(
                        "Error al procesar el pago: ${result.message}"
                    )
                }

                is ApiResult.Loading -> {
                    Log.d(TAG, "⏳ Procesando pago...")
                    // Ya está en estado Loading
                }
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