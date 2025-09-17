package com.icescream.nestpay.data.repository

import com.icescream.nestpay.data.api.OpenPaymentsApiService
import com.icescream.nestpay.data.models.*
import retrofit2.Response

class WalletRepository(
    private val apiService: OpenPaymentsApiService
) {

    // Simulamos el token de autorización - en producción esto vendría del almacenamiento seguro
    private val authToken = "Bearer YOUR_ACCESS_TOKEN"

    suspend fun createWalletAddress(
        publicName: String,
        assetCode: String = "USD",
        assetScale: Int = 2
    ): Result<WalletAddress> {
        return try {
            val request = CreateWalletAddressRequest(
                publicName = publicName,
                assetCode = assetCode,
                assetScale = assetScale
            )

            val response = apiService.createWalletAddress(request, authToken)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.walletAddress)
            } else {
                Result.failure(Exception("Error creating wallet address: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWalletAddresses(): Result<List<WalletAddress>> {
        return try {
            val response = apiService.listWalletAddresses(authToken)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching wallet addresses: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createIncomingPayment(
        walletAddressId: String,
        amount: String,
        assetCode: String = "USD",
        assetScale: Int = 2,
        metadata: Map<String, String>? = null
    ): Result<PaymentResponse> {
        return try {
            val incomingAmount = IncomingAmount(
                value = amount,
                assetCode = assetCode,
                assetScale = assetScale
            )

            val request = PaymentRequest(
                walletAddress = walletAddressId,
                incomingAmount = incomingAmount,
                metadata = metadata
            )

            val response = apiService.createIncomingPayment(walletAddressId, request, authToken)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error creating incoming payment: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIncomingPayments(walletAddressId: String): Result<List<PaymentResponse>> {
        return try {
            val response = apiService.listIncomingPayments(walletAddressId, authToken)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching incoming payments: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOutgoingPayment(
        walletAddressId: String,
        quoteId: String,
        metadata: Map<String, String>? = null
    ): Result<OutgoingPaymentResponse> {
        return try {
            val request = OutgoingPaymentRequest(
                walletAddress = walletAddressId,
                quoteId = quoteId,
                metadata = metadata
            )

            val response = apiService.createOutgoingPayment(walletAddressId, request, authToken)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error creating outgoing payment: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOutgoingPayments(walletAddressId: String): Result<List<OutgoingPaymentResponse>> {
        return try {
            val response = apiService.listOutgoingPayments(walletAddressId, authToken)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error fetching outgoing payments: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}