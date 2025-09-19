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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icescream.nestpay.ui.components.BottomNavItem
import com.icescream.nestpay.ui.theme.*

data class NotificationItem(
    val id: String,
    val type: NotificationType, // Agregar campo type
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
    val isRead: Boolean = false
)

enum class NotificationFilter {
    PRINCIPAL,
    TARDE,
    FECHAS,
    METAS,
    APORTES
}

enum class NotificationType {
    TARDE,
    FECHAS,
    METAS,
    APORTES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(NotificationFilter.PRINCIPAL) }

    // Color azul como en la imagen
    val notificationBlue = Color(0xFF374B82) // Azul más similar al de la imagen

    val notifications = listOf(
        NotificationItem(
            id = "1",
            type = NotificationType.TARDE,
            title = "Tarde",
            description = "¡Nuestro TU pago de $170 en la comunidad 'Materiales de IoT' está atrasado.",
            time = "Septiembre 12, 2025",
            icon = Icons.Default.Warning,
            color = AccentOrange
        ),
        NotificationItem(
            id = "2",
            type = NotificationType.FECHAS,
            title = "Fechas",
            description = "Quedan 5 días para completar tu contribución en la\ncomunidad 'Viaje a Cancún con Amigos'.",
            time = "Septiembre 10, 2025",
            icon = Icons.Default.DateRange,
            color = AccentPurple
        ),
        NotificationItem(
            id = "3",
            type = NotificationType.METAS,
            title = "Metas",
            description = "La comunidad 'Regalo de Cumpleaños Nathis' alcanzo su meta de $250 y todos completan sus aportes hoy.",
            time = "Septiembre 05, 2025",
            icon = Icons.Default.CheckCircle,
            color = AccentBlue
        ),
        NotificationItem(
            id = "4",
            type = NotificationType.APORTES,
            title = "Aportes",
            description = "Tu aporte de $100 en la comunidad 'Universidad Tecnológica'",
            time = "Septiembre 01, 2025",
            icon = Icons.Default.Warning,
            color = AccentOrange
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header con padding para status bar - usando azul de la imagen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(notificationBlue)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    text = "Notificaciones",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Mantente informado. Notificaciones de transferencias,\npagos pendientes y fechas límite.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Filter section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Filtrar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtrar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter tabs - usar azul de la imagen para el filtro seleccionado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterTab(
                        text = "Principal",
                        isSelected = selectedFilter == NotificationFilter.PRINCIPAL,
                        onClick = { selectedFilter = NotificationFilter.PRINCIPAL },
                        selectedColor = notificationBlue
                    )
                    FilterTab(
                        text = "Tarde",
                        isSelected = selectedFilter == NotificationFilter.TARDE,
                        onClick = { selectedFilter = NotificationFilter.TARDE },
                        selectedColor = notificationBlue
                    )
                    FilterTab(
                        text = "Fechas",
                        isSelected = selectedFilter == NotificationFilter.FECHAS,
                        onClick = { selectedFilter = NotificationFilter.FECHAS },
                        selectedColor = notificationBlue
                    )
                    FilterTab(
                        text = "Metas",
                        isSelected = selectedFilter == NotificationFilter.METAS,
                        onClick = { selectedFilter = NotificationFilter.METAS },
                        selectedColor = notificationBlue
                    )
                    FilterTab(
                        text = "Aportes",
                        isSelected = selectedFilter == NotificationFilter.APORTES,
                        onClick = { selectedFilter = NotificationFilter.APORTES },
                        selectedColor = notificationBlue
                    )
                }
            }
        }

        // Notifications list
        val filteredNotifications = filterNotifications(notifications, selectedFilter)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredNotifications) { notification ->
                NotificationCard(notification = notification)
            }
        }
    }

    // Bottom Navigation
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
                    selected = false,
                    onClick = onNavigateToHome
                )
                BottomNavItem(
                    icon = Icons.Default.List,
                    label = "Actividad",
                    selected = false,
                    onClick = onNavigateToActivity
                )

                // FAB in center
                FloatingActionButton(
                    onClick = { /* TODO: Add wallet */ },
                    containerColor = notificationBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar",
                        modifier = Modifier.size(28.dp)
                    )
                }

                BottomNavItem(
                    icon = Icons.Default.Notifications,
                    label = "Notificación",
                    selected = true,
                    onClick = { }
                )
                BottomNavItem(
                    icon = Icons.Default.AccountCircle,
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
fun FilterTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = NestPayPrimary // Parámetro de color personalizable
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "filter_background"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "filter_text"
    )

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(notification.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    notification.icon,
                    contentDescription = null,
                    tint = notification.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NestPayPrimary)
                        )
                    }
                }

                Text(
                    text = notification.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

fun filterNotifications(
    notifications: List<NotificationItem>,
    filter: NotificationFilter
): List<NotificationItem> {
    return when (filter) {
        NotificationFilter.PRINCIPAL -> notifications
        NotificationFilter.TARDE -> notifications.filter { it.type == NotificationType.TARDE }
        NotificationFilter.FECHAS -> notifications.filter { it.type == NotificationType.FECHAS }
        NotificationFilter.METAS -> notifications.filter { it.type == NotificationType.METAS }
        NotificationFilter.APORTES -> notifications.filter { it.type == NotificationType.APORTES }
    }
}
