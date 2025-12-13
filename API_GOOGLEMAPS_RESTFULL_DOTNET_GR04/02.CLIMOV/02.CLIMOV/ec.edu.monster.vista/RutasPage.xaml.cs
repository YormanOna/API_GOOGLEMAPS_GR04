using System.Globalization;
using Microsoft.Maui.Storage;
using _02.CLIMOV.Modelo;
using _02.CLIMOV.Servicio;

namespace _02.CLIMOV.Vista;

[QueryProperty(nameof(Codigo), "codigo")]
public partial class RutasPage : ContentPage
{
    private readonly IRestService _service = new RestService();
    private string _googleKey = "";

    public string Codigo { get; set; } = "";
    private Sucursal? _sucursal;

    // origen seleccionado
    private decimal? _origLat;
    private decimal? _origLng;

    private bool _pickOrigen = false;

    public RutasPage()
    {
        InitializeComponent();
        LblUsuario.Text = Preferences.Get("usuario", "MONSTER");

        PickerModo.Items.Clear();
        PickerModo.Items.Add("Carro");
        PickerModo.Items.Add("A pie");
        PickerModo.Items.Add("Bicicleta");
        PickerModo.Items.Add("Transporte");
        PickerModo.SelectedIndex = 0;

    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        await CargarDestinoAsync();
    }

    private async Task CargarDestinoAsync()
    {

        _googleKey = await _service.ObtenerGoogleMapsApiKeyAsync();

        if (string.IsNullOrWhiteSpace(_googleKey))
        {
            await DisplayAlert("Maps", "No se recibió Google Maps API Key desde el servidor.", "OK");
            // puedes retornar o seguir sin mapa
        }


        _sucursal = await _service.ObtenerSucursalAsync(Codigo);

        if (_sucursal == null)
        {
            await DisplayAlert("Aviso", "No se encontró la sucursal.", "OK");
            await Shell.Current.GoToAsync("//SucursalesPage");
            return;
        }

        if (!_sucursal.Latitud.HasValue || !_sucursal.Longitud.HasValue)
        {
            await DisplayAlert("Aviso", "La sucursal no tiene coordenadas. Edita y asigna ubicación.", "OK");
        }

        LblDestino.Text = $"Destino: {_sucursal.Nombre} ({_sucursal.Ciudad})";
        LblInfo.Text = "Pulsa 'ELEGIR ORIGEN (MAPA)' y toca el mapa para seleccionar el punto.";

        var lat = _sucursal.Latitud ?? 4.710989m;
        var lng = _sucursal.Longitud ?? -74.072092m;

        MapWeb.Source = new HtmlWebViewSource
        {
            Html = BuildHtml(lat, lng, null, null, null, null, false, "car", _googleKey)
        };

    }

    private async void OnElegirOrigenClicked(object sender, EventArgs e)
    {
        _pickOrigen = true;
        LblInfo.Text = "✅ Ahora toca el mapa para seleccionar el ORIGEN.";
        await DisplayAlert("Origen", "Toca el mapa para seleccionar el origen.", "OK");
    }


    private void OnCalcularRutaClicked(object sender, EventArgs e)
    {
        if (_sucursal?.Latitud == null || _sucursal.Longitud == null)
        {
            DisplayAlert("Rutas", "La sucursal no tiene coordenadas.", "OK");
            return;
        }

        if (_origLat == null || _origLng == null)
        {
            DisplayAlert("Rutas", "Selecciona un origen en el mapa.", "OK");
            return;
        }

        var modo = PickerModo.SelectedIndex switch
        {
            0 => "car",
            1 => "foot",
            2 => "bike",
            3 => "transit",
            _ => "car"
        };

        var dLat = _sucursal.Latitud.Value;
        var dLng = _sucursal.Longitud.Value;

        MapWeb.Source = new HtmlWebViewSource
        {
            Html = BuildHtml(
        dLat, dLng,
        _origLat.Value, _origLng.Value,
        dLat, dLng,
        true,
        modo,
        _googleKey
    )
        };

    }

    private void RedibujarMapa()
    {
        if (_sucursal?.Latitud == null || _sucursal.Longitud == null)
        {
            var lat = _origLat ?? 4.710989m;
            var lng = _origLng ?? -74.072092m;
            MapWeb.Source = new HtmlWebViewSource
            {
                Html = BuildHtml(lat, lng, _origLat, _origLng, null, null, false, "car", _googleKey)
            };

            return;
        }

        var dLat = _sucursal.Latitud.Value;
        var dLng = _sucursal.Longitud.Value;

        var modo = PickerModo.SelectedIndex switch
        {
            0 => "car",
            1 => "foot",
            2 => "bike",
            3 => "transit",
            _ => "car"
        };

        MapWeb.Source = new HtmlWebViewSource
        {
            Html = BuildHtml(dLat, dLng, _origLat, _origLng, dLat, dLng, false, modo, _googleKey)
        };

    }

    private void OnWebNavigating(object sender, WebNavigatingEventArgs e)
    {
        if (!e.Url.StartsWith("app://origin", StringComparison.OrdinalIgnoreCase))
            return;

        e.Cancel = true;

        if (!_pickOrigen) return;

        var uri = new Uri(e.Url);
        var query = uri.Query.TrimStart('?').Split('&', StringSplitOptions.RemoveEmptyEntries);

        string? latS = null, lngS = null;
        foreach (var part in query)
        {
            var kv = part.Split('=', 2);
            if (kv.Length != 2) continue;
            if (kv[0] == "lat") latS = Uri.UnescapeDataString(kv[1]);
            if (kv[0] == "lng") lngS = Uri.UnescapeDataString(kv[1]);
        }

        if (latS == null || lngS == null) return;

        latS = latS.Replace(",", ".");
        lngS = lngS.Replace(",", ".");

        if (decimal.TryParse(latS, NumberStyles.Any, CultureInfo.InvariantCulture, out var lat) &&
            decimal.TryParse(lngS, NumberStyles.Any, CultureInfo.InvariantCulture, out var lng))
        {
            _origLat = lat;
            _origLng = lng;
            _pickOrigen = false;

            LblInfo.Text = $"Origen seleccionado: {lat.ToString(CultureInfo.InvariantCulture)}, {lng.ToString(CultureInfo.InvariantCulture)}";
            RedibujarMapa();
        }
    }

    private async void OnVolverClicked(object sender, EventArgs e)
    {
        await Navigation.PopAsync();
    }


    private async void OnCerrarSesionClicked(object sender, EventArgs e)
    {
        Preferences.Remove("usuario");
        await Shell.Current.GoToAsync("//LoginPage");
    }

    private static string BuildHtml(
    decimal centerLat, decimal centerLng,
    decimal? origLat, decimal? origLng,
    decimal? destLat, decimal? destLng,
    bool calcularRuta,
    string modo = "car",
    string apiKey = "")
    {
        string inv(decimal v) => v.ToString(CultureInfo.InvariantCulture);

        var oLat = origLat.HasValue ? inv(origLat.Value) : "null";
        var oLng = origLng.HasValue ? inv(origLng.Value) : "null";
        var dLat = destLat.HasValue ? inv(destLat.Value) : "null";
        var dLng = destLng.HasValue ? inv(destLng.Value) : "null";

        var doRoute = calcularRuta ? "true" : "false";
        modo = string.IsNullOrWhiteSpace(modo) ? "car" : modo;

        // Mapear a TravelMode de Google
        var travelMode = modo switch
        {
            "foot" => "WALKING",
            "bike" => "BICYCLING",
            "transit" => "TRANSIT",
            _ => "DRIVING"
        };

        // Importante: si apiKey viene vacía, el script fallará.
        var key = (apiKey ?? "").Trim();

        return $@"
<!DOCTYPE html>
<html>
<head>
<meta name='viewport' content='width=device-width, initial-scale=1.0'>
<style>
  html, body, #map {{ height: 100%; margin: 0; padding: 0; }}
  .info {{
    position: absolute; z-index: 999; bottom: 10px; left: 10px;
    background: rgba(255,255,255,0.92);
    padding: 8px 10px; border-radius: 10px; font-family: Arial; font-size: 12px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.15);
  }}
</style>
</head>
<body>
<div id='map'></div>
<div class='info' id='info'>Toca el mapa para elegir origen</div>

<script>
  var center = {{ lat: {inv(centerLat)}, lng: {inv(centerLng)} }};
  var oLat = {oLat}, oLng = {oLng};
  var dLat = {dLat}, dLng = {dLng};
  var doRoute = {doRoute};
  var travelMode = '{travelMode}';

  var map, originMarker, destMarker, directionsService, directionsRenderer;

  function initMap() {{
    map = new google.maps.Map(document.getElementById('map'), {{
      center: center,
      zoom: 13,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: false
    }});

    directionsService = new google.maps.DirectionsService();
    directionsRenderer = new google.maps.DirectionsRenderer({{
      suppressMarkers: true,
      preserveViewport: true
    }});
    directionsRenderer.setMap(map);

    // Destino
    if (dLat !== null && dLng !== null) {{
      var destPos = {{ lat: dLat, lng: dLng }};
      destMarker = new google.maps.Marker({{
        position: destPos,
        map: map,
        title: 'Destino'
      }});
    }}

    // Origen
    if (oLat !== null && oLng !== null) {{
      var origPos = {{ lat: oLat, lng: oLng }};
      originMarker = new google.maps.Marker({{
        position: origPos,
        map: map,
        title: 'Origen'
      }});
    }}

    // Click para elegir origen
    map.addListener('click', function(e) {{
      var lat = e.latLng.lat();
      var lng = e.latLng.lng();
      // Avisar a MAUI
      window.location.href = 'app://origin?lat=' + lat + '&lng=' + lng;
    }});

    if (doRoute) {{
      fetchRoute();
    }}
  }}

  function fetchRoute() {{
  if (oLat === null || oLng === null || dLat === null || dLng === null) {{
    document.getElementById('info').innerText = 'Selecciona origen y destino.';
    return;
  }}

  document.getElementById('info').innerText = 'Calculando ruta...';

  var request = {{
    origin: {{ lat: oLat, lng: oLng }},
    destination: {{ lat: dLat, lng: dLng }},
    travelMode: travelMode
  }};

  // ✅ Solo para transporte público
  if (travelMode === 'TRANSIT') {{
    request.transitOptions = {{
      departureTime: new Date()
    }};
  }}

  directionsService.route(request, function(result, status) {{
    if (status !== 'OK' || !result || !result.routes || result.routes.length === 0) {{
      document.getElementById('info').innerText = 'No se encontró ruta.';
      return;
    }}

    directionsRenderer.setDirections(result);

    var bounds = new google.maps.LatLngBounds();
    bounds.extend(new google.maps.LatLng(oLat, oLng));
    bounds.extend(new google.maps.LatLng(dLat, dLng));
    map.fitBounds(bounds);

    var leg = result.routes[0].legs[0];
    var distText = leg.distance ? leg.distance.text : '';
    var durText = leg.duration ? leg.duration.text : '';
    document.getElementById('info').innerText = 'Distancia: ' + distText + ' | Tiempo: ' + durText;
  }});
  }}
</script>

<script async defer src='https://maps.googleapis.com/maps/api/js?key={key}&callback=initMap'></script>
</body>
</html>";
    }

}
