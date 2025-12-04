using EurekaWeb.Models.Rest;

public interface IRestEurekaClient
{
    Task<string> ValidarIngreso(string usuario, string password); // "Exitoso" | "Denegado"
    Task<Movimiento[]> TraerMovimientos(string cuenta);
    Task<OperacionCuentaResponse> RegDeposito(string cuenta, decimal importe);
    Task<OperacionCuentaResponse> RegRetiro(string cuenta, decimal importe);
    Task<OperacionCuentaResponse> RegTransferencia(string cuentaOrigen, string cuentaDestino, decimal importe);

    // Sucursales
    Task<Sucursal[]> ListarSucursales();
    Task<Sucursal?> ObtenerSucursal(string codigo);
    Task<bool> CrearSucursal(Sucursal sucursal);
    Task<bool> ActualizarSucursal(string codigo, Sucursal sucursal);
    Task<bool> EliminarSucursal(string codigo);
    Task<string> ObtenerApiKey();
}
