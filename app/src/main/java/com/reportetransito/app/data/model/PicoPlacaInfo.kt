package com.reportetransito.app.data.model

data class City(
    val id: String,
    val name: String,
    val defaultLat: Double,
    val defaultLng: Double,
    val defaultZoom: Float
)

data class PicoPlacaSchedule(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val label: String
)

data class PicoPlacaRule(
    val dayOfWeek: Int, // Calendar.MONDAY..FRIDAY
    val restrictedDigits: List<Int>,
    val schedules: List<PicoPlacaSchedule>
)

data class PicoPlacaInfo(
    val city: City,
    val rules: List<PicoPlacaRule>,
    val notes: String = ""
)

data class TodayRestriction(
    val hasRestriction: Boolean,
    val restrictedDigits: List<Int> = emptyList(),
    val schedules: List<PicoPlacaSchedule> = emptyList(),
    val isCurrentlyActive: Boolean = false
)
