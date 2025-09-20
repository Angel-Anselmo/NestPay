package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.repository.CommunityPaymentRepository
import com.icescream.nestpay.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TestBackendUiState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val hasError: Boolean = false,
    val result: String = "",
    val lastTestType: String = ""
)

class TestBackendViewModel : ViewModel() {

    private val repository = CommunityPaymentRepository()

    private val _uiState = MutableStateFlow(TestBackendUiState())
    val uiState: StateFlow<TestBackendUiState> = _uiState.asStateFlow()

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                hasError = false,
                result = "",
                lastTestType = "connection"
            )

            when (val result = repository.testConnection()) {
                is ApiResult.Success -> {
                    val health = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = true,
                        hasError = false,
                        result = "✅ Conexión exitosa\n" +
                                "Status: ${health.status}\n" +
                                "Message: ${health.message}\n" +
                                "Version: ${health.version}\n" +
                                "Environment: ${health.environment}\n" +
                                "Time: ${health.timestamp}"
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = false,
                        hasError = true,
                        result = "❌ Error de conexión:\n${result.message}\n\n" +
                                "Asegúrate de que:\n" +
                                "1. El backend esté corriendo (npm run dev)\n" +
                                "2. La URL sea correcta (http://10.0.2.2:3000)\n" +
                                "3. No haya problemas de firewall"
                    )
                }

                is ApiResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    fun getSystemInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                hasError = false,
                result = "",
                lastTestType = "system"
            )

            when (val result = repository.getSystemInfo()) {
                is ApiResult.Success -> {
                    val system = result.data.data
                    val adminWallet = system.wallets["adminWallet"]
                    val userWallet = system.wallets["userWallet"]

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = true,
                        hasError = false,
                        result = "✅ Sistema Multi-Wallet Funcionando\n\n" +
                                "Sistema: ${system.system}\n" +
                                "Timestamp: ${system.timestamp}\n\n" +
                                "👑 ADMIN WALLET:\n" +
                                "  Address: ${adminWallet?.address}\n" +
                                "  KeyId: ${adminWallet?.keyId}\n" +
                                "  Role: ${adminWallet?.role}\n" +
                                "  Initialized: ${adminWallet?.initialized}\n\n" +
                                "👤 USER WALLET:\n" +
                                "  Address: ${userWallet?.address}\n" +
                                "  KeyId: ${userWallet?.keyId}\n" +
                                "  Role: ${userWallet?.role}\n" +
                                "  Initialized: ${userWallet?.initialized}\n\n" +
                                "🔄 FLUJOS DISPONIBLES:\n" +
                                system.flows.entries.joinToString("\n") { (key, flow) ->
                                    "  $key: ${flow.description}"
                                }
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasError = true,
                        result = "❌ Error obteniendo system info:\n${result.message}"
                    )
                }

                is ApiResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    fun getTestWallets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                hasError = false,
                result = "",
                lastTestType = "wallets"
            )

            when (val result = repository.getTestWallets()) {
                is ApiResult.Success -> {
                    val wallets = result.data.data
                    val instructions = result.data.instructions

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = true,
                        hasError = false,
                        result = "✅ ${wallets.size} Wallets de Prueba Disponibles\n\n" +
                                wallets.joinToString("\n") { wallet ->
                                    "🏦 ${wallet.name}\n" +
                                            "   URL: ${wallet.url}\n" +
                                            "   Descripción: ${wallet.description}\n" +
                                            "   Asset: ${wallet.assetCode} (scale: ${wallet.assetScale})"
                                } + "\n\n📖 INSTRUCCIONES:\n" +
                                "• Crear wallet: ${instructions.createWallet}\n" +
                                "• Obtener clave: ${instructions.getPrivateKey}\n" +
                                "• Documentación: ${instructions.documentation}"
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasError = true,
                        result = "❌ Error obteniendo wallets:\n${result.message}"
                    )
                }

                is ApiResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    fun testPayment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                hasError = false,
                result = "",
                lastTestType = "payment"
            )

            when (val result = repository.initiateCommunityPayment(
                amount = "500",
                communityId = "test-community-android",
                conceptId = "test-concept-android",
                description = "Pago de prueba desde Android App"
            )) {
                is ApiResult.Success -> {
                    val payment = result.data.data
                    val fees = payment.estimatedFees

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isConnected = true,
                        hasError = false,
                        result = "✅ Pago de Prueba Iniciado\n\n" +
                                "💰 DETALLES DEL PAGO:\n" +
                                "  ID Incoming: ${payment.incomingPaymentId}\n" +
                                "  ID Quote: ${payment.quoteId}\n" +
                                "  Comunidad: ${payment.metadata.communityId}\n" +
                                "  Concepto: ${payment.metadata.conceptId}\n\n" +
                                "🏦 WALLETS:\n" +
                                "  Admin (recibe): ${payment.adminWallet}\n" +
                                "  User (paga): ${payment.userWallet}\n\n" +
                                "💸 TARIFAS ESTIMADAS:\n" +
                                "  Envía: ${fees.sendAmount.displayValue} ${fees.sendAmount.assetCode}\n" +
                                "  Recibe: ${fees.receiveAmount.displayValue} ${fees.receiveAmount.assetCode}\n\n" +
                                "🔗 URL DE AUTORIZACIÓN:\n" +
                                "${payment.authorizationUrl}\n\n" +
                                "📋 SIGUIENTE PASO:\n" +
                                "El usuario debe visitar la URL de autorización\n" +
                                "para completar el pago en su wallet."
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasError = true,
                        result = "❌ Error iniciando pago:\n${result.message}\n\n" +
                                "Posibles causas:\n" +
                                "• Backend no inicializado correctamente\n" +
                                "• Problemas con las credenciales de wallet\n" +
                                "• Error de conectividad con Open Payments"
                    )
                }

                is ApiResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    fun runFullTest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                hasError = false,
                result = "🚀 Ejecutando prueba completa...\n",
                lastTestType = "full"
            )

            try {
                val result = repository.runFullTest()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isConnected = !result.contains("❌ Error de conexión"),
                    hasError = result.contains("❌"),
                    result = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasError = true,
                    result = "❌ Error ejecutando prueba completa:\n${e.message}"
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            result = "",
            hasError = false,
            lastTestType = ""
        )
    }
}