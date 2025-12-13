using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using _02.CLIMOV.Modelo;

namespace _02.CLIMOV.Servicio
{
    public interface IRestService
    {
        // Métodos del servicio EurekaBank REST
        Task<bool> ValidarIngresoAsync(string usuario, string password);
        Task<List<Movimiento>> TraerMovimientosAsync(string cuenta);
        Task<OperacionResponse> RegistrarDepositoAsync(string cuenta, double importe);
        Task<OperacionResponse> RegistrarRetiroAsync(string cuenta, double importe);
        Task<OperacionResponse> RegistrarTransferenciaAsync(string origen, string destino, double importe);


        // ===== SUCURSALES =====
        Task<List<Sucursal>> ListarSucursalesAsync();
        Task<Sucursal?> ObtenerSucursalAsync(string codigo);
        Task<bool> CrearSucursalAsync(Sucursal sucursal);
        Task<bool> ActualizarSucursalAsync(string codigo, Sucursal sucursal);
        Task<bool> EliminarSucursalAsync(string codigo);
        Task<string> ObtenerGoogleMapsApiKeyAsync(); // opcional (si la app móvil usa Maps)
    }
}
