package com.icescream.nestpay.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icescream.nestpay.data.repository.CommunityPaymentRepository
import com.icescream.nestpay.network.ApiResult
import com.icescream.nestpay.ui.viewmodel.ContributionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTestScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ContributionViewModel = viewModel()
) {
    var testResult by remember { mutableStateOf("Sin pruebas ejecutadas") }
    var isLoading by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val repository = remember { CommunityPaymentRepository() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                Text(
                        "🧪 Test Backend",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        // Info del servidor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "🏠 Backend Local",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("URL: http://192.168.0.11:3000", fontSize = 12.sp)
                    Text("Dispositivo: Físico (IP local)", fontSize = 12.sp)
                    Text("Wallets: walltest ↔ testwall", fontSize = 12.sp)
                }
            }

            // Botones de prueba
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        testResult = "🔌 Probando conexión..."

                        scope.launch {
                            when (val result = repository.testConnection()) {
                                is ApiResult.Success -> {
                                    testResult = "✅ Conexión exitosa\n${result.data.message}"
                                    isConnected = true
                                }

                                is ApiResult.Error -> {
                                    testResult = "❌ Error de conexión\n${result.message}"
                                    isConnected = false
                                }

                                is ApiResult.Loading -> {
                                    testResult = "⏳ Conectando..."
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("🔌 Conectar", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        isLoading = true
                        testResult = "📊 Obteniendo info del sistema..."

                        scope.launch {
                            when (val result = repository.getSystemInfo()) {
                                is ApiResult.Success -> {
                                    val system = result.data.data
                                    testResult = "✅ Sistema funcionando\n" +
                                            "Sistema: ${system.system}\n" +
                                            "Admin: ${system.wallets["adminWallet"]?.address}\n" +
                                            "User: ${system.wallets["userWallet"]?.address}"
                                }

                                is ApiResult.Error -> {
                                    testResult = "❌ Error system info\n${result.message}"
                                }

                                is ApiResult.Loading -> {
                                    testResult = "⏳ Obteniendo info..."
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("📊 Info", fontSize = 12.sp)
                }
            }

            // Botón de pago de prueba
            Button(
                onClick = {
                    isLoading = true
                    testResult = "💰 Iniciando pago de prueba..."

                    scope.launch {
                        when (val result = repository.initiateCommunityPayment(
                            amount = "100",
                            communityId = "test-community",
                            conceptId = "test-concept",
                            description = "Pago de prueba desde Android"
                        )) {
                            is ApiResult.Success -> {
                                val payment = result.data.data
                                testResult = "✅ Pago iniciado exitosamente\n" +
                                        "ID: ${payment.incomingPaymentId}\n" +
                                        "Quote: ${payment.quoteId}\n" +
                                        "Admin: ${payment.adminWallet}\n" +
                                        "User: ${payment.userWallet}"
                            }

                            is ApiResult.Error -> {
                                testResult = "❌ Error iniciando pago\n${result.message}"
                            }

                            is ApiResult.Loading -> {
                                testResult = "⏳ Procesando pago..."
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("💰 Test Pago", fontSize = 14.sp)
            }

            // Resultado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (testResult.contains("❌"))
                        MaterialTheme.colorScheme.errorContainer
                    else
                    MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📋 Resultado:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        testResult,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Estado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isConnected -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Estado:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )

                    Text(
                        when {
                            isLoading -> "⏳ Probando..."
                            isConnected -> "✅ Conectado"
                            else -> "⚪ Sin probar"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Instrucciones
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📖 Instrucciones:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        """
                        1. Asegurate de tener el backend corriendo:
                        cd nestpay-backend && npm run dev
                        
                        2. Presiona "🔌 Conectar" para probar conexión
                        
                        3. Presiona "💰 Test Pago" para probar pagos
                        
                        4. Revisa los logs en Logcat para detalles
                        """.trimIndent(),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}