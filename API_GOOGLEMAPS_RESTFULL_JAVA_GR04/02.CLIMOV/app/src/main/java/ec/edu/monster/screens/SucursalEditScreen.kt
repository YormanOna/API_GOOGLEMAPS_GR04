package ec.edu.monster.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ec.edu.monster.components.DialogoError
import ec.edu.monster.components.DialogoExito
import ec.edu.monster.model.Sucursal
import ec.edu.monster.service.RestApiService
import ec.edu.monster.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucursalEditScreen(navController: NavController, usuario: String, codigo: String) {
    val esEdicion = codigo.isNotBlank()

    var isLoading by remember { mutableStateOf(false) }

    // ✅ IMPORTANTE: rememberSaveable para que NO se borre al navegar al mapa y volver
    var cod by rememberSaveable { mutableStateOf(codigo) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var ciudad by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf("") }
    var lng by rememberSaveable { mutableStateOf("") }

    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    var mostrarExito by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf("") }

    val api = remember { RestApiService() }
    val scope = rememberCoroutineScope()

    // ✅ evita que al volver del mapa vuelvas a pisar los datos con lo del server
    var loadedFromServer by rememberSaveable { mutableStateOf(false) }

    fun parseDoubleOrNull(s: String): Double? {
        val t = s.trim().replace(",", ".")
        if (t.isBlank()) return null
        return t.toDoubleOrNull()
    }

    // ✅ cargar del server SOLO UNA VEZ
    LaunchedEffect(esEdicion, codigo) {
        if (!esEdicion) return@LaunchedEffect
        if (loadedFromServer) return@LaunchedEffect

        isLoading = true
        val s = api.obtenerSucursal(codigo)
        isLoading = false

        if (s == null) {
            mensajeError = "No se pudo cargar la sucursal."
            mostrarError = true
        } else {
            cod = s.codigo
            nombre = s.nombre
            ciudad = s.ciudad
            direccion = s.direccion
            lat = s.latitud?.toString() ?: ""
            lng = s.longitud?.toString() ?: ""
            loadedFromServer = true
        }
    }

    // ✅ escuchar los valores que vienen del mapa (previousBackStackEntry -> currentBackStackEntry)
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("picked_ciudad", "").collect { v ->
            if (v.isNotBlank()) ciudad = v
            handle["picked_ciudad"] = ""
        }
    }

    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("picked_direccion", "").collect { v ->
            if (v.isNotBlank()) direccion = v
            handle["picked_direccion"] = ""
        }
    }

    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("picked_lat", "").collect { v ->
            if (v.isNotBlank()) lat = v
            handle["picked_lat"] = ""
        }
    }

    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow("picked_lng", "").collect { v ->
            if (v.isNotBlank()) lng = v
            handle["picked_lng"] = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Sucursal" else "Nueva Sucursal", color = Blanco) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Blanco)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    Text(
                        text = "Completa los datos y guarda",
                        color = Gris,
                        fontWeight = FontWeight.Bold
                    )

                    val tfColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulMedio,
                        unfocusedBorderColor = GrisClaro,
                        focusedLabelColor = AzulMedio,
                        unfocusedLabelColor = Gris,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = AzulMedio
                    )


                    OutlinedTextField(
                        value = cod,
                        onValueChange = { cod = it },
                        label = { Text("Código (máx 3)") },
                        enabled = !esEdicion,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = tfColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ciudad,
                        onValueChange = { ciudad = it },
                        label = { Text("Ciudad") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección (max 50)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = tfColors,
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = lat,
                            onValueChange = { lat = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = tfColors,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lng,
                            onValueChange = { lng = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = tfColors,
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            navController.navigate("ubicacionPicker?lat=${lat.trim()}&lng=${lng.trim()}")
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ELEGIR EN MAPA", fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                val c = cod.trim()
                                val n = nombre.trim()
                                val ci = ciudad.trim()

                                if (c.isBlank() || n.isBlank() || ci.isBlank()) {
                                    mensajeError = "Código, Nombre y Ciudad son obligatorios."
                                    mostrarError = true
                                    return@Button
                                }
                                if (c.length > 3) {
                                    mensajeError = "El código debe tener máximo 3 caracteres."
                                    mostrarError = true
                                    return@Button
                                }

                                val latV = parseDoubleOrNull(lat)
                                val lngV = parseDoubleOrNull(lng)

                                val dir = direccion.trim().take(50)

                                val req = Sucursal(
                                    codigo = c,
                                    nombre = n,
                                    ciudad = ci,
                                    direccion = dir,
                                    contadorCuentas = 0,
                                    latitud = latV,
                                    longitud = lngV,
                                    active = true
                                )

                                scope.launch {
                                    isLoading = true
                                    val ok = if (esEdicion) {
                                        api.actualizarSucursal(codigo, req) != null
                                    } else {
                                        api.crearSucursal(req) != null
                                    }
                                    isLoading = false

                                    if (ok) {
                                        mensajeExito = if (esEdicion) "Sucursal actualizada." else "Sucursal creada."
                                        mostrarExito = true
                                    } else {
                                        mensajeError = "No se pudo guardar la sucursal."
                                        mostrarError = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Blanco, modifier = Modifier.size(22.dp))
                            } else {
                                Text("GUARDAR", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isLoading
                        ) {
                            Text("CANCELAR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (mostrarExito) {
        DialogoExito(
            mensaje = mensajeExito,
            onDismiss = {
                mostrarExito = false
                navController.popBackStack()
            }
        )
    }

    if (mostrarError) {
        DialogoError(
            mensaje = mensajeError,
            mensajeAdvertencia = "Revisa los datos e intenta nuevamente.",
            onDismiss = { mostrarError = false }
        )
    }
}
