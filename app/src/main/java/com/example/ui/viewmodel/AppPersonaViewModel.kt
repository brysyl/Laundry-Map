package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.OfflineSyncManager
import com.example.domain.model.CorridorZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppPersona(val title: String, val subtitle: String) {
    CUSTOMER("Customer", "Requestor"),
    VENDOR("Vendor Portal", "Laundromat"),
    COURIER("Courier Rider", "Transporter")
}

data class PersonaUiState(
    val activePersona: AppPersona = AppPersona.CUSTOMER,
    val activeCorridor: CorridorZone = CorridorZone.KARU_MARARABA,
    val isNetworkOnline: Boolean = true,
    val pendingSyncCount: Int = 0
)

class AppPersonaViewModel(
    private val syncManager: OfflineSyncManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(PersonaUiState())
    val uiState: StateFlow<PersonaUiState> = _uiState.asStateFlow()

    fun switchPersona(persona: AppPersona) {
        _uiState.value = _uiState.value.copy(activePersona = persona)
    }

    fun switchCorridor(zone: CorridorZone) {
        _uiState.value = _uiState.value.copy(activeCorridor = zone)
    }

    fun toggleNetworkOnline() {
        val next = !_uiState.value.isNetworkOnline
        _uiState.value = _uiState.value.copy(isNetworkOnline = next)
        syncManager.setNetworkStatus(next)
    }

    class Factory(private val syncManager: OfflineSyncManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppPersonaViewModel(syncManager) as T
        }
    }
}
