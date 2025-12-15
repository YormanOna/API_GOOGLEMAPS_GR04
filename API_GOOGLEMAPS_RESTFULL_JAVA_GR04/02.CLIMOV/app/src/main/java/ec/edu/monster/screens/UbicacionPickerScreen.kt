package ec.edu.monster.screens

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ec.edu.monster.ui.theme.Blanco
import ec.edu.monster.ui.theme.RojoPrimario
import android.location.Geocoder
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbicacionPickerScreen(
    navController: NavController,
    lat: String,
    lng: String
) {
    val context = LocalContext.current

    val startLat = lat.toDoubleOrNull() ?: 4.710989
    val startLng = lng.toDoubleOrNull() ?: -74.072092
    val startPos = LatLng(startLat, startLng)
    var pickedCiudad by remember { mutableStateOf("") }
    var pickedDireccion by remember { mutableStateOf("") }

    var pickedLat by remember { mutableStateOf<Double?>(null) }
    var pickedLng by remember { mutableStateOf<Double?>(null) }

    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
            onResume()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elegir ubicación", color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RojoPrimario)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF))
                    )
                )
                .padding(padding)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Lat: ${pickedLat ?: "-"}")
                    Text("Lng: ${pickedLng ?: "-"}")

                    Button(
                        enabled = pickedLat != null,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_lat", pickedLat.toString())

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_lng", pickedLng.toString())

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_ciudad", pickedCiudad)

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("picked_direccion", pickedDireccion)

                            navController.popBackStack()
                        }
                    ) {
                        Text("USAR ESTA UBICACIÓN")
                    }

                }
            }

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        mapView.getMapAsync { map: GoogleMap ->
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(startPos, 15f)
                            )

                            map.setOnMapClickListener { p ->
                                map.clear()
                                map.addMarker(MarkerOptions().position(p))

                                pickedLat = p.latitude
                                pickedLng = p.longitude

                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val results = geocoder.getFromLocation(p.latitude, p.longitude, 1)

                                    if (!results.isNullOrEmpty()) {
                                        val addr = results[0]

                                        pickedCiudad = addr.locality
                                            ?: addr.subAdminArea
                                                    ?: addr.adminArea
                                                    ?: ""

                                        pickedDireccion = listOfNotNull(
                                            addr.thoroughfare,
                                            addr.subThoroughfare
                                        ).joinToString(" ")
                                    }
                                } catch (e: Exception) {
                                    pickedCiudad = ""
                                    pickedDireccion = ""
                                }
                            }

                        }
                        mapView
                    }
                )
            }
        }
    }
}
