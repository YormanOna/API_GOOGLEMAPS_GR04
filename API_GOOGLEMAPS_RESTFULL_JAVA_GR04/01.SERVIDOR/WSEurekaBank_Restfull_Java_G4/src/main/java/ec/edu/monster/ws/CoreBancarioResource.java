package ec.edu.monster.ws;

import ec.edu.monster.modelo.Movimiento;
import ec.edu.monster.modelo.OperacionCuentaResponse;
import ec.edu.monster.servicio.EurekaService;
import ec.edu.monster.util.GoogleMapsConfig;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("corebancario")
@Produces(MediaType.APPLICATION_JSON)
public class CoreBancarioResource {

    private final EurekaService service = new EurekaService();

    // ========= LOGIN =========
    @POST
    @Path("login")
    public Response login(@QueryParam("usuario") String usuario,
            @QueryParam("password") String password) {
        boolean ok = service.validarIngreso(usuario, password);
        String resultado = ok ? "Exitoso" : "Denegado";
        return Response.ok("{\"resultado\":\"" + resultado + "\"}").build();
        // Si prefieres, puedes devolver un objeto con booleano
    }

    // ========= DEPÓSITO =========
    @POST
    @Path("deposito")
    public Response registrarDeposito(@QueryParam("cuenta") String cuenta,
            @QueryParam("importe") double importe) {

        String codEmp = "0001";
        OperacionCuentaResponse resp = new OperacionCuentaResponse();

        try {
            double saldo = service.registrarDeposito(cuenta, importe, codEmp);
            resp.setEstado(1);
            resp.setSaldo(saldo);
            return Response.ok(resp).build();
        } catch (Exception e) {
            resp.setEstado(-1);
            resp.setSaldo(-1);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(resp)
                    .build();
        }
    }

    // ========= RETIRO =========
    @POST
    @Path("retiro")
    public Response registrarRetiro(@QueryParam("cuenta") String cuenta,
            @QueryParam("importe") double importe) {

        String codEmp = "0004";
        OperacionCuentaResponse resp = new OperacionCuentaResponse();

        try {
            double saldo = service.registrarRetiro(cuenta, importe, codEmp);
            resp.setEstado(1);
            resp.setSaldo(saldo);
            return Response.ok(resp).build();
        } catch (Exception e) {
            resp.setEstado(-1);
            resp.setSaldo(-1);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(resp)
                    .build();
        }
    }

    // ========= TRANSFERENCIA =========
    @POST
    @Path("transferencia")
    public Response registrarTransferencia(@QueryParam("cuentaOrigen") String cuentaOrigen,
            @QueryParam("cuentaDestino") String cuentaDestino,
            @QueryParam("importe") double importe) {

        String codEmp = "0004";
        OperacionCuentaResponse resp = new OperacionCuentaResponse();

        try {
            double saldoOrigen = service.registrarTransferencia(cuentaOrigen, cuentaDestino, importe, codEmp);
            resp.setEstado(1);
            resp.setSaldo(saldoOrigen);
            return Response.ok(resp).build();
        } catch (Exception e) {
            resp.setEstado(-1);
            resp.setSaldo(-1);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(resp)
                    .build();
        }
    }

    // ========= MOVIMIENTOS =========
    @GET
    @Path("movimientos/{cuenta}")
    public Response obtenerMovimientos(@PathParam("cuenta") String cuenta) {
        try {
            List<Movimiento> movimientos = service.leerMovimientos(cuenta);
            return Response.ok(movimientos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("ping")
    public Response ping() {
        return Response.ok("{\"msg\":\"ok\"}").build();
    }

    // ========= CRUD SUCURSALES =========
    
    /**
     * Lista todas las sucursales activas
     * GET /api/corebancario/sucursales
     */
    @GET
    @Path("sucursales")
    public Response listarSucursales() {
        try {
            List<ec.edu.monster.modelo.Sucursal> sucursales = service.listarSucursales();
            return Response.ok(sucursales).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Obtiene una sucursal por su código
     * GET /api/corebancario/sucursales/{codigo}
     */
    @GET
    @Path("sucursales/{codigo}")
    public Response obtenerSucursal(@PathParam("codigo") String codigo) {
        try {
            ec.edu.monster.modelo.Sucursal sucursal = service.obtenerSucursal(codigo);
            return Response.ok(sucursal).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Crea una nueva sucursal
     * POST /api/corebancario/sucursales
     * Body: JSON con datos de la sucursal
     */
    @POST
    @Path("sucursales")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearSucursal(ec.edu.monster.modelo.Sucursal sucursal) {
        try {
            ec.edu.monster.modelo.Sucursal nuevaSucursal = service.crearSucursal(sucursal);
            return Response.status(Response.Status.CREATED)
                    .entity(nuevaSucursal)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Actualiza una sucursal existente
     * PUT /api/corebancario/sucursales/{codigo}
     * Body: JSON con datos actualizados de la sucursal
     */
    @PUT
    @Path("sucursales/{codigo}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizarSucursal(@PathParam("codigo") String codigo, 
                                       ec.edu.monster.modelo.Sucursal sucursal) {
        try {
            ec.edu.monster.modelo.Sucursal sucursalActualizada = service.actualizarSucursal(codigo, sucursal);
            return Response.ok(sucursalActualizada).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Elimina lógicamente una sucursal
     * DELETE /api/corebancario/sucursales/{codigo}
     */
    @DELETE
    @Path("sucursales/{codigo}")
    public Response eliminarSucursal(@PathParam("codigo") String codigo) {
        try {
            service.eliminarSucursal(codigo);
            return Response.ok("{\"mensaje\":\"Sucursal eliminada correctamente\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Obtiene la API Key de Google Maps para uso en el frontend
     * GET /api/corebancario/config/googlemaps
     */
    @GET
    @Path("config/googlemaps")
    public Response getGoogleMapsConfig() {
        try {
            String apiKey = GoogleMapsConfig.getApiKey();
            boolean configured = GoogleMapsConfig.isApiKeyConfigured();
            
            String json = "{\"apiKey\":\"" + apiKey + "\","
                    + "\"configured\":" + configured + "}";
            
            return Response.ok(json).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

}
