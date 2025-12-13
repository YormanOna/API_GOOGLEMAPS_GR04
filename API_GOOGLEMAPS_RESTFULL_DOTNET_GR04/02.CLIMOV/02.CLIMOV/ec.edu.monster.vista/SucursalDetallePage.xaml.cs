using Microsoft.Maui.Storage;
using _02.CLIMOV.Modelo;
using _02.CLIMOV.Servicio;

namespace _02.CLIMOV.Vista;

[QueryProperty(nameof(Codigo), "codigo")]
public partial class SucursalDetallePage : ContentPage
{
    private readonly IRestService _service = new RestService();
    public string Codigo { get; set; } = "";

    private Sucursal? _sucursal;

    public SucursalDetallePage()
    {
        InitializeComponent();
        LblUsuario.Text = Preferences.Get("usuario", "MONSTER");
    }

    protected override async void OnAppearing()
    {
        base.OnAppearing();
        await CargarAsync();
    }

    private async Task CargarAsync()
    {
        try
        {
            LoadingIndicator.IsVisible = LoadingIndicator.IsRunning = true;

            _sucursal = await _service.ObtenerSucursalAsync(Codigo);
            if (_sucursal == null)
            {
                await DisplayAlert("Aviso", "No se encontró la sucursal.", "OK");
                await Shell.Current.GoToAsync("//SucursalesPage");
                return;
            }

            LblNombre.Text = _sucursal.Nombre ?? "-";
            LblCodigo.Text = $"Código: {_sucursal.Codigo}";
            LblCiudad.Text = $"Ciudad: {_sucursal.Ciudad ?? "-"}";
            LblDireccion.Text = $"Dirección: {_sucursal.Direccion ?? "-"}";
            LblContadorCuenta.Text = $"Contador Cuenta: {_sucursal.ContadorCuenta}";

            var lat = _sucursal.Latitud?.ToString() ?? "-";
            var lng = _sucursal.Longitud?.ToString() ?? "-";
            LblCoords.Text = $"Coords: {lat}, {lng}";
        }
        catch (Exception ex)
        {
            await DisplayAlert("Error", $"No se pudo cargar.\n{ex.Message}", "OK");
        }
        finally
        {
            LoadingIndicator.IsVisible = LoadingIndicator.IsRunning = false;
        }
    }

    private async void OnEditarClicked(object sender, EventArgs e)
    {
        if (_sucursal == null) return;
        await Navigation.PushAsync(new SucursalEditPage(_sucursal));
    }

    private async void OnRutasClicked(object sender, EventArgs e)
    {
        if (_sucursal?.Codigo == null) return;
        await Shell.Current.GoToAsync($"RutasPage?codigo={Uri.EscapeDataString(_sucursal.Codigo)}");
    }

    private async void OnVolverClicked(object sender, EventArgs e)
        => await Shell.Current.GoToAsync("//SucursalesPage");

    private async void OnCerrarSesionClicked(object sender, EventArgs e)
    {
        Preferences.Remove("usuario");
        await Shell.Current.GoToAsync("//LoginPage");
    }
}
