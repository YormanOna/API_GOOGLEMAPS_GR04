package ec.edu.monster.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ec.edu.monster.screens.*


@Composable
fun NavigationGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable(
            route = "menu/{usuario}",
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            MenuScreen(navController = navController, usuario = usuario)
        }
        
        composable(
            route = "movimientos/{usuario}",
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            MovimientosScreen(navController = navController, usuario = usuario)
        }
        
        composable(
            route = "deposito/{usuario}",
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            DepositoScreen(navController = navController, usuario = usuario)
        }
        
        composable(
            route = "retiro/{usuario}",
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            RetiroScreen(navController = navController, usuario = usuario)
        }
        
        composable(
            route = "transferencia/{usuario}",
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            TransferenciaScreen(navController = navController, usuario = usuario)
        }


        composable(
            route = "sucursales/{usuario}",
            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            SucursalesScreen(navController = navController, usuario = usuario)
        }

        composable(
            route = "sucursalDetalle/{usuario}/{codigo}",
            arguments = listOf(
                navArgument("usuario") { type = NavType.StringType },
                navArgument("codigo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
            SucursalDetalleScreen(navController = navController, usuario = usuario, codigo = codigo)
        }


        // Edit: permite crear (codigo vacío) o editar (codigo viene)
        composable(
            route = "sucursalEdit/{usuario}?codigo={codigo}",
            arguments = listOf(
                navArgument("usuario") { type = NavType.StringType },
                navArgument("codigo") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
            val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
            SucursalEditScreen(navController = navController, usuario = usuario, codigo = codigo)
        }


        composable(
            route = "rutas/{usuario}/{codigo}",
            arguments = listOf(
                navArgument("usuario") { type = NavType.StringType },
                navArgument("codigo") { type = NavType.StringType }
            )
        ) { back ->
            val usuario = back.arguments?.getString("usuario") ?: ""
            val codigo = back.arguments?.getString("codigo") ?: ""
            RutasScreen(navController = navController, usuario = usuario, codigo = codigo)
        }

        composable(
            route = "ubicacionPicker?lat={lat}&lng={lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType; defaultValue = "" },
                navArgument("lng") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val lat = back.arguments?.getString("lat").orEmpty()
            val lng = back.arguments?.getString("lng").orEmpty()
            UbicacionPickerScreen(navController = navController, lat = lat, lng = lng)
        }



    }
}
