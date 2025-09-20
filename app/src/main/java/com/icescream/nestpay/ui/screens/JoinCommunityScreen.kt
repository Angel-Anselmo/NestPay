package com.icescream.nestpay.ui.screens

import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icescream.nestpay.ui.theme.NestPayPrimary
import com.icescream.nestpay.ui.viewmodel.CommunityViewModel
import com.icescream.nestpay.ui.viewmodel.JoinCommunityState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCommunityScreen(
    onNavigateBack: () -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    var inviteCode by remember { mutableStateOf("") }
    var userPaymentPointer by remember { mutableStateOf("") }
    val joinState by viewModel.joinCommunityState.collectAsState()

    // Handle join success
    LaunchedEffect(joinState) {
        if (joinState is JoinCommunityState.Success) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NestPayPrimary),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Top bar with back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }

                    Text(
                        text = "Unirse a Comunidad",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ingresa el código de invitación y configura tu payment pointer",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Invite code input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Código de Invitación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: ABC123") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Pointer input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Tu Payment Pointer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Desde aquí se enviarán tus contribuciones",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = userPaymentPointer,
                        onValueChange = { userPaymentPointer = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("\$ilp.interledger-test.dev/tu-wallet") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.Blue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Se validará que tu payment pointer funcione",
                            fontSize = 11.sp,
                            color = Color.Blue
                        )
                    }

                    if (joinState is JoinCommunityState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (joinState as JoinCommunityState.Error).message,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Join button
            Button(
                onClick = {
                    if (inviteCode.isNotBlank() && userPaymentPointer.isNotBlank()) {
                        viewModel.joinCommunity(inviteCode.trim(), userPaymentPointer.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = inviteCode.isNotBlank() &&
                        userPaymentPointer.isNotBlank() &&
                        joinState !is JoinCommunityState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NestPayPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                when (joinState) {
                    is JoinCommunityState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validando y uniéndose...")
                    }

                    else -> {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unirse a Comunidad",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Help section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = NestPayPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "¿Cómo funciona?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Pide el código de invitación al administrador de la comunidad\n" +
                                "• Configura tu payment pointer de Interledger Test Wallet\n" +
                                "• Una vez configurado, podrás contribuir fácilmente a cualquier concepto\n" +
                                "• Tus pagos se procesarán automáticamente con Open Payments",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Obtener wallet en wallet.interledger-test.dev",
                            fontSize = 12.sp,
                            color = NestPayPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}