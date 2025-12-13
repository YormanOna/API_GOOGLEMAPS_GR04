using System.Globalization;

namespace _02.CLIMOV.Vista;

public partial class UbicacionPickerPage : ContentPage
{
    private readonly string _googleKey;
    private readonly Func<decimal, decimal, Task> _onPicked;

    private decimal _lat;
    private decimal _lng;

    public UbicacionPickerPage(Func<decimal, decimal, Task> onPicked, string apiKey, decimal? latInicial = null, decimal? lngInicial = null)
    {
        InitializeComponent();
        _onPicked = onPicked;

        _googleKey = apiKey ?? "";

        _lat = latInicial ?? 4.710989m;
        _lng = lngInicial ?? -74.072092m;

        MapWeb.Source = new HtmlWebViewSource { Html = BuildHtml(_lat, _lng, _googleKey) };
    }


    private static string BuildHtml(decimal lat, decimal lng, string apiKey)
    {
        var key = (apiKey ?? "").Trim();
        var latS = lat.ToString(CultureInfo.InvariantCulture);
        var lngS = lng.ToString(CultureInfo.InvariantCulture);

        return $@"
<!DOCTYPE html>
<html>
<head>
<meta name='viewport' content='width=device-width, initial-scale=1.0'>
<style>
  html, body, #map {{ height: 100%; margin: 0; padding: 0; }}
</style>
</head>
<body>
<div id='map'></div>

<script>
  var map, marker;

  function initMap() {{
    var center = {{ lat: {latS}, lng: {lngS} }};

    map = new google.maps.Map(document.getElementById('map'), {{
      center: center,
      zoom: 13,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: false
    }});

    marker = new google.maps.Marker({{
      position: center,
      map: map
    }});

    map.addListener('click', function(e) {{
      var lat = e.latLng.lat();
      var lng = e.latLng.lng();
      marker.setPosition({{ lat: lat, lng: lng }});
      window.location.href = 'app://picked?lat=' + lat + '&lng=' + lng;
    }});
  }}
</script>

<script async defer src='https://maps.googleapis.com/maps/api/js?key={key}&callback=initMap'></script>
</body>
</html>";
    }


    private void OnWebNavigating(object sender, WebNavigatingEventArgs e)
    {
        if (!e.Url.StartsWith("app://picked", StringComparison.OrdinalIgnoreCase))
            return;

        e.Cancel = true;

        var uri = new Uri(e.Url);
        var q = System.Web.HttpUtility.ParseQueryString(uri.Query);

        var latS = (q["lat"] ?? "").Replace(",", ".");
        var lngS = (q["lng"] ?? "").Replace(",", ".");

        if (decimal.TryParse(latS, NumberStyles.Any, CultureInfo.InvariantCulture, out var lat) &&
            decimal.TryParse(lngS, NumberStyles.Any, CultureInfo.InvariantCulture, out var lng))
        {
            _lat = lat;
            _lng = lng;
        }
    }

    private async void OnUsarClicked(object sender, EventArgs e)
    {
        await _onPicked(_lat, _lng);
        await Navigation.PopAsync();   // ✅ CORRECTO
    }

    private async void OnCancelarClicked(object sender, EventArgs e)
    {
        await Navigation.PopAsync();   // ✅ CORRECTO
    }


}
