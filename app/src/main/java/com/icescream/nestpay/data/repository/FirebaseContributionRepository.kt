package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.icescream.nestpay.data.auth.AuthService
import com.icescream.nestpay.data.models.UserContribution
import com.icescream.nestpay.data.models.ContributionStatus
import kotlinx.coroutines.tasks.await

class FirebaseContributionRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authService: AuthService = AuthService()

    private val contributionsCollection = firestore.collection("user_contributions")
    private val conceptsCollection = firestore.collection("payment_concepts")

    companion object {
        private const val TAG = "ContributionRepo"
    }

    private fun getCurrentUser(): com.google.firebase.auth.FirebaseUser {
        return authService.getCurrentUser()
            ?: throw Exception("Usuario no autenticado")
    }

    suspend fun createContribution(
        conceptId: String,
        communityId: String,
        amount: Double,
        userName: String
    ): Result<UserContribution> {
        return try {
            Log.d(TAG, "Creating contribution for concept: $conceptId, amount: $amount")

            val currentUser = getCurrentUser()
            val now = com.google.firebase.Timestamp.now()

            // Create contribution data
            val contributionData = mapOf(
                "conceptId" to conceptId,
                "communityId" to communityId,
                "userId" to currentUser.uid,
                "userName" to userName,
                "userAvatar" to "", // TODO: Get from user profile
                "amount" to amount,
                "status" to "COMPLETED",
                "transactionId" to "tx_${System.currentTimeMillis()}", // Mock transaction ID
                "createdAt" to now,
                "completedAt" to now
            )

            val docRef = contributionsCollection.add(contributionData).await()

            // Update concept's current amount
            updateConceptAmount(conceptId, amount)

            Log.d(TAG, "Contribution created successfully: ${docRef.id}")

            // Return the created contribution
            val createdContribution = UserContribution(
                id = docRef.id,
                conceptId = conceptId,
                communityId = communityId,
                userId = currentUser.uid,
                userName = userName,
                userAvatar = "",
                amount = amount,
                status = ContributionStatus.COMPLETED,
                transactionId = "tx_${System.currentTimeMillis()}",
                createdAt = now.toDate(),
                completedAt = now.toDate()
            )

            Result.success(createdContribution)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating contribution", e)
            Result.failure(e)
        }
    }

    suspend fun getContributionsForConcept(conceptId: String): Result<List<UserContribution>> {
        return try {
            Log.d(TAG, "Fetching contributions for concept: $conceptId")

            val snapshot = contributionsCollection
                .whereEqualTo("conceptId", conceptId)
                .whereEqualTo("status", "COMPLETED")
                .get()
                .await()

            val contributions = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        UserContribution(
                            id = document.id,
                            conceptId = data["conceptId"] as? String ?: "",
                            communityId = data["communityId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "Usuario",
                            userAvatar = data["userAvatar"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            status = ContributionStatus.COMPLETED,
                            transactionId = data["transactionId"] as? String ?: "",
                            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate(),
                            completedAt = (data["completedAt"] as? com.google.firebase.Timestamp)?.toDate()
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing contribution document", e)
                    null
                }
            }.sortedByDescending { it.completedAt }

            Log.d(
                TAG,
                "Successfully fetched ${contributions.size} contributions for concept $conceptId"
            )
            Result.success(contributions)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching contributions", e)
            Result.failure(e)
        }
    }

    suspend fun getContributionsForCommunity(communityId: String): Result<List<UserContribution>> {
        return try {
            Log.d(TAG, "Fetching contributions for community: $communityId")

            val snapshot = contributionsCollection
                .whereEqualTo("communityId", communityId)
                .whereEqualTo("status", "COMPLETED")
                .get()
                .await()

            val contributions = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        UserContribution(
                            id = document.id,
                            conceptId = data["conceptId"] as? String ?: "",
                            communityId = data["communityId"] as? String ?: "",
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "Usuario",
                            userAvatar = data["userAvatar"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            status = ContributionStatus.COMPLETED,
                            transactionId = data["transactionId"] as? String ?: "",
                            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate(),
                            completedAt = (data["completedAt"] as? com.google.firebase.Timestamp)?.toDate()
                        )
                    } else null
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing contribution document", e)
                    null
                }
            }.sortedByDescending { it.completedAt }

            Log.d(
                TAG,
                "Successfully fetched ${contributions.size} contributions for community $communityId"
            )
            Result.success(contributions)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching contributions", e)
            Result.failure(e)
        }
    }

    private suspend fun updateConceptAmount(conceptId: String, contributionAmount: Double) {
        try {
            val conceptDoc = conceptsCollection.document(conceptId).get().await()
            val currentAmount =
                (conceptDoc.data?.get("currentAmount") as? Number)?.toDouble() ?: 0.0
            val newAmount = currentAmount + contributionAmount

            conceptsCollection.document(conceptId).update(
                mapOf(
                    "currentAmount" to newAmount,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Log.d(TAG, "Updated concept $conceptId amount from $currentAmount to $newAmount")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating concept amount", e)
            throw e
        }
    }
}