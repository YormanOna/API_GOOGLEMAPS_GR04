<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String usuario = (String) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    String apiKeyJson = (String) request.getAttribute("apiKeyJson");
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Nueva Sucursal - EurekaBank LDU</title>
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
                overflow-x: hidden;
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
                max-width: 1200px;
                margin: 0 auto;
            }

            .content-wrapper {
                display: grid;
                grid-template-columns: 450px 1fr;
                gap: 20px;
                min-height: calc(100vh - 200px);
            }

            .form-panel {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                padding: 30px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                overflow-y: auto;
                max-height: calc(100vh - 180px);
            }

            .form-panel h2 {
                color: #c00000;
                margin-bottom: 25px;
                font-size: 24px;
                border-bottom: 3px solid #c00000;
                padding-bottom: 15px;
            }

            .info-box {
                background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
                border-left: 4px solid #c00000;
                padding: 15px;
                margin-bottom: 25px;
                border-radius: 8px;
                font-size: 14px;
                color: #666;
            }

            .form-group {
                margin-bottom: 20px;
            }

            .form-group label {
                display: block;
                margin-bottom: 8px;
                color: #333;
                font-weight: 600;
                font-size: 14px;
            }

            .form-group label .required {
                color: #c00000;
                margin-left: 3px;
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

            .form-group input:read-only {
                background: #f5f5f5;
                cursor: not-allowed;
            }

            .coordinate-info {
                font-size: 12px;
                color: #0052a3;
                margin-top: 5px;
                display: flex;
                align-items: center;
                gap: 5px;
            }

            .btn-submit {
                width: 100%;
                padding: 15px;
                border: none;
                border-radius: 12px;
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
                font-size: 16px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.3s ease;
                margin-top: 10px;
            }

            .btn-submit:hover {
                background: linear-gradient(135deg, #900000 0%, #600000 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(192,0,0,0.3);
            }

            .btn-submit:disabled {
                background: #ccc;
                cursor: not-allowed;
                transform: none;
            }

            .map-container {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                overflow: hidden;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                position: relative;
            }

            .map-instructions {
                position: absolute;
                top: 20px;
                left: 50%;
                transform: translateX(-50%);
                background: rgba(192,0,0,0.95);
                color: white;
                padding: 15px 25px;
                border-radius: 12px;
                font-size: 14px;
                font-weight: 600;
                z-index: 10;
                box-shadow: 0 5px 20px rgba(0,0,0,0.3);
                animation: pulse 2s infinite;
            }

            @keyframes pulse {
                0%, 100% { transform: translateX(-50%) scale(1); }
                50% { transform: translateX(-50%) scale(1.05); }
            }

            #map {
                width: 100%;
                height: calc(100vh - 180px);
            }

            .alert {
                padding: 15px 20px;
                margin-bottom: 20px;
                border-radius: 12px;
                font-size: 14px;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .alert-error {
                background: #f8d7da;
                color: #721c24;
                border: 2px solid #f5c6cb;
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
            <h1><i class="fas fa-store-slash"></i> Nueva Sucursal</h1>
            <a href="<%= request.getContextPath()%>/sucursales" class="btn-back">
                ← Volver a Sucursales
            </a>
        </div>

        <div class="main">
            <div class="content-wrapper">
                <div class="form-panel">
                    <h2>📋 Datos de la Sucursal</h2>

                    <div class="info-box">
                        <i class="fas fa-circle-info" style="color: #0052a3;"></i> <strong>Instruccion:</strong> Haz clic en el mapa para seleccionar la ubicacion de la sucursal. 
                        Los datos de ciudad, latitud y longitud se llenaran automaticamente.
                    </div>

                    <form action="<%= request.getContextPath()%>/sucursales" method="post" id="formSucursal">
                        <input type="hidden" name="action" value="create">

                        <div class="form-group">
                            <label><i class="fas fa-hashtag"></i> Codigo <span class="required">*</span></label>
                            <input type="text" name="codigo" id="codigo" required placeholder="Ej: 008 (max 3 caracteres)" maxlength="3">
                            <div class="coordinate-info">
                                <i class="fas fa-circle-info"></i> Solo 3 caracteres permitidos (Ej: 001, 008, ABC)
                            </div>
                        </div>

                        <div class="form-group">
                            <label><i class="fas fa-store"></i> Nombre <span class="required">*</span></label>
                            <input type="text" name="nombre" id="nombre" required placeholder="Ej: Sucursal Centro Historico">
                        </div>

                        <div class="form-group">
                            <label><i class="fas fa-building"></i> Ciudad <span class="required">*</span></label>
                            <input type="text" name="ciudad" id="ciudad" required placeholder="Selecciona en el mapa o escribe...">
                            <div class="coordinate-info">
                                <i class="fas fa-location-dot"></i> Se detectara automaticamente al hacer clic en el mapa
                            </div>
                        </div>

                        <div class="form-group">
                            <label><i class="fas fa-road"></i> Direccion <span class="required">*</span></label>
                            <input type="text" name="direccion" id="direccion" required placeholder="Ej: Av. Simon Bolivar y Calle Garcia Moreno">
                        </div>

                        <div class="form-group">
                            <label><i class="fas fa-map-pin"></i> Latitud <span class="required">*</span></label>
                            <input type="number" step="0.0000001" name="latitud" id="latitud" required readonly placeholder="Latitud...">
                            <div class="coordinate-info">
                                <i class="fas fa-earth-americas"></i> Se llenara al hacer clic en el mapa
                            </div>
                        </div>

                        <div class="form-group">
                            <label><i class="fas fa-map-pin"></i> Longitud <span class="required">*</span></label>
                            <input type="number" step="0.0000001" name="longitud" id="longitud" required readonly placeholder="Longitud...">
                            <div class="coordinate-info">
                                <i class="fas fa-earth-americas"></i> Se llenara al hacer clic en el mapa
                            </div>
                        </div>

                        <button type="submit" class="btn-submit" id="btnSubmit" disabled>
                            <i class="fas fa-check-circle"></i> Crear Sucursal
                        </button>
                    </form>
                </div>

                <div class="map-container">
                    <div class="map-instructions" id="mapInstructions">
                        <i class="fas fa-computer-mouse"></i> Haz clic en el mapa para ubicar la sucursal
                    </div>
                    <div id="map"></div>
                </div>
            </div>
        </div>

        <script>
            let map;
            let marker;
            let geocoder;
            let apiKey = '';

            // Parsear la API Key del servidor
            try {
                const apiKeyData = <%= apiKeyJson %>;
                apiKey = apiKeyData.apiKey || '';
                console.log('API Key configurada:', apiKey ? 'SI' : 'NO');
                console.log('API Key length:', apiKey ? apiKey.length : 0);
            } catch(e) {
                console.error('Error parsing API Key:', e);
                console.error('apiKeyJson recibido:', '<%= apiKeyJson %>');
            }

            function initMap() {
                // Centro inicial en Ecuador (Quito)
                const ecuador = { lat: -0.1806532, lng: -78.4678382 };

                map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 7,
                    center: ecuador,
                    mapTypeControl: true,
                    streetViewControl: false,
                    fullscreenControl: true
                });

                geocoder = new google.maps.Geocoder();

                // Agregar evento de clic en el mapa
                map.addListener('click', (event) => {
                    addMarker(event.latLng);
                    getAddressFromLatLng(event.latLng);
                });

                // Instrucciones que desaparecen después de 5 segundos
                setTimeout(() => {
                    const instructions = document.getElementById('mapInstructions');
                    if (instructions) {
                        instructions.style.opacity = '0';
                        instructions.style.transition = 'opacity 0.5s ease';
                        setTimeout(() => instructions.remove(), 500);
                    }
                }, 5000);
            }

            function addMarker(location) {
                // Remover marcador anterior si existe
                if (marker) {
                    marker.setMap(null);
                }

                // Crear nuevo marcador
                marker = new google.maps.Marker({
                    position: location,
                    map: map,
                    animation: google.maps.Animation.DROP,
                    title: 'Nueva Sucursal'
                });

                // Llenar coordenadas
                document.getElementById('latitud').value = location.lat().toFixed(7);
                document.getElementById('longitud').value = location.lng().toFixed(7);

                // Centrar mapa en el marcador
                map.panTo(location);
                map.setZoom(15);

                // Habilitar botón de envío
                checkFormCompletion();
            }

            function getAddressFromLatLng(latLng) {
                geocoder.geocode({ location: latLng }, (results, status) => {
                    if (status === 'OK' && results[0]) {
                        console.log('Geocode result:', results[0]);
                        
                        // Buscar ciudad en los componentes de dirección
                        let ciudad = '';
                        let pais = '';
                        let direccion = results[0].formatted_address;

                        for (let component of results[0].address_components) {
                            if (component.types.includes('locality')) {
                                ciudad = component.long_name;
                            } else if (component.types.includes('administrative_area_level_2')) {
                                if (!ciudad) ciudad = component.long_name;
                            } else if (component.types.includes('administrative_area_level_1')) {
                                if (!ciudad) ciudad = component.long_name;
                            } else if (component.types.includes('country')) {
                                pais = component.long_name;
                            }
                        }

                        // Llenar campo ciudad (usar país si no hay ciudad)
                        const ciudadInput = document.getElementById('ciudad');
                        if (ciudad) {
                            ciudadInput.value = ciudad;
                        } else if (pais) {
                            ciudadInput.value = pais;
                        } else {
                            ciudadInput.value = 'Ecuador';
                        }
                        
                        // Llenar dirección siempre
                        const direccionInput = document.getElementById('direccion');
                        direccionInput.value = direccion;

                        console.log('Ciudad detectada:', ciudadInput.value);
                        console.log('Direccion detectada:', direccion);
                        
                        checkFormCompletion();
                    } else {
                        console.error('Geocode error:', status);
                        // Asignar valores por defecto
                        document.getElementById('ciudad').value = 'Ecuador';
                        document.getElementById('direccion').value = 'Direccion en Ecuador';
                        checkFormCompletion();
                    }
                });
            }

            function checkFormCompletion() {
                const codigo = document.getElementById('codigo').value;
                const nombre = document.getElementById('nombre').value;
                const ciudad = document.getElementById('ciudad').value;
                const direccion = document.getElementById('direccion').value;
                const latitud = document.getElementById('latitud').value;
                const longitud = document.getElementById('longitud').value;
                const btnSubmit = document.getElementById('btnSubmit');

                if (codigo && nombre && ciudad && direccion && latitud && longitud) {
                    btnSubmit.disabled = false;
                } else {
                    btnSubmit.disabled = true;
                }
            }

            // Verificar cada vez que se editen los campos
            document.getElementById('codigo').addEventListener('input', checkFormCompletion);
            document.getElementById('nombre').addEventListener('input', checkFormCompletion);
            document.getElementById('ciudad').addEventListener('input', checkFormCompletion);
            document.getElementById('direccion').addEventListener('input', checkFormCompletion);

            // Agregar logging al envío del formulario
            document.getElementById('formSucursal').addEventListener('submit', function(e) {
                const formData = new FormData(this);
                console.log('=== ENVIANDO FORMULARIO ===');
                console.log('Action:', formData.get('action'));
                console.log('Código:', formData.get('codigo'));
                console.log('Nombre:', formData.get('nombre'));
                console.log('Ciudad:', formData.get('ciudad'));
                console.log('Direccion:', formData.get('direccion'));
                console.log('Latitud:', formData.get('latitud'));
                console.log('Longitud:', formData.get('longitud'));
                console.log('URL destino:', this.action);
                console.log('===========================');
            });

            // Cargar Google Maps API dinámicamente
            function loadGoogleMaps() {
                console.log('loadGoogleMaps - apiKey:', apiKey);
                if (apiKey && apiKey !== 'API_KEY_NOT_CONFIGURED' && apiKey.trim() !== '') {
                    const script = document.createElement('script');
                    script.src = 'https://maps.googleapis.com/maps/api/js?key=' + apiKey + '&callback=initMap';
                    script.async = true;
                    script.defer = true;
                    console.log('Cargando Google Maps con URL:', script.src);
                    document.head.appendChild(script);
                } else {
                    console.error('API Key no válida:', apiKey);
                    document.getElementById('map').innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #c00000; font-size: 18px; padding: 20px; text-align: center;"><div>⚠️ Google Maps API Key no configurada.<br>Por favor, configure la API Key en el servidor.</div></div>';
                }
            }

            // Cargar mapa al cargar la página
            window.addEventListener('load', loadGoogleMaps);
        </script>
    </body>
</html>
