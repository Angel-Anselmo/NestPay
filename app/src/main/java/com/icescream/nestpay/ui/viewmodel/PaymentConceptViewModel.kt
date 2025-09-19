package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.models.PaymentConcept
import com.icescream.nestpay.data.repository.FirebasePaymentConceptRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ==========================================
// UI STATES
// ==========================================

sealed class PaymentConceptUiState {
    object Loading : PaymentConceptUiState()
    data class Success(val concepts: List<PaymentConcept>) : PaymentConceptUiState()
    data class Error(val message: String) : PaymentConceptUiState()
}

sealed class CreateConceptState {
    object Idle : CreateConceptState()
    object Loading : CreateConceptState()
    object Success : CreateConceptState()
    data class Error(val message: String) : CreateConceptState()
}

// ==========================================
// VIEW MODEL
// ==========================================

class PaymentConceptViewModel(
    private val repository: FirebasePaymentConceptRepository = FirebasePaymentConceptRepository()
) : ViewModel() {

    // Payment Concepts UI State
    private val _uiState = MutableStateFlow<PaymentConceptUiState>(PaymentConceptUiState.Loading)
    val uiState: StateFlow<PaymentConceptUiState> = _uiState.asStateFlow()

    // Create Concept State
    private val _createConceptState = MutableStateFlow<CreateConceptState>(CreateConceptState.Idle)
    val createConceptState: StateFlow<CreateConceptState> = _createConceptState.asStateFlow()

    // Currently loaded community ID
    private val _currentCommunityId = MutableStateFlow<String?>(null)

    // ==========================================
    // CONCEPT OPERATIONS
    // ==========================================

    fun loadPaymentConcepts(communityId: String) {
        viewModelScope.launch {
            _currentCommunityId.value = communityId
            _uiState.value = PaymentConceptUiState.Loading

            repository.getPaymentConcepts(communityId)
                .onSuccess { concepts ->
                    _uiState.value = PaymentConceptUiState.Success(concepts)
                }
                .onFailure { exception ->
                    _uiState.value = PaymentConceptUiState.Error(
                        exception.message ?: "Error desconocido al cargar conceptos"
                    )
                }
        }
    }

    fun createPaymentConcept(
        communityId: String,
        name: String,
        description: String,
        targetAmount: Double,
        dueDate: String
    ) {
        viewModelScope.launch {
            _createConceptState.value = CreateConceptState.Loading

            repository.createPaymentConcept(
                communityId = communityId,
                name = name,
                description = description,
                targetAmount = targetAmount,
                dueDate = dueDate
            ).onSuccess {
                _createConceptState.value = CreateConceptState.Success
                // Recargar los conceptos para mostrar el nuevo
                loadPaymentConcepts(communityId)
            }.onFailure { exception ->
                _createConceptState.value = CreateConceptState.Error(
                    exception.message ?: "Error al crear el concepto"
                )
            }
        }
    }

    fun resetCreateConceptState() {
        _createConceptState.value = CreateConceptState.Idle
    }

    fun refresh() {
        _currentCommunityId.value?.let { communityId ->
            loadPaymentConcepts(communityId)
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    fun getCurrentConcepts(): List<PaymentConcept> {
        return when (val currentState = _uiState.value) {
            is PaymentConceptUiState.Success -> currentState.concepts
            else -> emptyList()
        }
    }

    fun getConceptById(id: String): PaymentConcept? {
        return getCurrentConcepts().find { it.id == id }
    }

    fun getTotalTargetAmount(): Double {
        return getCurrentConcepts().sumOf { it.targetAmount }
    }

    fun getTotalCurrentAmount(): Double {
        return getCurrentConcepts().sumOf { it.currentAmount }
    }

    fun getActiveConceptsCount(): Int {
        return getCurrentConcepts().count { it.status.name == "ACTIVE" }
    }
}