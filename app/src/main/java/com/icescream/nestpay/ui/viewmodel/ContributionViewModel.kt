package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.models.UserContribution
import com.icescream.nestpay.data.repository.FirebaseContributionRepository
import com.icescream.nestpay.data.repository.CommunityPaymentRepository
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
    object PaymentInitiated : CreateContributionState()
    data class AuthorizationRequired(
        val authorizationUrl: String,
        val paymentData: PaymentProcessData
    ) : CreateContributionState()
    object Success : CreateContributionState()
    data class Error(val message: String) : CreateContributionState()
}

// Data class para mantener información del proceso de pago
data class PaymentProcessData(
    val continueUri: String,
    val continueAccessToken: String,
    val quoteId: String,
    val incomingPaymentId: String,
    val communityId: String,
    val conceptId: String,
    val amount: String
)

// ==========================================
// VIEW MODEL
// ==========================================

class ContributionViewModel(
    private val repository: FirebaseContributionRepository = FirebaseContributionRepository(),
    private val paymentRepository: CommunityPaymentRepository = CommunityPaymentRepository()
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
     * PASO 1: Iniciar proceso de contribución
     * Este método inicia el pago y devuelve la URL de autorización
     */
    fun createContribution(
        conceptId: String,
        communityId: String,
        amount: Double,
        userName: String
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🔍 Iniciando proceso de contribución...")
            _createContributionState.value = CreateContributionState.Loading

            when (val result = paymentRepository.initiateCommunityPayment(
                amount = amount.toString(),
                communityId = communityId,
                conceptId = conceptId,
                description = "Aporte de $userName a concepto $conceptId"
            )) {
                is ApiResult.Success -> {
                    val paymentData = result.data.data
                    Log.d(TAG, "✅ Pago iniciado: ${paymentData.incomingPaymentId}")
                    Log.d(TAG, "🔗 URL autorización: ${paymentData.authorizationUrl}")

                    // Guardar datos para el siguiente paso
                    val processData = PaymentProcessData(
                        continueUri = paymentData.continueData.continueUri,
                        continueAccessToken = paymentData.continueData.continueAccessToken,
                        quoteId = paymentData.quoteId,
                        incomingPaymentId = paymentData.incomingPaymentId,
                        communityId = communityId,
                        conceptId = conceptId,
                        amount = amount.toString()
                    )

                    _createContributionState.value = CreateContributionState.AuthorizationRequired(
                        authorizationUrl = paymentData.authorizationUrl,
                        paymentData = processData
                    )
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "❌ Error iniciando pago: ${result.message}")
                    _createContributionState.value = CreateContributionState.Error(
                        "Error al procesar el pago: ${result.message}"
                    )
                }

                is ApiResult.Loading -> {
                    Log.d(TAG, "⏳ Procesando pago...")
                }
            }
        }
    }

    /**
     * PASO 2: Finalizar contribución después de autorización
     * Este método se llama cuando el usuario completa la autorización
     */
    fun finalizeContribution(
        interactRef: String,
        paymentData: PaymentProcessData
    ) {
        viewModelScope.launch {
            Log.d(TAG, "🏁 Finalizando contribución con interact_ref: $interactRef")
            _createContributionState.value = CreateContributionState.Loading

            when (val result = paymentRepository.finalizeCommunityPayment(
                continueUri = paymentData.continueUri,
                continueAccessToken = paymentData.continueAccessToken,
                interactRef = interactRef,
                quoteId = paymentData.quoteId,
                communityId = paymentData.communityId,
                conceptId = paymentData.conceptId
            )) {
                is ApiResult.Success -> {
                    val paymentResult = result.data.data
                    Log.d(TAG, "✅ Pago completado: ${paymentResult.outgoingPayment.id}")
                    Log.d(TAG, "💸 Estado: ${paymentResult.status}")

                    _createContributionState.value = CreateContributionState.Success

                    // Recargar las contribuciones para mostrar la nueva
                    _currentConceptId.value?.let { conceptId ->
                        loadContributionsForConcept(conceptId)
                    }
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "❌ Error finalizando pago: ${result.message}")
                    _createContributionState.value = CreateContributionState.Error(
                        "Error completando el pago: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {
                    Log.d(TAG, "⏳ Completando pago...")
                }
            }
        }
    }

    /**
     * Cancelar proceso de pago
     */
    fun cancelPaymentProcess() {
        Log.d(TAG, "❌ Usuario canceló el proceso de pago")
        _createContributionState.value = CreateContributionState.Error(
            "Proceso de pago cancelado por el usuario"
        )
    }

    /**
     * Test backend connection
     */
    fun testBackendConnection() {
        viewModelScope.launch {
            Log.d(TAG, "🧪 Probando conexión con backend...")

            when (val result = paymentRepository.testConnection()) {
                is ApiResult.Success -> {
                    Log.d(TAG, "✅ Backend conectado: ${result.data.message}")
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "❌ Error de conexión: ${result.message}")
                }
                is ApiResult.Loading -> {
                    Log.d(TAG, "⏳ Conectando...")
                }
            }
        }
    }

    /**
     * Get system info from backend
     */
    fun getSystemInfo() {
        viewModelScope.launch {
            Log.d(TAG, "📊 Obteniendo info del sistema...")

            when (val result = paymentRepository.getSystemInfo()) {
                is ApiResult.Success -> {
                    val system = result.data.data
                    Log.d(TAG, "✅ Sistema: ${system.system}")
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "❌ Error system info: ${result.message}")
                }

                is ApiResult.Loading -> {
                    Log.d(TAG, "⏳ Obteniendo info...")
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