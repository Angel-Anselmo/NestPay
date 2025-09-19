package com.icescream.nestpay.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icescream.nestpay.data.models.Community
import com.icescream.nestpay.data.models.PaymentConcept
import com.icescream.nestpay.ui.components.CreateConceptDialog
import com.icescream.nestpay.ui.components.ConceptProgressBar
import com.icescream.nestpay.ui.components.ContributeDialog
import com.icescream.nestpay.ui.theme.NestPayPrimary
import com.icescream.nestpay.ui.viewmodel.CommunityViewModel
import com.icescream.nestpay.ui.viewmodel.PaymentConceptUiState
import com.icescream.nestpay.ui.viewmodel.PaymentConceptViewModel
import com.icescream.nestpay.ui.viewmodel.ContributionViewModel
import com.icescream.nestpay.ui.viewmodel.CreateContributionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailsScreen(
    onNavigateBack: () -> Unit,
    communityId: String?,
    viewModel: CommunityViewModel,
    paymentConceptViewModel: PaymentConceptViewModel = viewModel(),
    contributionViewModel: ContributionViewModel = viewModel()
) {
    // Ensure communities are loaded
    LaunchedEffect(Unit) {
        // If communities are not loaded, load them
        if (communityId != null && viewModel.getCommunityById(communityId) == null) {
            viewModel.loadCommunities()
        }
    }

    // Get the community data
    val community = communityId?.let { viewModel.getCommunityById(it) }

    // Get payment concepts state
    val paymentConceptUiState by paymentConceptViewModel.uiState.collectAsState()

    // Dialog state for create concept
    var showCreateConceptDialog by remember { mutableStateOf(false) }
    // State for ContributeDialog
    var showContributeDialog by remember { mutableStateOf(false) }
    var selectedConceptToContribute by remember { mutableStateOf<PaymentConcept?>(null) }

    // Load concepts for this community
    LaunchedEffect(communityId) {
        if (communityId != null) {
            paymentConceptViewModel.loadPaymentConcepts(communityId)
        }
    }

    if (community == null) {
        // Show loading state first, then error if still not found
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = NestPayPrimary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cargando comunidad...",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Volver")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        CommunityDetailsHeader(
            community = community,
            onNavigateBack = onNavigateBack
        )

        // Create Concept Dialog 
        if (showCreateConceptDialog) {
            CreateConceptDialog(
                showDialog = showCreateConceptDialog,
                communityId = community.id,
                onDismiss = { showCreateConceptDialog = false },
                onConceptCreated = { 
                    showCreateConceptDialog = false
                    // Refresh concepts will happen automatically via ViewModel
                },
                viewModel = paymentConceptViewModel
            )
        }

        // ContributeDialog
        if (showContributeDialog && selectedConceptToContribute != null) {
            val contributionState by contributionViewModel.createContributionState.collectAsState()

            ContributeDialog(
                showDialog = showContributeDialog,
                concept = selectedConceptToContribute!!,
                userName = "Usuario", // TODO: Get actual user name
                onDismiss = {
                    showContributeDialog = false
                    selectedConceptToContribute = null
                    contributionViewModel.resetCreateContributionState()
                },
                onContribute = { amount ->
                    println("🔍 DEBUG: Starting contribution of $amount")
                    contributionViewModel.createContribution(
                        conceptId = selectedConceptToContribute!!.id,
                        communityId = community.id,
                        amount = amount,
                        userName = "Usuario" // TODO: Get actual user name
                    )
                },
                isLoading = contributionState is CreateContributionState.Loading
            )

            // Handle contribution success
            LaunchedEffect(contributionState) {
                when (contributionState) {
                    is CreateContributionState.Success -> {
                        println("✅ UI: Contribution successful, closing dialog")
                        showContributeDialog = false
                        selectedConceptToContribute = null
                        contributionViewModel.resetCreateContributionState()
                        // Reload concepts to show updated amounts
                        paymentConceptViewModel.loadPaymentConcepts(community.id)
                    }

                    is CreateContributionState.Error -> {
                        val errorMessage =
                            (contributionState as? CreateContributionState.Error)?.message
                                ?: "Error desconocido"
                        println("❌ UI: Contribution error: $errorMessage")
                        // Error will be shown in the dialog itself
                    }

                    else -> {
                        // Loading or Idle - do nothing
                    }
                }
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Community Stats Card
            item {
                CommunityStatsCard(
                    community = community,
                    paymentConceptUiState = paymentConceptUiState
                )
            }

            // Members Section
            item {
                MembersSection(community = community)
            }

            // Payment Concepts Section
            item {
                val concepts = when (val state = paymentConceptUiState) {
                    is PaymentConceptUiState.Success -> state.concepts
                    else -> emptyList()
                }

                PaymentConceptsSection(
                    community = community,
                    concepts = concepts,
                    isAdmin = viewModel.isCurrentUserAdmin(community.id),
                    onCreateConcept = {
                        // Fix dialog integration
                        showCreateConceptDialog = true
                    },
                    onContributeClick = { concept ->
                        selectedConceptToContribute = concept
                        showContributeDialog = true
                    }
                )
            }

            // Actions Section
            item {
                ActionsSection(community = community)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityDetailsHeader(
    community: Community,
    onNavigateBack: () -> Unit
) {
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
                    text = community.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { /* TODO: More options */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Community description
            if (community.description.isNotEmpty()) {
                Text(
                    text = community.description,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Invite code
            Row(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Código: ${community.inviteCode}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { /* TODO: Copy to clipboard */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Copiar código",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityStatsCard(
    community: Community,
    paymentConceptUiState: PaymentConceptUiState
) {
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
                text = "Estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            val conceptsCount = when (paymentConceptUiState) {
                is PaymentConceptUiState.Success -> paymentConceptUiState.concepts.size
                else -> 0
            }

            val totalAmount = when (paymentConceptUiState) {
                is PaymentConceptUiState.Success -> {
                    val total = paymentConceptUiState.concepts.sumOf { it.targetAmount }
                    "$${String.format("%.2f", total)}"
                }

                else -> "$0.00"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = "Miembros",
                    value = "${community.members.size}",
                    icon = Icons.Filled.Person
                )
                StatItem(
                    title = "Conceptos",
                    value = conceptsCount.toString(),
                    icon = Icons.Filled.Info
                )
                StatItem(
                    title = "Total",
                    value = totalAmount,
                    icon = Icons.Filled.Info
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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
                icon,
                contentDescription = null,
                tint = NestPayPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun MembersSection(community: Community) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Miembros (${community.members.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                IconButton(
                    onClick = { /* TODO: Add member */ }
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Agregar miembro",
                        tint = NestPayPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Show members (for now just show UIDs, later we'll get user info)
            community.members.forEachIndexed { index, memberId ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Member avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                NestPayPrimary.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = NestPayPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Miembro ${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = if (memberId == community.createdBy) "Administrador" else "Miembro",
                            fontSize = 12.sp,
                            color = if (memberId == community.createdBy) NestPayPrimary else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (memberId == community.createdBy) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Administrador",
                            tint = NestPayPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentConceptsSection(
    community: Community,
    concepts: List<com.icescream.nestpay.data.models.PaymentConcept>,
    isAdmin: Boolean,
    onCreateConcept: () -> Unit,
    onContributeClick: (PaymentConcept) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conceptos de Pago",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (isAdmin) {
                    Button(
                        onClick = onCreateConcept,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NestPayPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (concepts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay conceptos de pago",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Crea el primer concepto para empezar",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    concepts.forEachIndexed { index, concept ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Concept indicator
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            NestPayPrimary.copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = NestPayPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = concept.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${"%.2f".format(concept.targetAmount)} MXN",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                // Optionally, delete concept if admin (could add Button here)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Progress bar
                            ConceptProgressBar(
                                currentAmount = concept.currentAmount,
                                targetAmount = concept.targetAmount,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Basic info about the concept
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado: ${concept.status}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = concept.dueDate,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Button(
                                    onClick = { onContributeClick(concept) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NestPayPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Contribuir",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Contribuir", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsSection(community: Community) {
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
                text = "Acciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Share community button
            OutlinedButton(
                onClick = { /* TODO: Share community */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        NestPayPrimary
                    ).brush
                )
            ) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = null,
                    tint = NestPayPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compartir Comunidad",
                    color = NestPayPrimary
                )
            }
        }
    }
}