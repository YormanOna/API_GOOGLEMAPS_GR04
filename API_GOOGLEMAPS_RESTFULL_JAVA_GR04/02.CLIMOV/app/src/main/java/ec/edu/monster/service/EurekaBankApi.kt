package ec.edu.monster.service

import ec.edu.monster.model.LoginResponse
import ec.edu.monster.model.Movimiento
import ec.edu.monster.model.OperacionCuentaResponse
import ec.edu.monster.model.Sucursal
import retrofit2.Response
import retrofit2.http.*
import ec.edu.monster.model.GoogleMapsConfigResponse
/**
 * API Interface para el servicio RESTful de EurekaBank
 */
interface EurekaBankApi {
    
    @POST("corebancario/login")
    suspend fun login(
        @Query("usuario") usuario: String,
        @Query("password") password: String
    ): Response<LoginResponse>
    
    @POST("corebancario/deposito")
    suspend fun registrarDeposito(
        @Query("cuenta") cuenta: String,
        @Query("importe") importe: Double
    ): Response<OperacionCuentaResponse>
    
    @POST("corebancario/retiro")
    suspend fun registrarRetiro(
        @Query("cuenta") cuenta: String,
        @Query("importe") importe: Double
    ): Response<OperacionCuentaResponse>
    
    @POST("corebancario/transferencia")
    suspend fun registrarTransferencia(
        @Query("cuentaOrigen") cuentaOrigen: String,
        @Query("cuentaDestino") cuentaDestino: String,
        @Query("importe") importe: Double
    ): Response<OperacionCuentaResponse>
    
    @GET("corebancario/movimientos/{cuenta}")
    suspend fun obtenerMovimientos(
        @Path("cuenta") cuenta: String
    ): Response<List<Movimiento>>
    
    @GET("corebancario/ping")
    suspend fun ping(): Response<Map<String, String>>



    // =========================
    // CRUD SUCURSALES
    // =========================

    @GET("corebancario/sucursales")
    suspend fun listarSucursales(): Response<List<Sucursal>>

    @GET("corebancario/sucursales/{codigo}")
    suspend fun obtenerSucursal(
        @Path("codigo") codigo: String
    ): Response<Sucursal>

    @POST("corebancario/sucursales")
    suspend fun crearSucursal(
        @Body sucursal: Sucursal
    ): Response<Sucursal>

    @PUT("corebancario/sucursales/{codigo}")
    suspend fun actualizarSucursal(
        @Path("codigo") codigo: String,
        @Body sucursal: Sucursal
    ): Response<Sucursal>

    @DELETE("corebancario/sucursales/{codigo}")
    suspend fun eliminarSucursal(
        @Path("codigo") codigo: String
    ): Response<Map<String, String>>


    // Google Maps config (API KEY)
    @GET("corebancario/config/googlemaps")
    suspend fun googleMapsConfig(): Response<GoogleMapsConfigResponse>

}
