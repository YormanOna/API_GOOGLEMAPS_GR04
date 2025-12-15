package ec.edu.monster.service

import ec.edu.monster.model.Movimiento
import ec.edu.monster.model.OperacionCuentaResponse
import ec.edu.monster.model.Sucursal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RestApiService {

    // URL del servidor RESTful
    // 10.0.2.2 es la IP especial del emulador para acceder a localhost de tu PC
    // Si usas dispositivo físico, cambia a tu IP local (ej: 192.168.1.100)
    private val baseUrl = "http://192.168.0.105:8080/WSEurekaBank_Restfull_Java_G4/resources/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(EurekaBankApi::class.java)

    /**
     * Validar ingreso de usuario
     */
    suspend fun validarIngreso(usuario: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            println("🌐 Conectando a: $baseUrl")
            println("👤 Usuario: $usuario")

            val response = api.login(usuario, password)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                val resultado = loginResponse?.resultado ?: "Denegado"
                println("✅ Respuesta: $resultado")
                resultado.equals("Exitoso", ignoreCase = true)
            } else {
                println("❌ Error HTTP: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            println("❌ Error en validarIngreso: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Traer movimientos de una cuenta
     */
    suspend fun traerMovimientos(cuenta: String): List<Movimiento> = withContext(Dispatchers.IO) {
        try {
            println("📊 ===== CONSULTANDO MOVIMIENTOS =====")
            println("📋 Cuenta: $cuenta")

            val response = api.obtenerMovimientos(cuenta)

            if (response.isSuccessful) {
                val movimientos = response.body() ?: emptyList()
                println("✅ Total movimientos encontrados: ${movimientos.size}")
                println("📊 ===== FIN CONSULTA MOVIMIENTOS =====")
                movimientos
            } else {
                println("❌ Error HTTP: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ ERROR consultando movimientos: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Registrar depósito
     */
    suspend fun registrarDeposito(cuenta: String, monto: Double): OperacionCuentaResponse = withContext(Dispatchers.IO) {
        try {
            println("🏦 ===== INICIANDO DEPÓSITO =====")
            println("📋 Cuenta: $cuenta")
            println("💵 Monto: $monto")

            val response = api.registrarDeposito(cuenta, monto)

            if (response.isSuccessful) {
                val operacionResponse = response.body() ?: OperacionCuentaResponse(estado = -1, saldo = 0.0)
                println("✅ Estado: ${operacionResponse.estado}, Saldo: ${operacionResponse.saldo}")
                println("🏦 ===== FIN DEPÓSITO =====")
                operacionResponse
            } else {
                println("❌ Error HTTP: ${response.code()}")
                OperacionCuentaResponse(estado = -1, saldo = 0.0)
            }
        } catch (e: Exception) {
            println("❌ ERROR en depósito: ${e.message}")
            e.printStackTrace()
            OperacionCuentaResponse(estado = -1, saldo = 0.0)
        }
    }

    /**
     * Registrar retiro
     */
    suspend fun registrarRetiro(cuenta: String, monto: Double): OperacionCuentaResponse = withContext(Dispatchers.IO) {
        try {
            println("💸 ===== INICIANDO RETIRO =====")
            println("📋 Cuenta: $cuenta")
            println("💵 Monto: $monto")

            val response = api.registrarRetiro(cuenta, monto)

            if (response.isSuccessful) {
                val operacionResponse = response.body() ?: OperacionCuentaResponse(estado = -1, saldo = 0.0)
                println("✅ Estado: ${operacionResponse.estado}, Saldo: ${operacionResponse.saldo}")
                println("💸 ===== FIN RETIRO =====")
                operacionResponse
            } else {
                println("❌ Error HTTP: ${response.code()}")
                OperacionCuentaResponse(estado = -1, saldo = 0.0)
            }
        } catch (e: Exception) {
            println("❌ ERROR en retiro: ${e.message}")
            e.printStackTrace()
            OperacionCuentaResponse(estado = -1, saldo = 0.0)
        }
    }

    /**
     * Registrar transferencia
     */
    suspend fun registrarTransferencia(
        cuentaOrigen: String,
        cuentaDestino: String,
        monto: Double
    ): OperacionCuentaResponse = withContext(Dispatchers.IO) {
        try {
            println("💱 ===== INICIANDO TRANSFERENCIA =====")
            println("📋 Cuenta Origen: $cuentaOrigen")
            println("📋 Cuenta Destino: $cuentaDestino")
            println("💵 Monto: $monto")

            val response = api.registrarTransferencia(cuentaOrigen, cuentaDestino, monto)

            if (response.isSuccessful) {
                val operacionResponse = response.body() ?: OperacionCuentaResponse(estado = -1, saldo = 0.0)
                println("✅ Estado: ${operacionResponse.estado}, Saldo: ${operacionResponse.saldo}")
                println("💱 ===== FIN TRANSFERENCIA =====")
                operacionResponse
            } else {
                println("❌ Error HTTP: ${response.code()}")
                OperacionCuentaResponse(estado = -1, saldo = 0.0)
            }
        } catch (e: Exception) {
            println("❌ ERROR en transferencia: ${e.message}")
            e.printStackTrace()
            OperacionCuentaResponse(estado = -1, saldo = 0.0)
        }
    }










    // =========================
    // SUCURSALES
    // =========================

    /**
     * Listar sucursales activas
     */
    suspend fun listarSucursales(): List<Sucursal> = withContext(Dispatchers.IO) {
        try {
            println("🏢 ===== LISTANDO SUCURSALES =====")
            val response = api.listarSucursales()

            if (response.isSuccessful) {
                val sucursales = response.body() ?: emptyList()
                println("✅ Total sucursales: ${sucursales.size}")
                println("🏢 ===== FIN LISTADO SUCURSALES =====")
                sucursales
            } else {
                println("❌ Error HTTP listarSucursales: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ ERROR listarSucursales: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Obtener sucursal por código
     */
    suspend fun obtenerSucursal(codigo: String): Sucursal? = withContext(Dispatchers.IO) {
        try {
            println("🔎 ===== OBTENIENDO SUCURSAL =====")
            println("📌 Código: $codigo")

            val response = api.obtenerSucursal(codigo)

            if (response.isSuccessful) {
                val sucursal = response.body()
                println("✅ Sucursal: ${sucursal?.nombre ?: "(null)"}")
                println("🔎 ===== FIN OBTENER SUCURSAL =====")
                sucursal
            } else {
                println("❌ Error HTTP obtenerSucursal: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("❌ ERROR obtenerSucursal: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Crear sucursal
     */
    suspend fun crearSucursal(sucursal: Sucursal): Sucursal? = withContext(Dispatchers.IO) {
        try {
            println("➕ ===== CREANDO SUCURSAL =====")
            println("📌 Código: ${sucursal.codigo} | Nombre: ${sucursal.nombre}")

            val response = api.crearSucursal(sucursal)

            if (response.isSuccessful) {
                val nueva = response.body()
                println("✅ Creada: ${nueva?.codigo} - ${nueva?.nombre}")
                println("➕ ===== FIN CREAR SUCURSAL =====")
                nueva
            } else {
                println("❌ Error HTTP crearSucursal: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("❌ ERROR crearSucursal: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Actualizar sucursal
     */
    suspend fun actualizarSucursal(codigo: String, sucursal: Sucursal): Sucursal? = withContext(Dispatchers.IO) {
        try {
            println("✏️ ===== ACTUALIZANDO SUCURSAL =====")
            println("📌 Código: $codigo | Nombre: ${sucursal.nombre}")

            val response = api.actualizarSucursal(codigo, sucursal)

            if (response.isSuccessful) {
                val actualizada = response.body()
                println("✅ Actualizada: ${actualizada?.codigo} - ${actualizada?.nombre}")
                println("✏️ ===== FIN ACTUALIZAR SUCURSAL =====")
                actualizada
            } else {
                println("❌ Error HTTP actualizarSucursal: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("❌ ERROR actualizarSucursal: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Eliminar sucursal (soft delete)
     */
    suspend fun eliminarSucursal(codigo: String): Boolean = withContext(Dispatchers.IO) {
        try {
            println("🗑️ ===== ELIMINANDO SUCURSAL =====")
            println("📌 Código: $codigo")

            val response = api.eliminarSucursal(codigo)

            if (response.isSuccessful) {
                val body = response.body()
                println("✅ Respuesta: ${body ?: emptyMap<String, String>()}")
                println("🗑️ ===== FIN ELIMINAR SUCURSAL =====")
                true
            } else {
                println("❌ Error HTTP eliminarSucursal: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            println("❌ ERROR eliminarSucursal: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerGoogleMapsApiKey(): String = withContext(Dispatchers.IO) {
        try {
            val response = api.googleMapsConfig()
            if (!response.isSuccessful) return@withContext ""

            val resp = response.body() ?: return@withContext ""
            if (!resp.configured || resp.apiKey == "API_KEY_NOT_CONFIGURED") "" else resp.apiKey
        } catch (e: Exception) {
            ""
        }
    }


}
