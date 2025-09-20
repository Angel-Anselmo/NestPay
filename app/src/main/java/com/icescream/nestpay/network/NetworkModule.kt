package com.icescream.nestpay.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // Backend URL - Railway Production URL
    private const val BASE_URL = "https://nestpay-backend-production.up.railway.app/api/"
    // For local development, use: "http://10.0.2.2:3000/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: NestPayApiService = retrofit.create(NestPayApiService::class.java)
}

/**
 * Helper class to handle API responses
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val code: Int = 0) : ApiResult<T>()
    data class Loading<T>(val message: String = "Loading...") : ApiResult<T>()
}

/**
 * Extension function to safely handle API calls
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { body ->
                ApiResult.Success(body)
            } ?: ApiResult.Error("Empty response body")
        } else {
            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
            ApiResult.Error("HTTP ${response.code()}: $errorMessage", response.code())
        }
    } catch (e: Exception) {
        ApiResult.Error("Network error: ${e.message ?: "Unknown error"}")
    }
}