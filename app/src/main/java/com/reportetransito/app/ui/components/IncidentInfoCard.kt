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
import java.util.Date

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
    val isExpiringSoon = getRemainingMillis(incident.lastConfirmedAt.toDate()) < 10 * 60_000L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category banner
            Surface(
                color = type.category.chipColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(
                    text = "${type.category.emoji} ${type.category.label}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = type.category.chipColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = type.color.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = type.displayEmoji, fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = type.color.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = type.code,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = type.color
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = type.color
                            )
                        }
                        Text(
                            text = "Hace $timeAgo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isExpiringSoon) "⚠️ $expiresIn" else expiresIn,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isExpiringSoon) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
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
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ ${incident.confirmations} confirmación(es)",
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
                    Text("Aún está activo")
                }
            }
        }
    }
}

private fun getRemainingMillis(lastConfirmed: Date): Long {
    val expiresAt = lastConfirmed.time + 60 * 60_000L
    return expiresAt - System.currentTimeMillis()
}

private fun getTimeAgo(date: Date): String {
    val diffMs = System.currentTimeMillis() - date.time
    val diffMin = diffMs / 60_000
    return when {
        diffMin < 1 -> "menos de 1 min"
        diffMin < 60 -> "${diffMin} min"
        else -> "${diffMin / 60}h ${diffMin % 60}min"
    }
}

private fun getExpiresIn(lastConfirmed: Date): String {
    val remaining = getRemainingMillis(lastConfirmed)
    if (remaining <= 0) return "Expirado"
    val mins = remaining / 60_000
    return if (mins < 60) "Expira en ${mins}min"
    else "Expira en ${mins / 60}h ${mins % 60}min"
}
