package com.reportetransito.app.data.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory

enum class IncidentCategory(val label: String, val emoji: String, val chipColor: Color) {
    EMERGENCIA("Emergencias", "🔴", Color(0xFFB71C1C)),
    PRECAUCION("Precaución", "🟡", Color(0xFFE65100)),
    LIBRE("Libre", "🟢", Color(0xFF2E7D32))
}

enum class IncidentType(
    val code: String,
    val label: String,
    val displayEmoji: String,
    val fullLabel: String,
    val category: IncidentCategory,
    val markerHue: Float,
    val color: Color
) {
    // ─── 🔴 EMERGENCIAS ───────────────────────────────────────────────
    ACCIDENTE(
        code = "942",
        label = "Accidente de tránsito",
        displayEmoji = "💥",
        fullLabel = "942 💥🚗 Accidente de tránsito",
        category = IncidentCategory.EMERGENCIA,
        markerHue = BitmapDescriptorFactory.HUE_RED,
        color = Color(0xFFB71C1C)
    ),
    MUERTO(
        code = "901",
        label = "Fallecido en vía",
        displayEmoji = "☠️",
        fullLabel = "901 ☠️ Fallecido / Muerto",
        category = IncidentCategory.EMERGENCIA,
        markerHue = BitmapDescriptorFactory.HUE_VIOLET,
        color = Color(0xFF4A148C)
    ),
    HERIDO(
        code = "910",
        label = "Persona herida",
        displayEmoji = "🚑",
        fullLabel = "910 🩹🚑 Persona herida",
        category = IncidentCategory.EMERGENCIA,
        markerHue = BitmapDescriptorFactory.HUE_ROSE,
        color = Color(0xFFAD1457)
    ),
    EMERGENCIA_VIA(
        code = "940",
        label = "Emergencia en vía",
        displayEmoji = "🚨",
        fullLabel = "940 🚨🚑 Emergencia en vía",
        category = IncidentCategory.EMERGENCIA,
        markerHue = BitmapDescriptorFactory.HUE_RED,
        color = Color(0xFFD32F2F)
    ),
    CIERRE_VIAL(
        code = "970",
        label = "Cierre vial",
        displayEmoji = "⛔",
        fullLabel = "970 🚦⛔ Cierre vial",
        category = IncidentCategory.EMERGENCIA,
        markerHue = BitmapDescriptorFactory.HUE_MAGENTA,
        color = Color(0xFF880E4F)
    ),
    INCENDIO(
        code = "990",
        label = "Incendio / Situación grave",
        displayEmoji = "🔥",
        fullLabel = "990 🔥🚒 Incendio o situación grave",
        category = IncidentCategory.EMERGENCIA,
        markerHue = BitmapDescriptorFactory.HUE_ORANGE,
        color = Color(0xFFBF360C)
    ),

    // ─── 🟡 PRECAUCIÓN ───────────────────────────────────────────────
    RETEN(
        code = "720",
        label = "Retén activo",
        displayEmoji = "🚓",
        fullLabel = "720 🚓🚨 Retén de tránsito activo",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_BLUE,
        color = Color(0xFF1565C0)
    ),
    OPERATIVO(
        code = "620",
        label = "Operativo policial",
        displayEmoji = "👮",
        fullLabel = "620 👮🏻🚓 Operativo policial / puesto de control",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_AZURE,
        color = Color(0xFF0277BD)
    ),
    VARADO(
        code = "925",
        label = "Vehículo varado",
        displayEmoji = "⚠️",
        fullLabel = "925 🚗⚠️ Vehículo varado",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_YELLOW,
        color = Color(0xFFF57F17)
    ),
    CONGESTION(
        code = "930",
        label = "Congestión vehicular",
        displayEmoji = "🚧",
        fullLabel = "930 🚚🚧 Congestión vehicular",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_ORANGE,
        color = Color(0xFFE65100)
    ),
    VIA_PELIGROSA(
        code = "933",
        label = "Vía peligrosa",
        displayEmoji = "🌧️",
        fullLabel = "933 🌧️🛣️ Vía peligrosa / deslizamiento",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_CYAN,
        color = Color(0xFF006064)
    ),
    CONTROL_MOTOS(
        code = "950",
        label = "Control a motos",
        displayEmoji = "🏍️",
        fullLabel = "950 🏍️💨 Pico de controles a motos",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_BLUE,
        color = Color(0xFF283593)
    ),
    PATRULLA(
        code = "960",
        label = "Patrulla en recorrido",
        displayEmoji = "🚔",
        fullLabel = "960 🚔➡️ Patrulla móvil en recorrido",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_AZURE,
        color = Color(0xFF01579B)
    ),
    GRUA(
        code = "980",
        label = "Grúa en vía",
        displayEmoji = "🪝",
        fullLabel = "980 🚛🪝 Presencia de grúa",
        category = IncidentCategory.PRECAUCION,
        markerHue = BitmapDescriptorFactory.HUE_YELLOW,
        color = Color(0xFFF9A825)
    ),

    // ─── 🟢 LIBRE ────────────────────────────────────────────────────
    SIN_NOVEDAD(
        code = "SN",
        label = "Sin novedad",
        displayEmoji = "✅",
        fullLabel = "SN ✅ Sin novedad / tránsito libre",
        category = IncidentCategory.LIBRE,
        markerHue = BitmapDescriptorFactory.HUE_GREEN,
        color = Color(0xFF2E7D32)
    )
}
