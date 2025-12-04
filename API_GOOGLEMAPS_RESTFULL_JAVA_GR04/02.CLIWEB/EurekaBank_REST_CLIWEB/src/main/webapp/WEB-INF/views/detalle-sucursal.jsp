<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="ec.edu.monster.model.Sucursal"%>
<%
    String usuario = (String) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    Sucursal sucursal = (Sucursal) request.getAttribute("sucursal");
    String apiKeyJson = (String) request.getAttribute("apiKeyJson");
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Detalle Sucursal - EurekaBank LDU</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #1a1a2e 0%, #0f3460 50%, #16213e 100%);
                min-height: 100vh;
                position: relative;
            }

            body::before {
                content: '';
                position: fixed;
                width: 100%;
                height: 100%;
                background-image: radial-gradient(circle, rgba(255,255,255,0.05) 1px, transparent 1px);
                background-size: 30px 30px;
                animation: movePattern 20s linear infinite;
                z-index: 0;
            }

            @keyframes movePattern {
                0% { transform: translateY(0); }
                100% { transform: translateY(30px); }
            }

            .header {
                background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.98) 100%);
                backdrop-filter: blur(10px);
                border-bottom: 5px solid #c00000;
                padding: 20px 40px;
                box-shadow: 0 4px 20px rgba(0,0,0,0.3);
                position: relative;
                z-index: 100;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .header h1 {
                margin: 0;
                background: linear-gradient(135deg, #c00000 0%, #0052a3 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
                font-size: 28px;
                font-weight: 900;
            }

            .btn-back {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: #fff;
                border: none;
                padding: 12px 25px;
                border-radius: 25px;
                cursor: pointer;
                font-weight: bold;
                font-size: 14px;
                text-decoration: none;
                display: flex;
                align-items: center;
                gap: 8px;
                transition: all 0.3s ease;
                box-shadow: 0 3px 10px rgba(192,0,0,0.3);
            }

            .btn-back:hover {
                background: linear-gradient(135deg, #900000 0%, #600000 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(192,0,0,0.5);
            }

            .main {
                position: relative;
                z-index: 1;
                padding: 30px;
                max-width: 1400px;
                margin: 0 auto;
            }

            .content-wrapper {
                display: grid;
                grid-template-columns: 400px 1fr;
                gap: 20px;
                height: calc(100vh - 160px);
            }

            .info-panel {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                padding: 30px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                overflow-y: auto;
            }

            .sucursal-header {
                border-bottom: 3px solid #c00000;
                padding-bottom: 20px;
                margin-bottom: 25px;
            }

            .sucursal-nombre {
                color: #c00000;
                font-size: 26px;
                font-weight: 900;
                margin-bottom: 10px;
            }

            .sucursal-codigo {
                color: #666;
                font-size: 14px;
                font-weight: 600;
                background: #f0f0f0;
                padding: 5px 15px;
                border-radius: 20px;
                display: inline-block;
            }

            .info-group {
                margin-bottom: 25px;
            }

            .info-label {
                color: #0052a3;
                font-size: 12px;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 1px;
                margin-bottom: 8px;
            }

            .info-value {
                color: #333;
                font-size: 16px;
                font-weight: 500;
                padding: 12px 15px;
                background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
                border-radius: 10px;
                border-left: 4px solid #0052a3;
            }

            .coordinates {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 10px;
            }

            .coordinate-box {
                background: linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%);
                padding: 15px;
                border-radius: 12px;
                border: 2px solid #0052a3;
            }

            .coordinate-label {
                font-size: 11px;
                color: #0052a3;
                font-weight: 700;
                margin-bottom: 5px;
            }

            .coordinate-value {
                font-size: 15px;
                color: #333;
                font-weight: 600;
                font-family: 'Courier New', monospace;
            }

            .actions {
                display: flex;
                gap: 10px;
                margin-top: 30px;
            }

            .btn {
                flex: 1;
                padding: 15px;
                border: none;
                border-radius: 12px;
                cursor: pointer;
                font-weight: bold;
                font-size: 14px;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                text-decoration: none;
            }

            .btn-navigate {
                background: linear-gradient(135deg, #28a745 0%, #1e7e34 100%);
                color: white;
            }

            .btn-navigate:hover {
                background: linear-gradient(135deg, #1e7e34 0%, #155724 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(40,167,69,0.3);
            }

            .btn-edit {
                background: linear-gradient(135deg, #0052a3 0%, #003d7a 100%);
                color: white;
            }

            .btn-edit:hover {
                background: linear-gradient(135deg, #003d7a 0%, #002a54 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(0,82,163,0.3);
            }

            .map-container {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                overflow: hidden;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                position: relative;
                display: flex;
                flex-direction: column;
            }

            #map {
                width: 100%;
                height: 100%;
            }

            .map-overlay {
                position: absolute;
                top: 20px;
                left: 20px;
                background: rgba(255,255,255,0.95);
                padding: 15px 20px;
                border-radius: 12px;
                box-shadow: 0 5px 20px rgba(0,0,0,0.3);
                z-index: 10;
            }

            .map-overlay-title {
                font-size: 16px;
                font-weight: 700;
                color: #c00000;
                margin-bottom: 5px;
            }

            .map-overlay-text {
                font-size: 13px;
                color: #666;
            }

            @media (max-width: 1024px) {
                .content-wrapper {
                    grid-template-columns: 1fr;
                }

                #map {
                    height: 500px;
                }
            }
        </style>
    </head>
    <body>
        <div class="header">
            <h1><i class="fas fa-store"></i> Detalle de Sucursal</h1>
            <a href="<%= request.getContextPath()%>/sucursales" class="btn-back">
                ← Volver a Sucursales
            </a>
        </div>

        <div class="main">
            <% if (sucursal != null) { %>
                <div class="content-wrapper">
                    <div class="info-panel">
                        <div class="sucursal-header">
                            <div class="sucursal-nombre"><%= sucursal.getNombre() %></div>
                            <span class="sucursal-codigo">Código: <%= sucursal.getCodigo() %></span>
                        </div>

                        <div class="info-group">
                            <div class="info-label"><i class="fas fa-building"></i> Ciudad</div>
                            <div class="info-value"><%= sucursal.getCiudad() %></div>
                        </div>

                        <div class="info-group">
                            <div class="info-label"><i class="fas fa-location-dot"></i> Direccion</div>
                            <div class="info-value"><%= sucursal.getDireccion() %></div>
                        </div>

                        <div class="info-group">
                            <div class="info-label"><i class="fas fa-earth-americas"></i> Coordenadas GPS</div>
                            <div class="coordinates">
                                <div class="coordinate-box">
                                    <div class="coordinate-label">LATITUD</div>
                                    <div class="coordinate-value"><%= sucursal.getLatitud() != null ? String.format("%.7f", sucursal.getLatitud()) : "N/A" %></div>
                                </div>
                                <div class="coordinate-box">
                                    <div class="coordinate-label">LONGITUD</div>
                                    <div class="coordinate-value"><%= sucursal.getLongitud() != null ? String.format("%.7f", sucursal.getLongitud()) : "N/A" %></div>
                                </div>
                            </div>
                        </div>

                        <div class="info-group">
                            <div class="info-label"><i class="fas fa-user-group"></i> Cuentas Registradas</div>
                            <div class="info-value"><%= sucursal.getContadorCuentas() %> cuentas</div>
                        </div>

                        <div class="actions">
                            <% if (sucursal.getLatitud() != null && sucursal.getLongitud() != null) { %>
                                <a href="https://www.google.com/maps/dir/?api=1&destination=<%= sucursal.getLatitud() %>,<%= sucursal.getLongitud() %>&travelmode=driving" 
                                   target="_blank" 
                                   class="btn btn-navigate">
                                    <i class="fas fa-directions"></i> Cómo Llegar
                                </a>
                            <% } %>
                            <button onclick="window.location.href='<%= request.getContextPath()%>/sucursales'" class="btn btn-edit">
                                <i class="fas fa-arrow-circle-left"></i> Volver
                            </button>
                        </div>
                    </div>

                    <div class="map-container">
                        <div class="map-overlay">
                            <div class="map-overlay-title"><%= sucursal.getNombre() %></div>
                            <div class="map-overlay-text"><%= sucursal.getCiudad() %></div>
                        </div>
                        <div id="map"></div>
                    </div>
                </div>
            <% } else { %>
                <div style="background: rgba(255,255,255,0.98); padding: 40px; border-radius: 20px; text-align: center;">
                    <h2 style="color: #c00000;">⚠️ Sucursal no encontrada</h2>
                    <p style="color: #666; margin: 20px 0;">La sucursal solicitada no existe o fue eliminada.</p>
                    <a href="<%= request.getContextPath()%>/sucursales" class="btn btn-back" style="display: inline-flex; margin: 20px auto;">
                        ← Volver a Sucursales
                    </a>
                </div>
            <% } %>
        </div>

        <script>
            let map;
            let apiKey = '';

            // Parsear la API Key del servidor
            try {
                const apiKeyData = <%= apiKeyJson %>;
                apiKey = apiKeyData.apiKey || '';
            } catch(e) {
                console.error('Error parsing API Key:', e);
            }

            function initMap() {
                <% if (sucursal != null && sucursal.getLatitud() != null && sucursal.getLongitud() != null) { %>
                    const sucursalLocation = { 
                        lat: <%= sucursal.getLatitud() %>, 
                        lng: <%= sucursal.getLongitud() %> 
                    };

                    map = new google.maps.Map(document.getElementById('map'), {
                        zoom: 16,
                        center: sucursalLocation,
                        mapTypeControl: true,
                        streetViewControl: true,
                        fullscreenControl: true
                    });

                    // Marcador principal de la sucursal
                    const marker = new google.maps.Marker({
                        position: sucursalLocation,
                        map: map,
                        title: '<%= sucursal.getNombre() %>',
                        animation: google.maps.Animation.BOUNCE,
                        icon: {
                            path: google.maps.SymbolPath.CIRCLE,
                            scale: 15,
                            fillColor: '#c00000',
                            fillOpacity: 1,
                            strokeColor: '#ffffff',
                            strokeWeight: 3
                        }
                    });

                    // Detener la animación después de 2 segundos
                    setTimeout(() => {
                        marker.setAnimation(null);
                    }, 2000);

                    // InfoWindow con informacion de la sucursal
                    const infoWindow = new google.maps.InfoWindow({
                        content: `
                            <div style="padding: 10px; font-family: 'Segoe UI', sans-serif;">
                                <h3 style="color: #c00000; margin: 0 0 10px 0; font-size: 16px;">
                                    <%= sucursal.getNombre() %>
                                </h3>
                                <p style="margin: 5px 0; color: #666; font-size: 13px;">
                                    <strong>📍</strong> <%= sucursal.getDireccion() %>
                                </p>
                                <p style="margin: 5px 0; color: #666; font-size: 13px;">
                                    <strong>🏙️</strong> <%= sucursal.getCiudad() %>
                                </p>
                                <p style="margin: 5px 0; color: #0052a3; font-size: 12px;">
                                    <strong>Coordenadas:</strong> <%= String.format("%.7f", sucursal.getLatitud()) %>, <%= String.format("%.7f", sucursal.getLongitud()) %>
                                </p>
                            </div>
                        `
                    });

                    // Abrir InfoWindow automáticamente
                    infoWindow.open(map, marker);

                    // Evento click en el marcador
                    marker.addListener('click', () => {
                        infoWindow.open(map, marker);
                    });

                    // Círculo de área de cobertura (opcional)
                    new google.maps.Circle({
                        map: map,
                        center: sucursalLocation,
                        radius: 500, // 500 metros de radio
                        fillColor: '#c00000',
                        fillOpacity: 0.1,
                        strokeColor: '#c00000',
                        strokeOpacity: 0.3,
                        strokeWeight: 2
                    });
                <% } else { %>
                    // Sin coordenadas, mostrar Ecuador por defecto
                    const ecuador = { lat: -0.1806532, lng: -78.4678382 };
                    map = new google.maps.Map(document.getElementById('map'), {
                        zoom: 7,
                        center: ecuador
                    });
                <% } %>
            }

            // Cargar Google Maps API dinámicamente
            function loadGoogleMaps() {
                if (apiKey && apiKey !== 'API_KEY_NOT_CONFIGURED') {
                    const script = document.createElement('script');
                    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&callback=initMap`;
                    script.async = true;
                    script.defer = true;
                    document.head.appendChild(script);
                } else {
                    document.getElementById('map').innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #c00000; font-size: 18px; padding: 20px; text-align: center;"><div>⚠️ Google Maps API Key no configurada.<br>Por favor, configure la API Key en el servidor.</div></div>';
                }
            }

            // Cargar mapa al cargar la página
            window.addEventListener('load', loadGoogleMaps);
        </script>
    </body>
</html>
