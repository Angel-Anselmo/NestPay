package com.icescream.nestpay.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icescream.nestpay.ui.components.BottomNavItem
import com.icescream.nestpay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class PaymentCommunity(
    val id: String,
    val name: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val memberCount: Int,
    val dueDate: String,
    val color: Color,
    val icon: ImageVector,
    val status: CommunityStatus = CommunityStatus.ACTIVE,
    val createdBy: String,
    val walletAddress: String // Interledger wallet address for the community goal
)

enum class CommunityStatus {
    ACTIVE, COMPLETED, PAUSED
}

enum class CommunityFilter {
    MIS_CREADOS, PENDIENTES, PAGADOS, TARDE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToActivity: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onCreateCommunity: () -> Unit = {},
    viewModel: com.icescream.nestpay.ui.viewmodel.CommunityViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: com.icescream.nestpay.ui.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var selectedFilter by remember { mutableStateOf(CommunityFilter.MIS_CREADOS) }

    // Obtener información del usuario
    val userName = when (val currentAuthState = authState) {
        is com.icescream.nestpay.ui.viewmodel.AuthState.Success -> currentAuthState.userInfo.displayName
            ?: "Usuario25"

        else -> "Usuario25"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with greeting and help button - con padding top para status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NestPayPrimary)
                .statusBarsPadding() // Esto agrega el padding necesario para la status bar
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Solo el saludo con nombre, sin hora
                Text(
                    text = "Hola, $userName",
                    color = Color.White,
                    fontSize = 24.sp, // Aumentar el tamaño
                    fontWeight = FontWeight.Bold
                )

                // Help button with white circle and exclamation mark like in the image
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(50.dp) // Dar un ancho fijo para mejor control
                ) {
                    // Círculo blanco con signo de exclamación
                    Box(
                        modifier = Modifier
                            .size(32.dp) // Hacer el círculo un poco más pequeño
                            .background(Color.White, CircleShape)
                            .clickable { /* Handle help action */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            color = NestPayPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp)) // Espacio mínimo entre círculo y texto

                    // Texto "Ayuda" directamente debajo
                    Text(
                        text = "Ayuda",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Search section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchCommunities(it) },
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "¿Buscas una Wallet en específico?",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Category tabs with animated bubbles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedCategoryTab(
                text = "Mis Creados",
                isSelected = selectedFilter == CommunityFilter.MIS_CREADOS,
                onClick = { selectedFilter = CommunityFilter.MIS_CREADOS }
            )
            AnimatedCategoryTab(
                text = "Pendientes",
                isSelected = selectedFilter == CommunityFilter.PENDIENTES,
                onClick = { selectedFilter = CommunityFilter.PENDIENTES }
            )
            AnimatedCategoryTab(
                text = "Pagados",
                isSelected = selectedFilter == CommunityFilter.PAGADOS,
                onClick = { selectedFilter = CommunityFilter.PAGADOS }
            )
            AnimatedCategoryTab(
                text = "Tarde",
                isSelected = selectedFilter == CommunityFilter.TARDE,
                onClick = { selectedFilter = CommunityFilter.TARDE }
            )
        }

        // Communities section header (moved above divider)
        Text(
            text = "Tus Comunidades",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Divider
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Communities content based on state
        when (val currentState = uiState) {
            is com.icescream.nestpay.ui.viewmodel.CommunityUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = NestPayPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando comunidades...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is com.icescream.nestpay.ui.viewmodel.CommunityUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error al cargar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Red
                        )
                        Text(
                            text = currentState.message,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NestPayPrimary
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is com.icescream.nestpay.ui.viewmodel.CommunityUiState.Success -> {
                val filteredCommunities =
                    filterCommunities(currentState.communities, selectedFilter)

                if (filteredCommunities.isEmpty()) {
                    // Empty state - improved positioning and size
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(y = (-40).dp)
                        ) {
                            Text(
                                text = "Aún no eres anfitrión de ninguna\nComunidad.",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "¡Crea la tuya y empieza a organizar pagos en\nequipo!",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCommunities) { community ->
                            CommunityCard(community = community)
                        }
                    }
                }
            }
        }
    }

    // Bottom Navigation (as overlay)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))

                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    selected = true,
                    onClick = { }
                )
                BottomNavItem(
                    icon = Icons.Default.List,
                    label = "Actividad",
                    selected = false,
                    onClick = onNavigateToActivity
                )

                // FAB in center - circular shape
                FloatingActionButton(
                    onClick = onCreateCommunity,
                    containerColor = NestPayPrimary,
                    contentColor = Color.White,
                    shape = CircleShape // Asegurar que sea circular
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Crear Comunidad",
                        modifier = Modifier.size(28.dp) // Hacer el ícono un poco más grande también
                    )
                }

                BottomNavItem(
                    icon = Icons.Default.Notifications, // Mantener campana de notificación
                    label = "Notificación",
                    selected = false,
                    onClick = onNavigateToNotifications
                )
                BottomNavItem(
                    icon = Icons.Default.AccountCircle, // Ícono de perfil circular
                    label = "Perfil",
                    selected = false,
                    onClick = onNavigateToProfile
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun AnimatedCategoryTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) NestPayPrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "background_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

fun filterCommunities(
    communities: List<PaymentCommunity>,
    filter: CommunityFilter
): List<PaymentCommunity> {
    return when (filter) {
        CommunityFilter.MIS_CREADOS -> communities.filter { it.createdBy == "current_user" }
        CommunityFilter.PENDIENTES -> communities.filter { it.status == CommunityStatus.ACTIVE && it.currentAmount < it.targetAmount }
        CommunityFilter.PAGADOS -> communities.filter { it.status == CommunityStatus.COMPLETED }
        CommunityFilter.TARDE -> {
            // Lógica para comunidades tarde - por ahora retornamos lista vacía
            emptyList()
        }
    }
}

@Composable
fun CommunityCard(community: PaymentCommunity) {
    val progress = (community.currentAmount / community.targetAmount).coerceIn(0.0, 1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Community icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(community.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        community.icon,
                        contentDescription = null,
                        tint = community.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Community info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = community.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = community.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (community.status) {
                                CommunityStatus.COMPLETED -> Color(0xFF4CAF50)
                                CommunityStatus.PAUSED -> AccentOrange
                                CommunityStatus.ACTIVE -> NestPayPrimary
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Community stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${community.memberCount} miembros",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Meta: $${community.targetAmount.toInt()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "Vence: ${community.dueDate}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = community.color,
                    trackColor = community.color.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            // Current amount text
            Text(
                text = "Recaudado: $${community.currentAmount.toInt()}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}