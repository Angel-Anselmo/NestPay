package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.icescream.nestpay.data.auth.AuthService
import com.icescream.nestpay.data.models.UserProfile
import com.icescream.nestpay.data.models.FirebaseUserProfile
import kotlinx.coroutines.tasks.await

class FirebaseUserProfileRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authService: AuthService = AuthService()

    private val profilesCollection = firestore.collection("user_profiles")

    companion object {
        private const val TAG = "FirebaseUserProfileRepo"
    }

    private fun getCurrentUser(): com.google.firebase.auth.FirebaseUser {
        return authService.getCurrentUser()
            ?: throw Exception("Usuario no autenticado")
    }

    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            Log.d(TAG, "Fetching user profile")
            val currentUser = getCurrentUser()

            val document = profilesCollection.document(currentUser.uid).get().await()

            if (document.exists()) {
                val data = document.toObject(FirebaseUserProfile::class.java)
                val profile = data?.copy(id = document.id)?.toUserProfile()
                Log.d(TAG, "User profile found")
                Result.success(profile)
            } else {
                Log.d(TAG, "No user profile found")
                Result.success(null)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile", e)
            Result.failure(e)
        }
    }

    suspend fun createOrUpdateUserProfile(
        displayName: String? = null,
        email: String? = null,
        mainWalletAddress: String? = null
    ): Result<UserProfile> {
        return try {
            Log.d(TAG, "Creating or updating user profile")
            val currentUser = getCurrentUser()
            val now = com.google.firebase.Timestamp.now()

            // Get existing profile or create new one
            val existingProfile = getUserProfile().getOrNull()

            val profileData = mapOf(
                "id" to currentUser.uid,
                "displayName" to (displayName ?: existingProfile?.displayName
                ?: currentUser.displayName ?: "Usuario"),
                "email" to (email ?: existingProfile?.email ?: currentUser.email ?: ""),
                "mainWalletAddress" to (mainWalletAddress ?: existingProfile?.mainWalletAddress
                ?: ""),
                "isAnonymous" to currentUser.isAnonymous,
                "totalCommunities" to (existingProfile?.totalCommunities ?: 0),
                "totalConcepts" to (existingProfile?.totalConcepts ?: 0),
                "createdAt" to (existingProfile?.createdAt?.let { com.google.firebase.Timestamp(it) }
                    ?: now),
                "updatedAt" to now
            )

            profilesCollection.document(currentUser.uid).set(profileData).await()

            Log.d(TAG, "User profile saved successfully")

            // Return the updated profile
            val updatedProfile = UserProfile(
                id = currentUser.uid,
                displayName = profileData["displayName"] as String,
                email = profileData["email"] as String,
                mainWalletAddress = profileData["mainWalletAddress"] as String,
                isAnonymous = profileData["isAnonymous"] as Boolean,
                totalCommunities = profileData["totalCommunities"] as Int,
                totalConcepts = profileData["totalConcepts"] as Int,
                createdAt = (profileData["createdAt"] as com.google.firebase.Timestamp).toDate(),
                updatedAt = now.toDate()
            )

            Result.success(updatedProfile)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating user profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateMainWalletAddress(walletAddress: String): Result<UserProfile> {
        return try {
            Log.d(TAG, "Updating main wallet address: $walletAddress")

            val result = createOrUpdateUserProfile(mainWalletAddress = walletAddress)

            if (result.isSuccess) {
                Log.d(TAG, "Main wallet address updated successfully")
            }

            result

        } catch (e: Exception) {
            Log.e(TAG, "Error updating main wallet address", e)
            Result.failure(e)
        }
    }

    suspend fun hasMainWalletConfigured(): Result<Boolean> {
        return try {
            val profile = getUserProfile().getOrNull()
            val hasWallet = !profile?.mainWalletAddress.isNullOrBlank()

            Log.d(TAG, "User has main wallet configured: $hasWallet")
            Result.success(hasWallet)

        } catch (e: Exception) {
            Log.e(TAG, "Error checking main wallet configuration", e)
            Result.failure(e)
        }
    }
}