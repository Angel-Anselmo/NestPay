package com.icescream.nestpay.network

import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

// ==========================================
// DATA CLASSES - Backend Multi-Wallet Models
// ==========================================

data class CommunityPaymentRequest(
    @SerializedName("amount") val amount: PaymentAmount,
    @SerializedName("description") val description: String = "",
    @SerializedName("communityId") val communityId: String? = null,
    @SerializedName("conceptId") val conceptId: String? = null
)

data class PaymentAmount(
    @SerializedName("value") val value: String,
    @SerializedName("assetCode") val assetCode: String? = null,
    @SerializedName("assetScale") val assetScale: Int? = null
)

data class CommunityPaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CommunityPaymentData
)

data class CommunityPaymentData(
    @SerializedName("incomingPaymentId") val incomingPaymentId: String,
    @SerializedName("quoteId") val quoteId: String,
    @SerializedName("adminWallet") val adminWallet: String,
    @SerializedName("userWallet") val userWallet: String,
    @SerializedName("authorizationUrl") val authorizationUrl: String,
    @SerializedName("estimatedFees") val estimatedFees: FeeEstimation,
    @SerializedName("continueData") val continueData: ContinueData,
    @SerializedName("metadata") val metadata: PaymentMetadata
)

data class FeeEstimation(
    @SerializedName("sendAmount") val sendAmount: AmountDetails,
    @SerializedName("receiveAmount") val receiveAmount: AmountDetails
)

data class AmountDetails(
    @SerializedName("value") val value: String,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("assetScale") val assetScale: Int,
    @SerializedName("displayValue") val displayValue: String
)

data class ContinueData(
    @SerializedName("continueUri") val continueUri: String,
    @SerializedName("continueAccessToken") val continueAccessToken: String,
    @SerializedName("finishInteractionUrl") val finishInteractionUrl: String,
    @SerializedName("state") val state: String
)

data class PaymentMetadata(
    @SerializedName("communityId") val communityId: String?,
    @SerializedName("conceptId") val conceptId: String?,
    @SerializedName("description") val description: String,
    @SerializedName("flow") val flow: String
)

data class FinalizeCommunityPaymentRequest(
    @SerializedName("continueUri") val continueUri: String,
    @SerializedName("continueAccessToken") val continueAccessToken: String,
    @SerializedName("interactRef") val interactRef: String,
    @SerializedName("quoteId") val quoteId: String,
    @SerializedName("communityId") val communityId: String? = null,
    @SerializedName("conceptId") val conceptId: String? = null
)

data class CommunityPaymentResult(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PaymentResultData
)

data class PaymentResultData(
    @SerializedName("outgoingPayment") val outgoingPayment: OutgoingPaymentData,
    @SerializedName("status") val status: String,
    @SerializedName("fromWallet") val fromWallet: String,
    @SerializedName("toWallet") val toWallet: String,
    @SerializedName("metadata") val metadata: PaymentCompletionMetadata
)

data class OutgoingPaymentData(
    @SerializedName("id") val id: String,
    @SerializedName("walletAddress") val walletAddress: String,
    @SerializedName("quoteId") val quoteId: String,
    @SerializedName("state") val state: String,
    @SerializedName("sentAmount") val sentAmount: AmountDetails?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class PaymentCompletionMetadata(
    @SerializedName("communityId") val communityId: String?,
    @SerializedName("conceptId") val conceptId: String?,
    @SerializedName("paymentType") val paymentType: String,
    @SerializedName("completedAt") val completedAt: String
)

data class SystemInfoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SystemInfoData
)

data class SystemInfoData(
    @SerializedName("system") val system: String,
    @SerializedName("wallets") val wallets: Map<String, WalletInfo>,
    @SerializedName("flows") val flows: Map<String, FlowInfo>,
    @SerializedName("timestamp") val timestamp: String
)

data class WalletInfo(
    @SerializedName("address") val address: String,
    @SerializedName("keyId") val keyId: String,
    @SerializedName("initialized") val initialized: Boolean,
    @SerializedName("role") val role: String
)

data class FlowInfo(
    @SerializedName("description") val description: String,
    @SerializedName("endpoints") val endpoints: Map<String, String>
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("version") val version: String,
    @SerializedName("environment") val environment: String
)

// ==========================================
// API SERVICE INTERFACE - Multi-Wallet Backend
// ==========================================

interface NestPayApiService {

    // ==========================================
    // SYSTEM ENDPOINTS
    // ==========================================

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>

    @GET("payments/system/info")
    suspend fun getSystemInfo(): Response<SystemInfoResponse>

    // ==========================================
    // COMMUNITY PAYMENT ENDPOINTS
    // ==========================================

    /**
     * Iniciar un pago de aporte a comunidad
     * USER → ADMIN workflow
     */
    @POST("payments/community/initiate")
    suspend fun initiateCommunityPayment(
        @Body request: CommunityPaymentRequest
    ): Response<CommunityPaymentResponse>

    /**
     * Finalizar un pago de aporte a comunidad
     * Después de que el usuario autorice en el authorizationUrl
     */
    @POST("payments/community/finalize")
    suspend fun finalizeCommunityPayment(
        @Body request: FinalizeCommunityPaymentRequest
    ): Response<CommunityPaymentResult>

    // ==========================================
    // WALLET VALIDATION ENDPOINTS
    // ==========================================

    /**
     * Validar una wallet address
     */
    @GET("wallets/validate/{walletUrl}")
    suspend fun validateWallet(
        @Path("walletUrl", encoded = true) walletUrl: String
    ): Response<WalletValidationResponse>

    /**
     * Obtener wallets de prueba disponibles
     */
    @GET("wallets/test-wallets")
    suspend fun getTestWallets(): Response<TestWalletsResponse>

    // ==========================================
    // PAYMENT STATUS ENDPOINTS
    // ==========================================

    /**
     * Obtener estado de un pago
     */
    @GET("payments/status/{paymentId}")
    suspend fun getPaymentStatus(
        @Path("paymentId") paymentId: String,
        @Query("walletAddress") walletAddress: String,
        @Query("accessToken") accessToken: String,
        @Query("type") type: String // "incoming" o "outgoing"
    ): Response<PaymentStatusResponse>
}

// ==========================================
// ADDITIONAL RESPONSE MODELS
// ==========================================

data class WalletValidationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: WalletValidationData?,
    @SerializedName("error") val error: String?
)

data class WalletValidationData(
    @SerializedName("walletAddress") val walletAddress: WalletAddressData,
    @SerializedName("authServer") val authServer: String,
    @SerializedName("resourceServer") val resourceServer: String,
    @SerializedName("assetInfo") val assetInfo: AssetInfo
)

data class WalletAddressData(
    @SerializedName("id") val id: String,
    @SerializedName("publicName") val publicName: String?,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("assetScale") val assetScale: Int,
    @SerializedName("authServer") val authServer: String?,
    @SerializedName("resourceServer") val resourceServer: String?
)

data class AssetInfo(
    @SerializedName("code") val code: String,
    @SerializedName("scale") val scale: Int
)

data class TestWalletsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<TestWallet>,
    @SerializedName("instructions") val instructions: TestWalletInstructions
)

data class TestWallet(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("description") val description: String,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("assetScale") val assetScale: Int
)

data class TestWalletInstructions(
    @SerializedName("createWallet") val createWallet: String,
    @SerializedName("getPrivateKey") val getPrivateKey: String,
    @SerializedName("documentation") val documentation: String
)

data class PaymentStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PaymentStatusData
)

data class PaymentStatusData(
    @SerializedName("id") val id: String,
    @SerializedName("state") val state: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("amount") val amount: AmountDetails?,
    @SerializedName("walletAddress") val walletAddress: String,
    @SerializedName("paymentType") val paymentType: String
)