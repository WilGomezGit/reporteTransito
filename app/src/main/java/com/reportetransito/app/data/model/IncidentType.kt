package com.reportetransito.app.data.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory

enum class IncidentType(
    val label: String,
    val emoji: String,
    val markerHue: Float,
    val color: Color
) {
    ACCIDENT(
        label = "Accidente",
        emoji = "🚨",
        markerHue = BitmapDescriptorFactory.HUE_RED,
        color = Color(0xFFE53935)
    ),
    CHECKPOINT(
        label = "Retén policial",
        emoji = "🚔",
        markerHue = BitmapDescriptorFactory.HUE_BLUE,
        color = Color(0xFF1565C0)
    ),
    CONGESTION(
        label = "Congestión",
        emoji = "🚦",
        markerHue = BitmapDescriptorFactory.HUE_ORANGE,
        color = Color(0xFFE65100)
    ),
    ROADWORK(
        label = "Obra en vía",
        emoji = "🚧",
        markerHue = BitmapDescriptorFactory.HUE_YELLOW,
        color = Color(0xFFF9A825)
    ),
    HAZARD(
        label = "Obstáculo / Peligro",
        emoji = "⚠️",
        markerHue = BitmapDescriptorFactory.HUE_ROSE,
        color = Color(0xFFAD1457)
    ),
    FLOOD(
        label = "Inundación",
        emoji = "🌊",
        markerHue = BitmapDescriptorFactory.HUE_CYAN,
        color = Color(0xFF00838F)
    ),
    CLOSED_ROAD(
        label = "Vía cerrada",
        emoji = "🚫",
        markerHue = BitmapDescriptorFactory.HUE_MAGENTA,
        color = Color(0xFF6A1B9A)
    )
}
