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
        <title>Editar Sucursal - EurekaBank LDU</title>
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
                display: flex;
                align-items: center;
                gap: 15px;
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
                max-width: 1600px;
                margin: 0 auto;
            }

            .content-wrapper {
                display: grid;
                grid-template-columns: 45% 55%;
                gap: 30px;
                min-height: calc(100vh - 150px);
            }

            .form-section {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                padding: 30px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                max-height: calc(100vh - 150px);
                overflow-y: auto;
            }

            .form-section::-webkit-scrollbar {
                width: 10px;
            }

            .form-section::-webkit-scrollbar-track {
                background: #f1f1f1;
                border-radius: 10px;
            }

            .form-section::-webkit-scrollbar-thumb {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                border-radius: 10px;
            }

            .form-section::-webkit-scrollbar-thumb:hover {
                background: linear-gradient(135deg, #900000 0%, #600000 100%);
            }

            .map-section {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                overflow: hidden;
                box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                position: relative;
                height: calc(100vh - 150px);
            }

            .form-title {
                color: #c00000;
                margin-bottom: 20px;
                font-size: 24px;
                display: flex;
                align-items: center;
                gap: 10px;
                padding-bottom: 15px;
                border-bottom: 3px solid #c00000;
            }

            .info-box {
                background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
                border-left: 5px solid #0052a3;
                padding: 15px 20px;
                border-radius: 10px;
                margin-bottom: 25px;
                font-size: 14px;
                color: #1565c0;
                line-height: 1.6;
                display: flex;
                align-items: flex-start;
                gap: 10px;
            }

            .info-box i {
                font-size: 18px;
                margin-top: 2px;
            }

            .form-group {
                margin-bottom: 25px;
            }

            .form-group label {
                display: block;
                margin-bottom: 8px;
                color: #333;
                font-weight: 600;
                font-size: 14px;
                display: flex;
                align-items: center;
                gap: 8px;
            }

            .required {
                color: #c00000;
            }

            .form-group input {
                width: 100%;
                padding: 12px 15px;
                border: 2px solid #e0e0e0;
                border-radius: 12px;
                font-size: 14px;
                transition: all 0.3s ease;
                background: white;
            }

            .form-group input:focus {
                outline: none;
                border-color: #c00000;
                box-shadow: 0 0 0 3px rgba(192,0,0,0.1);
            }

            .form-group input:disabled,
            .form-group input[readonly] {
                background-color: #e9ecef;
                color: #6c757d;
                cursor: not-allowed;
                border-color: #ced4da;
            }

            .help-text {
                display: block;
                color: #6c757d;
                font-size: 12px;
                margin-top: 6px;
                font-style: italic;
                display: flex;
                align-items: center;
                gap: 5px;
            }

            .help-text i {
                font-size: 11px;
            }

            .map-instruction {
                position: absolute;
                top: 20px;
                left: 50%;
                transform: translateX(-50%);
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
                padding: 15px 25px;
                border-radius: 15px;
                font-weight: 600;
                font-size: 14px;
                box-shadow: 0 5px 15px rgba(0,0,0,0.3);
                z-index: 10;
                max-width: 90%;
                text-align: center;
                display: flex;
                align-items: center;
                gap: 10px;
                animation: pulse 2s infinite;
            }

            @keyframes pulse {
                0%, 100% { transform: translateX(-50%) scale(1); }
                50% { transform: translateX(-50%) scale(1.02); }
            }

            #editMap {
                width: 100%;
                height: 100%;
            }

            .btn-submit {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
                border: none;
                padding: 15px 30px;
                border-radius: 12px;
                cursor: pointer;
                font-weight: bold;
                font-size: 16px;
                width: 100%;
                transition: all 0.3s ease;
                box-shadow: 0 3px 10px rgba(192,0,0,0.3);
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 10px;
            }

            .btn-submit:hover {
                background: linear-gradient(135deg, #900000 0%, #600000 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(192,0,0,0.5);
            }

            @media (max-width: 1200px) {
                .content-wrapper {
                    grid-template-columns: 1fr;
                }

                .map-section {
                    height: 500px;
                }

                .form-section {
                    max-height: none;
                }
            }
        </style>
    </head>
    <body>
        <div class="header">
            <h1><i class="fas fa-edit"></i> Editar Sucursal</h1>
            <a href="<%= request.getContextPath()%>/sucursales" class="btn-back">
                <i class="fas fa-arrow-left"></i> Volver a Sucursales
            </a>
        </div>

        <div class="main">
            <div class="content-wrapper">
                <!-- Sección del Formulario -->
                <div class="form-section">
                    <div class="form-title">
                        <i class="fas fa-building"></i>
                        Editar Datos de la Sucursal
                    </div>

                    <div class="info-box">
                        <i class="fas fa-lightbulb"></i>
                        <div>
                            <strong>Instrucción:</strong> Puedes actualizar la ubicación haciendo clic en el mapa o arrastrando el marcador existente.
                        </div>
                    </div>

                    <form action="<%= request.getContextPath()%>/sucursales" method="post">
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="codigo" value="<%= sucursal != null ? sucursal.getCodigo() : "" %>">

                        <div class="form-group">
                            <label><i class="fas fa-hashtag"></i> Código</label>
                            <input type="text" value="<%= sucursal != null ? sucursal.getCodigo() : "" %>" disabled>
                            <span class="help-text">
                                <i class="fas fa-lock"></i>
                                El código no se puede modificar
                            </span>
                        </div>

                        <div class="form-group">
                            <label>
                                <i class="fas fa-building"></i> 
                                Nombre 
                                <span class="required">*</span>
                            </label>
                            <input type="text" 
                                   name="nombre" 
                                   id="nombre" 
                                   value="<%= sucursal != null ? sucursal.getNombre() : "" %>" 
                                   required>
                        </div>

                        <div class="form-group">
                            <label>
                                <i class="fas fa-city"></i> 
                                Ciudad 
                                <span class="required">*</span>
                            </label>
                            <input type="text" 
                                   name="ciudad" 
                                   id="ciudad" 
                                   value="<%= sucursal != null ? sucursal.getCiudad() : "" %>" 
                                   required>
                        </div>

                        <div class="form-group">
                            <label>
                                <i class="fas fa-map-marker-alt"></i> 
                                Dirección
                                <span class="required">*</span>
                            </label>
                            <input type="text" 
                                   name="direccion" 
                                   id="direccion" 
                                   value="<%= sucursal != null ? sucursal.getDireccion() : "" %>" 
                                   required>
                        </div>

                        <div class="form-group">
                            <label>
                                <i class="fas fa-location-arrow"></i> 
                                Latitud 
                                <span class="required">*</span>
                            </label>
                            <input type="number" 
                                   step="0.0000001" 
                                   name="latitud" 
                                   id="latitud" 
                                   value="<%= sucursal != null && sucursal.getLatitud() != null ? sucursal.getLatitud() : "" %>" 
                                   readonly>
                            <span class="help-text">
                                <i class="fas fa-info-circle"></i>
                                Las coordenadas se actualizan haciendo clic en el mapa
                            </span>
                        </div>

                        <div class="form-group">
                            <label>
                                <i class="fas fa-location-arrow"></i> 
                                Longitud 
                                <span class="required">*</span>
                            </label>
                            <input type="number" 
                                   step="0.0000001" 
                                   name="longitud" 
                                   id="longitud" 
                                   value="<%= sucursal != null && sucursal.getLongitud() != null ? sucursal.getLongitud() : "" %>" 
                                   readonly>
                            <span class="help-text">
                                <i class="fas fa-info-circle"></i>
                                Las coordenadas se actualizan haciendo clic en el mapa
                            </span>
                        </div>

                        <button type="submit" class="btn-submit">
                            <i class="fas fa-save"></i>
                            GUARDAR CAMBIOS
                        </button>
                    </form>
                </div>

                <!-- Sección del Mapa -->
                <div class="map-section">
                    <div class="map-instruction">
                        <i class="fas fa-hand-pointer"></i>
                        📍 Ubicación actual - Haz clic para actualizar
                    </div>
                    <div id="editMap"></div>
                </div>
            </div>
        </div>

        <script>
            let editMap;
            let editMarker;
            let apiKey = '';

            // Parsear la API Key del servidor
            try {
                <% if (apiKeyJson != null) { %>
                    const apiKeyData = <%= apiKeyJson %>;
                    apiKey = apiKeyData.apiKey || '';
                <% } %>
            } catch(e) {
                console.error('Error parsing API Key:', e);
            }

            function initEditMap() {
                <% 
                    boolean hasCoords = sucursal != null && 
                                       sucursal.getLatitud() != null && 
                                       sucursal.getLongitud() != null;
                    double lat = hasCoords ? sucursal.getLatitud() : -0.1806532;
                    double lng = hasCoords ? sucursal.getLongitud() : -78.4678382;
                %>

                const initialPosition = { 
                    lat: <%= lat %>, 
                    lng: <%= lng %> 
                };

                // Crear mapa
                editMap = new google.maps.Map(document.getElementById('editMap'), {
                    zoom: <%= hasCoords ? 16 : 7 %>,
                    center: initialPosition,
                    mapTypeControl: true,
                    streetViewControl: true,
                    fullscreenControl: true
                });

                // Crear marcador draggable
                editMarker = new google.maps.Marker({
                    position: initialPosition,
                    map: editMap,
                    draggable: true,
                    animation: google.maps.Animation.DROP,
                    title: '<%= sucursal.getNombre() %>'
                });

                // Actualizar coordenadas cuando se arrastra el marcador
                editMarker.addListener('dragend', function(event) {
                    updateCoordinates(event.latLng.lat(), event.latLng.lng());
                });

                // Actualizar coordenadas cuando se hace clic en el mapa
                editMap.addListener('click', function(event) {
                    const newLat = event.latLng.lat();
                    const newLng = event.latLng.lng();
                    
                    // Mover el marcador a la nueva posición
                    editMarker.setPosition(event.latLng);
                    
                    // Animar el marcador
                    editMarker.setAnimation(google.maps.Animation.BOUNCE);
                    setTimeout(() => {
                        editMarker.setAnimation(null);
                    }, 700);
                    
                    updateCoordinates(newLat, newLng);
                });
            }

            function updateCoordinates(lat, lng) {
                document.getElementById('latitud').value = lat.toFixed(7);
                document.getElementById('longitud').value = lng.toFixed(7);
            }

            // Cargar Google Maps API dinámicamente
            function loadGoogleMaps() {
                if (apiKey && apiKey !== 'API_KEY_NOT_CONFIGURED' && apiKey.trim() !== '') {
                    const script = document.createElement('script');
                    script.src = 'https://maps.googleapis.com/maps/api/js?key=' + apiKey + '&callback=initEditMap';
                    script.async = true;
                    script.defer = true;
                    document.head.appendChild(script);
                } else {
                    document.getElementById('editMap').innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #c00000; font-size: 18px; padding: 20px; text-align: center;"><div>⚠️ Google Maps API Key no configurada.</div></div>';
                }
            }

            // Cargar mapa al cargar la página
            window.addEventListener('load', loadGoogleMaps);
        </script>
    </body>
</html>
