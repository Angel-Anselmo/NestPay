package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.icescream.nestpay.data.auth.AuthService
import com.icescream.nestpay.data.models.PaymentConcept
import com.icescream.nestpay.data.models.FirebasePaymentConcept
import com.icescream.nestpay.data.models.ConceptStatus
import kotlinx.coroutines.tasks.await

class FirebasePaymentConceptRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authService: AuthService = AuthService()

    private val conceptsCollection = firestore.collection("payment_concepts")

    companion object {
        private const val TAG = "FirebaseConceptRepo"
    }

    private fun getCurrentUser(): com.google.firebase.auth.FirebaseUser {
        return authService.getCurrentUser()
            ?: throw Exception("Usuario no autenticado")
    }

    fun getCurrentUserId(): String? {
        return authService.getCurrentUser()?.uid
    }

    suspend fun createPaymentConcept(
        communityId: String,
        name: String,
        description: String,
        targetAmount: Double,
        dueDate: String
    ): Result<PaymentConcept> {
        return try {
            Log.d(TAG, "Creating new payment concept: $name for community: $communityId")

            val currentUser = getCurrentUser()
            val now = com.google.firebase.Timestamp.now()

            // Create concept data (no wallet address needed - uses community's payment pointer)
            val conceptData = mapOf(
                "communityId" to communityId,
                "name" to name,
                "description" to description,
                "targetAmount" to targetAmount,
                "currentAmount" to 0.0,
                "dueDate" to dueDate,
                "createdBy" to currentUser.uid,
                "status" to "ACTIVE",
                "createdAt" to now,
                "updatedAt" to now
            )

            val docRef = conceptsCollection.add(conceptData).await()

            Log.d(TAG, "Payment concept created successfully: ${docRef.id}")

            // Return the created concept
            val createdConcept = PaymentConcept(
                id = docRef.id,
                communityId = communityId,
                name = name,
                description = description,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                dueDate = dueDate,
                createdBy = currentUser.uid,
                status = ConceptStatus.ACTIVE,
                createdAt = now.toDate(),
                updatedAt = now.toDate()
            )

            Result.success(createdConcept)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating payment concept", e)
            Result.failure(e)
        }
    }

    suspend fun getPaymentConcepts(communityId: String): Result<List<PaymentConcept>> {
        return try {
            Log.d(TAG, "Fetching payment concepts for community: $communityId")

            val snapshot = conceptsCollection
                .whereEqualTo("communityId", communityId)
                .get()
                .await()

            val concepts = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.toObject(FirebasePaymentConcept::class.java)
                    data?.copy(id = document.id)?.toPaymentConcept()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing payment concept document", e)
                    null
                }
            }.sortedByDescending { it.createdAt }

            Log.d(
                TAG,
                "Successfully fetched ${concepts.size} payment concepts for community $communityId"
            )
            Result.success(concepts)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching payment concepts", e)
            Result.failure(e)
        }
    }

    suspend fun updatePaymentConcept(conceptId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            Log.d(TAG, "Updating payment concept: $conceptId")

            val updateData = updates + mapOf("updatedAt" to com.google.firebase.Timestamp.now())
            conceptsCollection.document(conceptId).update(updateData).await()

            Log.d(TAG, "Payment concept updated successfully: $conceptId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment concept", e)
            Result.failure(e)
        }
    }

    suspend fun deletePaymentConcept(conceptId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting payment concept: $conceptId")

            conceptsCollection.document(conceptId).delete().await()

            Log.d(TAG, "Payment concept deleted successfully: $conceptId")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting payment concept", e)
            Result.failure(e)
        }
    }
}