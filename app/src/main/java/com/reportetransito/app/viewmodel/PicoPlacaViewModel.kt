package com.reportetransito.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reportetransito.app.data.model.City
import com.reportetransito.app.data.model.PicoPlacaInfo
import com.reportetransito.app.data.model.TodayRestriction
import com.reportetransito.app.data.repository.PicoPlacaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PicoPlacaUiState(
    val selectedCity: City? = null,
    val picoPlacaInfo: PicoPlacaInfo? = null,
    val todayRestriction: TodayRestriction = TodayRestriction(false),
    val plateQuery: String = "",
    val plateIsRestricted: Boolean? = null
)

@HiltViewModel
class PicoPlacaViewModel @Inject constructor(
    private val repository: PicoPlacaRepository
) : ViewModel() {

    val cities: List<City> = repository.cities

    private val _uiState = MutableStateFlow(PicoPlacaUiState())
    val uiState: StateFlow<PicoPlacaUiState> = _uiState.asStateFlow()

    init {
        selectCity("bogota")
        // Refresh every minute to keep "isCurrentlyActive" up to date
        viewModelScope.launch {
            while (true) {
                delay(60_000L)
                refreshRestriction()
            }
        }
    }

    fun selectCity(cityId: String) {
        val city = repository.cities.firstOrNull { it.id == cityId } ?: return
        val info = repository.getPicoPlacaInfo(cityId)
        val restriction = repository.getTodayRestriction(cityId)
        _uiState.update {
            it.copy(
                selectedCity = city,
                picoPlacaInfo = info,
                todayRestriction = restriction,
                plateQuery = "",
                plateIsRestricted = null
            )
        }
    }

    fun checkPlate(plate: String) {
        val trimmed = plate.trim().uppercase()
        if (trimmed.isEmpty()) {
            _uiState.update { it.copy(plateQuery = "", plateIsRestricted = null) }
            return
        }
        val lastChar = trimmed.last()
        val lastDigit = lastChar.digitToIntOrNull()
        val restriction = _uiState.value.todayRestriction
        val isRestricted = if (lastDigit != null && restriction.hasRestriction) {
            lastDigit in restriction.restrictedDigits
        } else false
        _uiState.update { it.copy(plateQuery = trimmed, plateIsRestricted = isRestricted) }
    }

    private fun refreshRestriction() {
        val cityId = _uiState.value.selectedCity?.id ?: return
        val restriction = repository.getTodayRestriction(cityId)
        _uiState.update { it.copy(todayRestriction = restriction) }
    }
}
