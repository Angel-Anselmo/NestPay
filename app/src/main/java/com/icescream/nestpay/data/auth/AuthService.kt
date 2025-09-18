package com.icescream.nestpay.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        private const val TAG = "AuthService"
    }

    // Obtener usuario actual
    fun getCurrentUser() = auth.currentUser

    // Verificar si hay usuario logueado
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Login como invitado (anónimo)
    suspend fun signInAnonymously(): Result<String> {
        return try {
            Log.d(TAG, "Signing in anonymously")

            val result = auth.signInAnonymously().await()
            val userId = result.user?.uid

            if (userId != null) {
                Log.d(TAG, "Anonymous sign-in successful. User ID: $userId")
                Result.success(userId)
            } else {
                Log.e(TAG, "Anonymous sign-in failed: User ID is null")
                Result.failure(Exception("Error al iniciar sesión como invitado"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in failed", e)
            Result.failure(e)
        }
    }

    // Logout
    suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Signing out user")
            auth.signOut()
            Log.d(TAG, "Sign out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
            Result.failure(e)
        }
    }

    // Obtener ID del usuario actual
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Verificar si el usuario es anónimo
    fun isAnonymousUser(): Boolean {
        return auth.currentUser?.isAnonymous == true
    }

    // Obtener información del usuario
    fun getUserInfo(): UserInfo? {
        val user = auth.currentUser
        return if (user != null) {
            UserInfo(
                id = user.uid,
                email = user.email,
                isAnonymous = user.isAnonymous,
                displayName = user.displayName ?: if (user.isAnonymous) "Invitado" else "Usuario"
            )
        } else null
    }
}

data class UserInfo(
    val id: String,
    val email: String?,
    val isAnonymous: Boolean,
    val displayName: String
)