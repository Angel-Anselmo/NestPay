package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.icescream.nestpay.network.*
import kotlinx.coroutines.tasks.await

class PaymentRepository {

    private val apiService = NetworkModule.apiService
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "PaymentRepository"
    }

    /**
     * Get Firebase Auth token for API authentication
     */
    private suspend fun getAuthToken(): String? {
        return try {
            val user = auth.currentUser ?: return null
            val tokenResult = user.getIdToken(false).await()
            tokenResult.token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting auth token", e)
            null
        }
    }

    /**
     * Create a new payment
     */
    suspend fun createPayment(
        amount: Double,
        communityId: String,
        conceptId: String,
        description: String? = null
    ): Result<PaymentData> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Usuario no autenticado"))

            val request = CreatePaymentRequest(
                amount = amount,
                communityId = communityId,
                conceptId = conceptId,
                description = description
            )

            Log.d(
                TAG,
                "Creating payment: amount=$amount, community=$communityId, concept=$conceptId"
            )

            when (val result = safeApiCall {
                apiService.createPayment("Bearer $token", request)
            }) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Payment created successfully: ${result.data.data.paymentId}")
                    Result.success(result.data.data)
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "Error creating payment: ${result.message}")
                    Result.failure(Exception(result.message))
                }

                is ApiResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating payment", e)
            Result.failure(e)
        }
    }

    /**
     * Get payment status by ID
     */
    suspend fun getPaymentStatus(paymentId: String): Result<PaymentStatusData> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Usuario no autenticado"))

            when (val result = safeApiCall {
                apiService.getPaymentStatus("Bearer $token", paymentId)
            }) {
                is ApiResult.Success -> {
                    Result.success(result.data.data)
                }

                is ApiResult.Error -> {
                    Result.failure(Exception(result.message))
                }

                is ApiResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting payment status", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's payment pointer for a community
     */
    suspend fun getUserPaymentPointer(communityId: String): Result<PaymentPointerData> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Usuario no autenticado"))

            Log.d(TAG, "Getting payment pointer for community: $communityId")

            when (val result = safeApiCall {
                apiService.getUserPaymentPointer("Bearer $token", communityId)
            }) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Payment pointer retrieved: ${result.data.data.paymentPointer}")
                    Result.success(result.data.data)
                }

                is ApiResult.Error -> {
                    Log.e(TAG, "Error getting payment pointer: ${result.message}")
                    Result.failure(Exception(result.message))
                }

                is ApiResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting payment pointer", e)
            Result.failure(e)
        }
    }

    /**
     * Get user's payment history
     */
    suspend fun getUserPaymentHistory(
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<PaymentStatusData>> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Usuario no autenticado"))

            when (val result = safeApiCall {
                apiService.getUserPaymentHistory("Bearer $token", limit, offset)
            }) {
                is ApiResult.Success -> {
                    Result.success(result.data.data.payments)
                }

                is ApiResult.Error -> {
                    Result.failure(Exception(result.message))
                }

                is ApiResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting payment history", e)
            Result.failure(e)
        }
    }

    /**
     * Get payments for a community
     */
    suspend fun getCommunityPayments(communityId: String): Result<List<PaymentStatusData>> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Usuario no autenticado"))

            when (val result = safeApiCall {
                apiService.getCommunityPayments("Bearer $token", communityId)
            }) {
                is ApiResult.Success -> {
                    Result.success(result.data.data.payments)
                }

                is ApiResult.Error -> {
                    Result.failure(Exception(result.message))
                }

                is ApiResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting community payments", e)
            Result.failure(e)
        }
    }

    /**
     * Validate a payment pointer
     */
    suspend fun validatePaymentPointer(paymentPointer: String): Result<ValidatePaymentPointerData> {
        return try {
            val token = getAuthToken() ?: return Result.failure(Exception("Usuario no autenticado"))

            val request = ValidatePaymentPointerRequest(walletAddress = paymentPointer)

            when (val result = safeApiCall {
                apiService.validatePaymentPointer("Bearer $token", request)
            }) {
                is ApiResult.Success -> {
                    result.data.data?.let { data ->
                        Result.success(data)
                    } ?: Result.failure(Exception("Invalid response"))
                }

                is ApiResult.Error -> {
                    Result.failure(Exception(result.message))
                }

                is ApiResult.Loading -> {
                    Result.failure(Exception("Unexpected loading state"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception validating payment pointer", e)
            Result.failure(e)
        }
    }
}