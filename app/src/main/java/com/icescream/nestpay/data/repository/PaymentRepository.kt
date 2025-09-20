package com.icescream.nestpay.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.icescream.nestpay.network.*
import kotlinx.coroutines.tasks.await

class PaymentRepository {

    // URL del backend para dispositivo físico
    // No olvidar actualizar también en NetworkModule si está ahí configurado
    private val apiService = NetworkModule.apiService
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "PaymentRepository"
    }

    /**
     * Get Firebase Auth token for API authentication
     */
    private suspend fun getAuthToken(): String? {
        return try {
            val user = auth.currentUser ?: return null
            val tokenResult = user.getIdToken(false).await()
            tokenResult.token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting auth token", e)
            null
        }
    }

    /**
     * Probar conexión con el backend
     */
    suspend fun testConnection(): ApiResult<String> {
        return try {
            Log.d(TAG, "🧪 Probando conexión con backend local...")
            val response = apiService.healthCheck()

            if (response.isSuccessful) {
                val healthData = response.body()
                Log.d(TAG, "✅ Backend conectado: ${healthData?.message}")
                ApiResult.Success("Backend conectado correctamente")
            } else {
                Log.e(TAG, "❌ Error de conexión: ${response.code()}")
                ApiResult.Error("Error de conexión: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error de red: ${e.message}")
            ApiResult.Error("Error de red: ${e.message}")
        }
    }

    /**
     * Obtener información del sistema
     */
    suspend fun getSystemInfo(): ApiResult<String> {
        return try {
            Log.d(TAG, "📊 Obteniendo info del sistema...")
            val response = apiService.getSystemInfo()

            if (response.isSuccessful) {
                val systemData = response.body()?.data
                Log.d(TAG, "✅ Sistema: ${systemData?.system}")
                ApiResult.Success("Sistema funcionando: ${systemData?.system}")
            } else {
                Log.e(TAG, "❌ Error system info: ${response.code()}")
                ApiResult.Error("Error obteniendo system info: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
            ApiResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Iniciar pago de aporte a comunidad
     */
    suspend fun initiateCommunityPayment(
        amount: String,
        communityId: String,
        conceptId: String,
        description: String = ""
    ): ApiResult<String> {
        return try {
            Log.d(TAG, "💰 Iniciando pago de aporte...")

            val request = CommunityPaymentRequest(
                amount = PaymentAmount(value = amount),
                description = description.ifEmpty { "Aporte a comunidad $communityId - $conceptId" },
                communityId = communityId,
                conceptId = conceptId
            )

            val response = apiService.initiateCommunityPayment(request)

            if (response.isSuccessful) {
                val paymentData = response.body()?.data
                Log.d(TAG, "✅ Pago iniciado: ${paymentData?.incomingPaymentId}")
                ApiResult.Success("Pago iniciado - ID: ${paymentData?.incomingPaymentId}")
            } else {
                Log.e(TAG, "❌ Error iniciando pago: ${response.code()}")
                ApiResult.Error("Error iniciando pago: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
            ApiResult.Error("Error: ${e.message}")
        }
    }

    /**
     * Obtener wallets de prueba
     */
    suspend fun getTestWallets(): ApiResult<String> {
        return try {
            Log.d(TAG, "🧪 Obteniendo wallets de prueba...")
            val response = apiService.getTestWallets()

            if (response.isSuccessful) {
                val walletsData = response.body()?.data
                Log.d(TAG, "✅ Wallets disponibles: ${walletsData?.size}")
                ApiResult.Success("${walletsData?.size} wallets de prueba disponibles")
            } else {
                Log.e(TAG, "❌ Error obteniendo wallets: ${response.code()}")
                ApiResult.Error("Error obteniendo wallets: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
            ApiResult.Error("Error: ${e.message}")
        }
    }
}