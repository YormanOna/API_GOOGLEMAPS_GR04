using System.Net.Http;
using System.Net.Http.Json;
using System.Text;
using EurekaWeb.Models.Rest;

public class RestEurekaClient : IRestEurekaClient
{
    private readonly HttpClient _http;
    public RestEurekaClient(HttpClient http) => _http = http;

    static string Pad8(string? c)
    {
        var s = (c ?? "").Trim();
        return (s.Length > 0 && s.Length < 8) ? s.PadLeft(8, '0') : s;
    }

    public async Task<string> ValidarIngreso(string usuario, string password)
    {
        // El servidor devuelve text/plain
        var req = new LoginRequest(usuario ?? "", password ?? "");
        using var resp = await _http.PostAsJsonAsync("api/CoreBancario/validarIngreso", req);
        resp.EnsureSuccessStatusCode();
        var txt = await resp.Content.ReadAsStringAsync();
        return txt.Trim(); // "Exitoso" / "Denegado"
    }

    public async Task<Movimiento[]> TraerMovimientos(string cuenta)
    {
        var c = Pad8(cuenta);
        var arr = await _http.GetFromJsonAsync<Movimiento[]>($"api/CoreBancario/cuentas/{c}/movimientos");
        return arr ?? Array.Empty<Movimiento>();
    }

    public async Task<OperacionCuentaResponse> RegDeposito(string cuenta, decimal importe)
    {
        var req = new OperacionRequest(Pad8(cuenta), importe, null);
        var resp = await _http.PostAsJsonAsync("api/CoreBancario/deposito", req);
        resp.EnsureSuccessStatusCode();
        return (await resp.Content.ReadFromJsonAsync<OperacionCuentaResponse>())!;
    }

    public async Task<OperacionCuentaResponse> RegRetiro(string cuenta, decimal importe)
    {
        var req = new OperacionRequest(Pad8(cuenta), importe, null);
        var resp = await _http.PostAsJsonAsync("api/CoreBancario/retiro", req);
        resp.EnsureSuccessStatusCode();
        return (await resp.Content.ReadFromJsonAsync<OperacionCuentaResponse>())!;
    }

    public async Task<OperacionCuentaResponse> RegTransferencia(string cuentaOrigen, string cuentaDestino, decimal importe)
    {
        var req = new TransferenciaRequest(Pad8(cuentaOrigen), Pad8(cuentaDestino), importe, null);
        var resp = await _http.PostAsJsonAsync("api/CoreBancario/transferencia", req);
        resp.EnsureSuccessStatusCode();
        return (await resp.Content.ReadFromJsonAsync<OperacionCuentaResponse>())!;
    }

    // ===== SUCURSALES =====
    public async Task<Sucursal[]> ListarSucursales()
    {
        var arr = await _http.GetFromJsonAsync<Sucursal[]>("api/sucursales");
        return arr ?? Array.Empty<Sucursal>();
    }

    public async Task<Sucursal?> ObtenerSucursal(string codigo)
    {
        try
        {
            return await _http.GetFromJsonAsync<Sucursal>($"api/sucursales/{codigo}");
        }
        catch
        {
            return null;
        }
    }

    public async Task<bool> CrearSucursal(Sucursal sucursal)
    {
        try
        {
            // Asegurar que el código tenga exactamente 3 caracteres
            if (!string.IsNullOrWhiteSpace(sucursal.Codigo))
            {
                sucursal.Codigo = sucursal.Codigo.Trim().PadLeft(3, '0').Substring(0, 3);
            }

            var resp = await _http.PostAsJsonAsync("api/sucursales", sucursal);
            if (!resp.IsSuccessStatusCode)
            {
                var error = await resp.Content.ReadAsStringAsync();
                Console.WriteLine($"Error al crear sucursal: {error}");
            }
            return resp.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Excepción al crear sucursal: {ex.Message}");
            return false;
        }
    }

    public async Task<bool> ActualizarSucursal(string codigo, Sucursal sucursal)
    {
        try
        {
            // Asegurar que el código esté correctamente formateado
            sucursal.Codigo = codigo;
            
            // Log del objeto que se va a enviar
            Console.WriteLine($"=== Enviando PUT a api/sucursales/{codigo} ===");
            Console.WriteLine($"Codigo: {sucursal.Codigo}");
            Console.WriteLine($"Nombre: {sucursal.Nombre}");
            Console.WriteLine($"Ciudad: {sucursal.Ciudad}");
            Console.WriteLine($"Direccion: {sucursal.Direccion}");
            Console.WriteLine($"Latitud: {sucursal.Latitud} (tipo: {sucursal.Latitud?.GetType().Name ?? "null"})");
            Console.WriteLine($"Longitud: {sucursal.Longitud} (tipo: {sucursal.Longitud?.GetType().Name ?? "null"})");
            Console.WriteLine($"ContadorCuenta: {sucursal.ContadorCuenta}");
            Console.WriteLine($"IsActive: {sucursal.IsActive}");
            
            var resp = await _http.PutAsJsonAsync($"api/sucursales/{codigo}", sucursal);
            if (!resp.IsSuccessStatusCode)
            {
                var error = await resp.Content.ReadAsStringAsync();
                Console.WriteLine($"Error al actualizar sucursal {codigo}: Status {resp.StatusCode}, Respuesta: {error}");
                throw new HttpRequestException($"Error al actualizar sucursal: {error}");
            }
            Console.WriteLine($"✓ Sucursal {codigo} actualizada exitosamente");
            return resp.IsSuccessStatusCode;
        }
        catch (HttpRequestException)
        {
            throw;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Excepción al actualizar sucursal: {ex.Message}");
            throw;
        }
    }

    public async Task<bool> EliminarSucursal(string codigo)
    {
        try
        {
            var resp = await _http.DeleteAsync($"api/sucursales/{codigo}");
            return resp.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }

    public async Task<string> ObtenerApiKey()
    {
        try
        {
            Console.WriteLine("=== Obteniendo API Key desde servidor ===");
            Console.WriteLine($"URL: {_http.BaseAddress}api/sucursales/config/apikey");
            
            var resp = await _http.GetFromJsonAsync<SucursalApiKeyResponse>("api/sucursales/config/apikey");
            
            Console.WriteLine($"API Key recibida: {(string.IsNullOrEmpty(resp?.ApiKey) ? "VACÍA o NULL" : resp.ApiKey)}");
            Console.WriteLine($"Longitud: {resp?.ApiKey?.Length ?? 0}");
            
            return resp?.ApiKey ?? string.Empty;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"✗ ERROR al obtener API Key: {ex.Message}");
            Console.WriteLine($"Tipo de error: {ex.GetType().Name}");
            if (ex.InnerException != null)
            {
                Console.WriteLine($"Error interno: {ex.InnerException.Message}");
            }
            return string.Empty;
        }
    }
}
