package com.reportetransito.app.data.repository

import com.reportetransito.app.data.model.*
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PicoPlacaRepository @Inject constructor() {

    val cities: List<City> = listOf(
        City("bogota", "Bogotá D.C.", 4.7110, -74.0721, 13f),
        City("medellin", "Medellín", 6.2442, -75.5812, 13f),
        City("cali", "Cali", 3.4516, -76.5320, 13f),
        City("barranquilla", "Barranquilla", 10.9685, -74.7813, 13f),
        City("bucaramanga", "Bucaramanga", 7.1254, -73.1198, 13f),
        City("pereira", "Pereira", 4.8133, -75.6961, 13f),
        City("manizales", "Manizales", 5.0703, -75.5138, 13f),
    )

    private val morningSchedule = PicoPlacaSchedule(6, 0, 8, 30, "Mañana")
    private val afternoonSchedule = PicoPlacaSchedule(15, 0, 19, 30, "Tarde")

    private val bogotaRules = listOf(
        PicoPlacaRule(Calendar.MONDAY, listOf(9, 0), listOf(morningSchedule, afternoonSchedule)),
        PicoPlacaRule(Calendar.TUESDAY, listOf(1, 2), listOf(morningSchedule, afternoonSchedule)),
        PicoPlacaRule(Calendar.WEDNESDAY, listOf(3, 4), listOf(morningSchedule, afternoonSchedule)),
        PicoPlacaRule(Calendar.THURSDAY, listOf(5, 6), listOf(morningSchedule, afternoonSchedule)),
        PicoPlacaRule(Calendar.FRIDAY, listOf(7, 8), listOf(morningSchedule, afternoonSchedule)),
    )

    // Medellín: restricción por último dígito de la placa, diferente horario
    private val morningMed = PicoPlacaSchedule(7, 0, 8, 30, "Mañana")
    private val afternoonMed = PicoPlacaSchedule(17, 30, 20, 0, "Tarde-noche")

    private val medellinRules = listOf(
        PicoPlacaRule(Calendar.MONDAY, listOf(1, 2), listOf(morningMed, afternoonMed)),
        PicoPlacaRule(Calendar.TUESDAY, listOf(3, 4), listOf(morningMed, afternoonMed)),
        PicoPlacaRule(Calendar.WEDNESDAY, listOf(5, 6), listOf(morningMed, afternoonMed)),
        PicoPlacaRule(Calendar.THURSDAY, listOf(7, 8), listOf(morningMed, afternoonMed)),
        PicoPlacaRule(Calendar.FRIDAY, listOf(9, 0), listOf(morningMed, afternoonMed)),
    )

    // Cali
    private val morningCali = PicoPlacaSchedule(6, 30, 8, 30, "Mañana")
    private val afternoonCali = PicoPlacaSchedule(15, 30, 19, 30, "Tarde")

    private val caliRules = listOf(
        PicoPlacaRule(Calendar.MONDAY, listOf(1, 2), listOf(morningCali, afternoonCali)),
        PicoPlacaRule(Calendar.TUESDAY, listOf(3, 4), listOf(morningCali, afternoonCali)),
        PicoPlacaRule(Calendar.WEDNESDAY, listOf(5, 6), listOf(morningCali, afternoonCali)),
        PicoPlacaRule(Calendar.THURSDAY, listOf(7, 8), listOf(morningCali, afternoonCali)),
        PicoPlacaRule(Calendar.FRIDAY, listOf(9, 0), listOf(morningCali, afternoonCali)),
    )

    private val picoPlacaInfoMap = mapOf(
        "bogota" to PicoPlacaInfo(
            city = cities.first { it.id == "bogota" },
            rules = bogotaRules,
            notes = "Aplica para vehículos particulares. No aplica festivos."
        ),
        "medellin" to PicoPlacaInfo(
            city = cities.first { it.id == "medellin" },
            rules = medellinRules,
            notes = "Aplica para vehículos particulares. Verifique cambios estacionales."
        ),
        "cali" to PicoPlacaInfo(
            city = cities.first { it.id == "cali" },
            rules = caliRules,
            notes = "Aplica para vehículos particulares. No aplica festivos."
        ),
        "barranquilla" to PicoPlacaInfo(
            city = cities.first { it.id == "barranquilla" },
            rules = emptyList(),
            notes = "Barranquilla no tiene pico y placa activo actualmente. Verifique con la Alcaldía."
        ),
        "bucaramanga" to PicoPlacaInfo(
            city = cities.first { it.id == "bucaramanga" },
            rules = emptyList(),
            notes = "Consulte las restricciones vigentes en la Alcaldía de Bucaramanga."
        ),
        "pereira" to PicoPlacaInfo(
            city = cities.first { it.id == "pereira" },
            rules = emptyList(),
            notes = "Consulte las restricciones vigentes en la Alcaldía de Pereira."
        ),
        "manizales" to PicoPlacaInfo(
            city = cities.first { it.id == "manizales" },
            rules = emptyList(),
            notes = "Consulte las restricciones vigentes en la Alcaldía de Manizales."
        ),
    )

    fun getPicoPlacaInfo(cityId: String): PicoPlacaInfo? = picoPlacaInfoMap[cityId]

    fun getTodayRestriction(cityId: String): TodayRestriction {
        val info = picoPlacaInfoMap[cityId] ?: return TodayRestriction(false)
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val rule = info.rules.firstOrNull { it.dayOfWeek == dayOfWeek }
            ?: return TodayRestriction(false)

        val isCurrentlyActive = rule.schedules.any { schedule ->
            val afterStart = (hour > schedule.startHour) ||
                    (hour == schedule.startHour && minute >= schedule.startMinute)
            val beforeEnd = (hour < schedule.endHour) ||
                    (hour == schedule.endHour && minute < schedule.endMinute)
            afterStart && beforeEnd
        }

        return TodayRestriction(
            hasRestriction = true,
            restrictedDigits = rule.restrictedDigits,
            schedules = rule.schedules,
            isCurrentlyActive = isCurrentlyActive
        )
    }
}
