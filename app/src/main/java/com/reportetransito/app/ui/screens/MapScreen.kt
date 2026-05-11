package com.reportetransito.app.ui.screens

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.reportetransito.app.data.model.Incident
import com.reportetransito.app.data.repository.PicoPlacaRepository
import com.reportetransito.app.ui.components.IncidentInfoCard
import com.reportetransito.app.viewmodel.MapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cityId by viewModel.cityId.collectAsState()
    val city = viewModel.currentCity

    val defaultPosition = LatLng(
        city?.defaultLat ?: 4.7110,
        city?.defaultLng ?: -74.0721
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, city?.defaultZoom ?: 13f)
    }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coroutineScope = rememberCoroutineScope()

    var showReportSheet by remember { mutableStateOf(false) }
    var reportLocation by remember { mutableStateOf(defaultPosition) }
    var showCityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(city) {
        city?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.defaultLat, it.defaultLng), it.defaultZoom)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            onMapClick = {
                viewModel.selectIncident(null)
            }
        ) {
            uiState.incidents.forEach { incident ->
                IncidentMarker(
                    incident = incident,
                    onClick = {
                        viewModel.selectIncident(incident)
                        true
                    }
                )
            }
        }

        // Top bar: city selector
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
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
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                TextButton(onClick = { showCityPicker = true }) {
                    Text("Cambiar")
                }
            }
        }

        // Hint: long press to report
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Mantén presionado el mapa para reportar",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // FAB: report at current location
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (locationPermission.status.isGranted) {
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            // Center on user location is handled by MyLocation button style
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }
            } else {
                SmallFloatingActionButton(
                    onClick = { locationPermission.launchPermissionRequest() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Permitir ubicación")
                }
            }

            FloatingActionButton(
                onClick = {
                    reportLocation = cameraPositionState.position.target
                    showReportSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Reportar novedad")
            }
        }

        // Active incidents count badge
        if (uiState.incidents.isNotEmpty()) {
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
                    text = "🚨 ${uiState.incidents.size} novedad(es) activa(s)",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Selected incident card
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

        // Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }

    // Report sheet
    if (showReportSheet) {
        ReportIncidentBottomSheet(
            reportLocation = reportLocation,
            viewModel = viewModel,
            onDismiss = { showReportSheet = false }
        )
    }

    // City picker dialog
    if (showCityPicker) {
        CityPickerDialog(
            cities = viewModel.currentCity?.let {
                // pass all cities from repo
                listOf()
            } ?: emptyList(),
            viewModel = viewModel,
            onDismiss = { showCityPicker = false }
        )
    }
}

@Composable
private fun IncidentMarker(
    incident: Incident,
    onClick: () -> Boolean
) {
    val type = incident.incidentType
    Marker(
        state = MarkerState(position = LatLng(incident.latitude, incident.longitude)),
        icon = BitmapDescriptorFactory.defaultMarker(type.markerHue),
        title = type.label,
        snippet = incident.description.ifBlank { "Toca para ver detalles" },
        onClick = { onClick() }
    )
}

@Composable
private fun CityPickerDialog(
    cities: List<Any>,
    viewModel: MapViewModel,
    onDismiss: () -> Unit
) {
    val allCities = remember {
        listOf(
            "bogota" to "Bogotá D.C.",
            "medellin" to "Medellín",
            "cali" to "Cali",
            "barranquilla" to "Barranquilla",
            "bucaramanga" to "Bucaramanga",
            "pereira" to "Pereira",
            "manizales" to "Manizales"
        )
    }
    val cityId by viewModel.cityId.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar ciudad") },
        text = {
            Column {
                allCities.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = cityId == id,
                            onClick = {
                                viewModel.selectCity(id)
                                onDismiss()
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
