package com.reportetransito.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.reportetransito.app.data.model.Incident
import com.reportetransito.app.data.model.IncidentCategory
import com.reportetransito.app.ui.components.IncidentInfoCard
import com.reportetransito.app.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val uiState by viewModel.uiState.collectAsState()
    val cityId by viewModel.cityId.collectAsState()
    val city = viewModel.currentCity

    val defaultPosition = LatLng(city?.defaultLat ?: 4.7110, city?.defaultLng ?: -74.0721)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, city?.defaultZoom ?: 13f)
    }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coroutineScope = rememberCoroutineScope()

    var showReportSheet by remember { mutableStateOf(false) }
    var reportLocation by remember { mutableStateOf(defaultPosition) }
    var showCityPicker by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Center on city when selection changes
    LaunchedEffect(city) {
        city?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.defaultLat, it.defaultLng), it.defaultZoom)
            )
        }
    }

    // Center on user location once permission is granted
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            runCatching {
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    userLocation = latLng
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── MAP ──────────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermission.status.isGranted,
                mapType = MapType.NORMAL
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true
            ),
            onMapLongClick = { latLng ->
                reportLocation = latLng
                showReportSheet = true
            },
            onMapClick = { viewModel.selectIncident(null) }
        ) {
            uiState.filteredIncidents.forEach { incident ->
                IncidentMarker(incident = incident, onClick = {
                    viewModel.selectIncident(incident)
                    true
                })
            }
        }

        // ── TOP COLUMN: city bar + filter chips ──────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // City selector card
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = city?.name ?: "Seleccionar ciudad",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = { showCityPicker = true }) {
                        Text("Cambiar")
                    }
                }
            }

            // Filter chips row
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "Todos" chip
                FilterChip(
                    selected = uiState.activeFilter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("Todos (${uiState.incidents.size})") }
                )
                IncidentCategory.entries.forEach { cat ->
                    val count = uiState.incidents.count { it.incidentType.category == cat }
                    FilterChip(
                        selected = uiState.activeFilter == cat,
                        onClick = { viewModel.setFilter(cat) },
                        label = { Text("${cat.emoji} ${cat.label} ($count)") }
                    )
                }
            }

            // Long-press hint (only shown when no incidents)
            if (uiState.incidents.isEmpty() && !uiState.isLoading) {
                Card(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
                    )
                ) {
                    Text(
                        text = "Mantén presionado el mapa para reportar",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // ── FABs bottom-right ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    if (locationPermission.status.isGranted) {
                        coroutineScope.launch {
                            runCatching {
                                val location = fusedLocationClient.lastLocation.await()
                                location?.let {
                                    val latLng = LatLng(it.latitude, it.longitude)
                                    userLocation = latLng
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                                    )
                                }
                            }
                        }
                    } else {
                        locationPermission.launchPermissionRequest()
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
            }

            FloatingActionButton(
                onClick = {
                    reportLocation = userLocation ?: cameraPositionState.position.target
                    showReportSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Reportar novedad")
            }
        }

        // ── Active incidents badge ────────────────────────────────────────
        if (uiState.filteredIncidents.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "🚨 ${uiState.filteredIncidents.size} novedad(es) activa(s)",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // ── Selected incident card ────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.selectedIncident != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            uiState.selectedIncident?.let { incident ->
                IncidentInfoCard(
                    incident = incident,
                    onConfirm = {
                        viewModel.confirmIncident(incident.id)
                        viewModel.selectIncident(null)
                    },
                    onDismiss = { viewModel.selectIncident(null) }
                )
            }
        }

        // ── Loading bar ───────────────────────────────────────────────────
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // ── Error snackbar ────────────────────────────────────────────────
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                }
            ) { Text(error) }
        }
    }

    if (showReportSheet) {
        ReportIncidentBottomSheet(
            reportLocation = reportLocation,
            viewModel = viewModel,
            onDismiss = { showReportSheet = false }
        )
    }

    if (showCityPicker) {
        CityPickerDialog(
            currentCityId = cityId,
            onCitySelected = { viewModel.selectCity(it) },
            onDismiss = { showCityPicker = false }
        )
    }
}

@Composable
private fun IncidentMarker(incident: Incident, onClick: () -> Boolean) {
    val type = incident.incidentType
    Marker(
        state = MarkerState(position = LatLng(incident.latitude, incident.longitude)),
        icon = BitmapDescriptorFactory.defaultMarker(type.markerHue),
        title = "${type.displayEmoji} ${type.label}",
        snippet = incident.description.ifBlank { "Toca para ver detalles" },
        onClick = { onClick() }
    )
}

@Composable
private fun CityPickerDialog(
    currentCityId: String,
    onCitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cities = listOf(
        "bogota" to "Bogotá D.C.",
        "medellin" to "Medellín",
        "cali" to "Cali",
        "barranquilla" to "Barranquilla",
        "bucaramanga" to "Bucaramanga",
        "pereira" to "Pereira",
        "manizales" to "Manizales"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar ciudad") },
        text = {
            Column {
                cities.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentCityId == id,
                            onClick = { onCitySelected(id); onDismiss() }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}
