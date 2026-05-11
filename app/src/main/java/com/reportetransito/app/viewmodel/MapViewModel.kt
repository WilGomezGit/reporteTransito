package com.reportetransito.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.reportetransito.app.data.model.Incident
import com.reportetransito.app.data.model.IncidentType
import com.reportetransito.app.data.repository.IncidentRepository
import com.reportetransito.app.data.repository.PicoPlacaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val incidents: List<Incident> = emptyList(),
    val selectedIncident: Incident? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val reportSuccess: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val picoPlacaRepository: PicoPlacaRepository
) : ViewModel() {

    private val _cityId = MutableStateFlow("bogota")
    val cityId: StateFlow<String> = _cityId.asStateFlow()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    val currentCity get() = picoPlacaRepository.cities.firstOrNull { it.id == _cityId.value }

    init {
        observeIncidents()
    }

    private fun observeIncidents() {
        viewModelScope.launch {
            _cityId.flatMapLatest { cityId ->
                _uiState.update { it.copy(isLoading = true) }
                incidentRepository.getActiveIncidents(cityId)
            }.collect { incidents ->
                _uiState.update { it.copy(incidents = incidents, isLoading = false) }
            }
        }
    }

    fun selectCity(cityId: String) {
        _cityId.value = cityId
    }

    fun selectIncident(incident: Incident?) {
        _uiState.update { it.copy(selectedIncident = incident) }
    }

    fun reportIncident(type: IncidentType, location: LatLng, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = incidentRepository.reportIncident(
                Incident(
                    type = type.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    description = description,
                    cityId = _cityId.value
                )
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, reportSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun confirmIncident(incidentId: String) {
        viewModelScope.launch {
            incidentRepository.confirmIncident(incidentId)
        }
    }

    fun clearReportSuccess() {
        _uiState.update { it.copy(reportSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
