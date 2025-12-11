<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="ec.edu.monster.model.Sucursal"%>
<%
    String usuario = (String) session.getAttribute("usuario");
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    List<Sucursal> sucursales = (List<Sucursal>) request.getAttribute("sucursales");
    String apiKeyJson = (String) request.getAttribute("apiKeyJson");
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Sucursales - EurekaBank LDU</title>
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
                grid-template-columns: 380px 1fr;
                gap: 20px;
                min-height: calc(100vh - 200px);
            }

            .sucursales-list {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                padding: 25px;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                overflow-y: auto;
                max-height: calc(100vh - 180px);
            }

            .sucursales-list h2 {
                color: #c00000;
                margin-bottom: 20px;
                font-size: 22px;
                border-bottom: 3px solid #c00000;
                padding-bottom: 10px;
            }

            .sucursal-item {
                background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
                border: 2px solid #e0e0e0;
                border-radius: 15px;
                padding: 15px;
                margin-bottom: 15px;
                cursor: pointer;
                transition: all 0.3s ease;
                position: relative;
            }

            .sucursal-item:hover {
                transform: translateX(5px);
                border-color: #c00000;
                box-shadow: 0 5px 20px rgba(192,0,0,0.2);
            }

            .sucursal-item.active {
                border-color: #c00000;
                background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
                box-shadow: 0 5px 20px rgba(192,0,0,0.3);
            }

            .sucursal-nombre {
                font-weight: bold;
                color: #c00000;
                font-size: 16px;
                margin-bottom: 5px;
            }

            .sucursal-ciudad {
                color: #0052a3;
                font-size: 14px;
                margin-bottom: 3px;
            }

            .sucursal-direccion {
                color: #666;
                font-size: 13px;
                margin-bottom: 10px;
            }

            .sucursal-actions {
                display: flex;
                gap: 10px;
                margin-top: 10px;
            }

            .btn-detail, .btn-navigate, .btn-edit, .btn-delete {
                flex: 1;
                padding: 10px 12px;
                border: none;
                border-radius: 10px;
                cursor: pointer;
                font-size: 14px;
                font-weight: 600;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 5px;
            }

            .btn-detail i, .btn-navigate i, .btn-edit i, .btn-delete i {
                font-size: 15px;
            }

            .btn-detail {
                background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%);
                color: white;
            }

            .btn-detail:hover {
                background: linear-gradient(135deg, #5a6268 0%, #4e555b 100%);
                transform: translateY(-2px);
                box-shadow: 0 3px 10px rgba(108,117,125,0.3);
            }

            .btn-navigate {
                background: linear-gradient(135deg, #28a745 0%, #1e7e34 100%);
                color: white;
            }

            .btn-navigate:hover {
                background: linear-gradient(135deg, #1e7e34 0%, #155724 100%);
                transform: translateY(-2px);
                box-shadow: 0 3px 10px rgba(40,167,69,0.3);
            }

            .btn-edit {
                background: linear-gradient(135deg, #0052a3 0%, #003d7a 100%);
                color: white;
            }

            .btn-edit:hover {
                background: linear-gradient(135deg, #003d7a 0%, #002a54 100%);
                transform: translateY(-2px);
                box-shadow: 0 3px 10px rgba(0,82,163,0.3);
            }

            .btn-delete {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
            }

            .btn-delete:hover {
                background: linear-gradient(135deg, #900000 0%, #600000 100%);
                transform: translateY(-2px);
                box-shadow: 0 3px 10px rgba(192,0,0,0.3);
            }

            .map-container {
                background: rgba(255,255,255,0.98);
                border-radius: 20px;
                overflow: hidden;
                box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                position: relative;
            }

            #map {
                width: 100%;
                height: calc(100vh - 180px);
            }

            .btn-secondary:hover {
                background: linear-gradient(135deg, #5a6268 0%, #4e555b 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(108,117,125,0.3);
            }

            .alert {
                padding: 15px 20px;
                margin-bottom: 20px;
                border-radius: 12px;
                font-size: 14px;
                display: flex;
                align-items: center;
                gap: 10px;
                animation: slideIn 0.3s ease;
            }

            @keyframes slideIn {
                from {
                    transform: translateX(-20px);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }

            .alert-success {
                background: #d4edda;
                color: #155724;
                border: 2px solid #c3e6cb;
            }

            .alert-error {
                background: #f8d7da;
                color: #721c24;
                border: 2px solid #f5c6cb;
            }

            /* Modal de confirmación */
            .modal-overlay {
                display: none;
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.7);
                z-index: 9999;
                justify-content: center;
                align-items: center;
                animation: fadeIn 0.3s ease;
            }

            .modal-overlay.active {
                display: flex;
            }

            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }

            .modal-confirm {
                background: white;
                border-radius: 20px;
                padding: 0;
                width: 90%;
                max-width: 450px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.5);
                animation: slideUp 0.3s ease;
                overflow: hidden;
            }

            @keyframes slideUp {
                from {
                    transform: translateY(50px);
                    opacity: 0;
                }
                to {
                    transform: translateY(0);
                    opacity: 1;
                }
            }

            .modal-header {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
                padding: 25px;
                text-align: center;
            }

            .modal-header i {
                font-size: 50px;
                margin-bottom: 10px;
                display: block;
                animation: shake 0.5s ease;
            }

            @keyframes shake {
                0%, 100% { transform: rotate(0deg); }
                25% { transform: rotate(-10deg); }
                75% { transform: rotate(10deg); }
            }

            .modal-header h3 {
                margin: 0;
                font-size: 24px;
                font-weight: bold;
            }

            .modal-body {
                padding: 30px;
                text-align: center;
            }

            .modal-body p {
                font-size: 16px;
                color: #333;
                line-height: 1.6;
                margin-bottom: 10px;
            }

            .modal-sucursal-name {
                font-weight: bold;
                color: #c00000;
                font-size: 18px;
                margin: 15px 0;
            }

            .modal-warning {
                background: #fff3cd;
                border: 2px solid #ffc107;
                border-radius: 10px;
                padding: 12px;
                color: #856404;
                font-size: 14px;
                margin-top: 15px;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .modal-warning i {
                font-size: 20px;
            }

            .modal-footer {
                padding: 20px 30px;
                display: flex;
                gap: 15px;
                background: #f8f9fa;
            }

            .modal-btn {
                flex: 1;
                padding: 15px;
                border: none;
                border-radius: 12px;
                font-size: 16px;
                font-weight: bold;
                cursor: pointer;
                transition: all 0.3s ease;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
            }

            .modal-btn-cancel {
                background: #6c757d;
                color: white;
            }

            .modal-btn-cancel:hover {
                background: #5a6268;
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(108,117,125,0.3);
            }

            .modal-btn-confirm {
                background: linear-gradient(135deg, #c00000 0%, #900000 100%);
                color: white;
            }

            .modal-btn-confirm:hover {
                background: linear-gradient(135deg, #900000 0%, #600000 100%);
                transform: translateY(-2px);
                box-shadow: 0 5px 15px rgba(192,0,0,0.4);
            }

            @media (max-width: 1024px) {
                .content-wrapper {
                    grid-template-columns: 1fr;
                }

                .sucursales-list {
                    max-height: 400px;
                }

                #map {
                    height: 500px;
                }
            }
        </style>
    </head>
    <body>
        <!-- Modal de Confirmación -->
        <div class="modal-overlay" id="deleteModal">
            <div class="modal-confirm">
                <div class="modal-header">
                    <i class="fas fa-exclamation-triangle"></i>
                    <h3>Confirmar Eliminación</h3>
                </div>
                <div class="modal-body">
                    <p>¿Está seguro que desea eliminar la sucursal?</p>
                    <div class="modal-sucursal-name" id="modalSucursalName"></div>
                    <div class="modal-warning">
                        <i class="fas fa-info-circle"></i>
                        <span>Esta acción marcará la sucursal como inactiva.</span>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="modal-btn modal-btn-cancel" onclick="closeDeleteModal()">
                        <i class="fas fa-times"></i> Cancelar
                    </button>
                    <button class="modal-btn modal-btn-confirm" id="confirmDeleteBtn">
                        <i class="fas fa-trash-alt"></i> Eliminar
                    </button>
                </div>
            </div>
        </div>

        <div class="header">
            <h1><i class="fas fa-store-alt"></i> Nuestras Sucursales</h1>
            <a href="<%= request.getContextPath()%>/menu" class="btn-back">
                ← Volver al Menú
            </a>
        </div>

        <div class="main">
            <% if (request.getParameter("created") != null && "true".equals(request.getParameter("created"))) { %>
                <div class="alert alert-success">
                    ✓ Sucursal creada correctamente
                </div>
            <% } %>

            <% if (request.getParameter("updated") != null) { %>
                <div class="alert alert-success">
                    ✓ Sucursal actualizada correctamente
                </div>
            <% } %>

            <% 
            String created = request.getParameter("created");
            String updated = request.getParameter("updated");
            String deleted = request.getParameter("deleted");
            String error = request.getParameter("error");
            %>
            
            <script>
                window.addEventListener('DOMContentLoaded', function() {
                    <% if ("true".equals(created)) { %>
                        showToast('✓ Sucursal Creada', 'La sucursal se ha creado exitosamente');
                    <% } else if ("false".equals(created)) { %>
                        showToast('✗ Error', 'No se pudo crear la sucursal. Verifica los datos e intenta nuevamente.', true);
                    <% } %>
                    
                    <% if ("true".equals(updated)) { %>
                        showToast('✓ Sucursal Actualizada', 'Los cambios se guardaron correctamente');
                    <% } else if ("false".equals(updated)) { %>
                        showToast('✗ Error', 'No se pudo actualizar la sucursal', true);
                    <% } %>
                    
                    <% if ("true".equals(deleted)) { %>
                        showToast('✓ Sucursal Eliminada', 'La sucursal se elimino correctamente');
                    <% } %>
                    
                    <% if (error != null) { %>
                        showToast('✗ Error', 'Ocurrio un error: <%= error %>', true);
                    <% } %>
                });
            </script>

            <div class="content-wrapper">
                <div class="sucursales-list">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                        <h2 style="margin: 0;">Lista de Sucursales</h2>
                        <button onclick="window.location.href='<%= request.getContextPath()%>/sucursales?action=nueva'" 
                                style="background: linear-gradient(135deg, #0052a3 0%, #003d7a 100%); color: white; border: none; padding: 10px 20px; border-radius: 10px; cursor: pointer; font-weight: bold; font-size: 13px; display: flex; align-items: center; gap: 8px; transition: all 0.3s ease;" 
                                onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 5px 15px rgba(0,82,163,0.3)'" 
                                onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='none'">
                            <i class="fas fa-plus-square"></i> Nueva Sucursal
                        </button>
                    </div>
                    <% if (sucursales != null && !sucursales.isEmpty()) { 
                        for (Sucursal s : sucursales) { %>
                            <div class="sucursal-item" 
                                 data-codigo="<%= s.getCodigo() %>"
                                 data-lat="<%= s.getLatitud() != null ? s.getLatitud() : "" %>"
                                 data-lng="<%= s.getLongitud() != null ? s.getLongitud() : "" %>"
                                 data-has-coords="<%= (s.getLatitud() != null && s.getLongitud() != null) ? "true" : "false" %>"
                                 onclick="selectSucursal(this)">
                                <div class="sucursal-nombre"><%= s.getNombre() %></div>
                                <div class="sucursal-ciudad"><i class="fas fa-location-dot"></i> <%= s.getCiudad() %></div>
                                <div class="sucursal-direccion"><%= s.getDireccion() %></div>
                                <div class="sucursal-actions" onclick="event.stopPropagation();">
                                    <button class="btn-detail" onclick="window.location.href='<%= request.getContextPath()%>/sucursales?action=detalle&codigo=<%= s.getCodigo() %>'" title="Ver detalles">
                                        <i class="fas fa-info-circle"></i>
                                    </button>
                                    <% if (s.getLatitud() != null && s.getLongitud() != null) { %>
                                        <button class="btn-navigate" 
                                                data-codigo="<%= s.getCodigo() %>"
                                                data-lat="<%= s.getLatitud() %>"
                                                data-lng="<%= s.getLongitud() %>"
                                                data-nombre="<%= s.getNombre() %>"
                                                data-direccion="<%= s.getDireccion() %>"
                                                onclick="navigateToSucursalFromButton(this)" 
                                                title="Cómo llegar">
                                            <i class="fas fa-directions"></i>
                                        </button>
                                    <% } else { %>
                                        <button class="btn-navigate" style="opacity: 0.5; cursor: not-allowed;" disabled title="Coordenadas no disponibles (lat=<%= s.getLatitud() %>, lng=<%= s.getLongitud() %>)">
                                            <i class="fas fa-directions"></i>
                                        </button>
                                    <% } %>
                                    <button class="btn-edit" onclick="window.location.href='<%= request.getContextPath()%>/sucursales?action=editar&codigo=<%= s.getCodigo() %>'" title="Editar">
                                        <i class="fas fa-pen"></i>
                                    </button>
                                    <button class="btn-delete" onclick="confirmDelete('<%= s.getCodigo() %>', '<%= s.getNombre() %>')" title="Eliminar">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </div>
                    <%  }
                    } else { %>
                        <p style="color: #666; text-align: center; padding: 20px;">
                            No hay sucursales disponibles
                        </p>
                    <% } %>
                </div>

                <div class="map-container">
                    <div id="map"></div>
                </div>
            </div>
        </div>

        <!-- Scripts -->
        <script>
            let map;
            let markers = [];
            let apiKey = '';

            // Parsear la API Key del servidor
            try {
                const apiKeyData = <%= apiKeyJson %>;
                apiKey = apiKeyData.apiKey || '';
            } catch(e) {
                console.error('Error parsing API Key:', e);
            }

            function initMap() {
                // Centro inicial en Ecuador (Quito)
                const ecuador = { lat: -0.1806532, lng: -78.4678382 };

                map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 7,
                    center: ecuador,
                    styles: [
                        {
                            featureType: 'poi',
                            elementType: 'labels',
                            stylers: [{ visibility: 'off' }]
                        }
                    ]
                });

                // Agregar marcadores de todas las sucursales
                const sucursalItems = document.querySelectorAll('.sucursal-item');
                sucursalItems.forEach(item => {
                    const lat = parseFloat(item.dataset.lat);
                    const lng = parseFloat(item.dataset.lng);
                    const nombre = item.querySelector('.sucursal-nombre').textContent;

                    if (!isNaN(lat) && !isNaN(lng)) {
                        const marker = new google.maps.Marker({
                            position: { lat, lng },
                            map: map,
                            title: nombre,
                            animation: google.maps.Animation.DROP
                        });

                        const infoWindow = new google.maps.InfoWindow({
                            content: '<div style="padding: 10px;"><strong>' + nombre + '</strong></div>'
                        });

                        marker.addListener('click', () => {
                            infoWindow.open(map, marker);
                            // Resaltar sucursal en la lista
                            document.querySelectorAll('.sucursal-item').forEach(i => i.classList.remove('active'));
                            item.classList.add('active');
                        });

                        markers.push({ marker, item: item.dataset.codigo });
                    }
                });
            }

            function selectSucursal(element) {
                const lat = parseFloat(element.dataset.lat);
                const lng = parseFloat(element.dataset.lng);

                // Remover clase active de todos
                document.querySelectorAll('.sucursal-item').forEach(item => {
                    item.classList.remove('active');
                });

                // Agregar clase active al seleccionado
                element.classList.add('active');

                // Centrar el mapa en la sucursal
                if (!isNaN(lat) && !isNaN(lng)) {
                    map.setCenter({ lat, lng });
                    map.setZoom(15);
                }
            }

            // Función que lee las coordenadas desde los data attributes del botón
            function navigateToSucursalFromButton(button) {
                const codigo = button.getAttribute('data-codigo');
                const lat = button.getAttribute('data-lat');
                const lng = button.getAttribute('data-lng');
                
                // Validar que tenga coordenadas
                if (!lat || !lng || lat === 'null' || lng === 'null' || lat === '' || lng === '') {
                    alert('Esta sucursal no tiene coordenadas configuradas correctamente');
                    return;
                }
                
                // Validar que las coordenadas sean números válidos
                const latNum = parseFloat(lat);
                const lngNum = parseFloat(lng);
                if (isNaN(latNum) || isNaN(lngNum)) {
                    alert('Las coordenadas de esta sucursal tienen un formato inválido');
                    return;
                }
                
                // Validar rangos de coordenadas
                if (latNum < -90 || latNum > 90 || lngNum < -180 || lngNum > 180) {
                    alert('Las coordenadas de esta sucursal están fuera del rango válido');
                    return;
                }
                
                // Redirigir a la página de rutas
                window.location.href = '<%= request.getContextPath() %>/sucursales?action=ruta&codigo=' + codigo;
            }

            function navigateToSucursal(lat, lng) {
                // Esta función ya no se usa, pero se mantiene por compatibilidad
                alert('Use el botón "Cómo llegar" para calcular la ruta');
            }

            function showToast(title, message, isError = false) {
                const toast = document.createElement('div');
                toast.className = 'toast' + (isError ? ' error' : '');
                toast.innerHTML = `
                    <i class="fas ${isError ? 'fa-circle-xmark' : 'fa-circle-check'}"></i>
                    <div class="toast-content">
                        <div class="toast-title">${title}</div>
                        <div class="toast-message">${message}</div>
                    </div>
                `;
                document.body.appendChild(toast);
                setTimeout(() => {
                    toast.remove();
                }, 3000);
            }

            function confirmDelete(codigo, nombre) {
                const modal = document.getElementById('deleteModal');
                const modalName = document.getElementById('modalSucursalName');
                const confirmBtn = document.getElementById('confirmDeleteBtn');
                
                modalName.textContent = nombre;
                modal.classList.add('active');
                
                // Remover listeners anteriores
                const newConfirmBtn = confirmBtn.cloneNode(true);
                confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
                
                // Agregar nuevo listener
                newConfirmBtn.addEventListener('click', function() {
                    closeDeleteModal();
                    fetch('<%= request.getContextPath()%>/sucursales?action=delete&codigo=' + codigo)
                        .then(() => {
                            showToast('Sucursal Eliminada', 'La sucursal "' + nombre + '" ha sido eliminada correctamente');
                            setTimeout(() => {
                                window.location.href = '<%= request.getContextPath()%>/sucursales';
                            }, 1500);
                        })
                        .catch(() => {
                            showToast('Error', 'No se pudo eliminar la sucursal', true);
                        });
                });
            }
            
            function closeDeleteModal() {
                const modal = document.getElementById('deleteModal');
                modal.classList.remove('active');
            }
            
            // Cerrar modal al hacer clic fuera de él
            document.getElementById('deleteModal').addEventListener('click', function(e) {
                if (e.target === this) {
                    closeDeleteModal();
                }
            });
            
            // Cerrar modal con tecla ESC
            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    closeDeleteModal();
                }
            });

            // Cerrar modal al hacer clic fuera de él
            window.onclick = function(event) {
                const modal = document.getElementById('editModal');
                if (event.target == modal) {
                    closeModal();
                }
            }

            // Cargar Google Maps API dinámicamente
            function loadGoogleMaps() {
                if (apiKey && apiKey !== 'API_KEY_NOT_CONFIGURED') {
                    const script = document.createElement('script');
                    script.src = 'https://maps.googleapis.com/maps/api/js?key=' + apiKey + '&callback=initMap';
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
