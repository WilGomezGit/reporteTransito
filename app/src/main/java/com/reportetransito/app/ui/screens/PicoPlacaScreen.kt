package com.reportetransito.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reportetransito.app.data.model.PicoPlacaSchedule
import com.reportetransito.app.data.model.TodayRestriction
import com.reportetransito.app.ui.theme.TrafficGreen
import com.reportetransito.app.ui.theme.TrafficRed
import com.reportetransito.app.viewmodel.PicoPlacaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicoPlacaScreen(viewModel: PicoPlacaViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val today = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "CO")).format(Date())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Pico y Placa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = today.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // City selector
        item {
            CityDropdown(
                selectedCityId = uiState.selectedCity?.id ?: "bogota",
                cities = viewModel.cities.map { it.id to it.name },
                onCitySelected = { viewModel.selectCity(it) }
            )
        }

        // Today's restriction card
        item {
            RestrictionStatusCard(restriction = uiState.todayRestriction)
        }

        // Restricted digits display
        if (uiState.todayRestriction.hasRestriction) {
            item {
                RestrictedDigitsCard(restriction = uiState.todayRestriction)
            }

            item {
                ScheduleCard(schedules = uiState.todayRestriction.schedules)
            }
        }

        // Plate checker
        item {
            PlateCheckerCard(
                plateQuery = uiState.plateQuery,
                plateIsRestricted = uiState.plateIsRestricted,
                onPlateChange = { viewModel.checkPlate(it) },
                hasRestrictionToday = uiState.todayRestriction.hasRestriction
            )
        }

        // Notes
        uiState.picoPlacaInfo?.notes?.let { notes ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun RestrictionStatusCard(restriction: TodayRestriction) {
    val (bgColor, emoji, title, subtitle) = when {
        restriction.isCurrentlyActive -> listOf(
            TrafficRed,
            "🚫",
            "¡Pico y Placa ACTIVO ahora!",
            "Hay restricción en este momento"
        )
        restriction.hasRestriction -> listOf(
            Color(0xFFE65100),
            "⚠️",
            "Pico y Placa hoy",
            "Restricción durante los horarios indicados"
        )
        else -> listOf(
            TrafficGreen,
            "✅",
            "Sin restricción hoy",
            "No hay pico y placa para el día de hoy"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = (bgColor as Color).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = emoji as String, fontSize = 40.sp)
            Column {
                Text(
                    text = title as String,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = bgColor
                )
                Text(
                    text = subtitle as String,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RestrictedDigitsCard(restriction: TodayRestriction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Placas restringidas hoy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                restriction.restrictedDigits.forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(TrafficRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Último dígito\nde la placa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ScheduleCard(schedules: List<PicoPlacaSchedule>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Horarios de restricción",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            schedules.forEach { schedule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = schedule.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "%02d:%02d – %02d:%02d".format(
                            schedule.startHour, schedule.startMinute,
                            schedule.endHour, schedule.endMinute
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TrafficRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (schedules.last() != schedule) Divider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlateCheckerCard(
    plateQuery: String,
    plateIsRestricted: Boolean?,
    onPlateChange: (String) -> Unit,
    hasRestrictionToday: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Consultar mi placa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = plateQuery,
                onValueChange = onPlateChange,
                label = { Text("Número de placa") },
                placeholder = { Text("Ej: ABC123") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (plateQuery.isNotEmpty() && plateIsRestricted != null) {
                Spacer(Modifier.height(12.dp))
                val (color, msg) = if (plateIsRestricted)
                    TrafficRed to "🚫 Tu placa tiene restricción hoy en los horarios indicados"
                else
                    TrafficGreen to "✅ Tu placa NO tiene restricción hoy"

                Card(
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        color = color,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (plateQuery.isNotEmpty() && !hasRestrictionToday) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "✅ No hay pico y placa hoy",
                    color = TrafficGreen,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    selectedCityId: String,
    cities: List<Pair<String, String>>,
    onCitySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = cities.firstOrNull { it.first == selectedCityId }?.second ?: selectedCityId

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ciudad") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cities.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onCitySelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
