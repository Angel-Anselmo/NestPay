package com.icescream.nestpay.data.repository

import android.util.Log
import com.icescream.nestpay.network.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Repositorio específico para pagos de comunidad usando el backend NestPay local
 * Maneja el flujo User → Admin a través de Open Payments
 */
class CommunityPaymentRepository {

    companion object {
        private const val TAG = "CommunityPaymentRepo"
        private const val BACKEND_URL = "http://192.168.0.11:3000/api/" // Para dispositivo físico
        // Para emulador usar: "http://10.0.2.2:3000/api/"
    }

    private val apiService: NestPayApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(NestPayApiService::class.java)
    }

    /**
     * Probar conexión con el backend local
     */
    suspend fun testConnection(): ApiResult<HealthResponse> {
        return safeApiCall {
            Log.d(TAG, "🧪 Probando conexión con backend local...")
            apiService.healthCheck()
        }
    }

    /**
     * Obtener información del sistema multi-wallet
     */
    suspend fun getSystemInfo(): ApiResult<SystemInfoResponse> {
        return safeApiCall {
            Log.d(TAG, "📊 Obteniendo información del sistema...")
            apiService.getSystemInfo()
        }
    }

    /**
     * Iniciar un pago de aporte a comunidad
     * Este método inicia el flujo donde el USER paga al ADMIN
     */
    suspend fun initiateCommunityPayment(
        amount: String,
        communityId: String,
        conceptId: String,
        description: String = ""
    ): ApiResult<CommunityPaymentResponse> {
        return safeApiCall {
            Log.d(TAG, "💰 Iniciando pago de aporte...")
            Log.d(TAG, "📊 Monto: $amount, Comunidad: $communityId, Concepto: $conceptId")

            val request = CommunityPaymentRequest(
                amount = PaymentAmount(value = amount),
                description = description.ifEmpty { "Aporte a comunidad $communityId - $conceptId" },
                communityId = communityId,
                conceptId = conceptId
            )

            apiService.initiateCommunityPayment(request)
        }
    }

    /**
     * Finalizar un pago después de que el usuario autorice
     */
    suspend fun finalizeCommunityPayment(
        continueUri: String,
        continueAccessToken: String,
        interactRef: String,
        quoteId: String,
        communityId: String? = null,
        conceptId: String? = null
    ): ApiResult<CommunityPaymentResult> {
        return safeApiCall {
            Log.d(TAG, "🏁 Finalizando pago de comunidad...")

            val request = FinalizeCommunityPaymentRequest(
                continueUri = continueUri,
                continueAccessToken = continueAccessToken,
                interactRef = interactRef,
                quoteId = quoteId,
                communityId = communityId,
                conceptId = conceptId
            )

            apiService.finalizeCommunityPayment(request)
        }
    }

    /**
     * Obtener wallets de prueba disponibles
     */
    suspend fun getTestWallets(): ApiResult<TestWalletsResponse> {
        return safeApiCall {
            Log.d(TAG, "🧪 Obteniendo wallets de prueba...")
            apiService.getTestWallets()
        }
    }

    /**
     * Validar una wallet address
     */
    suspend fun validateWallet(walletUrl: String): ApiResult<WalletValidationResponse> {
        return safeApiCall {
            Log.d(TAG, "🔍 Validando wallet: $walletUrl")
            apiService.validateWallet(walletUrl)
        }
    }

    /**
     * Obtener estado de un pago
     */
    suspend fun getPaymentStatus(
        paymentId: String,
        walletAddress: String,
        accessToken: String,
        type: String
    ): ApiResult<PaymentStatusResponse> {
        return safeApiCall {
            Log.d(TAG, "📋 Obteniendo estado del pago: $paymentId")
            apiService.getPaymentStatus(paymentId, walletAddress, accessToken, type)
        }
    }

    /**
     * Método de prueba completo para verificar toda la funcionalidad
     */
    suspend fun runFullTest(): String {
        val results = mutableListOf<String>()

        try {
            // 1. Probar conexión
            results.add("=== PRUEBA DE CONEXIÓN ===")
            when (val healthResult = testConnection()) {
                is ApiResult.Success -> {
                    results.add("✅ Backend conectado: ${healthResult.data.message}")
                }

                is ApiResult.Error -> {
                    results.add("❌ Error de conexión: ${healthResult.message}")
                    return results.joinToString("\n")
                }

                is ApiResult.Loading -> {
                    results.add("⏳ Conectando...")
                }
            }

            // 2. Probar system info
            results.add("\n=== INFO DEL SISTEMA ===")
            when (val systemResult = getSystemInfo()) {
                is ApiResult.Success -> {
                    val system = systemResult.data.data
                    results.add("✅ Sistema: ${system.system}")
                    results.add("👑 Admin: ${system.wallets["adminWallet"]?.address}")
                    results.add("👤 User: ${system.wallets["userWallet"]?.address}")
                }

                is ApiResult.Error -> {
                    results.add("❌ Error system info: ${systemResult.message}")
                }

                is ApiResult.Loading -> {
                    results.add("⏳ Obteniendo info...")
                }
            }

            // 3. Probar wallets de prueba
            results.add("\n=== WALLETS DE PRUEBA ===")
            when (val walletsResult = getTestWallets()) {
                is ApiResult.Success -> {
                    val wallets = walletsResult.data.data
                    results.add("✅ ${wallets.size} wallets disponibles:")
                    wallets.forEach { wallet ->
                        results.add("  - ${wallet.name}: ${wallet.url}")
                    }
                }

                is ApiResult.Error -> {
                    results.add("❌ Error wallets: ${walletsResult.message}")
                }

                is ApiResult.Loading -> {
                    results.add("⏳ Obteniendo wallets...")
                }
            }

            // 4. Probar iniciar pago
            results.add("\n=== PRUEBA DE PAGO ===")
            when (val paymentResult = initiateCommunityPayment(
                amount = "100",
                communityId = "test-community",
                conceptId = "test-concept",
                description = "Pago de prueba desde Android"
            )) {
                is ApiResult.Success -> {
                    val payment = paymentResult.data.data
                    results.add("✅ Pago iniciado:")
                    results.add("  ID: ${payment.incomingPaymentId}")
                    results.add("  Quote: ${payment.quoteId}")
                    results.add("  Admin → ${payment.adminWallet}")
                    results.add("  User → ${payment.userWallet}")
                    results.add("🔗 URL Autorización:")
                    results.add("  ${payment.authorizationUrl}")
                }

                is ApiResult.Error -> {
                    results.add("❌ Error pago: ${paymentResult.message}")
                }

                is ApiResult.Loading -> {
                    results.add("⏳ Iniciando pago...")
                }
            }

        } catch (e: Exception) {
            results.add("❌ Error general: ${e.message}")
        }

        return results.joinToString("\n")
    }
}