package ec.edu.monster.controller;

import ec.edu.monster.model.Sucursal;
import ec.edu.monster.service.EurekaWebClient;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "SucursalesController", urlPatterns = {"/sucursales"})
public class SucursalesController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            String codigo = request.getParameter("codigo");
            boolean success = EurekaWebClient.eliminarSucursal(codigo);
            response.sendRedirect(request.getContextPath() + "/sucursales?deleted=" + success);
            return;
        }

        if ("nueva".equals(action)) {
            String apiKeyJson = EurekaWebClient.obtenerApiKeyGoogleMaps();
            request.setAttribute("apiKeyJson", apiKeyJson);
            RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/nueva-sucursal.jsp");
            rd.forward(request, response);
            return;
        }

        if ("detalle".equals(action)) {
            String codigo = request.getParameter("codigo");
            Sucursal sucursal = EurekaWebClient.obtenerSucursal(codigo);
            String apiKeyJson = EurekaWebClient.obtenerApiKeyGoogleMaps();
            request.setAttribute("sucursal", sucursal);
            request.setAttribute("apiKeyJson", apiKeyJson);
            RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/detalle-sucursal.jsp");
            rd.forward(request, response);
            return;
        }

        // Obtener lista de sucursales
        List<Sucursal> sucursales = EurekaWebClient.listarSucursales();
        request.setAttribute("sucursales", sucursales);

        // Obtener API Key de Google Maps
        String apiKeyJson = EurekaWebClient.obtenerApiKeyGoogleMaps();
        request.setAttribute("apiKeyJson", apiKeyJson);

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/sucursales.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("update".equals(action)) {
            String codigo = request.getParameter("codigo");
            String nombre = request.getParameter("nombre");
            String ciudad = request.getParameter("ciudad");
            String direccion = request.getParameter("direccion");
            String latitudStr = request.getParameter("latitud");
            String longitudStr = request.getParameter("longitud");

            Sucursal sucursal = new Sucursal();
            sucursal.setNombre(nombre);
            sucursal.setCiudad(ciudad);
            sucursal.setDireccion(direccion);

            if (latitudStr != null && !latitudStr.trim().isEmpty()) {
                sucursal.setLatitud(Double.parseDouble(latitudStr));
            }
            if (longitudStr != null && !longitudStr.trim().isEmpty()) {
                sucursal.setLongitud(Double.parseDouble(longitudStr));
            }

            boolean success = EurekaWebClient.actualizarSucursal(codigo, sucursal);
            response.sendRedirect(request.getContextPath() + "/sucursales?updated=" + success);
        } else if ("create".equals(action)) {
            try {
                String codigo = request.getParameter("codigo");
                String nombre = request.getParameter("nombre");
                String ciudad = request.getParameter("ciudad");
                String direccion = request.getParameter("direccion");
                String latitudStr = request.getParameter("latitud");
                String longitudStr = request.getParameter("longitud");

                // Validar campos requeridos
                if (codigo == null || codigo.trim().isEmpty() ||
                    nombre == null || nombre.trim().isEmpty() ||
                    ciudad == null || ciudad.trim().isEmpty() ||
                    direccion == null || direccion.trim().isEmpty() ||
                    latitudStr == null || latitudStr.trim().isEmpty() ||
                    longitudStr == null || longitudStr.trim().isEmpty()) {
                    
                    System.err.println("ERROR: Datos incompletos para crear sucursal");
                    System.err.println("Codigo: " + codigo + ", Nombre: " + nombre + ", Ciudad: " + ciudad);
                    System.err.println("Direccion: " + direccion + ", Lat: " + latitudStr + ", Lng: " + longitudStr);
                    response.sendRedirect(request.getContextPath() + "/sucursales?error=incomplete");
                    return;
                }

                Sucursal sucursal = new Sucursal();
                sucursal.setCodigo(codigo.trim());
                sucursal.setNombre(nombre.trim());
                sucursal.setCiudad(ciudad.trim());
                sucursal.setDireccion(direccion.trim());
                sucursal.setContadorCuentas(0); // Iniciar en 0 para nuevas sucursales
                sucursal.setLatitud(Double.parseDouble(latitudStr.trim()));
                sucursal.setLongitud(Double.parseDouble(longitudStr.trim()));

                System.out.println("Creando sucursal: " + sucursal.getCodigo() + " - " + sucursal.getNombre());
                System.out.println("  Ciudad: " + sucursal.getCiudad() + ", Dirección: " + sucursal.getDireccion());
                System.out.println("  Coordenadas: " + sucursal.getLatitud() + ", " + sucursal.getLongitud());
                
                boolean success = EurekaWebClient.crearSucursal(sucursal);
                System.out.println("Resultado de creación: " + (success ? "ÉXITO" : "FALLO"));
                
                response.sendRedirect(request.getContextPath() + "/sucursales?created=" + success);
            } catch (NumberFormatException e) {
                System.err.println("ERROR: Formato inválido de coordenadas");
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/sucursales?created=false&error=Formato inválido de coordenadas");
            } catch (Exception e) {
                System.err.println("ERROR al crear sucursal: " + e.getMessage());
                e.printStackTrace();
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("3 caracteres")) {
                    response.sendRedirect(request.getContextPath() + "/sucursales?created=false&error=El código debe tener máximo 3 caracteres");
                } else {
                    response.sendRedirect(request.getContextPath() + "/sucursales?created=false&error=" + (errorMsg != null ? errorMsg : "Error desconocido"));
                }
            }
        }
    }
}
