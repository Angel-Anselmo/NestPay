package com.icescream.nestpay.data.models

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("walletAddress")
    val walletAddress: String,

    @SerializedName("incomingAmount")
    val incomingAmount: IncomingAmount?,

    @SerializedName("metadata")
    val metadata: Map<String, String>? = null
)

data class IncomingAmount(
    @SerializedName("value")
    val value: String,

    @SerializedName("assetCode")
    val assetCode: String,

    @SerializedName("assetScale")
    val assetScale: Int
)

data class PaymentResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("walletAddress")
    val walletAddress: String,

    @SerializedName("incomingAmount")
    val incomingAmount: IncomingAmount?,

    @SerializedName("receivedAmount")
    val receivedAmount: IncomingAmount?,

    @SerializedName("completed")
    val completed: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("metadata")
    val metadata: Map<String, String>?
)

data class OutgoingPaymentRequest(
    @SerializedName("walletAddress")
    val walletAddress: String,

    @SerializedName("quoteId")
    val quoteId: String,

    @SerializedName("metadata")
    val metadata: Map<String, String>? = null
)

data class OutgoingPaymentResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("walletAddress")
    val walletAddress: String,

    @SerializedName("quoteId")
    val quoteId: String,

    @SerializedName("receiveAmount")
    val receiveAmount: IncomingAmount,

    @SerializedName("sentAmount")
    val sentAmount: IncomingAmount,

    @SerializedName("state")
    val state: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)