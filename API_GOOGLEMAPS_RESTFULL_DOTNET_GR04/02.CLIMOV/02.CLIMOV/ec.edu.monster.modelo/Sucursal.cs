using System;

namespace _02.CLIMOV.Modelo
{
    public class Sucursal
    {
        public string Codigo { get; set; } = string.Empty;   // chr_sucucodigo (CHAR(3))
        public string Nombre { get; set; } = string.Empty;   // vch_sucunombre
        public string Ciudad { get; set; } = string.Empty;   // vch_sucuciudad
        public string? Direccion { get; set; }               // vch_sucudireccion
        public int ContadorCuenta { get; set; }              // int_sucucontcuenta
        public decimal? Latitud { get; set; }                // dec_sululatitud
        public decimal? Longitud { get; set; }               // dec_suculongitud
        public bool IsActive { get; set; } = true;           // bit_sucuisactive
    }

    // Respuesta del endpoint: GET api/sucursales/config/apikey
    public class SucursalApiKeyResponse
    {
        public string ApiKey { get; set; } = string.Empty;
    }
}
