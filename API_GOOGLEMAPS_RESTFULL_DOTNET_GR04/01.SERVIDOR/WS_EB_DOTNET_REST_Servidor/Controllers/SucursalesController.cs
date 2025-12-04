using Microsoft.AspNetCore.Mvc;
using WS_EB_DOTNET_REST_Servidor.Models;
using WS_EB_DOTNET_REST_Servidor.Services;

namespace WS_EB_DOTNET_REST_Servidor.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class SucursalesController : ControllerBase
    {
        private readonly EurekaService _service;
        private readonly IConfiguration _config;

        public SucursalesController(EurekaService service, IConfiguration config)
        {
            _service = service;
            _config = config;
        }

        // GET: api/sucursales
        // Listar todas las sucursales activas
        [HttpGet]
        public ActionResult<IEnumerable<Sucursal>> GetSucursales()
        {
            try
            {
                var sucursales = _service.ListarSucursales();
                return Ok(sucursales);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al listar sucursales", error = ex.Message });
            }
        }

        // GET: api/sucursales/{id}
        // Obtener una sucursal por código
        [HttpGet("{id}")]
        public ActionResult<Sucursal> GetSucursal(string id)
        {
            try
            {
                var sucursal = _service.ObtenerSucursal(id);
                if (sucursal == null)
                {
                    return NotFound(new { mensaje = $"Sucursal con código {id} no encontrada" });
                }
                return Ok(sucursal);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener sucursal", error = ex.Message });
            }
        }

        // POST: api/sucursales
        // Crear una nueva sucursal
        [HttpPost]
        public ActionResult<Sucursal> CreateSucursal([FromBody] Sucursal sucursal)
        {
            try
            {
                // Validaciones
                if (string.IsNullOrWhiteSpace(sucursal.Codigo) || sucursal.Codigo.Length != 3)
                {
                    return BadRequest(new { mensaje = "El código de sucursal debe tener exactamente 3 caracteres" });
                }

                if (string.IsNullOrWhiteSpace(sucursal.Nombre))
                {
                    return BadRequest(new { mensaje = "El nombre de la sucursal es obligatorio" });
                }

                if (string.IsNullOrWhiteSpace(sucursal.Ciudad))
                {
                    return BadRequest(new { mensaje = "La ciudad es obligatoria" });
                }

                // Validar coordenadas si se proporcionan
                if (sucursal.Latitud.HasValue && (sucursal.Latitud < -90 || sucursal.Latitud > 90))
                {
                    return BadRequest(new { mensaje = "La latitud debe estar entre -90 y 90 grados" });
                }

                if (sucursal.Longitud.HasValue && (sucursal.Longitud < -180 || sucursal.Longitud > 180))
                {
                    return BadRequest(new { mensaje = "La longitud debe estar entre -180 y 180 grados" });
                }

                // Ambas coordenadas deben estar presentes o ninguna
                if ((sucursal.Latitud.HasValue && !sucursal.Longitud.HasValue) ||
                    (!sucursal.Latitud.HasValue && sucursal.Longitud.HasValue))
                {
                    return BadRequest(new { mensaje = "Debe proporcionar tanto latitud como longitud, o ninguna" });
                }

                // Verificar si ya existe una sucursal con ese código
                var existente = _service.ObtenerSucursal(sucursal.Codigo);
                if (existente != null)
                {
                    return Conflict(new { mensaje = $"Ya existe una sucursal con el código {sucursal.Codigo}" });
                }

                bool creada = _service.CrearSucursal(sucursal);
                if (creada)
                {
                    return CreatedAtAction(nameof(GetSucursal), new { id = sucursal.Codigo }, sucursal);
                }

                return StatusCode(500, new { mensaje = "No se pudo crear la sucursal" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al crear sucursal", error = ex.Message });
            }
        }

        // PUT: api/sucursales/{id}
        // Actualizar una sucursal existente
        [HttpPut("{id}")]
        public ActionResult UpdateSucursal(string id, [FromBody] Sucursal sucursal)
        {
            try
            {
                // Log para debugging
                Console.WriteLine($"=== PUT /api/sucursales/{id} ===");
                Console.WriteLine($"Sucursal recibida: Codigo={sucursal?.Codigo}, Nombre={sucursal?.Nombre}, Ciudad={sucursal?.Ciudad}");
                Console.WriteLine($"Coordenadas: Lat={sucursal?.Latitud}, Lng={sucursal?.Longitud}");
                Console.WriteLine($"Contador={sucursal?.ContadorCuenta}, IsActive={sucursal?.IsActive}");

                // Validar que el objeto no sea null
                if (sucursal == null)
                {
                    return BadRequest(new { mensaje = "El cuerpo de la solicitud está vacío" });
                }

                // Validaciones
                if (string.IsNullOrWhiteSpace(sucursal.Nombre))
                {
                    return BadRequest(new { mensaje = "El nombre de la sucursal es obligatorio" });
                }

                if (string.IsNullOrWhiteSpace(sucursal.Ciudad))
                {
                    return BadRequest(new { mensaje = "La ciudad es obligatoria" });
                }

                // Validar coordenadas si se proporcionan
                if (sucursal.Latitud.HasValue && (sucursal.Latitud < -90 || sucursal.Latitud > 90))
                {
                    return BadRequest(new { mensaje = "La latitud debe estar entre -90 y 90 grados" });
                }

                if (sucursal.Longitud.HasValue && (sucursal.Longitud < -180 || sucursal.Longitud > 180))
                {
                    return BadRequest(new { mensaje = "La longitud debe estar entre -180 y 180 grados" });
                }

                // Ambas coordenadas deben estar presentes o ninguna
                if ((sucursal.Latitud.HasValue && !sucursal.Longitud.HasValue) ||
                    (!sucursal.Latitud.HasValue && sucursal.Longitud.HasValue))
                {
                    return BadRequest(new { mensaje = "Debe proporcionar tanto latitud como longitud, o ninguna" });
                }

                bool actualizada = _service.ActualizarSucursal(id, sucursal);
                if (actualizada)
                {
                    Console.WriteLine($"✓ Sucursal {id} actualizada correctamente");
                    return Ok(new { mensaje = "Sucursal actualizada correctamente" });
                }

                Console.WriteLine($"✗ Sucursal {id} no encontrada");
                return NotFound(new { mensaje = $"Sucursal con código {id} no encontrada" });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"✗ Error al actualizar sucursal {id}: {ex.Message}");
                Console.WriteLine($"StackTrace: {ex.StackTrace}");
                return StatusCode(500, new { mensaje = "Error al actualizar sucursal", error = ex.Message });
            }
        }

        // DELETE: api/sucursales/{id}
        // Eliminación lógica de una sucursal
        [HttpDelete("{id}")]
        public ActionResult DeleteSucursal(string id)
        {
            try
            {
                bool eliminada = _service.EliminarSucursal(id);
                if (eliminada)
                {
                    return Ok(new { mensaje = "Sucursal eliminada correctamente" });
                }

                return NotFound(new { mensaje = $"Sucursal con código {id} no encontrada" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al eliminar sucursal", error = ex.Message });
            }
        }

        // GET: api/sucursales/config/apikey
        // Endpoint opcional para obtener la API Key de Google Maps (solo para frontend)
        [HttpGet("config/apikey")]
        public ActionResult<object> GetGoogleMapsApiKey()
        {
            try
            {
                var apiKey = _config["GoogleMaps:ApiKey"];
                if (string.IsNullOrWhiteSpace(apiKey) || apiKey == "TU_GOOGLE_MAPS_API_KEY_AQUI")
                {
                    return StatusCode(500, new { mensaje = "Google Maps API Key no configurada" });
                }

                return Ok(new { apiKey });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { mensaje = "Error al obtener API Key", error = ex.Message });
            }
        }
    }
}
