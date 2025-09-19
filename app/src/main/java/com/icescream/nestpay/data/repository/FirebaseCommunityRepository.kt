package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.icescream.nestpay.data.auth.AuthService
import com.icescream.nestpay.data.models.Community
import com.icescream.nestpay.data.models.FirebaseCommunity
import kotlinx.coroutines.tasks.await

class FirebaseCommunityRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authService: AuthService = AuthService()

    private val communitiesCollection = firestore.collection("communities")

    companion object {
        private const val TAG = "FirebaseCommunityRepo"
    }

    private fun getCurrentUser(): com.google.firebase.auth.FirebaseUser {
        return authService.getCurrentUser()
            ?: throw Exception("Usuario no autenticado")
    }

    fun getCurrentUserId(): String? {
        return authService.getCurrentUser()?.uid
    }

    suspend fun getCommunities(): Result<List<Community>> {
        return try {
            Log.d(TAG, "Fetching communities from Firestore")
            val currentUser = getCurrentUser()

            // Get communities where the user is a member
            val snapshot = communitiesCollection
                .whereArrayContains("members", currentUser.uid)
                .get()
                .await()

            val communities = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.toObject(FirebaseCommunity::class.java)
                    data?.copy(id = document.id)?.toCommunity()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing community document", e)
                    null
                }
            }.sortedByDescending { it.id } // Sort in client side temporarily

            Log.d(
                TAG,
                "Successfully fetched ${communities.size} communities for user ${currentUser.uid}"
            )
            Result.success(communities)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching communities", e)
            Result.failure(e)
        }
    }

    suspend fun createCommunity(
        name: String,
        description: String,
        category: String,
        paymentPointer: String,
        isPublic: Boolean = false
    ): Result<Community> {
        return try {
            Log.d(TAG, "Creating new community: $name")

            val currentUser = getCurrentUser()
            val inviteCode = generateInviteCode()
            val now = com.google.firebase.Timestamp.now()

            // Create community data without ID field initially
            val communityData = mapOf(
                "name" to name,
                "description" to description,
                "members" to listOf(currentUser.uid),
                "createdBy" to currentUser.uid,
                "inviteCode" to inviteCode,
                "category" to category,
                "isActive" to true,
                "paymentPointer" to paymentPointer,
                "createdAt" to now,
                "updatedAt" to now
            )

            val docRef = communitiesCollection.add(communityData).await()

            Log.d(TAG, "Community created successfully: ${docRef.id}")

            // Return the created community
            val createdCommunity = Community(
                id = docRef.id,
                name = name,
                description = description,
                members = listOf(currentUser.uid),
                createdBy = currentUser.uid,
                inviteCode = inviteCode,
                category = category,
                isActive = true,
                paymentPointer = paymentPointer,
                createdAt = now.toDate(),
                updatedAt = now.toDate()
            )

            Result.success(createdCommunity)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating community", e)
            Result.failure(e)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun joinCommunity(inviteCode: String): Result<Community> {
        return try {
            Log.d(TAG, "Attempting to join community with invite code: $inviteCode")

            val currentUser = getCurrentUser()
            Log.d(TAG, "Current user UID: ${currentUser.uid}")

            // Find community by invite code only (removed isActive filter)
            val snapshot = communitiesCollection
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()

            Log.d(TAG, "Query result size: ${snapshot.size()}")

            if (snapshot.isEmpty) {
                Log.w(TAG, "No community found with invite code: $inviteCode")
                return Result.failure(Exception("Código de invitación '$inviteCode' no existe"))
            }

            val document = snapshot.documents.first()
            Log.d(TAG, "Found community document with ID: ${document.id}")

            // Debug: log all document data
            Log.d(TAG, "Full document data: ${document.data}")

            val communityData = document.toObject(FirebaseCommunity::class.java)
                ?: return Result.failure(Exception("Error al procesar los datos de la comunidad"))

            Log.d(TAG, "Community name: ${communityData.name}")
            Log.d(TAG, "Community isActive: ${communityData.isActive}")
            Log.d(TAG, "Current members: ${communityData.members}")

            // Check if community is active
            if (!communityData.isActive) {
                Log.w(TAG, "Community ${document.id} is not active")
                return Result.failure(Exception("La comunidad con código '$inviteCode' no está activa"))
            }

            // Check if user is already a member
            if (communityData.members.contains(currentUser.uid)) {
                Log.i(
                    TAG,
                    "User ${currentUser.uid} is already a member of community ${document.id}"
                )
                return Result.failure(Exception("Ya eres miembro de esta comunidad"))
            }

            // Add user to members list
            val updatedMembers = communityData.members + currentUser.uid
            Log.d(TAG, "Updated members list: $updatedMembers")

            val updateData = mapOf(
                "members" to updatedMembers,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            document.reference.update(updateData).await()

            Log.d(TAG, "User ${currentUser.uid} successfully joined community ${document.id}")

            // Return the updated community
            val updatedCommunity = communityData.copy(
                id = document.id,
                members = updatedMembers,
                updatedAt = com.google.firebase.Timestamp.now()
            ).toCommunity()

            Result.success(updatedCommunity)

        } catch (e: Exception) {
            Log.e(TAG, "Error joining community with invite code: $inviteCode", e)
            Result.failure(e)
        }
    }
}