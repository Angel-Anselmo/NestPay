package com.icescream.nestpay.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConceptProgressBar(
    currentAmount: Double,
    targetAmount: Double,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true
) {
    val progress = if (targetAmount > 0) {
        (currentAmount / targetAmount).toFloat().coerceAtMost(1f)
    } else 0f

    val isCompleted = currentAmount >= targetAmount
    val isOverAchieved = currentAmount > targetAmount

    Column(modifier = modifier) {
        // Barra de progreso visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        ) {
            // Progreso completado
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isOverAchieved -> Color(0xFFFFD700) // Dorado
                            isCompleted -> Color(0xFF4CAF50)    // Verde brillante
                            progress > 0.9f -> Color(0xFF66BB6A) // Verde intenso
                            progress > 0.7f -> Color(0xFF81C784) // Verde medio
                            else -> Color(0xFFA5D6A7)           // Verde claro
                        }
                    )
                    .animateContentSize() // Animación suave
            )
        }

        if (showDetails) {
            Spacer(modifier = Modifier.height(8.dp))

            // Texto de progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${String.format("%.2f", currentAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCompleted -> Color(0xFF4CAF50)
                        else -> Color.Black
                    }
                )
                Text(
                    text = when {
                        isOverAchieved -> "¡Meta superada! 🎉"
                        isCompleted -> "¡Completado! ✅"
                        else -> "Meta: $${String.format("%.2f", targetAmount)}"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Información adicional
            if (!isCompleted && targetAmount > currentAmount) {
                val remaining = targetAmount - currentAmount
                Text(
                    text = "Faltan: $${String.format("%.2f", remaining)}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ConceptProgressBarCompact(
    currentAmount: Double,
    targetAmount: Double,
    modifier: Modifier = Modifier
) {
    val progress = if (targetAmount > 0) {
        (currentAmount / targetAmount).toFloat().coerceAtMost(1f)
    } else 0f

    val isCompleted = currentAmount >= targetAmount
    val isOverAchieved = currentAmount > targetAmount

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Barra compacta
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            isOverAchieved -> Color(0xFFFFD700) // Dorado
                            isCompleted -> Color(0xFF4CAF50)    // Verde brillante
                            progress > 0.7f -> Color(0xFF66BB6A) // Verde intenso
                            else -> Color(0xFF81C784)           // Verde claro
                        }
                    )
                    .animateContentSize()
            )
        }

        // Texto compacto
        Text(
            text = "$${String.format("%.0f", currentAmount)}/$${
                String.format(
                    "%.0f",
                    targetAmount
                )
            }",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}