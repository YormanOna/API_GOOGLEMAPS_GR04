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
        <title>Como llegar - EurekaBank LDU</title>
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

            .header-left {
                display: flex;
                align-items: center;
                gap: 15px;
            }

            .header-icon {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
                width: 45px;
                height: 45px;
                border-radius: 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 24px;
                box-shadow: 0 3px 10px rgba(192,0,0,0.3);
            }

            .header-title {
                display: flex;
                flex-direction: column;
            }

            .header-title h1 {
                margin: 0;
                background: linear-gradient(135deg, #c00000 0%, #0052a3 100%);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
                font-size: 28px;
                font-weight: 900;
            }

            .header-title p {
                color: #666;
                font-size: 14px;
                margin-top: 2px;
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

            .main-container {
                position: relative;
                z-index: 1;
                display: flex;
                height: calc(100vh - 95px);
            }

            .sidebar {
                width: 420px;
                background: rgba(255,255,255,0.98);
                overflow-y: auto;
                box-shadow: 4px 0 20px rgba(0,0,0,0.3);
            }

            .sidebar-section {
                padding: 25px;
                border-bottom: 1px solid #e0e0e0;
            }

            .section-title {
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 15px;
                font-size: 20px;
                font-weight: bold;
            }

            .section-title.destino {
                color: #c00000;
            }

            .section-title.origen {
                color: #0052a3;
            }

            .destino-card {
                background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
                border: 2px solid #e0e0e0;
                border-radius: 15px;
                padding: 20px;
            }

            .destino-card h3 {
                color: #333;
                font-size: 22px;
                margin-bottom: 15px;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .destino-info {
                display: flex;
                flex-direction: column;
                gap: 10px;
            }

            .info-row {
                display: flex;
                align-items: start;
                gap: 10px;
                color: #555;
                font-size: 14px;
            }

            .info-row i {
                color: #c00000;
                width: 20px;
                margin-top: 2px;
            }

            .origen-input-group {
                position: relative;
            }

            .origen-input {
                width: 100%;
                padding: 14px 16px;
                border: 2px solid #ddd;
                border-radius: 10px;
                font-size: 15px;
                transition: all 0.3s ease;
                font-family: inherit;
            }

            .origen-input:focus {
                outline: none;
                border-color: #0052a3;
                box-shadow: 0 0 0 3px rgba(0,82,163,0.1);
            }

            .input-hint {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-top: 10px;
                padding: 10px;
                background: #fff3cd;
                border-radius: 8px;
                font-size: 13px;
                color: #856404;
            }

            .btn-select-map {
                width: 100%;
                margin-top: 15px;
                padding: 14px;
                background: linear-gradient(135deg, #28a745 0%, #218838 100%);
                color: white;
                border: none;
                border-radius: 10px;
                font-size: 15px;
                font-weight: bold;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                transition: all 0.3s ease;
                box-shadow: 0 3px 10px rgba(40,167,69,0.3);
            }

            .btn-select-map:hover {
                background: linear-gradient(135deg, #218838 0%, #1e7e34 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(40,167,69,0.5);
            }

            .btn-select-map.active {
                background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                animation: pulse 1.5s infinite;
            }

            @keyframes pulse {
                0%, 100% { transform: scale(1); }
                50% { transform: scale(1.03); }
            }

            .map-selection-alert {
                display: none;
                margin-top: 15px;
                padding: 12px;
                background: linear-gradient(135deg, #0052a3 0%, #003d7a 100%);
                color: white;
                border-radius: 10px;
                text-align: center;
                font-weight: bold;
                animation: pulse 1.5s infinite;
            }

            .map-selection-alert.active {
                display: block;
            }

            .travel-mode-selector {
                padding: 20px 25px;
                border-bottom: 1px solid #e0e0e0;
            }

            .travel-modes {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 12px;
                margin-top: 15px;
            }

            .mode-btn {
                padding: 18px;
                border: 3px solid #e0e0e0;
                border-radius: 12px;
                background: white;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 8px;
                font-size: 14px;
                font-weight: 600;
                color: #555;
            }

            .mode-btn:hover {
                border-color: #0052a3;
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(0,82,163,0.2);
            }

            .mode-btn.active {
                border-color: #0052a3;
                background: linear-gradient(135deg, #e3f2fd 0%, #ffffff 100%);
                color: #0052a3;
            }

            .mode-btn i {
                font-size: 32px;
            }

            .btn-calculate {
                width: calc(100% - 50px);
                margin: 25px;
                padding: 16px;
                background: linear-gradient(135deg, #0052a3 0%, #003d7a 100%);
                color: white;
                border: none;
                border-radius: 12px;
                font-size: 16px;
                font-weight: bold;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 10px;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(0,82,163,0.3);
            }

            .btn-calculate:hover {
                background: linear-gradient(135deg, #003d7a 0%, #002952 100%);
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(0,82,163,0.5);
            }

            .btn-calculate:disabled {
                background: #ccc;
                cursor: not-allowed;
                transform: none;
            }

            .results-section {
                display: none;
                padding: 25px;
                border-bottom: 1px solid #e0e0e0;
            }

            .results-section.active {
                display: block;
            }

            .route-summary {
                background: linear-gradient(135deg, #0052a3 0%, #003d7a 100%);
                color: white;
                padding: 20px;
                border-radius: 12px;
                margin-bottom: 20px;
                display: flex;
                justify-content: space-around;
                text-align: center;
            }

            .summary-item {
                display: flex;
                flex-direction: column;
                gap: 5px;
            }

            .summary-value {
                font-size: 24px;
                font-weight: bold;
            }

            .summary-label {
                font-size: 13px;
                opacity: 0.9;
            }

            .directions-list {
                max-height: 400px;
                overflow-y: auto;
            }

            .direction-step {
                padding: 15px;
                border: 2px solid #e0e0e0;
                border-radius: 10px;
                margin-bottom: 10px;
                cursor: pointer;
                transition: all 0.3s ease;
                background: white;
            }

            .direction-step:hover {
                border-color: #0052a3;
                box-shadow: 0 3px 10px rgba(0,82,163,0.2);
                transform: translateX(5px);
            }

            .step-number {
                display: inline-block;
                background: #0052a3;
                color: white;
                width: 28px;
                height: 28px;
                border-radius: 50%;
                text-align: center;
                line-height: 28px;
                font-weight: bold;
                margin-right: 10px;
            }

            .step-instruction {
                color: #333;
                font-size: 14px;
                margin: 8px 0;
            }

            .step-distance {
                color: #666;
                font-size: 12px;
            }

            .map-container {
                flex: 1;
                position: relative;
            }

            #map {
                width: 100%;
                height: 100%;
            }

            .map-controls {
                position: absolute;
                top: 15px;
                left: 15px;
                z-index: 10;
                display: flex;
                gap: 5px;
                background: white;
                border-radius: 10px;
                padding: 5px;
                box-shadow: 0 3px 15px rgba(0,0,0,0.3);
            }

            .map-control-btn {
                padding: 10px 18px;
                border: none;
                background: white;
                cursor: pointer;
                font-size: 14px;
                font-weight: 600;
                color: #555;
                border-radius: 8px;
                transition: all 0.3s ease;
            }

            .map-control-btn:hover {
                background: #f0f0f0;
            }

            .map-control-btn.active {
                background: #0052a3;
                color: white;
            }

            .map-click-message {
                display: none;
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                z-index: 15;
                background: rgba(0,82,163,0.95);
                color: white;
                padding: 30px 50px;
                border-radius: 20px;
                font-size: 28px;
                font-weight: bold;
                box-shadow: 0 10px 40px rgba(0,0,0,0.5);
                animation: pulse 1.5s infinite;
                pointer-events: none;
            }

            .map-click-message.active {
                display: block;
            }

            .loading-overlay {
                display: none;
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                z-index: 20;
                background: rgba(255,255,255,0.95);
                padding: 30px 50px;
                border-radius: 15px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                text-align: center;
            }

            .loading-overlay.active {
                display: block;
            }

            .spinner {
                border: 4px solid #f3f3f3;
                border-top: 4px solid #0052a3;
                border-radius: 50%;
                width: 50px;
                height: 50px;
                animation: spin 1s linear infinite;
                margin: 0 auto 15px;
            }

            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }

            .sidebar::-webkit-scrollbar,
            .directions-list::-webkit-scrollbar {
                width: 8px;
            }

            .sidebar::-webkit-scrollbar-track,
            .directions-list::-webkit-scrollbar-track {
                background: #f1f1f1;
            }

            .sidebar::-webkit-scrollbar-thumb,
            .directions-list::-webkit-scrollbar-thumb {
                background: #c00000;
                border-radius: 4px;
            }

            .sidebar::-webkit-scrollbar-thumb:hover,
            .directions-list::-webkit-scrollbar-thumb:hover {
                background: #900000;
            }
        </style>
    </head>
    <body>
        <div class="header">
            <div class="header-left">
                <div class="header-icon">
                    <i class="fas fa-route"></i>
                </div>
                <div class="header-title">
                    <h1>Como llegar</h1>
                    <p>Calcular ruta a la sucursal</p>
                </div>
            </div>
            <a href="<%= request.getContextPath() %>/sucursales" class="btn-back">
                <i class="fas fa-arrow-left"></i>
                Volver a Sucursales
            </a>
        </div>

        <div class="main-container">
            <div class="sidebar">
                <div class="sidebar-section">
                    <div class="section-title destino">
                        <i class="fas fa-map-marker-alt"></i>
                        Destino
                    </div>
                    <div class="destino-card">
                        <h3>
                            <i class="fas fa-building"></i>
                            <%= sucursal.getNombre() %>
                        </h3>
                        <div class="destino-info">
                            <div class="info-row">
                                <i class="fas fa-city"></i>
                                <span><strong>Ciudad:</strong> <%= sucursal.getCiudad() %></span>
                            </div>
                            <div class="info-row">
                                <i class="fas fa-location-dot"></i>
                                <span><strong>Direccion:</strong> <%= sucursal.getDireccion() %></span>
                            </div>
                            <div class="info-row">
                                <i class="fas fa-tag"></i>
                                <span><strong>Codigo:</strong> <%= sucursal.getCodigo() %></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="sidebar-section">
                    <div class="section-title origen">
                        <i class="fas fa-location-crosshairs"></i>
                        Origen
                    </div>
                    <p style="color: #666; font-size: 14px; margin-bottom: 12px;">
                        Ingrese su ubicacion:
                    </p>
                    <div class="origen-input-group">
                        <input type="text" 
                               id="origenInput" 
                               class="origen-input" 
                               placeholder="Ej: Av. Atahualpa OE3192Y, Quito 170147, Ecuador">
                    </div>
                    <div class="input-hint">
                        <i class="fas fa-lightbulb"></i>
                        <span>Tambien puede seleccionar un punto en el mapa</span>
                    </div>
                    <button id="btnSelectMap" class="btn-select-map">
                        <i class="fas fa-map-location-dot"></i>
                        Seleccionar en mapa
                    </button>
                    <div id="mapSelectionAlert" class="map-selection-alert">
                        HAZ CLIC EN EL MAPA
                    </div>
                </div>

                <div class="travel-mode-selector">
                    <div class="section-title" style="color: #555;">
                        <i class="fas fa-car"></i>
                        Modo de viaje
                    </div>
                    <div class="travel-modes">
                        <div class="mode-btn active" data-mode="DRIVING">
                            <i class="fas fa-car"></i>
                            <span>Automovil</span>
                        </div>
                        <div class="mode-btn" data-mode="WALKING">
                            <i class="fas fa-person-walking"></i>
                            <span>Caminando</span>
                        </div>
                        <div class="mode-btn" data-mode="TRANSIT">
                            <i class="fas fa-bus"></i>
                            <span>Transporte</span>
                        </div>
                        <div class="mode-btn" data-mode="BICYCLING">
                            <i class="fas fa-bicycle"></i>
                            <span>Bicicleta</span>
                        </div>
                    </div>
                </div>

                <button id="btnCalculate" class="btn-calculate" disabled>
                    <i class="fas fa-route"></i>
                    Calcular Ruta
                </button>

                <div id="resultsSection" class="results-section">
                    <div class="section-title" style="color: #0052a3;">
                        <i class="fas fa-info-circle"></i>
                        Resumen del viaje
                    </div>
                    <div class="route-summary">
                        <div class="summary-item">
                            <div id="routeDistance" class="summary-value">-</div>
                            <div class="summary-label">Distancia</div>
                        </div>
                        <div class="summary-item">
                            <div id="routeDuration" class="summary-value">-</div>
                            <div class="summary-label">Tiempo</div>
                        </div>
                    </div>

                    <div class="section-title" style="color: #555; margin-bottom: 15px;">
                        <i class="fas fa-list-ol"></i>
                        Indicaciones paso a paso
                    </div>
                    <div id="directionsList" class="directions-list">
                    </div>
                </div>
            </div>

            <div class="map-container">
                <div class="map-controls">
                    <button class="map-control-btn active" data-type="roadmap">Mapa</button>
                    <button class="map-control-btn" data-type="satellite">Satelite</button>
                    <button class="map-control-btn" data-type="terrain">Terreno</button>
                </div>
                <div id="mapClickMessage" class="map-click-message">
                    HAZ CLIC AQUI
                </div>
                <div id="loadingOverlay" class="loading-overlay">
                    <div class="spinner"></div>
                    <div style="color: #0052a3; font-weight: bold; font-size: 16px;">
                        Calculando ruta...
                    </div>
                </div>
                <div id="map"></div>
            </div>
        </div>

        <script>
            var map;
            var directionsService;
            var directionsRenderer;
            var geocoder;
            var autocomplete;
            var originMarker;
            var destinationMarker;
            var isSelectingOnMap = false;
            var selectedTravelMode = 'DRIVING';
            var originLocation = null;
            var apiKey = '';

            var destination = {
                lat: <%= sucursal.getLatitud() %>,
                lng: <%= sucursal.getLongitud() %>,
                name: '<%= sucursal.getNombre() %>',
                address: '<%= sucursal.getDireccion() %>'
            };

            try {
                var apiKeyData = <%= apiKeyJson %>;
                apiKey = apiKeyData.apiKey || '';
                console.log('API Key configurada:', apiKey ? 'SI' : 'NO');
            } catch(e) {
                console.error('Error parsing API Key:', e);
            }

            function initMap() {
                console.log('initMap llamado');
                map = new google.maps.Map(document.getElementById('map'), {
                    center: destination,
                    zoom: 13,
                    mapTypeControl: false,
                    streetViewControl: false,
                    fullscreenControl: true
                });

                directionsService = new google.maps.DirectionsService();
                directionsRenderer = new google.maps.DirectionsRenderer({
                    map: map,
                    suppressMarkers: true,
                    polylineOptions: {
                        strokeColor: '#0052a3',
                        strokeWeight: 5,
                        strokeOpacity: 0.8
                    }
                });
                geocoder = new google.maps.Geocoder();

                destinationMarker = new google.maps.Marker({
                    position: destination,
                    map: map,
                    title: destination.name,
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 25,
                        fillColor: '#c00000',
                        fillOpacity: 1,
                        strokeColor: '#ffffff',
                        strokeWeight: 3
                    },
                    label: {
                        text: 'B',
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: 'bold'
                    }
                });

                autocomplete = new google.maps.places.Autocomplete(
                    document.getElementById('origenInput'),
                    {
                        componentRestrictions: { country: 'ec' },
                        fields: ['geometry', 'formatted_address']
                    }
                );

                autocomplete.addListener('place_changed', onPlaceChanged);

                map.addListener('click', function(event) {
                    if (isSelectingOnMap) {
                        setOriginFromMap(event.latLng);
                    }
                });

                setupEventListeners();
            }

            function setupEventListeners() {
                document.getElementById('btnSelectMap').addEventListener('click', function() {
                    isSelectingOnMap = !isSelectingOnMap;
                    const btn = this;
                    const alert = document.getElementById('mapSelectionAlert');
                    const message = document.getElementById('mapClickMessage');

                    if (isSelectingOnMap) {
                        btn.classList.add('active');
                        btn.innerHTML = '<i class="fas fa-times"></i> Cancelar seleccion';
                        alert.classList.add('active');
                        message.classList.add('active');
                        map.setOptions({ draggableCursor: 'crosshair' });
                    } else {
                        btn.classList.remove('active');
                        btn.innerHTML = '<i class="fas fa-map-location-dot"></i> Seleccionar en mapa';
                        alert.classList.remove('active');
                        message.classList.remove('active');
                        map.setOptions({ draggableCursor: null });
                    }
                });

                document.querySelectorAll('.map-control-btn').forEach(btn => {
                    btn.addEventListener('click', function() {
                        document.querySelectorAll('.map-control-btn').forEach(b => b.classList.remove('active'));
                        this.classList.add('active');
                        map.setMapTypeId(this.dataset.type);
                    });
                });

                document.querySelectorAll('.mode-btn').forEach(btn => {
                    btn.addEventListener('click', function() {
                        document.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('active'));
                        this.classList.add('active');
                        selectedTravelMode = this.dataset.mode;
                    });
                });

                document.getElementById('btnCalculate').addEventListener('click', calculateRoute);

                document.getElementById('origenInput').addEventListener('input', function() {
                    document.getElementById('btnCalculate').disabled = !this.value.trim();
                });
            }

            function onPlaceChanged() {
                const place = autocomplete.getPlace();
                if (!place.geometry) {
                    alert('No se encontro la ubicacion. Por favor, seleccione una opcion de la lista.');
                    return;
                }

                originLocation = {
                    lat: place.geometry.location.lat(),
                    lng: place.geometry.location.lng()
                };

                setOriginMarker(originLocation);
                document.getElementById('btnCalculate').disabled = false;
            }

            function setOriginFromMap(latLng) {
                originLocation = {
                    lat: latLng.lat(),
                    lng: latLng.lng()
                };

                geocoder.geocode({ location: latLng }, function(results, status) {
                    if (status === 'OK' && results[0]) {
                        document.getElementById('origenInput').value = results[0].formatted_address;
                    }
                });

                setOriginMarker(originLocation);

                isSelectingOnMap = false;
                const btn = document.getElementById('btnSelectMap');
                btn.classList.remove('active');
                btn.innerHTML = '<i class="fas fa-map-location-dot"></i> Seleccionar en mapa';
                document.getElementById('mapSelectionAlert').classList.remove('active');
                document.getElementById('mapClickMessage').classList.remove('active');
                map.setOptions({ draggableCursor: null });

                document.getElementById('btnCalculate').disabled = false;

                calculateRoute();
            }

            function setOriginMarker(location) {
                if (originMarker) {
                    originMarker.setMap(null);
                }

                originMarker = new google.maps.Marker({
                    position: location,
                    map: map,
                    title: 'Tu ubicacion',
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        scale: 25,
                        fillColor: '#28a745',
                        fillOpacity: 1,
                        strokeColor: '#ffffff',
                        strokeWeight: 3
                    },
                    label: {
                        text: 'A',
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: 'bold'
                    },
                    animation: google.maps.Animation.BOUNCE
                });

                setTimeout(() => {
                    originMarker.setAnimation(null);
                }, 2000);

                const bounds = new google.maps.LatLngBounds();
                bounds.extend(location);
                bounds.extend(destination);
                map.fitBounds(bounds);
            }

            function calculateRoute() {
                if (!originLocation) {
                    alert('Por favor, seleccione una ubicacion de origen.');
                    return;
                }

                document.getElementById('loadingOverlay').classList.add('active');

                const request = {
                    origin: originLocation,
                    destination: destination,
                    travelMode: google.maps.TravelMode[selectedTravelMode]
                };

                directionsService.route(request, function(result, status) {
                    document.getElementById('loadingOverlay').classList.remove('active');

                    if (status === 'OK') {
                        displayRoute(result);
                    } else {
                        let errorMsg = 'No se pudo calcular la ruta.';
                        let suggestion = '';
                        
                        if (status === 'ZERO_RESULTS') {
                            if (selectedTravelMode === 'TRANSIT') {
                                errorMsg = 'No hay rutas de transporte publico disponibles entre estos puntos.';
                                suggestion = '\n\nSugerencia: Intenta con "Automovil" o "Caminando".';
                            } else if (selectedTravelMode === 'BICYCLING') {
                                errorMsg = 'No hay rutas para bicicleta disponibles entre estos puntos.';
                                suggestion = '\n\nSugerencia: Intenta con "Automovil" o "Caminando".';
                            } else {
                                errorMsg = 'No se encontro ninguna ruta entre estos puntos con el modo seleccionado.';
                                suggestion = '\n\nSugerencia: Intenta con otro modo de transporte.';
                            }
                        } else if (status === 'NOT_FOUND') {
                            errorMsg = 'No se pudo encontrar una o ambas ubicaciones.';
                            suggestion = '\n\nVerifica que las direcciones sean correctas.';
                        } else if (status === 'INVALID_REQUEST') {
                            errorMsg = 'La solicitud no es valida. Verifica los datos ingresados.';
                        } else if (status === 'OVER_QUERY_LIMIT') {
                            errorMsg = 'Se ha excedido el limite de consultas a la API.';
                            suggestion = '\n\nIntenta nuevamente en unos minutos.';
                        }
                        
                        alert(errorMsg + suggestion);
                    }
                });
            }

            function displayRoute(result) {
                directionsRenderer.setDirections(result);

                const route = result.routes[0];
                const leg = route.legs[0];

                document.getElementById('routeDistance').textContent = leg.distance.text;
                document.getElementById('routeDuration').textContent = leg.duration.text;

                const directionsList = document.getElementById('directionsList');
                directionsList.innerHTML = '';

                leg.steps.forEach((step, index) => {
                    const stepDiv = document.createElement('div');
                    stepDiv.className = 'direction-step';
                    
                    const stepNumber = document.createElement('span');
                    stepNumber.className = 'step-number';
                    stepNumber.textContent = index + 1;
                    
                    const stepInstruction = document.createElement('div');
                    stepInstruction.className = 'step-instruction';
                    stepInstruction.textContent = cleanHTML(step.instructions);
                    
                    const stepDistance = document.createElement('div');
                    stepDistance.className = 'step-distance';
                    stepDistance.textContent = step.distance.text + ' - ' + step.duration.text;
                    
                    stepDiv.appendChild(stepNumber);
                    stepDiv.appendChild(stepInstruction);
                    stepDiv.appendChild(stepDistance);

                    stepDiv.addEventListener('click', function() {
                        map.setCenter(step.start_location);
                        map.setZoom(16);
                    });

                    directionsList.appendChild(stepDiv);
                });

                document.getElementById('resultsSection').classList.add('active');

                setOriginMarker(originLocation);
                destinationMarker.setMap(map);
            }

            function cleanHTML(html) {
                const div = document.createElement('div');
                div.innerHTML = html;
                return div.textContent || div.innerText || '';
            }

            function loadGoogleMaps() {
                console.log('loadGoogleMaps - apiKey:', apiKey);
                if (apiKey && apiKey !== 'API_KEY_NOT_CONFIGURED' && apiKey.trim() !== '') {
                    var script = document.createElement('script');
                    script.src = 'https://maps.googleapis.com/maps/api/js?key=' + apiKey + '&libraries=places&callback=initMap';
                    script.async = true;
                    script.defer = true;
                    console.log('Cargando Google Maps con URL:', script.src);
                    document.head.appendChild(script);
                } else {
                    console.error('API Key no valida:', apiKey);
                    document.getElementById('map').innerHTML = '<div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #c00000; font-size: 18px; padding: 20px; text-align: center;"><div>Google Maps API Key no configurada.<br>Por favor, configure la API Key en el servidor.</div></div>';
                }
            }

            window.addEventListener('load', loadGoogleMaps);
        </script>
    </body>
</html>

