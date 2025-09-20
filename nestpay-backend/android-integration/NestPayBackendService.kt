// Ejemplo de integración del backend con la app Android NestPay
package com.icescream.nestpay.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio para comunicarse con el backend NestPay que maneja Open Payments
 * Reemplaza las llamadas directas a Open Payments API
 */
interface NestPayBackendService {

    // =================== HEALTH CHECKS ===================

    @GET("api/health")
    suspend fun healthCheck(): Response<HealthResponse>

    @GET("api/health/detailed")
    suspend fun detailedHealthCheck(): Response<DetailedHealthResponse>

    // =================== WALLET OPERATIONS ===================

    @GET("api/wallets/validate/{walletUrl}")
    suspend fun validateWallet(
        @Path("walletUrl", encoded = true) walletUrl: String
    ): Response<WalletValidationResponse>

    @GET("api/wallets/info/{walletUrl}")
    suspend fun getWalletInfo(
        @Path("walletUrl", encoded = true) walletUrl: String
    ): Response<WalletInfoResponse>

    @POST("api/wallets/check-compatibility")
    suspend fun checkWalletCompatibility(
        @Body request: WalletCompatibilityRequest
    ): Response<WalletCompatibilityResponse>

    @GET("api/wallets/test-wallets")
    suspend fun getTestWallets(): Response<TestWalletsResponse>

    @POST("api/wallets/estimate-fees")
    suspend fun estimateFees(
        @Body request: FeeEstimationRequest
    ): Response<FeeEstimationResponse>

    // =================== PAYMENT OPERATIONS ===================

    @POST("api/payments/initiate")
    suspend fun initiatePayment(
        @Body request: InitiatePaymentRequest
    ): Response<PaymentFlowResponse>

    @POST("api/payments/finalize")
    suspend fun finalizePayment(
        @Body request: FinalizePaymentRequest
    ): Response<PaymentResult>

    @GET("api/payments/status/{paymentId}")
    suspend fun getPaymentStatus(
        @Path("paymentId") paymentId: String,
        @Query("walletAddress") walletAddress: String,
        @Query("accessToken") accessToken: String,
        @Query("type") type: String // "incoming" o "outgoing"
    ): Response<PaymentStatusResponse>

    // =================== INDIVIDUAL PAYMENT OPERATIONS ===================

    @POST("api/payments/incoming")
    suspend fun createIncomingPayment(
        @Body request: CreateIncomingPaymentRequest
    ): Response<IncomingPaymentResponse>

    @POST("api/payments/quotes")
    suspend fun createQuote(
        @Body request: CreateQuoteRequest
    ): Response<QuoteResponse>

    @POST("api/payments/outgoing")
    suspend fun createOutgoingPayment(
        @Body request: CreateOutgoingPaymentRequest
    ): Response<OutgoingPaymentResponse>
}

// =================== DATA MODELS ===================

// Health Check Models
data class HealthResponse(
    val status: String,
    val message: String,
    val timestamp: String,
    val version: String,
    val environment: String
)

data class DetailedHealthResponse(
    val status: String,
    val timestamp: String,
    val version: String,
    val environment: String,
    val services: Map<String, ServiceStatus>,
    val configuration: Map<String, Boolean>
)

data class ServiceStatus(
    val status: String,
    val message: String
)

// Wallet Models
data class WalletValidationResponse(
    val success: Boolean,
    val message: String? = null,
    val data: WalletValidationData? = null,
    val error: String? = null
)

data class WalletValidationData(
    val walletAddress: WalletAddressData,
    val authServer: String,
    val resourceServer: String,
    val assetInfo: AssetInfo
)

data class WalletAddressData(
    val id: String,
    val publicName: String?,
    val assetCode: String,
    val assetScale: Int,
    val authServer: String?,
    val resourceServer: String?
)

data class AssetInfo(
    val code: String,
    val scale: Int
)

data class WalletInfoResponse(
    val success: Boolean,
    val data: WalletInfoData
)

data class WalletInfoData(
    val id: String,
    val url: String,
    val publicName: String?,
    val assetCode: String,
    val assetScale: Int,
    val authServer: String?,
    val resourceServer: String?,
    val receiverEndpoint: String,
    val quotesEndpoint: String,
    val outgoingPaymentsEndpoint: String
)

data class WalletCompatibilityRequest(
    val senderWallet: String,
    val receiverWallet: String
)

data class WalletCompatibilityResponse(
    val success: Boolean,
    val data: CompatibilityData
)

data class CompatibilityData(
    val compatible: Boolean,
    val senderWallet: WalletSummary,
    val receiverWallet: WalletSummary,
    val compatibilityNotes: String
)

data class WalletSummary(
    val url: String,
    val publicName: String?,
    val assetCode: String,
    val assetScale: Int,
    val authServer: String?
)

data class TestWalletsResponse(
    val success: Boolean,
    val message: String,
    val data: List<TestWallet>,
    val instructions: TestWalletInstructions
)

data class TestWallet(
    val name: String,
    val url: String,
    val description: String,
    val assetCode: String,
    val assetScale: Int
)

data class TestWalletInstructions(
    val createWallet: String,
    val getPrivateKey: String,
    val documentation: String
)

// Fee Estimation Models
data class FeeEstimationRequest(
    val senderWallet: String,
    val receiverWallet: String,
    val amount: AmountData
)

data class FeeEstimationResponse(
    val success: Boolean,
    val data: FeeEstimationData
)

data class FeeEstimationData(
    val quote: QuoteInfo,
    val estimation: EstimationDetails,
    val paymentData: PaymentData
)

data class QuoteInfo(
    val id: String,
    val expiresAt: String
)

data class EstimationDetails(
    val sendAmount: AmountDetails,
    val receiveAmount: AmountDetails,
    val fees: FeeDetails
)

data class AmountDetails(
    val value: String,
    val assetCode: String,
    val assetScale: Int,
    val displayValue: String
)

data class FeeDetails(
    val value: String,
    val assetCode: String,
    val assetScale: Int,
    val displayValue: String,
    val percentage: String
)

data class PaymentData(
    val incomingPaymentId: String,
    val quoteId: String
)

// Payment Flow Models
data class InitiatePaymentRequest(
    val senderWallet: String,
    val receiverWallet: String,
    val amount: AmountData,
    val description: String = ""
)

data class AmountData(
    val value: String,
    val assetCode: String = "USD",
    val assetScale: Int = 2
)

data class PaymentFlowResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentFlowData
)

data class PaymentFlowData(
    val paymentId: String,
    val quoteId: String,
    val authorizationUrl: String,
    val estimatedFees: FeeEstimation,
    val continueData: ContinueData
)

data class FeeEstimation(
    val sendAmount: AmountDetails,
    val receiveAmount: AmountDetails
)

data class ContinueData(
    val continueUri: String,
    val continueAccessToken: String,
    val finishInteractionUrl: String,
    val state: String
)

data class FinalizePaymentRequest(
    val continueUri: String,
    val continueAccessToken: String,
    val interactRef: String,
    val walletAddress: String,
    val quoteId: String
)

data class PaymentResult(
    val success: Boolean,
    val message: String,
    val data: PaymentResultData
)

data class PaymentResultData(
    val outgoingPayment: OutgoingPaymentData,
    val status: String
)

data class OutgoingPaymentData(
    val id: String,
    val walletAddress: String,
    val quoteId: String,
    val state: String,
    val sentAmount: AmountDetails?,
    val createdAt: String,
    val updatedAt: String
)

// Payment Status Models  
data class PaymentStatusResponse(
    val success: Boolean,
    val data: PaymentStatusData
)

data class PaymentStatusData(
    val id: String,
    val state: String,
    val createdAt: String,
    val updatedAt: String,
    val amount: AmountDetails?
)

// Individual Payment Models
data class CreateIncomingPaymentRequest(
    val walletAddress: String,
    val amount: AmountData,
    val description: String? = null
)

data class IncomingPaymentResponse(
    val success: Boolean,
    val data: IncomingPaymentData
)

data class IncomingPaymentData(
    val incomingPayment: IncomingPaymentDetails,
    val walletAddress: WalletAddressData
)

data class IncomingPaymentDetails(
    val id: String,
    val walletAddress: String,
    val incomingAmount: AmountDetails,
    val receivedAmount: AmountDetails?,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val metadata: Map<String, String>?
)

data class CreateQuoteRequest(
    val senderWallet: String,
    val receiverPaymentUrl: String,
    val amount: AmountDataWithType
)

data class AmountDataWithType(
    val value: String,
    val type: String, // "send" o "receive"
    val assetCode: String? = null,
    val assetScale: Int? = null
)

data class QuoteResponse(
    val success: Boolean,
    val data: QuoteData
)

data class QuoteData(
    val quote: QuoteDetails,
    val walletAddress: WalletAddressData
)

data class QuoteDetails(
    val id: String,
    val walletAddress: String,
    val receiver: String,
    val sendAmount: AmountDetails,
    val receiveAmount: AmountDetails,
    val maxPacketAmount: String,
    val minExchangeRate: String,
    val lowEstimatedExchangeRate: String,
    val highEstimatedExchangeRate: String,
    val createdAt: String,
    val expiresAt: String
)

data class CreateOutgoingPaymentRequest(
    val walletAddress: String,
    val quoteId: String,
    val accessToken: String,
    val metadata: Map<String, String> = emptyMap()
)

data class OutgoingPaymentResponse(
    val success: Boolean,
    val data: OutgoingPaymentData
)

// =================== REPOSITORY INTEGRATION EXAMPLE ===================

/**
 * Ejemplo de cómo integrar el backend service en tu repositorio existente
 */
class NestPayRepository(
    private val backendService: NestPayBackendService,
    // Mantén tus otros servicios existentes si los necesitas
    private val firebaseService: FirebaseService? = null
) {

    /**
     * Validar una wallet address
     */
    suspend fun validateWallet(walletUrl: String): Result<WalletValidationData> {
        return try {
            val response = backendService.validateWallet(walletUrl)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Error validando wallet"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Iniciar un pago
     */
    suspend fun initiatePayment(
        senderWallet: String,
        receiverWallet: String,
        amount: String,
        description: String = ""
    ): Result<PaymentFlowData> {
        return try {
            val request = InitiatePaymentRequest(
                senderWallet = senderWallet,
                receiverWallet = receiverWallet,
                amount = AmountData(value = amount),
                description = description
            )

            val response = backendService.initiatePayment(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Error iniciando pago"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Finalizar un pago
     */
    suspend fun finalizePayment(
        continueUri: String,
        continueAccessToken: String,
        interactRef: String,
        walletAddress: String,
        quoteId: String
    ): Result<PaymentResultData> {
        return try {
            val request = FinalizePaymentRequest(
                continueUri = continueUri,
                continueAccessToken = continueAccessToken,
                interactRef = interactRef,
                walletAddress = walletAddress,
                quoteId = quoteId
            )

            val response = backendService.finalizePayment(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Error finalizando pago"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener wallets de prueba
     */
    suspend fun getTestWallets(): Result<List<TestWallet>> {
        return try {
            val response = backendService.getTestWallets()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Error obteniendo wallets de prueba"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}