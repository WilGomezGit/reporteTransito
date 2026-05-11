package com.reportetransito.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reportetransito.app.data.model.Incident
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IncidentInfoCard(
    incident: Incident,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val type = incident.incidentType
    val timeAgo = getTimeAgo(incident.lastConfirmedAt.toDate())
    val expiresIn = getExpiresIn(incident.lastConfirmedAt.toDate())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = type.color.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = type.emoji, fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = type.color
                        )
                        Text(
                            text = "Hace $timeAgo · $expiresIn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            if (incident.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = incident.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${incident.confirmations} confirmación(es)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = onConfirm,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Confirmar")
                }
            }
        }
    }
}

private fun getTimeAgo(date: Date): String {
    val diffMs = System.currentTimeMillis() - date.time
    val diffMin = diffMs / 60_000
    return when {
        diffMin < 1 -> "menos de 1 min"
        diffMin < 60 -> "$diffMin min"
        else -> "${diffMin / 60}h ${diffMin % 60}min"
    }
}

private fun getExpiresIn(lastConfirmed: Date): String {
    val expiresAt = lastConfirmed.time + 60 * 60_000L
    val remaining = expiresAt - System.currentTimeMillis()
    return if (remaining <= 0) "expirado"
    else {
        val mins = remaining / 60_000
        if (mins < 10) "⚠️ expira en ${mins}min" else "expira en ${mins / 60}h ${mins % 60}min"
    }
}
