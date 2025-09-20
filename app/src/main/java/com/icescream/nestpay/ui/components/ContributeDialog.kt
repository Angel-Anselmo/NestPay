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
import com.icescream.nestpay.ui.viewmodel.ContributionViewModel
import com.icescream.nestpay.ui.viewmodel.CreateContributionState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeDialog(
    showDialog: Boolean,
    concept: PaymentConcept,
    userName: String,
    communityId: String,
    onDismiss: () -> Unit,
    onContribute: (amount: Double) -> Unit,
    isLoading: Boolean = false,
    contributionViewModel: ContributionViewModel = viewModel()
) {
    var contributionAmount by remember { mutableStateOf("") }

    // Observar el estado de la contribución
    val contributionState by contributionViewModel.createContributionState.collectAsState()

    // Estados para el WebView de autorización
    var showAuthorizationWebView by remember { mutableStateOf(false) }
    var authorizationUrl by remember { mutableStateOf("") }
    var paymentProcessData by remember {
        mutableStateOf<com.icescream.nestpay.ui.viewmodel.PaymentProcessData?>(
            null
        )
    }

    // Reset form when dialog shows
    LaunchedEffect(showDialog) {
        if (showDialog) {
            contributionAmount = ""
            contributionViewModel.resetCreateContributionState()
        }
    }

    // Manejar cambios en el estado de contribución
    LaunchedEffect(contributionState) {
        when (val state = contributionState) {
            is CreateContributionState.AuthorizationRequired -> {
                authorizationUrl = state.authorizationUrl
                paymentProcessData = state.paymentData
                showAuthorizationWebView = true
            }
            is CreateContributionState.Success -> {
                // Éxito - cerrar diálogo después de un breve delay
                delay(1500)
                onDismiss()
                contributionViewModel.resetCreateContributionState()
            }
            else -> {
                showAuthorizationWebView = false
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = "Contribuir al Concepto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = concept.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = NestPayPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Concept progress
                    val progress = if (concept.targetAmount > 0) {
                        (concept.currentAmount / concept.targetAmount).coerceIn(0.0, 1.0).toFloat()
                    } else 0f

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Recaudado: $${concept.currentAmount}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Meta: $${concept.targetAmount}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = NestPayPrimary,
                            trackColor = Color.Gray.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${(progress * 100).toInt()}% completado",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Payment info - Actualizado con estado
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (contributionState) {
                                is CreateContributionState.Loading -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                is CreateContributionState.Success -> Color.Green.copy(alpha = 0.1f)
                                is CreateContributionState.Error -> Color.Red.copy(alpha = 0.1f)
                                else -> NestPayPrimary.copy(alpha = 0.1f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (contributionState) {
                                    is CreateContributionState.Loading -> Icons.Default.Refresh
                                    is CreateContributionState.Success -> Icons.Default.CheckCircle
                                    is CreateContributionState.Error -> Icons.Default.Warning
                                    else -> Icons.Default.AccountCircle
                                },
                                contentDescription = null,
                                tint = when (contributionState) {
                                    is CreateContributionState.Loading -> Color(0xFFFF9800)
                                    is CreateContributionState.Success -> Color.Green
                                    is CreateContributionState.Error -> Color.Red
                                    else -> NestPayPrimary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (contributionState) {
                                        is CreateContributionState.Loading -> "Procesando..."
                                        is CreateContributionState.AuthorizationRequired -> "Esperando autorización"
                                        is CreateContributionState.Success -> "¡Pago completado!"
                                        is CreateContributionState.Error -> "Error en pago"
                                        else -> "Pago vía:"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = when (val state = contributionState) {
                                        is CreateContributionState.Error -> state.message
                                        is CreateContributionState.Success -> "Contribución registrada exitosamente"
                                        else -> "Open Payments (Backend Local)"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when (contributionState) {
                                        is CreateContributionState.Loading -> Color(0xFFFF9800)
                                        is CreateContributionState.Success -> Color.Green
                                        is CreateContributionState.Error -> Color.Red
                                        else -> NestPayPrimary
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount input - Solo mostrar si no está en proceso
                    if (contributionState !is CreateContributionState.Loading &&
                        contributionState !is CreateContributionState.AuthorizationRequired &&
                        contributionState !is CreateContributionState.Success
                    ) {

                        Column {
                            Text(
                                text = "Monto a contribuir",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = contributionAmount,
                                onValueChange = { value ->
                                    // Only allow numbers and decimal point
                                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        contributionAmount = value
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                prefix = { Text("$") },
                                suffix = { Text("USD", color = Color.Gray) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NestPayPrimary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                isError = contributionState is CreateContributionState.Error
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Se abrirá una ventana para autorizar el pago en tu wallet",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    } else if (contributionState is CreateContributionState.AuthorizationRequired) {
                        // Mostrar información de autorización pendiente
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = NestPayPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Esperando autorización en tu wallet...",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (contributionState is CreateContributionState.AuthorizationRequired) {
                                    contributionViewModel.cancelPaymentProcess()
                                }
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (contributionState is CreateContributionState.AuthorizationRequired)
                                    "Cancelar Pago"
                                else
                                    "Cancelar"
                            )
                        }

                        Button(
                            onClick = {
                                val amount = contributionAmount.toDoubleOrNull()
                                if (amount != null && amount > 0) {
                                    // Usar el ViewModel para iniciar el proceso
                                    contributionViewModel.createContribution(
                                        conceptId = concept.id,
                                        communityId = communityId,
                                        amount = amount,
                                        userName = userName
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = contributionState !is CreateContributionState.Loading &&
                                    contributionState !is CreateContributionState.AuthorizationRequired &&
                                    contributionState !is CreateContributionState.Success &&
                                    contributionAmount.isNotBlank() &&
                                    contributionAmount.toDoubleOrNull() != null &&
                                    contributionAmount.toDoubleOrNull()!! > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (contributionState) {
                                    is CreateContributionState.Success -> Color.Green
                                    else -> NestPayPrimary
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            when (contributionState) {
                                is CreateContributionState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Procesando...")
                                }

                                is CreateContributionState.Success -> {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("¡Completado!")
                                }

                                else -> {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Contribuir")
                                }
                            }
                        }
                    }

                    // Estado adicional de procesamiento
                    if (contributionState is CreateContributionState.Loading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Iniciando pago con Open Payments...",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // WebView para autorización
    PaymentAuthorizationWebView(
        showDialog = showAuthorizationWebView,
        authorizationUrl = authorizationUrl,
        onDismiss = {
            showAuthorizationWebView = false
            contributionViewModel.cancelPaymentProcess()
        },
        onAuthorizationComplete = { interactRef ->
            showAuthorizationWebView = false
            paymentProcessData?.let { processData ->
                contributionViewModel.finalizeContribution(interactRef, processData)
            }
        },
        onAuthorizationError = { error ->
            showAuthorizationWebView = false
            contributionViewModel.cancelPaymentProcess()
        }
    )
}