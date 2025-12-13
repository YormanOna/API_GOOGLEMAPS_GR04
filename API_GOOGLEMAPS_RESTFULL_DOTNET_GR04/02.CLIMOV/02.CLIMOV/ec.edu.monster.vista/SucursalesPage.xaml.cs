using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Maui.Storage;
using _02.CLIMOV.Modelo;
using _02.CLIMOV.Servicio;

namespace _02.CLIMOV.Vista
{
    public partial class SucursalesPage : ContentPage
    {
        private readonly IRestService _service = new RestService();
        private List<Sucursal> _data = new();

        public SucursalesPage()
        {
            InitializeComponent();

            // Usuario (mismo estilo que tus páginas)
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
                LoadingIndicator.IsVisible = true;
                LoadingIndicator.IsRunning = true;

                _data = await _service.ListarSucursalesAsync();
                ListaSucursales.ItemsSource = _data;
            }
            catch (Exception ex)
            {
                await DisplayAlert("Error", $"No se pudo cargar sucursales.\n{ex.Message}", "OK");
            }
            finally
            {
                LoadingIndicator.IsRunning = false;
                LoadingIndicator.IsVisible = false;
            }
        }

        private void OnBuscarTextChanged(object sender, TextChangedEventArgs e)
        {
            var q = (EntryBuscar.Text ?? "").Trim().ToLowerInvariant();

            if (string.IsNullOrWhiteSpace(q))
            {
                ListaSucursales.ItemsSource = _data;
                return;
            }

            var filtrado = _data.Where(s =>
                    (s.Codigo ?? "").ToLowerInvariant().Contains(q) ||
                    (s.Nombre ?? "").ToLowerInvariant().Contains(q) ||
                    (s.Ciudad ?? "").ToLowerInvariant().Contains(q))
                .ToList();

            ListaSucursales.ItemsSource = filtrado;
        }

        private async void OnNuevaSucursalClicked(object sender, EventArgs e)
        {
            await Shell.Current.GoToAsync("SucursalEditPage");
        }


        private async void OnDetallesClicked(object sender, EventArgs e)
        {
            if (sender is Button btn && btn.CommandParameter is string codigo)
                await Shell.Current.GoToAsync($"SucursalDetallePage?codigo={Uri.EscapeDataString(codigo)}");
        }



        private async void OnRefrescarClicked(object sender, EventArgs e)
        {
            EntryBuscar.Text = string.Empty;
            await CargarAsync();
        }

        private async void OnEditarClicked(object sender, EventArgs e)
        {
            if (sender is Button btn && btn.CommandParameter is string codigo)
            {
                var suc = await _service.ObtenerSucursalAsync(codigo);
                if (suc == null)
                {
                    await DisplayAlert("Aviso", "No se encontró la sucursal.", "OK");
                    return;
                }
                await Navigation.PushAsync(new SucursalEditPage(suc));
            }
        }

        private async void OnEliminarClicked(object sender, EventArgs e)
        {
            if (sender is Button btn && btn.CommandParameter is string codigo)
            {
                var ok = await DisplayAlert("Confirmar",
                    $"¿Deseas eliminar la sucursal {codigo}?",
                    "Sí, eliminar", "Cancelar");

                if (!ok) return;

                var exito = await _service.EliminarSucursalAsync(codigo);

                if (exito)
                {
                    await DisplayAlert("Listo", "Sucursal eliminada.", "OK");
                    await CargarAsync();
                }
                else
                {
                    await DisplayAlert("Error", "No se pudo eliminar la sucursal.", "OK");
                }
            }
        }

        private async void OnMapaClicked(object sender, EventArgs e)
        {
            if (sender is Button btn && btn.CommandParameter is string codigo)
            {
                var suc = await _service.ObtenerSucursalAsync(codigo);
                if (suc == null)
                {
                    await DisplayAlert("Aviso", "No se encontró la sucursal.", "OK");
                    return;
                }

                // Si hay lat/long, abrimos Google Maps (simple, sin dependencia extra)
                if (suc.Latitud.HasValue && suc.Longitud.HasValue)
                {
                    var url = $"https://www.google.com/maps?q={suc.Latitud.Value},{suc.Longitud.Value}";
                    await Launcher.Default.OpenAsync(url);
                    return;
                }

                // Si no hay coords, buscamos por texto
                var query = Uri.EscapeDataString($"{suc.Nombre} {suc.Direccion} {suc.Ciudad}");
                await Launcher.Default.OpenAsync($"https://www.google.com/maps/search/?api=1&query={query}");
            }
        }

        private async void OnVolverClicked(object sender, EventArgs e)
        {
            await Navigation.PopAsync();
        }

        private async void OnCerrarSesionClicked(object sender, EventArgs e)
        {
            // Ajusta según tu flujo real
            Preferences.Remove("usuario");
            await Navigation.PopToRootAsync();
        }

        private async void OnRutasClicked(object sender, EventArgs e)
        {
            if (sender is Button btn && btn.CommandParameter is string codigo)
                await Shell.Current.GoToAsync($"RutasPage?codigo={Uri.EscapeDataString(codigo)}");
        }

    }
}
