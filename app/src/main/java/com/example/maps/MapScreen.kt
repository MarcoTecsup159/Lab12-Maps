package com.example.maps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.MapProperties
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch


@Composable
fun MapScreen() {
    val context = LocalContext.current
    val arequipaLocation = LatLng(-16.4040102, -71.559611) // Arequipa, Perú
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaLocation, 12f)
    }

    val scope = rememberCoroutineScope()

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para el tipo de mapa seleccionado
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }

    // Propiedades del mapa
    val mapProperties by remember(selectedMapType) {
        mutableStateOf(
            MapProperties(
                mapType = when (selectedMapType) {
                    MapType.NORMAL -> com.google.maps.android.compose.MapType.NORMAL
                    MapType.SATELLITE -> com.google.maps.android.compose.MapType.SATELLITE
                    MapType.TERRAIN -> com.google.maps.android.compose.MapType.TERRAIN
                    MapType.HYBRID -> com.google.maps.android.compose.MapType.HYBRID
                },
                isMyLocationEnabled = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        )
    }

    val locationPermissionState = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { location ->
                currentLocation = location
                scope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(location, 15f)
                        ),
                        durationMs = 2000
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation(context) { location ->
                    currentLocation = location
                }
            }
            else -> locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column {

        Button(onClick = {
            currentLocation?.let { location ->
                scope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(location, 15f)
                        ),
                        durationMs = 1000
                    )
                }
            } ?: run {
                //si no tenemos la ubicación actual, solicitamos los permisos
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionState.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    getCurrentLocation(context) { location ->
                        currentLocation = location
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(location, 15f)
                                ),
                                durationMs = 2000
                            )
                        }
                    }
                }
            }
        }) {
            Text(text = "Mi ubicación")
        }

        // Selector de tipo de mapa
        MapTypeSelector(
            selectedMapType = selectedMapType,
            onMapTypeSelected = { selectedMapType = it }
        )

        // GoogleMap con propiedades actualizadas
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {

            currentLocation?.let { location ->
                Marker(
                    state = rememberMarkerState(position = location),
                    title = "Mi ubicación",
                )
            }

            val customIcon = BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.montana)
            )

            val locations = listOf(
                LatLng(-16.433415,-71.5442652), // JLByR
                LatLng(-16.4205151,-71.4945209), // Paucarpata
                LatLng(-16.3524187,-71.5675994) // Zamacola
            )


            // Añadir marcador en Arequipa Perú
            locations.forEach { location ->
                Marker(
                    state = rememberMarkerState(position = location),
                    icon = customIcon,
                    title = "Ubicación",
                    snippet = "Punto de interés"
                )
            }

            //Mover la camara a Yura
            /*LaunchedEffect(Unit) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.2520984,-71.6836503), 12f), // Mover a Yura
                    durationMs = 3000
                )
            }*/

            val mallAventuraPolygon = listOf(
                LatLng(-16.432292, -71.509145),
                LatLng(-16.432757, -71.509626),
                LatLng(-16.433013, -71.509310),
                LatLng(-16.432566, -71.508853)
            )


            val parqueLambramaniPolygon = listOf(
                LatLng(-16.422704, -71.530830),
                LatLng(-16.422920, -71.531340),
                LatLng(-16.423264, -71.531110),
                LatLng(-16.423050, -71.530600)
            )

            val plazaDeArmasPolygon = listOf(
                LatLng(-16.398866, -71.536961),
                LatLng(-16.398744, -71.536529),
                LatLng(-16.399178, -71.536289),
                LatLng(-16.399299, -71.536721)
            )

            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 10f
            )
            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 10f
            )
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color.Red,
                fillColor = Color.Blue,
                strokeWidth = 10f
            )

            //Polilineas
            val polylinePoints = listOf(
                LatLng(-16.4040102, -71.559611), // Punto inicial
                LatLng(-16.433415, -71.5442652), // JLByR
                LatLng(-16.4205151, -71.4945209), // Paucarpata
                LatLng(-16.3524187, -71.5675994) // Zamacola
            )

            Polyline(
                points = polylinePoints,
                color = Color.Green,
                width = 15f
            )

        }
    }
}

private fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (LatLng) -> Unit
) {
    try {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(LatLng(it.latitude, it.longitude))
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

enum class MapType {
    NORMAL,      // Tipo de mapa normal
    SATELLITE,   // Mapa satelital
    TERRAIN,     // Mapa de terreno
    HYBRID       // Mapa híbrido
}

@Composable
fun MapTypeSelector(
    selectedMapType: MapType,
    onMapTypeSelected: (MapType) -> Unit
) {
    val mapTypes = listOf(
        MapType.NORMAL to "Normal",
        MapType.SATELLITE to "Satélite",
        MapType.TERRAIN to "Terreno",
        MapType.HYBRID to "Híbrido"
    )

    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(text = "Tipo de mapa: ${mapTypes.first { it.first == selectedMapType }.second}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            mapTypes.forEach { (type, name) ->
                DropdownMenuItem(
                    onClick = {
                        onMapTypeSelected(type)
                        expanded = false
                    },
                    text = { Text(text = name) }
                )
            }
        }
    }
}