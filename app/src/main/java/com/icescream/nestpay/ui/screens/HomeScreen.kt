package com.icescream.nestpay.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icescream.nestpay.ui.components.BottomNavItem
import com.icescream.nestpay.ui.theme.*
import com.icescream.nestpay.data.models.Community
import java.text.SimpleDateFormat
import java.util.*

// Data class for UI display of communities
data class CommunityDisplayData(
    val community: Community,
    val color: Color,
    val icon: ImageVector,
    val memberCount: Int,
    val formattedDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToActivity: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onCreateCommunity: () -> Unit = {},
    onJoinCommunity: () -> Unit = {},
    onCommunityClick: (String) -> Unit = {},
    viewModel: com.icescream.nestpay.ui.viewmodel.CommunityViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    authViewModel: com.icescream.nestpay.ui.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // State for showing the action sheet
    var showActionSheet by remember { mutableStateOf(false) }

    // Obtener información del usuario
    val userName = when (val currentAuthState = authState) {
        is com.icescream.nestpay.ui.viewmodel.AuthState.Success -> currentAuthState.userInfo.displayName
        else -> "Invitado"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with time and greeting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NestPayPrimary)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getCurrentTime(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Hola, $userName",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-12).dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
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
                                text = "¿Buscas una comunidad específica?",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Communities section header
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tus Comunidades",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Refresh button
                IconButton(
                    onClick = { viewModel.refresh() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = NestPayPrimary
                    )
                }
            }
        }

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentState.communities) { community ->
                        CommunityCard(
                            communityData = convertToDisplayData(community),
                            onCommunityClick = onCommunityClick
                        )
                    }

                    // Show empty state message if no communities
                    if (currentState.communities.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .padding(bottom = 16.dp),
                                        tint = Color.Gray.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = if (searchQuery.isNotEmpty())
                                            "No se encontraron comunidades"
                                        else
                                            "No tienes comunidades aún",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = if (searchQuery.isNotEmpty())
                                            "Intenta con otros términos de búsqueda"
                                        else
                                            "Crea tu primera comunidad de pagos\no únete a una existente",
                                        fontSize = 14.sp,
                                        color = Color.Gray.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
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
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // FAB in center
                FloatingActionButton(
                    onClick = { showActionSheet = true },
                    containerColor = NestPayPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Crear Comunidad",
                        modifier = Modifier.size(24.dp)
                    )
                }

                BottomNavItem(
                    icon = Icons.Default.Notifications,
                    label = "Notificación",
                    selected = false,
                    onClick = onNavigateToNotifications
                )
                BottomNavItem(
                    icon = Icons.Default.Person,
                    label = "Perfil",
                    selected = false,
                    onClick = onNavigateToProfile
                )
            }
        }
    }

    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "¿Qué quieres hacer?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Create Community Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showActionSheet = false
                            onCreateCommunity()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    NestPayPrimary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = NestPayPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Crear Comunidad",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Crea tu propia comunidad de pagos",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Join Community Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showActionSheet = false
                            onJoinCommunity()
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Unirse a Comunidad",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Únete usando un código de invitación",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CommunityCard(communityData: CommunityDisplayData, onCommunityClick: (String) -> Unit) {
    val community = communityData.community

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCommunityClick(community.id) },
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
                        .background(communityData.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        communityData.icon,
                        contentDescription = null,
                        tint = communityData.color,
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
                            if (community.isActive) NestPayPrimary else Color.Gray
                        )
                )

                // More options button
                IconButton(
                    onClick = { /* TODO: Show more options */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Community stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${communityData.memberCount} miembros",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Categoría: ${community.category}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "Creado: ${communityData.formattedDate}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Invite code display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Código: ${community.inviteCode}",
                    fontSize = 12.sp,
                    color = NestPayPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.Default.Share,
                    contentDescription = "Compartir código",
                    tint = NestPayPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Helper function to convert Community to display data
@Composable
private fun convertToDisplayData(community: Community): CommunityDisplayData {
    val color = getCommunityColor(community.category)
    val icon = getCommunityIcon(community.category)
    val memberCount = community.members.size
    val formattedDate = formatDate(community.createdAt)

    return CommunityDisplayData(
        community = community,
        color = color,
        icon = icon,
        memberCount = memberCount,
        formattedDate = formattedDate
    )
}

// Helper functions
private fun getCommunityColor(category: String): Color {
    return when (category.uppercase()) {
        "CASA" -> Color(0xFF4CAF50)
        "VIAJE" -> Color(0xFF2196F3)
        "TRABAJO" -> Color(0xFFFF9800)
        "AMIGOS" -> Color(0xFFE91E63)
        "FAMILIA" -> Color(0xFF9C27B0)
        else -> NestPayPrimary
    }
}

private fun getCommunityIcon(category: String): ImageVector {
    return when (category.uppercase()) {
        "CASA" -> Icons.Default.Home
        "VIAJE" -> Icons.Default.Place
        "TRABAJO" -> Icons.Default.Settings
        "AMIGOS" -> Icons.Default.Person
        "FAMILIA" -> Icons.Default.Favorite
        else -> Icons.Default.AccountCircle
    }
}

private fun formatDate(date: Date?): String {
    return if (date != null) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    } else {
        "Fecha no disponible"
    }
}

@Composable
private fun getCurrentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}