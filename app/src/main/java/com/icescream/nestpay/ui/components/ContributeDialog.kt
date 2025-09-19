package com.icescream.nestpay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.icescream.nestpay.data.models.PaymentConcept
import com.icescream.nestpay.ui.theme.NestPayPrimary

@Composable
fun ContributeDialog(
    showDialog: Boolean,
    concept: PaymentConcept,
    userName: String,
    onDismiss: () -> Unit,
    onContribute: (Double) -> Unit,
    isLoading: Boolean = false
) {
    var contributionAmount by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Reset state when dialog is shown/hidden
    LaunchedEffect(showDialog) {
        if (showDialog) {
            contributionAmount = ""
            showError = false
            errorMessage = ""
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = NestPayPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contribuir",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = concept.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = NestPayPrimary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Estado actual:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ConceptProgressBar(
                                currentAmount = concept.currentAmount,
                                targetAmount = concept.targetAmount,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contribution amount input
                    Text(
                        text = "Monto a contribuir:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = contributionAmount,
                        onValueChange = { newValue ->
                            // Only allow numbers and decimal point
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                contributionAmount = newValue
                                showError = false
                            }
                        },
                        label = { Text("Cantidad") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text(
                                text = "$",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        ),
                        isError = showError,
                        supportingText = if (showError) {
                            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // User info
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contribuyendo como: $userName",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remaining amount info
                    val remainingAmount = concept.targetAmount - concept.currentAmount
                    if (remainingAmount > 0) {
                        Text(
                            text = "💡 Faltan $${
                                String.format(
                                    "%.2f",
                                    remainingAmount
                                )
                            } para completar la meta",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "🎉 ¡Meta completada! Puedes contribuir extra si deseas",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            Text("Cancelar")
                        }

                        // Contribute Button
                        Button(
                            onClick = {
                                val amount = contributionAmount.toDoubleOrNull()

                                when {
                                    amount == null || amount <= 0 -> {
                                        showError = true
                                        errorMessage = "Ingresa un monto válido"
                                    }

                                    amount > 10000 -> {
                                        showError = true
                                        errorMessage = "Monto máximo: $10,000"
                                    }

                                    else -> {
                                        onContribute(amount)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NestPayPrimary
                            ),
                            enabled = contributionAmount.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Procesando...")
                            } else {
                                val amount = contributionAmount.toDoubleOrNull() ?: 0.0
                                Text("Contribuir $${String.format("%.2f", amount)}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Disclaimer
                    Text(
                        text = "🔒 Tu contribución será procesada de forma segura",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}