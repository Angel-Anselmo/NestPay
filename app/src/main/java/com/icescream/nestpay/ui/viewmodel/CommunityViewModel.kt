package com.icescream.nestpay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icescream.nestpay.data.repository.CommunityRepository
import com.icescream.nestpay.data.repository.FirebaseCommunityRepository
import com.icescream.nestpay.ui.screens.PaymentCommunity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CommunityUiState {
    object Loading : CommunityUiState()
    data class Success(val communities: List<PaymentCommunity>) : CommunityUiState()
    data class Error(val message: String) : CommunityUiState()
}

sealed class CreateCommunityState {
    object Idle : CreateCommunityState()
    object Loading : CreateCommunityState()
    object Success : CreateCommunityState()
    data class Error(val message: String) : CreateCommunityState()
}

class CommunityViewModel(
    private val repository: FirebaseCommunityRepository = FirebaseCommunityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _createCommunityState =
        MutableStateFlow<CreateCommunityState>(CreateCommunityState.Idle)
    val createCommunityState: StateFlow<CreateCommunityState> = _createCommunityState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allCommunities = MutableStateFlow<List<PaymentCommunity>>(emptyList())

    init {
        loadCommunities()
        observeSearchQuery()
    }

    fun loadCommunities() {
        viewModelScope.launch {
            _uiState.value = CommunityUiState.Loading

            repository.getCommunities()
                .onSuccess { communities ->
                    _allCommunities.value = communities
                    _uiState.value = CommunityUiState.Success(communities)
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
        targetAmount: Double,
        walletAddress: String,
        dueDate: String,
        category: String
    ) {
        viewModelScope.launch {
            _createCommunityState.value = CreateCommunityState.Loading

            repository.createCommunity(
                name = name,
                description = description,
                targetAmount = targetAmount,
                walletAddress = walletAddress,
                dueDate = dueDate,
                category = category
            ).onSuccess { newCommunity ->
                _createCommunityState.value = CreateCommunityState.Success

                // Actualizar la lista de comunidades
                val currentCommunities = _allCommunities.value.toMutableList()
                currentCommunities.add(0, newCommunity)
                _allCommunities.value = currentCommunities

                // Aplicar filtro actual si hay búsqueda
                filterCommunities(_searchQuery.value)

            }.onFailure { exception ->
                _createCommunityState.value = CreateCommunityState.Error(
                    exception.message ?: "Error al crear la comunidad"
                )
            }
        }
    }

    fun searchCommunities(query: String) {
        _searchQuery.value = query
    }

    fun joinCommunity(communityId: String) {
        viewModelScope.launch {
            repository.joinCommunity(communityId)
                .onSuccess {
                    // Recargar comunidades para mostrar la nueva
                    loadCommunities()
                }
                .onFailure { exception ->
                    _uiState.value = CommunityUiState.Error(
                        exception.message ?: "Error al unirse a la comunidad"
                    )
                }
        }
    }

    fun resetCreateCommunityState() {
        _createCommunityState.value = CreateCommunityState.Idle
    }

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
    fun getCommunityById(id: String): PaymentCommunity? {
        return _allCommunities.value.find { it.id == id }
    }

    // Función para refrescar datos
    fun refresh() {
        loadCommunities()
    }
}