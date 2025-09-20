package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.models.Community
import com.icescream.nestpay.data.repository.FirebaseCommunityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.collections.find

// ==========================================
// UI STATES
// ==========================================

sealed class CommunityUiState {
    object Loading : CommunityUiState()
    data class Success(val communities: List<Community>) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}

sealed class CreateCommunityState {
    object Idle : CreateCommunityState()
    object Loading : CreateCommunityState()
    object Success : CreateCommunityState()
    data class Error(val message: String) : CreateCommunityState()
}

sealed class JoinCommunityState {
    object Idle : JoinCommunityState()
    object Loading : JoinCommunityState()
    object Success : JoinCommunityState()
    data class Error(val message: String) : JoinCommunityState()
}

// ==========================================
// VIEW MODEL
// ==========================================

class CommunityViewModel(
    private val repository: FirebaseCommunityRepository = FirebaseCommunityRepository()
) : ViewModel() {

    // Communities
    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allCommunities = MutableStateFlow<List<Community>>(emptyList())

    // Create Community
    private val _createCommunityState =
        MutableStateFlow<CreateCommunityState>(CreateCommunityState.Idle)
    val createCommunityState: StateFlow<CreateCommunityState> = _createCommunityState.asStateFlow()

    // Join Community
    private val _joinCommunityState =
        MutableStateFlow<JoinCommunityState>(JoinCommunityState.Idle)
    val joinCommunityState: StateFlow<JoinCommunityState> = _joinCommunityState.asStateFlow()

    init {
        loadCommunities()
        observeSearchQuery()
    }

    // ==========================================
    // COMMUNITY OPERATIONS
    // ==========================================

    fun loadCommunities() {
        viewModelScope.launch {
            _uiState.value = CommunityUiState.Loading

            repository.getCommunities()
                .onSuccess { communities ->
                    _allCommunities.value = communities
                    filterCommunities(_searchQuery.value)
                }
                .onFailure { exception ->
                    _uiState.value = CommunityUiState.Error(
                        exception.message ?: "Error desconocido al cargar comunidades"
                    )
                }
        }
    }

    fun createCommunity(
        name: String,
        description: String,
        paymentPointer: String,
        category: String = "CUSTOM"
    ) {
        viewModelScope.launch {
            _createCommunityState.value = CreateCommunityState.Loading

            repository.createCommunity(
                name = name,
                description = description,
                category = category,
                paymentPointer = paymentPointer
            ).onSuccess {
                _createCommunityState.value = CreateCommunityState.Success
                // Recargar todas las comunidades desde Firebase para asegurar sincronización
                loadCommunities()
            }.onFailure { exception ->
                _createCommunityState.value = CreateCommunityState.Error(
                    exception.message ?: "Error al crear la comunidad"
                )
            }
        }
    }

    fun joinCommunity(inviteCode: String, userPaymentPointer: String) {
        viewModelScope.launch {
            _joinCommunityState.value = JoinCommunityState.Loading

            repository.joinCommunity(inviteCode, userPaymentPointer)
                .onSuccess {
                    _joinCommunityState.value = JoinCommunityState.Success
                    // Recargar todas las comunidades para mostrar la nueva comunidad
                    loadCommunities()
                }
                .onFailure { exception ->
                    _joinCommunityState.value = JoinCommunityState.Error(
                        exception.message ?: "Error al unirse a la comunidad"
                    )
                }
        }
    }

    fun searchCommunities(query: String) {
        _searchQuery.value = query
    }

    fun resetCreateCommunityState() {
        _createCommunityState.value = CreateCommunityState.Idle
    }

    fun resetJoinCommunityState() {
        _joinCommunityState.value = JoinCommunityState.Idle
    }

    fun refresh() {
        loadCommunities()
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Esperar 300ms después de que el usuario deje de escribir
                .distinctUntilChanged()
                .collect { query ->
                    filterCommunities(query)
                }
        }
    }

    private fun filterCommunities(query: String) {
        val filteredCommunities = if (query.isBlank()) {
            _allCommunities.value
        } else {
            _allCommunities.value.filter { community ->
                community.name.contains(query, ignoreCase = true) ||
                        community.description.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = CommunityUiState.Success(filteredCommunities)
    }

    // Función para obtener una comunidad específica
    fun getCommunityById(id: String): Community? {
        return _allCommunities.value.find { it.id == id }
    }

    // Función para verificar si el usuario actual es administrador de una comunidad
    fun isCurrentUserAdmin(communityId: String): Boolean {
        val community = getCommunityById(communityId)
        val currentUserId = repository.getCurrentUserId()
        return community?.createdBy == currentUserId
    }
}