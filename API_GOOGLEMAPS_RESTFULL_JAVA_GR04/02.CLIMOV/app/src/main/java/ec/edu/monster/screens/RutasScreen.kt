package ec.edu.monster.screens

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import com.google.android.gms.maps.model.*
import ec.edu.monster.model.Sucursal
import ec.edu.monster.service.RestApiService
import ec.edu.monster.ui.theme.Blanco
import ec.edu.monster.ui.theme.RojoPrimario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutasScreen(navController: NavController, usuario: String, codigo: String) {
    val context = LocalContext.current
    val api = remember { RestApiService() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var sucursal by remember { mutableStateOf<Sucursal?>(null) }
    var apiKey by remember { mutableStateOf("") }

    var info by rememberSaveable { mutableStateOf("Cargando...") }

    // modos
    val modos = listOf("Carro", "A pie", "Bicicleta", "Transporte")
    var modoIdx by rememberSaveable { mutableIntStateOf(0) }
    var modoMenu by remember { mutableStateOf(false) }

    // origen
    var pickOrigen by rememberSaveable { mutableStateOf(false) }
    var origLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var origLng by rememberSaveable { mutableStateOf<Double?>(null) }

    // estado mapa
    var mapRef by remember { mutableStateOf<GoogleMap?>(null) }
    var originMarker by remember { mutableStateOf<Marker?>(null) }
    var destMarker by remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }

    // MapView estable
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
            onResume()
        }
    }

    fun modoToDirections(): String = when (modoIdx) {
        1 -> "walking"
        2 -> "bicycling"
        3 -> "transit"
        else -> "driving"
    }

    // --- Helpers Directions ---
    val httpClient = remember {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    suspend fun fetchDirections(
        key: String,
        origin: LatLng,
        destination: LatLng,
        mode: String
    ): Triple<List<LatLng>, String, String>? = withContext(Dispatchers.IO) {
        try {
            val originStr = "${origin.latitude},${origin.longitude}"
            val destStr = "${destination.latitude},${destination.longitude}"

            val url =
                "https://maps.googleapis.com/maps/api/directions/json" +
                        "?origin=${URLEncoder.encode(originStr, "UTF-8")}" +
                        "&destination=${URLEncoder.encode(destStr, "UTF-8")}" +
                        "&mode=${URLEncoder.encode(mode, "UTF-8")}" +
                        "&key=${URLEncoder.encode(key, "UTF-8")}"

            val req = Request.Builder().url(url).get().build()
            val resp = httpClient.newCall(req).execute()
            val body = resp.body?.string().orEmpty()

            if (!resp.isSuccessful) return@withContext null

            val json = JSONObject(body)
            val status = json.optString("status")
            if (status != "OK") {
                // Puedes loguear json.optString("error_message")
                return@withContext null
            }

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext null

            val route0 = routes.getJSONObject(0)
            val overview = route0.getJSONObject("overview_polyline").getString("points")

            val legs = route0.getJSONArray("legs")
            val leg0 = legs.getJSONObject(0)
            val distText = leg0.getJSONObject("distance").getString("text")
            val durText = leg0.getJSONObject("duration").getString("text")

            val points = decodePolyline(overview)
            Triple(points, distText, durText)
        } catch (e: Exception) {
            null
        }
    }

    fun clearRoute() {
        routePolyline?.remove()
        routePolyline = null
    }

    fun updateMarkersAndCamera() {
        val map = mapRef ?: return
        val s = sucursal ?: return
        val dLat = s.latitud
        val dLng = s.longitud
        if (dLat == null || dLng == null) return

        val dest = LatLng(dLat, dLng)

        // destino
        if (destMarker == null) {
            destMarker = map.addMarker(
                MarkerOptions()
                    .position(dest)
                    .title("Destino: ${s.nombre}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        } else {
            destMarker?.position = dest
        }

        // origen (si existe)
        val oLat = origLat
        val oLng = origLng
        if (oLat != null && oLng != null) {
            val orig = LatLng(oLat, oLng)
            if (originMarker == null) {
                originMarker = map.addMarker(
                    MarkerOptions()
                        .position(orig)
                        .title("Origen")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            } else {
                originMarker?.position = orig
            }

            // encuadrar ambos
            val bounds = LatLngBounds.builder()
                .include(orig)
                .include(dest)
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 140))
        } else {
            // solo destino
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 14f))
        }
    }

    // --- cargar data ---
    LaunchedEffect(codigo) {
        isLoading = true
        apiKey = api.obtenerGoogleMapsApiKey()
        if (apiKey.isBlank()) {
            info = "Google Maps API Key no configurada en el servidor."
            isLoading = false
            return@LaunchedEffect
        }
        sucursal = api.obtenerSucursal(codigo)
        isLoading = false

        if (sucursal == null) {
            info = "No se encontró la sucursal."
            return@LaunchedEffect
        }

        info = "Pulsa 'ELEGIR ORIGEN' y toca el mapa."
        clearRoute()
        updateMarkersAndCamera()
    }

    // si cambias modo, borra ruta (para que recalcules)
    LaunchedEffect(modoIdx) {
        clearRoute()
        if (!isLoading) {
            info = "Modo cambiado. Pulsa CALCULAR RUTA."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutas", color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RojoPrimario)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E3A8A), Color(0xFF1E40AF), Color(0xFF312E81))
                    )
                )
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Blanco, modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // panel superior
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = sucursal?.let { "Destino: ${it.nombre} (${it.ciudad})" } ?: "Destino: -",
                            color = Color(0xFF111827)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { modoMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Modo: ${modos[modoIdx]}") }

                                DropdownMenu(
                                    expanded = modoMenu,
                                    onDismissRequest = { modoMenu = false }
                                ) {
                                    modos.forEachIndexed { idx, m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                modoIdx = idx
                                                modoMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    pickOrigen = true
                                    info = "✅ Ahora toca el mapa para seleccionar el ORIGEN."
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                            ) { Text("ELEGIR ORIGEN") }
                        }

                        Button(
                            onClick = {
                                val s = sucursal
                                val map = mapRef

                                if (map == null) {
                                    info = "Mapa no listo aún."
                                    return@Button
                                }
                                if (s?.latitud == null || s.longitud == null) {
                                    info = "La sucursal no tiene coordenadas."
                                    return@Button
                                }
                                val oLat = origLat
                                val oLng = origLng
                                if (oLat == null || oLng == null) {
                                    info = "Selecciona un origen en el mapa."
                                    return@Button
                                }

                                val origin = LatLng(oLat, oLng)
                                val dest = LatLng(s.latitud!!, s.longitud!!)
                                val mode = modoToDirections()

                                info = "Calculando ruta..."
                                clearRoute()

                                scope.launch {
                                    val res = fetchDirections(apiKey, origin, dest, mode)
                                    if (res == null) {
                                        info = "No se encontró ruta."
                                        return@launch
                                    }

                                    val (points, distText, durText) = res

                                    // dibuja polyline
                                    routePolyline = map.addPolyline(
                                        PolylineOptions()
                                            .addAll(points)
                                            .width(12f)
                                            .color(AColor.BLUE)
                                            .geodesic(true)
                                    )

                                    // encuadrar
                                    val boundsBuilder = LatLngBounds.builder()
                                    points.forEach { boundsBuilder.include(it) }
                                    val bounds = boundsBuilder.build()
                                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 140))

                                    info = "Distancia: $distText | Tiempo: $durText"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                        ) { Text("CALCULAR RUTA") }

                        Text(info, color = Color(0xFF6B7280))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // mapa (MapView)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            mapView.getMapAsync { map ->
                                mapRef = map

                                // click para origen
                                map.setOnMapClickListener { p ->
                                    if (!pickOrigen) return@setOnMapClickListener

                                    origLat = p.latitude
                                    origLng = p.longitude
                                    pickOrigen = false

                                    // marker origen
                                    originMarker?.remove()
                                    originMarker = map.addMarker(
                                        MarkerOptions()
                                            .position(p)
                                            .title("Origen")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    )

                                    clearRoute()
                                    info = "Origen seleccionado: ${p.latitude}, ${p.longitude}. Pulsa CALCULAR RUTA."
                                    updateMarkersAndCamera()
                                }

                                // al iniciar: markers + cámara
                                updateMarkersAndCamera()
                            }
                            mapView
                        },
                        update = {
                            // cuando recomponen, intentamos mantener cámara/markers
                            updateMarkersAndCamera()
                        }
                    )
                }
            }
        }
    }
}
