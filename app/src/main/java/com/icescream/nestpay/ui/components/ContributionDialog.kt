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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icescream.nestpay.data.models.PaymentConcept
import com.icescream.nestpay.data.models.Community
import com.icescream.nestpay.ui.theme.NestPayPrimary
import com.icescream.nestpay.ui.viewmodel.UserProfileViewModel

@Composable
fun ContributionDialog(
    showDialog: Boolean,
    concept: PaymentConcept,
    community: Community,
    onDismiss: () -> Unit,
    onContributionSubmitted: (Double) -> Unit,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    var amount by remember { mutableStateOf("") }
    var showWalletDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val userProfile by userProfileViewModel.userProfile.collectAsState()

    LaunchedEffect(showDialog) {
        if (showDialog) {
            // Cargar perfil del usuario al abrir el dialog
            userProfileViewModel.loadUserProfile()
        }
        if (!showDialog) {
            amount = ""
            isLoading = false
        }
    }

    // User Wallet Config Dialog
    UserWalletConfigDialog(
        showDialog = showWalletDialog,
        onDismiss = { showWalletDialog = false },
        onWalletSaved = { walletAddress ->
            userProfileViewModel.updateMainWalletAddress(walletAddress)
            showWalletDialog = false
        }
    )

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
                            Icons.Filled.Send,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Concept Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = concept.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = community.name,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Progress
                            val progress =
                                (concept.currentAmount / concept.targetAmount).coerceIn(0.0, 1.0)
                            LinearProgressIndicator(
                                progress = progress.toFloat(),
                                modifier = Modifier.fillMaxWidth(),
                                color = NestPayPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$${concept.currentAmount}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$${concept.targetAmount}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Destination Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = NestPayPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Destino del pago:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = community.paymentPointer,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amount = it
                            }
                        },
                        label = { Text("Monto a contribuir") },
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
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }

                        // Contribute Button
                        Button(
                            onClick = {
                                val contributionAmount = amount.toDoubleOrNull() ?: 0.0
                                if (contributionAmount > 0) {
                                    // Verificar si el usuario tiene wallet configurada
                                    if (userProfile?.mainWalletAddress.isNullOrBlank()) {
                                        showWalletDialog = true
                                    } else {
                                        isLoading = true
                                        onContributionSubmitted(contributionAmount)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NestPayPrimary
                            ),
                            enabled = amount.isNotBlank() &&
                                    amount.toDoubleOrNull() != null &&
                                    amount.toDoubleOrNull()!! > 0 &&
                                    !isLoading
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
                                Text("Contribuir")
                            }
                        }
                    }

                    // Wallet status info
                    if (userProfile?.mainWalletAddress.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Se te pedirá configurar tu wallet al contribuir",
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
}