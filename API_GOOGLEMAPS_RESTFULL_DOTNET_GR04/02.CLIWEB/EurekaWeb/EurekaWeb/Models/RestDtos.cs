namespace EurekaWeb.Models.Rest
{
    // ==== Requests ====
    public record LoginRequest(string usuario, string password);
    public record OperacionRequest(string cuenta, decimal importe, string? empleado = null);
    public record TransferenciaRequest(string cuentaOrigen, string cuentaDestino, decimal importe, string? empleado = null);

    // ==== Responses ====
    public class Movimiento
    {
        public string CuentaCodigo { get; set; } = string.Empty;
        public int Numero { get; set; }
        public DateTime Fecha { get; set; }
        public string EmpleadoCodigo { get; set; } = string.Empty;
        public string TipoCodigo { get; set; } = string.Empty;
        public decimal Importe { get; set; }
        public string? ReferenciaCuenta { get; set; }
    }

    public class OperacionCuentaResponse
    {
        public int Estado { get; set; }       // 1 ok, -1 error
        public decimal Saldo { get; set; }
    }

    // ==== Sucursales ====
    public class Sucursal
    {
        public string Codigo { get; set; } = string.Empty;
        public string Nombre { get; set; } = string.Empty;
        public string Ciudad { get; set; } = string.Empty;
        public string? Direccion { get; set; }
        public int ContadorCuenta { get; set; }
        public decimal? Latitud { get; set; }
        public decimal? Longitud { get; set; }
        public bool IsActive { get; set; } = true;
    }

    public class SucursalApiKeyResponse
    {
        public string ApiKey { get; set; } = string.Empty;
    }
}
