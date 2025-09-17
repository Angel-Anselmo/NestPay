package com.icescream.nestpay.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icescream.nestpay.ui.components.BottomNavItem
import com.icescream.nestpay.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class Payment(
    val id: String,
    val walletName: String,
    val amount: Double,
    val section: String,
    val date: String,
    val icon: ImageVector,
    val color: Color
)

data class PaymentSection(
    val title: String,
    val payments: List<Payment>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Sample payment data
    val samplePayments = listOf(
        PaymentSection(
            title = "Esta Semana",
            payments = listOf(
                Payment(
                    id = "1",
                    walletName = "Universidad Tecnológica El Retoño",
                    amount = 100.0,
                    section = "Sección: Multa",
                    date = "Septiembre 14, 2025",
                    icon = Icons.Default.AccountBox,
                    color = AccentYellow
                ),
                Payment(
                    id = "2",
                    walletName = "Regalo de Cumpleaños Nathe",
                    amount = 25.0,
                    section = "Sección: Personal",
                    date = "Septiembre 10, 2025",
                    icon = Icons.Default.Favorite,
                    color = AccentBlue
                )
            )
        ),
        PaymentSection(
            title = "Este Mes",
            payments = listOf(
                Payment(
                    id = "3",
                    walletName = "Universidad Tecnológica El Retoño",
                    amount = 150.0,
                    section = "Sección: Examen Extra",
                    date = "Septiembre 05, 2025",
                    icon = Icons.Default.AccountBox,
                    color = AccentYellow
                ),
                Payment(
                    id = "4",
                    walletName = "Viaje a Cancún con Amigos",
                    amount = 200.0,
                    section = "Sección: Hospedaje",
                    date = "Septiembre 05, 2025",
                    icon = Icons.Default.Place,
                    color = AccentPurple
                ),
                Payment(
                    id = "5",
                    walletName = "Material de IoT",
                    amount = 50.0,
                    section = "Sección: Componentes",
                    date = "Agosto 28, 2025",
                    icon = Icons.Default.Build,
                    color = AccentOrange
                )
            )
        )
    )

    val totalPaid = samplePayments.flatMap { it.payments }.sumOf { it.amount }
    val totalPending = 430.0 // Sample pending amount
    val nextDueDate = "10/05/2025"
    val nextWallet = "Material de IoT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with activity info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NestPayPrimary)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                // Title
                Text(
                    text = "Mi Actividad",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Stats
                Text(
                    text = "Total pagado este mes: $${totalPaid.toInt()}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Total pendiente: $${totalPending.toInt()}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Próxima fecha límite: $nextDueDate ($nextWallet)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // More Details Card
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Más Detalles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                // Progress indicator
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Payments list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            samplePayments.forEach { section ->
                item {
                    // Section header
                    Text(
                        text = section.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(section.payments) { payment ->
                    PaymentCard(payment = payment)
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
                    selected = false,
                    onClick = onNavigateBack
                )
                BottomNavItem(
                    icon = Icons.Default.List,
                    label = "Actividad",
                    selected = true,
                    onClick = { }
                )

                // FAB in center
                FloatingActionButton(
                    onClick = { /* TODO: Add wallet */ },
                    containerColor = NestPayPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar",
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
}

@Composable
fun PaymentCard(payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Payment icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(payment.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    payment.icon,
                    contentDescription = null,
                    tint = payment.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Payment info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = payment.walletName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = payment.section,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(
                    text = payment.date,
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            // Amount
            Text(
                text = "$${payment.amount.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}