package com.icescream.nestpay.network

import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

// ==========================================
// DATA CLASSES - Request/Response models
// ==========================================

data class CreatePaymentRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("communityId") val communityId: String,
    @SerializedName("conceptId") val conceptId: String,
    @SerializedName("description") val description: String? = null
)

data class CreatePaymentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PaymentData
)

data class PaymentData(
    @SerializedName("paymentId") val paymentId: String,
    @SerializedName("openPaymentId") val openPaymentId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("status") val status: String,
    @SerializedName("state") val state: String,
    @SerializedName("fromPaymentPointer") val fromPaymentPointer: String,
    @SerializedName("toPaymentPointer") val toPaymentPointer: String,
    @SerializedName("createdAt") val createdAt: String
)

data class PaymentStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PaymentStatusData
)

data class PaymentStatusData(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("status") val status: String,
    @SerializedName("state") val state: String,
    @SerializedName("fromWallet") val fromWallet: String,
    @SerializedName("toWallet") val toWallet: String,
    @SerializedName("communityId") val communityId: String,
    @SerializedName("conceptId") val conceptId: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class PaymentPointerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PaymentPointerData
)

data class PaymentPointerData(
    @SerializedName("paymentPointer") val paymentPointer: String,
    @SerializedName("role") val role: String,
    @SerializedName("joinedAt") val joinedAt: String
)

data class ApiErrorResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String
)

// ==========================================
// API SERVICE INTERFACE
// ==========================================

interface NestPayApiService {

    // ==========================================
    // PAYMENT ENDPOINTS
    // ==========================================

    /**
     * Create a new payment
     */
    @POST("payments/create")
    suspend fun createPayment(
        @Header("Authorization") authorization: String,
        @Body request: CreatePaymentRequest
    ): Response<CreatePaymentResponse>

    /**
     * Get payment status by ID
     */
    @GET("payments/{paymentId}")
    suspend fun getPaymentStatus(
        @Header("Authorization") authorization: String,
        @Path("paymentId") paymentId: String
    ): Response<PaymentStatusResponse>

    /**
     * Get user's payment pointer for a community
     */
    @GET("payments/user/payment-pointer/{communityId}")
    suspend fun getUserPaymentPointer(
        @Header("Authorization") authorization: String,
        @Path("communityId") communityId: String
    ): Response<PaymentPointerResponse>

    /**
     * Get payment history for current user
     */
    @GET("payments/user/history")
    suspend fun getUserPaymentHistory(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<PaymentHistoryResponse>

    /**
     * Get payments for a community
     */
    @GET("payments/community/{communityId}")
    suspend fun getCommunityPayments(
        @Header("Authorization") authorization: String,
        @Path("communityId") communityId: String
    ): Response<CommunityPaymentsResponse>

    // ==========================================
    // WALLET ENDPOINTS (for future use)
    // ==========================================

    /**
     * Validate a payment pointer
     */
    @POST("wallets/validate")
    suspend fun validatePaymentPointer(
        @Header("Authorization") authorization: String,
        @Body request: ValidatePaymentPointerRequest
    ): Response<ValidatePaymentPointerResponse>
}

// ==========================================
// ADDITIONAL DATA CLASSES
// ==========================================

data class PaymentHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PaymentHistoryData
)

data class PaymentHistoryData(
    @SerializedName("payments") val payments: List<PaymentStatusData>,
    @SerializedName("total") val total: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int
)

data class CommunityPaymentsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CommunityPaymentsData
)

data class CommunityPaymentsData(
    @SerializedName("communityId") val communityId: String,
    @SerializedName("payments") val payments: List<PaymentStatusData>,
    @SerializedName("total") val total: Int
)

data class ValidatePaymentPointerRequest(
    @SerializedName("walletAddress") val walletAddress: String
)

data class ValidatePaymentPointerResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ValidatePaymentPointerData?
)

data class ValidatePaymentPointerData(
    @SerializedName("walletAddress") val walletAddress: String,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("assetScale") val assetScale: Int,
    @SerializedName("isValid") val isValid: Boolean
)