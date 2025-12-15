package ec.edu.monster.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import ec.edu.monster.components.DialogoExito
import ec.edu.monster.model.Sucursal
import ec.edu.monster.service.RestApiService
import ec.edu.monster.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.RowScope



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SucursalesScreen(navController: NavController, usuario: String) {
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var data by remember { mutableStateOf<List<Sucursal>>(emptyList()) }
    val filtrado = remember(query, data) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) data else data.filter {
            it.codigo.lowercase().contains(q) ||
                    it.nombre.lowercase().contains(q) ||
                    it.ciudad.lowercase().contains(q)
        }
    }

    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    var mostrarExito by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf("") }

    var confirmarEliminar by remember { mutableStateOf(false) }
    var codigoAEliminar by remember { mutableStateOf("") }

    val api = remember { RestApiService() }

    suspend fun cargar() {
        isLoading = true
        val res = api.listarSucursales()
        isLoading = false
        data = res
    }

    LaunchedEffect(Unit) { cargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sucursales", color = Blanco) },
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
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F1729), Color(0xFF1A237E), Color(0xFF0D47A1))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Listado y administración",
                            fontSize = 14.sp,
                            color = Gris,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Buscar") },
                            placeholder = { Text("Código, nombre o ciudad") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulMedio,
                                unfocusedBorderColor = GrisClaro,
                                focusedLabelColor = AzulMedio,
                                unfocusedTextColor = Color(0xFF212121),
                                focusedTextColor = Color(0xFF000000)
                            ),
                            singleLine = true
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { navController.navigate("sucursalEdit/$usuario?codigo=") },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RojoPrimario),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("NUEVA SUCURSAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { query = ""; /* recargar */ },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                Text("REFRESCAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                ) {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (filtrado.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay sucursales para mostrar.", color = Gris)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtrado) { s ->
                                SucursalItem(
                                    sucursal = s,
                                    onDetalle = {
                                        navController.navigate("sucursalDetalle/$usuario/${s.codigo}")
                                    },
                                    onEditar = {
                                        navController.navigate("sucursalEdit/$usuario?codigo=${s.codigo}")
                                    },
                                    onRutas = {
                                        navController.navigate("rutas/$usuario/${s.codigo}")
                                    },
                                    onEliminar = {
                                        codigoAEliminar = s.codigo
                                        confirmarEliminar = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Mejor: eliminar con coroutine normal
    val scope = rememberCoroutineScope()
    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("Confirmar") },
            text = { Text("¿Deseas eliminar la sucursal $codigoAEliminar?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    scope.launch {
                        isLoading = true
                        val ok = api.eliminarSucursal(codigoAEliminar)
                        isLoading = false

                        if (ok) {
                            mensajeExito = "Sucursal eliminada."
                            mostrarExito = true
                            scope.launch {
                                cargar()
                            }

                        } else {
                            mensajeError = "No se pudo eliminar la sucursal."
                            mostrarError = true
                        }
                    }
                }) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarExito) {
        DialogoExito(
            mensaje = mensajeExito,
            onDismiss = { mostrarExito = false }
        )
    }

    if (mostrarError) {
        DialogoError(
            mensaje = mensajeError,
            mensajeAdvertencia = "Verifica e intenta nuevamente.",
            onDismiss = { mostrarError = false }
        )
    }
}

@Composable
private fun SucursalItem(
    sucursal: Sucursal,
    onDetalle: () -> Unit,
    onEditar: () -> Unit,
    onRutas: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF1E40AF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏦", fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                        text = sucursal.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Código: ${sucursal.codigo}", fontSize = 12.sp, color = Gris)
                        Text("Ciudad: ${sucursal.ciudad}", fontSize = 12.sp, color = Gris)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Dirección: ${sucursal.direccion}",
                fontSize = 12.sp,
                color = Color(0xFF374151),
                maxLines = 1
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniBtn("ℹ️", Color(0xFF1E40AF), onDetalle)
                MiniBtn("✏️", Color(0xFFF59E0B), onEditar)
                MiniBtn("🗺️", Color(0xFF10B981), onRutas)
                MiniBtn("🗑️", RojoPrimario, onEliminar)
            }
        }
    }
}

@Composable
private fun RowScope.MiniBtn(text: String, bg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 18.sp)
    }
}
