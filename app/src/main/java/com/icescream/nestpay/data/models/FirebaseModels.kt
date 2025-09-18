package com.icescream.nestpay.data.models

import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp

data class FirebaseCommunity(
    @PropertyName("id") val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("targetAmount") val targetAmount: Double = 0.0,
    @PropertyName("currentAmount") val currentAmount: Double = 0.0,
    @PropertyName("walletAddress") val walletAddress: String = "",
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("members") val members: List<String> = emptyList(),
    @PropertyName("status") val status: String = "ACTIVE", // ACTIVE, COMPLETED, PAUSED
    @PropertyName("dueDate") val dueDate: String = "",
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("updatedAt") val updatedAt: Timestamp = Timestamp.now(),
    @PropertyName("category") val category: String = "CUSTOM", // TRAVEL, GIFT, EDUCATION, EVENT, CUSTOM
    @PropertyName("isPublic") val isPublic: Boolean = false,
    @PropertyName("inviteCode") val inviteCode: String = ""
)

data class FirebaseUser(
    @PropertyName("id") val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("walletAddress") val walletAddress: String = "",
    @PropertyName("joinedCommunities") val joinedCommunities: List<String> = emptyList(),
    @PropertyName("createdCommunities") val createdCommunities: List<String> = emptyList(),
    @PropertyName("createdAt") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("isWalletVerified") val isWalletVerified: Boolean = false
)

data class FirebasePayment(
    @PropertyName("id") val id: String = "",
    @PropertyName("communityId") val communityId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("amount") val amount: Double = 0.0,
    @PropertyName("timestamp") val timestamp: Timestamp = Timestamp.now(),
    @PropertyName("status") val status: String = "PENDING", // PENDING, COMPLETED, FAILED
    @PropertyName("openPaymentsTransactionId") val openPaymentsTransactionId: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("fromWallet") val fromWallet: String = "",
    @PropertyName("toWallet") val toWallet: String = ""
)

data class CreateCommunityRequest(
    val name: String,
    val description: String,
    val targetAmount: Double,
    val walletAddress: String,
    val dueDate: String,
    val category: String,
    val isPublic: Boolean = false
)

data class JoinCommunityRequest(
    val communityId: String? = null,
    val inviteCode: String? = null
)

// Extension functions para convertir entre modelos
fun FirebaseCommunity.toPaymentCommunity(): com.icescream.nestpay.ui.screens.PaymentCommunity {
    return com.icescream.nestpay.ui.screens.PaymentCommunity(
        id = this.id,
        name = this.name,
        description = this.description,
        targetAmount = this.targetAmount,
        currentAmount = this.currentAmount,
        memberCount = this.members.size,
        dueDate = this.dueDate,
        color = getCommunityColor(this.category),
        icon = getCommunityIcon(this.category),
        status = com.icescream.nestpay.ui.screens.CommunityStatus.valueOf(this.status),
        createdBy = this.createdBy,
        walletAddress = this.walletAddress
    )
}

private fun getCommunityColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category) {
        "TRAVEL" -> com.icescream.nestpay.ui.theme.AccentPurple
        "GIFT" -> com.icescream.nestpay.ui.theme.AccentYellow
        "EDUCATION" -> com.icescream.nestpay.ui.theme.AccentBlue
        "EVENT" -> com.icescream.nestpay.ui.theme.AccentGreen
        else -> com.icescream.nestpay.ui.theme.NestPayPrimary
    }
}

private fun getCommunityIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "TRAVEL" -> androidx.compose.material.icons.Icons.Default.Place
        "GIFT" -> androidx.compose.material.icons.Icons.Default.Favorite
        "EDUCATION" -> androidx.compose.material.icons.Icons.Default.AccountBox
        "EVENT" -> androidx.compose.material.icons.Icons.Default.Star
        else -> androidx.compose.material.icons.Icons.Default.Add
    }
}