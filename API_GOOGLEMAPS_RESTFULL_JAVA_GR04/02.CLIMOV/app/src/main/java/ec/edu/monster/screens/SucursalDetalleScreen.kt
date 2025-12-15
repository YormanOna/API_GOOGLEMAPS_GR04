package ec.edu.monster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ec.edu.monster.components.DialogoError
import ec.edu.monster.model.Sucursal
import ec.edu.monster.service.RestApiService
import ec.edu.monster.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucursalDetalleScreen(navController: NavController, usuario: String, codigo: String) {
    var isLoading by remember { mutableStateOf(false) }
    var sucursal by remember { mutableStateOf<Sucursal?>(null) }

    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val api = remember { RestApiService() }

    LaunchedEffect(codigo) {
        isLoading = true
        val s = api.obtenerSucursal(codigo)
        isLoading = false
        if (s == null) {
            mensajeError = "No se encontró la sucursal."
            mostrarError = true
        } else {
            sucursal = s
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle Sucursal", color = Blanco) },
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
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Blanco)
                return@Box
            }

            val s = sucursal ?: return@Box

            Card(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column {
                    // header morado como MAUI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF8B5CF6))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(s.nombre, color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Código: ${s.codigo}", color = Blanco.copy(alpha = 0.9f), fontSize = 13.sp)
                        }
                    }

                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ciudad: ${s.ciudad}", color = Color(0xFF374151))
                        Text("Dirección: ${s.direccion}", color = Color(0xFF374151))
                        Text("Contador Cuenta: ${s.contadorCuentas}", color = Color(0xFF374151))

                        val lat = s.latitud?.toString() ?: "-"
                        val lng = s.longitud?.toString() ?: "-"
                        Text("Coords: $lat, $lng", color = Color(0xFF374151))

                        Spacer(Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { navController.navigate("sucursalEdit/$usuario?codigo=${s.codigo}") },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("EDITAR", fontWeight = FontWeight.Bold) }

                            Button(
                                onClick = { navController.navigate("rutas/$usuario/${s.codigo}") },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("RUTAS / INDICACIONES", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }

    if (mostrarError) {
        DialogoError(
            mensaje = mensajeError,
            mensajeAdvertencia = "Regresa al listado e intenta otra vez.",
            onDismiss = { mostrarError = false; navController.popBackStack() }
        )
    }
}
