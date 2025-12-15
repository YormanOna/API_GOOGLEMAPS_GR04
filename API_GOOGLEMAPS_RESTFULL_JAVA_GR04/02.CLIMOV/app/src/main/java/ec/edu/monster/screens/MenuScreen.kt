package ec.edu.monster.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import ec.edu.monster.R
import ec.edu.monster.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController, usuario: String) {
    val bg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F1729),
            Color(0xFF12204A),
            Color(0xFF0D47A1)
        )
    )

    val opciones = listOf(
        MenuItem(
            titulo = "Consultar\nMovimientos",
            icono = Icons.Default.BarChart,
            gradiente = listOf(Color(0xFF9C1818), Color(0xFF1565C0)),
            onClick = { navController.navigate("movimientos/$usuario") },
            fullWidth = false
        ),
        MenuItem(
            titulo = "Depósito",
            icono = Icons.Default.AccountBalance,
            gradiente = listOf(Color(0xFF5B1885), Color(0xFF1565C0)),
            onClick = { navController.navigate("deposito/$usuario") },
            fullWidth = false
        ),
        MenuItem(
            titulo = "Retiro",
            icono = Icons.Default.Savings,
            gradiente = listOf(Color(0xFF6B2884), Color(0xFF1565C0)),
            onClick = { navController.navigate("retiro/$usuario") },
            fullWidth = false
        ),
        MenuItem(
            titulo = "Transferencia",
            icono = Icons.Default.CreditCard,
            gradiente = listOf(Color(0xFF7A3787), Color(0xFF1565C0)),
            onClick = { navController.navigate("transferencia/$usuario") },
            fullWidth = false
        ),
        MenuItem(
            titulo = "Sucursales",
            icono = Icons.Default.Business,
            gradiente = listOf(Color(0xFFB71C1C), Color(0xFF1565C0)),
            onClick = { navController.navigate("sucursales/$usuario") },
            fullWidth = true
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.monster),
                            contentDescription = "Monster",
                            modifier = Modifier.size(36.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MONSTER",
                            color = Blanco,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RojoPrimario)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(paddingValues)
        ) {
            // decorativos más sutiles
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(x = (-80).dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(Color(0x1A673AB7))
            )
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 120.dp, y = 120.dp)
                    .clip(CircleShape)
                    .background(Color(0x14512DA8))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EurekaBank",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tu banco, tu equipo.",
                    fontSize = 15.sp,
                    color = Color(0xFFB0BEC5)
                )

                Spacer(modifier = Modifier.height(22.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = opciones,
                        span = { item ->
                            if (item.fullWidth) GridItemSpan(2) else GridItemSpan(1)
                        }
                    ) { item ->
                        CardOpcionMejorada(
                            titulo = item.titulo,
                            icono = item.icono,
                            gradiente = item.gradiente,
                            onClick = item.onClick,
                            fullWidth = item.fullWidth
                        )
                    }
                }
            }
        }
    }
}

private data class MenuItem(
    val titulo: String,
    val icono: ImageVector,
    val gradiente: List<Color>,
    val onClick: () -> Unit,
    val fullWidth: Boolean
)

@Composable
fun CardOpcionMejorada(
    titulo: String,
    icono: ImageVector,
    gradiente: List<Color>,
    onClick: () -> Unit,
    fullWidth: Boolean
) {
    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (fullWidth) 120.dp else 160.dp)
            .clickable(onClick = onClick),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradiente))
                .padding(16.dp)
        ) {
            // Icono
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = Blanco,
                modifier = Modifier
                    .size(if (fullWidth) 46.dp else 52.dp)
                    .align(Alignment.TopStart)
            )

            // Texto
            Text(
                text = titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Blanco,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(top = 12.dp)
            )
        }
    }
}
