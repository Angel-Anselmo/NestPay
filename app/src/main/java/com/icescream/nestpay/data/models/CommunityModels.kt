package com.icescream.nestpay.data.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.NonCancellable.isActive
import java.util.*

// ==========================================
// COMMUNITY - Comunidades de personas
// ==========================================

data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList(), // Lista de UIDs
    val createdBy: String = "",
    val inviteCode: String = "",
    val category: String = "CUSTOM",
    val isActive: Boolean = true,
    val paymentPointer: String = "", // Payment pointer para toda la comunidad
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

// ==========================================
// PAYMENT CONCEPT - Conceptos de pago dentro de una comunidad
// ==========================================

data class PaymentConcept(
    val id: String = "",
    val communityId: String = "",
    val name: String = "", // "Luz", "Agua", "Viaje - Hotel"
    val description: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val dueDate: String = "",
    val createdBy: String = "",
    val status: ConceptStatus = ConceptStatus.ACTIVE,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

enum class ConceptStatus {
    ACTIVE,     // Activo, aceptando pagos
    COMPLETED,  // Meta alcanzada
    PAUSED,     // Pausado temporalmente
    CANCELLED   // Cancelado
}

// ==========================================
// USER PAYMENT - Pagos individuales de usuarios a conceptos
// ==========================================

data class UserPayment(
    val id: String = "",
    val conceptId: String = "",
    val communityId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val transactionId: String = "", // ID de Open Payments
    val fromWallet: String = "",
    val toWallet: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val completedAt: Date? = null
)

// ==========================================
// USER CONTRIBUTION - Contribuciones de usuarios
// ==========================================

data class UserContribution(
    val id: String = "",
    val conceptId: String = "",
    val communityId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val amount: Double = 0.0,
    val status: ContributionStatus = ContributionStatus.COMPLETED,
    val transactionId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val completedAt: Date? = null
)

enum class ContributionStatus {
    PENDING,    // Pendiente de procesamiento
    COMPLETED,  // Completado exitosamente
    FAILED,     // Falló la contribución
    CANCELLED   // Cancelado por el usuario
}

enum class PaymentStatus {
    PENDING,    // Pendiente de procesamiento
    PROCESSING, // En proceso
    COMPLETED,  // Completado exitosamente
    FAILED,     // Falló el pago
    CANCELLED   // Cancelado por el usuario
}

// ==========================================
// UI DATA CLASSES - Para la interfaz
// ==========================================

data class CommunityWithStats(
    val community: Community,
    val totalConcepts: Int = 0,
    val activeConcepts: Int = 0,
    val totalTargetAmount: Double = 0.0,
    val totalCurrentAmount: Double = 0.0,
    val memberCount: Int = 0,
    val color: Color = Color.Gray,
    val icon: ImageVector? = null
)

data class ConceptWithProgress(
    val concept: PaymentConcept,
    val progress: Float = 0f, // 0.0 to 1.0
    val contributorsCount: Int = 0,
    val userContribution: Double = 0.0,
    val color: Color = Color.Gray,
    val icon: ImageVector? = null
)

// ==========================================
// FIREBASE DATA CLASSES - Para Firestore
// ==========================================

data class FirebaseCommunity(
    @PropertyName("id") val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("members") val members: List<String> = emptyList(),
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("inviteCode") val inviteCode: String = "",
    @PropertyName("category") val category: String = "CUSTOM",
    @PropertyName("isActive") val isActive: Boolean = true,
    @PropertyName("paymentPointer") val paymentPointer: String = "",
    @PropertyName("createdAt") val createdAt: com.google.firebase.Timestamp? = null,
    @PropertyName("updatedAt") val updatedAt: com.google.firebase.Timestamp? = null
) {
    fun toCommunity(): Community {
        return Community(
            id = id,
            name = name,
            description = description,
            members = members,
            createdBy = createdBy,
            inviteCode = inviteCode,
            category = category,
            isActive = isActive,
            paymentPointer = paymentPointer,
            createdAt = createdAt?.toDate(),
            updatedAt = updatedAt?.toDate()
        )
    }
}

data class FirebasePaymentConcept(
    @PropertyName("id") val id: String = "",
    @PropertyName("communityId") val communityId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("targetAmount") val targetAmount: Double = 0.0,
    @PropertyName("currentAmount") val currentAmount: Double = 0.0,
    @PropertyName("dueDate") val dueDate: String = "",
    @PropertyName("createdBy") val createdBy: String = "",
    @PropertyName("status") val status: String = "ACTIVE",
    @PropertyName("createdAt") val createdAt: com.google.firebase.Timestamp? = null,
    @PropertyName("updatedAt") val updatedAt: com.google.firebase.Timestamp? = null
) {
    fun toPaymentConcept(): PaymentConcept {
        return PaymentConcept(
            id = id,
            communityId = communityId,
            name = name,
            description = description,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            dueDate = dueDate,
            createdBy = createdBy,
            status = ConceptStatus.valueOf(status.uppercase()),
            createdAt = createdAt?.toDate(),
            updatedAt = updatedAt?.toDate()
        )
    }
}

// ==========================================
// USER PROFILE - Perfil del usuario
// ==========================================

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val mainWalletAddress: String = "", // Wallet principal de Interledger del usuario
    val isAnonymous: Boolean = true,
    val totalCommunities: Int = 0,
    val totalConcepts: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)

data class FirebaseUserProfile(
    @PropertyName("id") val id: String = "",
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("mainWalletAddress") val mainWalletAddress: String = "",
    @PropertyName("isAnonymous") val isAnonymous: Boolean = true,
    @PropertyName("totalCommunities") val totalCommunities: Int = 0,
    @PropertyName("totalConcepts") val totalConcepts: Int = 0,
    @PropertyName("createdAt") val createdAt: com.google.firebase.Timestamp? = null,
    @PropertyName("updatedAt") val updatedAt: com.google.firebase.Timestamp? = null
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            displayName = displayName,
            email = email,
            mainWalletAddress = mainWalletAddress,
            isAnonymous = isAnonymous,
            totalCommunities = totalCommunities,
            totalConcepts = totalConcepts,
            createdAt = createdAt?.toDate(),
            updatedAt = updatedAt?.toDate()
        )
    }
}