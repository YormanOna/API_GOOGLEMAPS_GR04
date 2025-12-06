using Microsoft.AspNetCore.Mvc;
using EurekaWeb.Models.Rest;

namespace EurekaWeb.Controllers
{
    public class SucursalesController : Controller
    {
        private readonly IRestEurekaClient _client;
        private readonly IConfiguration _config;

        public SucursalesController(IRestEurekaClient client, IConfiguration config)
        {
            _client = client;
            _config = config;
        }

        // GET: Sucursales
        public async Task<IActionResult> Index()
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                var sucursales = await _client.ListarSucursales();
                var apiKey = await _client.ObtenerApiKey();
                ViewBag.GoogleMapsApiKey = apiKey;
                ViewBag.Usuario = usuario;
                return View(sucursales);
            }
            catch (Exception ex)
            {
                ViewBag.Error = $"Error al cargar sucursales: {ex.Message}";
                return View(Array.Empty<Sucursal>());
            }
        }

        // GET: Sucursales/Crear
        public async Task<IActionResult> Crear()
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                var apiKey = await _client.ObtenerApiKey();
                ViewBag.GoogleMapsApiKey = apiKey;
                ViewBag.Usuario = usuario;
                return View();
            }
            catch (Exception ex)
            {
                ViewBag.Error = $"Error: {ex.Message}";
                return View();
            }
        }

        // POST: Sucursales/Crear
        [HttpPost]
        public async Task<IActionResult> Crear(Sucursal sucursal)
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                if (string.IsNullOrWhiteSpace(sucursal.Codigo) || sucursal.Codigo.Length != 3)
                {
                    ViewBag.Error = "El código debe tener exactamente 3 caracteres";
                    ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                    ViewBag.Usuario = usuario;
                    return View(sucursal);
                }

                var exito = await _client.CrearSucursal(sucursal);
                if (exito)
                {
                    TempData["Mensaje"] = "Sucursal creada exitosamente";
                    return RedirectToAction("Index");
                }
                else
                {
                    ViewBag.Error = "No se pudo crear la sucursal";
                    ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                    ViewBag.Usuario = usuario;
                    return View(sucursal);
                }
            }
            catch (Exception ex)
            {
                ViewBag.Error = $"Error: {ex.Message}";
                ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                ViewBag.Usuario = usuario;
                return View(sucursal);
            }
        }

        // GET: Sucursales/Detalle/007
        public async Task<IActionResult> Detalle(string id)
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                var sucursal = await _client.ObtenerSucursal(id);
                if (sucursal == null)
                {
                    TempData["Error"] = "Sucursal no encontrada";
                    return RedirectToAction("Index");
                }

                var apiKey = await _client.ObtenerApiKey();
                ViewBag.GoogleMapsApiKey = apiKey;
                ViewBag.Usuario = usuario;
                return View(sucursal);
            }
            catch (Exception ex)
            {
                TempData["Error"] = $"Error: {ex.Message}";
                return RedirectToAction("Index");
            }
        }

        // GET: Sucursales/Editar/007
        public async Task<IActionResult> Editar(string id)
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                var sucursal = await _client.ObtenerSucursal(id);
                if (sucursal == null)
                {
                    TempData["Error"] = "Sucursal no encontrada";
                    return RedirectToAction("Index");
                }

                var apiKey = await _client.ObtenerApiKey();
                ViewBag.GoogleMapsApiKey = apiKey;
                ViewBag.Usuario = usuario;
                return View(sucursal);
            }
            catch (Exception ex)
            {
                TempData["Error"] = $"Error: {ex.Message}";
                return RedirectToAction("Index");
            }
        }

        // POST: Sucursales/Editar/007
        [HttpPost]
        public async Task<IActionResult> Editar(string id, Sucursal sucursal)
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                // Log para ver qué llega del formulario
                Console.WriteLine($"=== POST Editar/{id} - Datos recibidos del formulario ===");
                Console.WriteLine($"Latitud recibida: {sucursal.Latitud}");
                Console.WriteLine($"Longitud recibida: {sucursal.Longitud}");
                Console.WriteLine($"Nombre: {sucursal.Nombre}");
                Console.WriteLine($"Ciudad: {sucursal.Ciudad}");
                
                // Validaciones básicas antes de enviar
                if (string.IsNullOrWhiteSpace(sucursal.Nombre))
                {
                    ViewBag.Error = "El nombre es obligatorio";
                    ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                    ViewBag.Usuario = usuario;
                    return View(sucursal);
                }

                if (string.IsNullOrWhiteSpace(sucursal.Ciudad))
                {
                    ViewBag.Error = "La ciudad es obligatoria";
                    ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                    ViewBag.Usuario = usuario;
                    return View(sucursal);
                }

                if (!sucursal.Latitud.HasValue || !sucursal.Longitud.HasValue)
                {
                    ViewBag.Error = "Las coordenadas son obligatorias";
                    ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                    ViewBag.Usuario = usuario;
                    return View(sucursal);
                }

                var exito = await _client.ActualizarSucursal(id, sucursal);
                if (exito)
                {
                    TempData["Mensaje"] = "Sucursal actualizada exitosamente";
                    return RedirectToAction("Index");
                }
                else
                {
                    ViewBag.Error = "No se pudo actualizar la sucursal";
                    ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                    ViewBag.Usuario = usuario;
                    return View(sucursal);
                }
            }
            catch (HttpRequestException httpEx)
            {
                // Extraer el mensaje del error HTTP
                var errorMsg = httpEx.Message;
                if (errorMsg.Contains("Error al actualizar sucursal:"))
                {
                    errorMsg = errorMsg.Substring(errorMsg.IndexOf("Error al actualizar sucursal:"));
                }
                ViewBag.Error = errorMsg;
                ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                ViewBag.Usuario = usuario;
                return View(sucursal);
            }
            catch (Exception ex)
            {
                ViewBag.Error = $"Error: {ex.Message}";
                ViewBag.GoogleMapsApiKey = await _client.ObtenerApiKey();
                ViewBag.Usuario = usuario;
                return View(sucursal);
            }
        }

        // POST: Sucursales/Eliminar/007
        [HttpPost]
        public async Task<IActionResult> Eliminar(string id)
        {
            var usuario = HttpContext.Session.GetString("usuario");
            if (string.IsNullOrEmpty(usuario))
                return RedirectToAction("Login", "Account");

            try
            {
                var exito = await _client.EliminarSucursal(id);
                if (exito)
                {
                    TempData["Mensaje"] = "Sucursal eliminada exitosamente";
                }
                else
                {
                    TempData["Error"] = "No se pudo eliminar la sucursal";
                }
            }
            catch (Exception ex)
            {
                TempData["Error"] = $"Error: {ex.Message}";
            }

            return RedirectToAction("Index");
        }
    }
}
