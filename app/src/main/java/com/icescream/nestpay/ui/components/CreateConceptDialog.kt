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
import com.icescream.nestpay.ui.theme.NestPayPrimary
import com.icescream.nestpay.ui.viewmodel.PaymentConceptViewModel
import com.icescream.nestpay.ui.viewmodel.CreateConceptState

@Composable
fun CreateConceptDialog(
    showDialog: Boolean,
    communityId: String,
    onDismiss: () -> Unit,
    onConceptCreated: () -> Unit,
    viewModel: PaymentConceptViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    val createState by viewModel.createConceptState.collectAsState()

    // Handle create success
    LaunchedEffect(createState) {
        if (createState is CreateConceptState.Success) {
            onConceptCreated()
            onDismiss()
            viewModel.resetCreateConceptState()
        }
    }

    // Reset form when dialog is dismissed
    LaunchedEffect(showDialog) {
        if (!showDialog) {
            name = ""
            description = ""
            targetAmount = ""
            dueDate = ""
            viewModel.resetCreateConceptState()
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
                            text = "Crear Concepto de Pago",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Define un nuevo concepto para que los miembros puedan contribuir",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form Fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del concepto") },
                        placeholder = { Text("Ej: Luz - Febrero 2024") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Detalles adicionales...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = {
                            // Only allow numbers and decimal point
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                targetAmount = it
                            }
                        },
                        label = { Text("Monto objetivo") },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Fecha límite") },
                        placeholder = { Text("Ej: 2024-03-15 o Marzo 15") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Detalles adicionales...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Payment Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = NestPayPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Los pagos se enviarán al payment pointer de la comunidad",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show error if any
                    if (createState is CreateConceptState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (createState as? CreateConceptState.Error)?.message ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }

                        // Create Button
                        Button(
                            onClick = {
                                val amount = targetAmount.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && amount > 0) {
                                    viewModel.createPaymentConcept(
                                        communityId = communityId,
                                        name = name.trim(),
                                        description = description.trim(),
                                        targetAmount = amount,
                                        dueDate = dueDate.trim().ifEmpty { "Sin fecha límite" }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NestPayPrimary
                            ),
                            enabled = name.isNotBlank() &&
                                    targetAmount.isNotBlank() &&
                                    targetAmount.toDoubleOrNull() != null &&
                                    targetAmount.toDoubleOrNull()!! > 0 &&
                                    createState !is CreateConceptState.Loading
                        ) {
                            if (createState is CreateConceptState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creando...")
                            } else {
                                Text("Crear")
                            }
                        }
                    }
                }
            }
        }
    }
}