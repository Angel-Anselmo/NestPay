package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.icescream.nestpay.ui.screens.PaymentCommunity
import com.icescream.nestpay.ui.screens.CommunityStatus
import com.icescream.nestpay.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

data class FirebaseCommunityData(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val walletAddress: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val status: String = "ACTIVE",
    val dueDate: String = "",
    val category: String = "CUSTOM",
    val isPublic: Boolean = false,
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val updatedAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

class FirebaseCommunityRepository {

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val communitiesCollection = firestore.collection("communities")
    private val authService = com.icescream.nestpay.data.auth.AuthService()

    companion object {
        private const val TAG = "FirebaseCommunityRepo"
    }

    suspend fun getCommunities(): Result<List<PaymentCommunity>> {
        return try {
            Log.d(TAG, "Fetching communities from Firestore")

            val snapshot = communitiesCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val communities = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.toObject(FirebaseCommunityData::class.java)
                    data?.let {
                        it.copy(id = document.id).toPaymentCommunity()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing community document ${document.id}", e)
                    null
                }
            }

            Log.d(TAG, "Successfully fetched ${communities.size} communities")
            Result.success(communities)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching communities", e)
            Result.failure(e)
        }
    }

    suspend fun getCommunityById(id: String): Result<PaymentCommunity?> {
        return try {
            Log.d(TAG, "Fetching community by ID: $id")

            val document = communitiesCollection.document(id).get().await()

            if (document.exists()) {
                val data = document.toObject(FirebaseCommunityData::class.java)
                val community = data?.copy(id = document.id)?.toPaymentCommunity()
                Result.success(community)
            } else {
                Log.w(TAG, "Community with ID $id not found")
                Result.success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching community by ID: $id", e)
            Result.failure(e)
        }
    }

    suspend fun createCommunity(
        name: String,
        description: String,
        targetAmount: Double,
        walletAddress: String,
        dueDate: String,
        category: String,
        isPublic: Boolean = true
    ): Result<PaymentCommunity> {
        return try {
            Log.d(TAG, "Creating new community: $name")

            val currentUserId = authService.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            val now = com.google.firebase.Timestamp.now()

            val communityData = FirebaseCommunityData(
                name = name,
                description = description,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                walletAddress = walletAddress,
                createdBy = currentUserId,
                members = listOf(currentUserId),
                status = "ACTIVE",
                dueDate = dueDate,
                category = category,
                isPublic = isPublic,
                createdAt = now,
                updatedAt = now
            )

            val documentRef = communitiesCollection.add(communityData).await()
            val createdCommunity = communityData.copy(id = documentRef.id).toPaymentCommunity()

            Log.d(TAG, "Successfully created community with ID: ${documentRef.id}")
            Result.success(createdCommunity)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating community", e)
            Result.failure(e)
        }
    }

    suspend fun joinCommunity(communityId: String): Result<PaymentCommunity?> {
        return try {
            Log.d(TAG, "Joining community: $communityId")

            val currentUserId = authService.getCurrentUserId()
                ?: return Result.failure(Exception("Usuario no autenticado"))
            val communityRef = communitiesCollection.document(communityId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(communityRef)
                val data = snapshot.toObject(FirebaseCommunityData::class.java)

                if (data != null && currentUserId !in data.members) {
                    val updatedMembers = data.members + currentUserId
                    transaction.update(
                        communityRef,
                        mapOf(
                            "members" to updatedMembers,
                            "updatedAt" to com.google.firebase.Timestamp.now()
                        )
                    )
                }
            }.await()

            // Return updated community
            getCommunityById(communityId)

        } catch (e: Exception) {
            Log.e(TAG, "Error joining community: $communityId", e)
            Result.failure(e)
        }
    }

    suspend fun searchCommunities(query: String): Result<List<PaymentCommunity>> {
        return try {
            Log.d(TAG, "Searching communities with query: $query")

            // Firestore no tiene búsqueda de texto completa nativa
            // Por simplicidad, obtenemos todas y filtramos localmente
            val allCommunities = getCommunities().getOrThrow()

            val filteredCommunities = allCommunities.filter { community ->
                community.name.contains(query, ignoreCase = true) ||
                        community.description.contains(query, ignoreCase = true)
            }

            Log.d(TAG, "Found ${filteredCommunities.size} communities matching query")
            Result.success(filteredCommunities)

        } catch (e: Exception) {
            Log.e(TAG, "Error searching communities", e)
            Result.failure(e)
        }
    }

    // Observar cambios en tiempo real
    fun observeCommunities(): Flow<Result<List<PaymentCommunity>>> = flow {
        try {
            communitiesCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing communities", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val communities = snapshot.documents.mapNotNull { document ->
                            try {
                                val data = document.toObject(FirebaseCommunityData::class.java)
                                data?.copy(id = document.id)?.toPaymentCommunity()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing observed community", e)
                                null
                            }
                        }

                        Log.d(TAG, "Observed ${communities.size} communities")
                        // Note: En un flujo real necesitarías un canal para emitir
                    }
                }

            // Por ahora, emitir resultado inicial
            emit(getCommunities())

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

// Extension function para convertir FirebaseCommunityData a PaymentCommunity
private fun FirebaseCommunityData.toPaymentCommunity(): PaymentCommunity {
    return PaymentCommunity(
        id = this.id,
        name = this.name,
        description = this.description,
        targetAmount = this.targetAmount,
        currentAmount = this.currentAmount,
        memberCount = this.members.size,
        dueDate = this.dueDate,
        color = getCommunityColor(this.category),
        icon = getCommunityIcon(this.category),
        status = CommunityStatus.valueOf(this.status),
        createdBy = this.createdBy,
        walletAddress = this.walletAddress
    )
}

private fun getCommunityColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category) {
        "TRAVEL" -> AccentPurple
        "GIFT" -> AccentYellow
        "EDUCATION" -> AccentBlue
        "EVENT" -> AccentGreen
        else -> NestPayPrimary
    }
}

private fun getCommunityIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "TRAVEL" -> Icons.Default.Place
        "GIFT" -> Icons.Default.Favorite
        "EDUCATION" -> Icons.Default.AccountBox
        "EVENT" -> Icons.Default.Star
        else -> Icons.Default.Add
    }
}