package com.icescream.nestpay.data.api

import com.icescream.nestpay.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface OpenPaymentsApiService {

    // Wallet Address endpoints
    @POST("wallet-addresses")
    suspend fun createWalletAddress(
        @Body request: CreateWalletAddressRequest,
        @Header("Authorization") authorization: String
    ): Response<WalletAddressResponse>

    @GET("wallet-addresses/{id}")
    suspend fun getWalletAddress(
        @Path("id") id: String,
        @Header("Authorization") authorization: String
    ): Response<WalletAddress>

    @GET("wallet-addresses")
    suspend fun listWalletAddresses(
        @Header("Authorization") authorization: String
    ): Response<List<WalletAddress>>

    // Incoming Payment endpoints
    @POST("wallet-addresses/{id}/incoming-payments")
    suspend fun createIncomingPayment(
        @Path("id") walletAddressId: String,
        @Body request: PaymentRequest,
        @Header("Authorization") authorization: String
    ): Response<PaymentResponse>

    @GET("wallet-addresses/{id}/incoming-payments/{paymentId}")
    suspend fun getIncomingPayment(
        @Path("id") walletAddressId: String,
        @Path("paymentId") paymentId: String,
        @Header("Authorization") authorization: String
    ): Response<PaymentResponse>

    @GET("wallet-addresses/{id}/incoming-payments")
    suspend fun listIncomingPayments(
        @Path("id") walletAddressId: String,
        @Header("Authorization") authorization: String
    ): Response<List<PaymentResponse>>

    // Outgoing Payment endpoints
    @POST("wallet-addresses/{id}/outgoing-payments")
    suspend fun createOutgoingPayment(
        @Path("id") walletAddressId: String,
        @Body request: OutgoingPaymentRequest,
        @Header("Authorization") authorization: String
    ): Response<OutgoingPaymentResponse>

    @GET("wallet-addresses/{id}/outgoing-payments/{paymentId}")
    suspend fun getOutgoingPayment(
        @Path("id") walletAddressId: String,
        @Path("paymentId") paymentId: String,
        @Header("Authorization") authorization: String
    ): Response<OutgoingPaymentResponse>

    @GET("wallet-addresses/{id}/outgoing-payments")
    suspend fun listOutgoingPayments(
        @Path("id") walletAddressId: String,
        @Header("Authorization") authorization: String
    ): Response<List<OutgoingPaymentResponse>>
}