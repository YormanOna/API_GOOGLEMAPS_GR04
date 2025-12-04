namespace WS_EB_DOTNET_REST_Servidor.Models
{
    public class Sucursal
    {
        public string Codigo { get; set; } = string.Empty;      // chr_sucucodigo
        public string Nombre { get; set; } = string.Empty;       // vch_sucunombre
        public string Ciudad { get; set; } = string.Empty;       // vch_sucuciudad
        public string? Direccion { get; set; }                   // vch_sucudireccion
        public int ContadorCuenta { get; set; }                  // int_sucucontcuenta
        public decimal? Latitud { get; set; }                    // dec_sululatitud
        public decimal? Longitud { get; set; }                   // dec_suculongitud
        public bool IsActive { get; set; } = true;               // bit_sucuisactive
    }
}
