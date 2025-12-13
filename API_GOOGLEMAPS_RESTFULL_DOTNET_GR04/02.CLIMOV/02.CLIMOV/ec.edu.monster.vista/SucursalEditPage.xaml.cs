using System;
using System.Globalization;
using Microsoft.Maui.Storage;
using _02.CLIMOV.Modelo;
using _02.CLIMOV.Servicio;

namespace _02.CLIMOV.Vista
{
    public partial class SucursalEditPage : ContentPage
    {
        private readonly IRestService _service = new RestService();
        private readonly bool _esEdicion;
        private readonly string _codigoOriginal = "";
        private readonly Sucursal? _original;
        private const int MAX_DIR = 50;

        private static string Recortar(string? s, int max)
        {
            s = (s ?? "").Trim();
            if (s.Length <= max) return s;
            return s.Substring(0, max).Trim();
        }


        public SucursalEditPage()
        {
            InitializeComponent();
            LblUsuario.Text = Preferences.Get("usuario", "MONSTER");
            _esEdicion = false;

            LblTitulo.Text = "Nueva Sucursal";
            EntryCodigo.IsEnabled = true;
        }

        public SucursalEditPage(Sucursal sucursal) : this()
        {
            _esEdicion = true;
            _original = sucursal;
            _codigoOriginal = sucursal.Codigo ?? "";

            LblTitulo.Text = "Editar Sucursal";
            EntryCodigo.Text = sucursal.Codigo;
            EntryNombre.Text = sucursal.Nombre;
            EntryCiudad.Text = sucursal.Ciudad;
            EntryDireccion.Text = sucursal.Direccion;

            EntryLat.Text = sucursal.Latitud?.ToString(CultureInfo.InvariantCulture) ?? "";
            EntryLng.Text = sucursal.Longitud?.ToString(CultureInfo.InvariantCulture) ?? "";

            EntryCodigo.IsEnabled = false; // típico como en el web
        }

        private async void OnGuardarClicked(object sender, EventArgs e)
        {
            var codigo = (EntryCodigo.Text ?? "").Trim();
            var nombre = (EntryNombre.Text ?? "").Trim();
            var ciudad = (EntryCiudad.Text ?? "").Trim();

            if (string.IsNullOrWhiteSpace(codigo) || string.IsNullOrWhiteSpace(nombre) || string.IsNullOrWhiteSpace(ciudad))
            {
                await DisplayAlert("Validación", "Código, Nombre y Ciudad son obligatorios.", "OK");
                return;
            }

            decimal? lat = TryParseDecimal(EntryLat.Text);
            decimal? lng = TryParseDecimal(EntryLng.Text);



            var suc = new Sucursal
            {
                Codigo = codigo,
                Nombre = nombre,
                Ciudad = ciudad,
                Direccion = Recortar(EntryDireccion.Text, MAX_DIR),
                ContadorCuenta = _original?.ContadorCuenta ?? 0,
                Latitud = lat,
                Longitud = lng,
                IsActive = true
            };

            try
            {
                SetLoading(true);

                bool ok = _esEdicion
                    ? await _service.ActualizarSucursalAsync(_codigoOriginal, suc)
                    : await _service.CrearSucursalAsync(suc);

                if (ok)
                {
                    await DisplayAlert("Listo", _esEdicion ? "Sucursal actualizada." : "Sucursal creada.", "OK");
                    await Shell.Current.GoToAsync("..");

                }
                else
                {
                    await DisplayAlert("Error", "No se pudo guardar la sucursal.", "OK");
                }
            }
            catch (Exception ex)
            {
                await DisplayAlert("Error", $"No se pudo guardar.\n{ex.Message}", "OK");
            }
            finally
            {
                SetLoading(false);
            }
        }

        private static decimal? TryParseDecimal(string? text)
        {
            var t = (text ?? "").Trim();
            if (string.IsNullOrWhiteSpace(t)) return null;

            // Acepta coma o punto
            t = t.Replace(",", ".");
            if (decimal.TryParse(t, NumberStyles.Any, CultureInfo.InvariantCulture, out var v))
                return v;

            return null;
        }


        private class GeoInfo
        {
            public string? Ciudad { get; set; }
            public string? Direccion { get; set; }
        }



        private async Task<GeoInfo?> ReverseGeocodeAsync(decimal lat, decimal lng)
        {
            try
            {
                var apiKey = await _service.ObtenerGoogleMapsApiKeyAsync();
                if (string.IsNullOrWhiteSpace(apiKey)) return null;

                using var http = new HttpClient();

                var url =
                    $"https://maps.googleapis.com/maps/api/geocode/json?latlng={lat.ToString(CultureInfo.InvariantCulture)},{lng.ToString(CultureInfo.InvariantCulture)}&key={Uri.EscapeDataString(apiKey)}";

                var json = await http.GetStringAsync(url);

                using var doc = System.Text.Json.JsonDocument.Parse(json);
                var root = doc.RootElement;

                if (!root.TryGetProperty("status", out var st) || st.GetString() != "OK")
                    return null;

                var results = root.GetProperty("results");
                if (results.GetArrayLength() == 0) return null;

                var first = results[0];

                // formatted_address como “dirección bonita”
                string? direccion = null;
                if (first.TryGetProperty("formatted_address", out var fa))
                    direccion = fa.GetString();

                // ciudad: buscar "locality" en address_components
                string? ciudad = null;
                if (first.TryGetProperty("address_components", out var comps))
                {
                    foreach (var comp in comps.EnumerateArray())
                    {
                        if (!comp.TryGetProperty("types", out var types)) continue;

                        bool isLocality = false;
                        foreach (var t in types.EnumerateArray())
                        {
                            if (t.GetString() == "locality") { isLocality = true; break; }
                        }

                        if (isLocality && comp.TryGetProperty("long_name", out var ln))
                        {
                            ciudad = ln.GetString();
                            break;
                        }
                    }
                }

                return new GeoInfo
                {
                    Ciudad = ciudad,
                    Direccion = direccion
                };
            }
            catch
            {
                return null;
            }
        }



        private void SetLoading(bool loading)
        {
            LoadingIndicator.IsVisible = loading;
            LoadingIndicator.IsRunning = loading;
            BtnGuardar.IsEnabled = !loading;
        }

        private async void OnCancelarClicked(object sender, EventArgs e)
        {
            // Ir SIEMPRE a la página de Sucursales (ruta real del Shell)
            await Shell.Current.GoToAsync("//SucursalesPage");
        }

        private async void OnVolverClicked(object sender, EventArgs e)
        {
            await Shell.Current.GoToAsync("//SucursalesPage");
        }

        private async void OnElegirEnMapaClicked(object sender, EventArgs e)
        {
            decimal? lat = TryParseDecimal(EntryLat.Text);
            decimal? lng = TryParseDecimal(EntryLng.Text);

            var key = await _service.ObtenerGoogleMapsApiKeyAsync();

            await Navigation.PushAsync(new UbicacionPickerPage(async (pickedLat, pickedLng) =>
            {
                EntryLat.Text = pickedLat.ToString(CultureInfo.InvariantCulture);
                EntryLng.Text = pickedLng.ToString(CultureInfo.InvariantCulture);

                var info = await ReverseGeocodeAsync(pickedLat, pickedLng);
                if (info != null)
                {
                    if (!string.IsNullOrWhiteSpace(info.Ciudad))
                        EntryCiudad.Text = info.Ciudad;

                    if (!string.IsNullOrWhiteSpace(info.Direccion))
                        EntryDireccion.Text = Recortar(info.Direccion, MAX_DIR);
                }

            }, key, lat, lng));
        }





        private async void OnCerrarSesionClicked(object sender, EventArgs e)
        {
            Preferences.Remove("usuario");
            await Navigation.PopToRootAsync();
        }
    }
}
