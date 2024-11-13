package com.example.maps

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker


@Composable
fun MapScreen() {
    val context = LocalContext.current
    val ArequipaLocation = LatLng(-16.4040102, -71.559611) // Arequipa, Perú
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(ArequipaLocation, 12f)
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // Añadir GoogleMap al layout
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {

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

            LaunchedEffect(Unit) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.2520984,-71.6836503), 12f), // Mover a Yura
                    durationMs = 3000
                )
            }
        }
    }
}