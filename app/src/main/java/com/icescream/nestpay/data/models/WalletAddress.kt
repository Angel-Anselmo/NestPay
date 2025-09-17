package com.icescream.nestpay.data.models

import com.google.gson.annotations.SerializedName

data class WalletAddress(
    @SerializedName("id")
    val id: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("publicName")
    val publicName: String?,

    @SerializedName("assetCode")
    val assetCode: String,

    @SerializedName("assetScale")
    val assetScale: Int,

    @SerializedName("authServer")
    val authServer: String?,

    @SerializedName("resourceServer")
    val resourceServer: String?
)

data class WalletAddressResponse(
    @SerializedName("walletAddress")
    val walletAddress: WalletAddress
)

data class CreateWalletAddressRequest(
    @SerializedName("publicName")
    val publicName: String,

    @SerializedName("assetCode")
    val assetCode: String = "USD",

    @SerializedName("assetScale")
    val assetScale: Int = 2
)