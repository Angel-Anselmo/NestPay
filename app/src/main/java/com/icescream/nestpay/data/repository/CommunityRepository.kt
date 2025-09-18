package com.icescream.nestpay.data.repository

import com.icescream.nestpay.ui.screens.PaymentCommunity
import com.icescream.nestpay.ui.screens.CommunityStatus
import com.icescream.nestpay.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class CommunityRepository {

    // Por ahora usamos datos mockeados, después conectaremos Firebase
    private val mockCommunities = listOf(
        PaymentCommunity(
            id = "1",
            name = "Viaje a Cancún con Amigos",
            description = "Vacaciones de verano 2024",
            targetAmount = 500.0,
            currentAmount = 250.0,
            memberCount = 8,
            dueDate = "15/12/2024",
            color = AccentPurple,
            icon = Icons.Default.Place,
            status = CommunityStatus.ACTIVE,
            createdBy = "user123",
            walletAddress = "https://wallet.interledger-test.dev/cancun-trip"
        ),
        PaymentCommunity(
            id = "2",
            name = "Regalo para María",
            description = "Cumpleaños sorpresa",
            targetAmount = 150.0,
            currentAmount = 120.0,
            memberCount = 5,
            dueDate = "20/11/2024",
            color = AccentYellow,
            icon = Icons.Default.Favorite,
            status = CommunityStatus.ACTIVE,
            createdBy = "user456",
            walletAddress = "https://wallet.interledger-test.dev/maria-gift"
        ),
        PaymentCommunity(
            id = "3",
            name = "Material Escolar",
            description = "Libros y útiles para el semestre",
            targetAmount = 200.0,
            currentAmount = 200.0,
            memberCount = 12,
            dueDate = "01/02/2024",
            color = AccentBlue,
            icon = Icons.Default.AccountBox,
            status = CommunityStatus.COMPLETED,
            createdBy = "user789",
            walletAddress = "https://wallet.interledger-test.dev/school-supplies"
        )
    )

    suspend fun getCommunities(): Result<List<PaymentCommunity>> {
        return try {
            // Simular llamada a red
            delay(1000)
            Result.success(mockCommunities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommunityById(id: String): Result<PaymentCommunity?> {
        return try {
            delay(500)
            val community = mockCommunities.find { it.id == id }
            Result.success(community)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCommunity(
        name: String,
        description: String,
        targetAmount: Double,
        walletAddress: String,
        dueDate: String,
        category: String
    ): Result<PaymentCommunity> {
        return try {
            delay(1500)

            val newCommunity = PaymentCommunity(
                id = "new_${System.currentTimeMillis()}",
                name = name,
                description = description,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                memberCount = 1, // Solo el creador inicialmente
                dueDate = dueDate,
                color = getCommunityColor(category),
                icon = getCommunityIcon(category),
                status = CommunityStatus.ACTIVE,
                createdBy = "current_user", // TODO: Obtener del auth
                walletAddress = walletAddress
            )

            Result.success(newCommunity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinCommunity(communityId: String): Result<PaymentCommunity?> {
        return try {
            delay(1000)
            // TODO: Implementar lógica de unirse a comunidad
            val community = mockCommunities.find { it.id == communityId }
            Result.success(community)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCommunities(query: String): Result<List<PaymentCommunity>> {
        return try {
            delay(800)
            val filteredCommunities = mockCommunities.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
            Result.success(filteredCommunities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Funciones de observación en tiempo real (para Firebase después)
    fun observeCommunities(): Flow<Result<List<PaymentCommunity>>> = flow {
        while (true) {
            emit(getCommunities())
            delay(30000) // Actualizar cada 30 segundos
        }
    }

    fun observeCommunity(id: String): Flow<Result<PaymentCommunity?>> = flow {
        while (true) {
            emit(getCommunityById(id))
            delay(10000) // Actualizar cada 10 segundos
        }
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
}