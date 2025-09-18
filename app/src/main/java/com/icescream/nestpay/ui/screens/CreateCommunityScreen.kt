package com.icescream.nestpay.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icescream.nestpay.ui.theme.*
import com.icescream.nestpay.ui.viewmodel.CommunityViewModel
import com.icescream.nestpay.ui.viewmodel.CreateCommunityState

data class CommunityTemplate(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val suggestedAmount: Double? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommunityScreen(
    onNavigateBack: () -> Unit = {},
    onCommunityCreated: () -> Unit = {},
    viewModel: CommunityViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var communityName by remember { mutableStateOf("") }
    var communityDescription by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<CommunityTemplate?>(null) }
    var walletAddress by remember { mutableStateOf("") }

    val communityTemplates = listOf(
        CommunityTemplate(
            name = "Viaje Grupal",
            description = "Organiza pagos para viajes con amigos",
            icon = Icons.Default.Place,
            color = AccentPurple,
            suggestedAmount = 500.0
        ),
        CommunityTemplate(
            name = "Regalo Colectivo",
            description = "Recolecta dinero para un regalo especial",
            icon = Icons.Default.Favorite,
            color = AccentYellow,
            suggestedAmount = 100.0
        ),
        CommunityTemplate(
            name = "Proyecto Educativo",
            description = "Financia materiales educativos",
            icon = Icons.Default.AccountBox,
            color = AccentBlue,
            suggestedAmount = 200.0
        ),
        CommunityTemplate(
            name = "Evento Social",
            description = "Organiza eventos y celebraciones",
            icon = Icons.Default.Star,
            color = AccentGreen,
            suggestedAmount = 300.0
        ),
        CommunityTemplate(
            name = "Personalizado",
            description = "Crea tu propia comunidad",
            icon = Icons.Default.Add,
            color = NestPayPrimary
        )
    )

    val createCommunityState by viewModel.createCommunityState.collectAsState()

    LaunchedEffect(createCommunityState) {
        when (createCommunityState) {
            is CreateCommunityState.Success -> {
                onCommunityCreated()
                viewModel.resetCreateCommunityState()
            }

            else -> { /* No action needed */
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        item {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NestPayPrimary)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Crear Comunidad",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Templates section
            Text(
                text = "Elige un tipo de comunidad",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(communityTemplates) { template ->
                    TemplateCard(
                        template = template,
                        isSelected = selectedTemplate == template,
                        onClick = {
                            selectedTemplate = template
                            if (template.name != "Personalizado") {
                                communityName = template.name
                                communityDescription = template.description
                                targetAmount = template.suggestedAmount?.toString() ?: ""
                            }
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))

            // Form section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detalles de la comunidad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Community Name
                    OutlinedTextField(
                        value = communityName,
                        onValueChange = { communityName = it },
                        label = { Text("Nombre de la comunidad") },
                        placeholder = { Text("Ej: Viaje a Cancún 2024") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = communityDescription,
                        onValueChange = { communityDescription = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("¿Para qué es esta comunidad?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    // Target Amount
                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = { targetAmount = it },
                        label = { Text("Meta de recaudación ($)") },
                        placeholder = { Text("500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    // Due Date
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Fecha límite") },
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    // Wallet Address for receiving payments
                    OutlinedTextField(
                        value = walletAddress,
                        onValueChange = { walletAddress = it },
                        label = { Text("Dirección de wallet destino") },
                        placeholder = { Text("https://wallet.interledger-test.dev/alice") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            focusedLabelColor = NestPayPrimary
                        )
                    )

                    // Info card about Open Payments
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NestPayPrimary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = NestPayPrimary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Pagos vía Interledger",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NestPayPrimary
                                )
                                Text(
                                    text = "Los pagos se procesarán usando Open Payments API",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.createCommunity(
                                name = communityName,
                                description = communityDescription,
                                targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                walletAddress = walletAddress,
                                dueDate = dueDate,
                                category = selectedTemplate?.name?.uppercase()?.replace(" ", "_")
                                    ?: "CUSTOM"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NestPayPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = communityName.isNotBlank() &&
                                targetAmount.isNotBlank() &&
                                walletAddress.isNotBlank() &&
                                createCommunityState !is CreateCommunityState.Loading
                    ) {
                        when (createCommunityState) {
                            is CreateCommunityState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creando...")
                            }

                            else -> {
                                Text(
                                    text = "Crear Comunidad",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    val currentCreateState = createCommunityState
                    if (currentCreateState is CreateCommunityState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentCreateState.message,
                            color = Color.Red,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TemplateCard(
    template: CommunityTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) template.color.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(template.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    template.icon,
                    contentDescription = null,
                    tint = template.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}