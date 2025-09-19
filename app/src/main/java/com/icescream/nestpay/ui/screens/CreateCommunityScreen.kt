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
    val color: Color
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
    var paymentPointer by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<CommunityTemplate?>(null) }

    val createCommunityState by viewModel.createCommunityState.collectAsState()

    // Observar el estado de creación
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

    val communityTemplates = listOf(
        CommunityTemplate(
            name = "Roommates",
            description = "Gastos compartidos del hogar",
            icon = Icons.Default.Home,
            color = AccentBlue
        ),
        CommunityTemplate(
            name = "Viaje Grupal",
            description = "Gastos de viaje con amigos",
            icon = Icons.Default.Place,
            color = AccentPurple
        ),
        CommunityTemplate(
            name = "Oficina",
            description = "Gastos de oficina compartidos",
            icon = Icons.Default.Build,
            color = AccentGreen
        ),
        CommunityTemplate(
            name = "Evento Social",
            description = "Organización de eventos",
            icon = Icons.Default.Star,
            color = AccentYellow
        ),
        CommunityTemplate(
            name = "Familia",
            description = "Gastos familiares",
            icon = Icons.Default.Favorite,
            color = AccentOrange
        ),
        CommunityTemplate(
            name = "Personalizado",
            description = "Crea tu propia comunidad",
            icon = Icons.Default.Add,
            color = NestPayPrimary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFD770))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Centered title and subtitle
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crear Comunidad",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Crea una nueva comunidad para gestionar pagos colaborativos",
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // X button in top right corner
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Información Básica Section
                Text(
                    text = "Información de la Comunidad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                // Nombre de la Comunidad
                Column {
                    Text(
                        text = "Nombre de la Comunidad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = communityName,
                        onValueChange = { communityName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                        Text(
                                "Ej: Roommates Casa 123",
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            item {
                // Descripción (opcional)
                Column {
                    Text(
                        text = "Descripción (opcional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = communityDescription,
                        onValueChange = { communityDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Describe el propósito de la comunidad",
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2
                    )
                }
            }

            item {
                // Payment Pointer
                Column {
                    Text(
                        text = "Payment Pointer (requerido)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = paymentPointer,
                        onValueChange = { paymentPointer = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "https://ilp.interledger-test.dev/tu-wallet",
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NestPayPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Todos los pagos de conceptos en esta comunidad irán a este payment pointer",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            item {
                // Tipo de Comunidad
                Column {
                    Text(
                        text = "Tipo de Comunidad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTemplate?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = {
                            Text(
                                "Selecciona el tipo de comunidad",
                                    color = Color.Gray
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NestPayPrimary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            communityTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                template.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = template.color
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(template.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    template.description,
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTemplate = template
                                        if (template.name != "Personalizado") {
                                            // Auto-fill description for non-custom templates
                                            if (communityDescription.isBlank()) {
                                                communityDescription = template.description
                                            }
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "Comunidades de Pago",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NestPayPrimary
                            )
                            Text(
                                text = "Después podrás agregar conceptos específicos de pago dentro de la comunidad",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Crear Comunidad Button
                Button(
                    onClick = {
                        viewModel.createCommunity(
                            name = communityName,
                            description = communityDescription,
                            paymentPointer = paymentPointer,
                            category = selectedTemplate?.name?.uppercase()?.replace(" ", "_")
                                ?: "CUSTOM"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C3E50)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    enabled = communityName.isNotBlank() &&
                            paymentPointer.isNotBlank() &&
                            selectedTemplate != null &&
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

                // Show error if any
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

                Spacer(modifier = Modifier.height(100.dp)) // Space for navigation
            }
        }
    }
}