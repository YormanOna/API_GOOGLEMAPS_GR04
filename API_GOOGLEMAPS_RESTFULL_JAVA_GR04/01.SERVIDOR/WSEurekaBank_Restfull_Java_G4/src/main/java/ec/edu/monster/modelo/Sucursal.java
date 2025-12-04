package ec.edu.monster.modelo;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Modelo de datos para la entidad Sucursal
 * Incluye campos para integración con Google Maps (latitud, longitud)
 * y soporte para eliminación lógica (isActive)
 * 
 * @author MONSTER
 */
@XmlRootElement(name = "sucursal")
public class Sucursal {

    private String codigo;
    private String nombre;
    private String ciudad;
    private String direccion;
    private int contadorCuentas;
    private Double latitud;
    private Double longitud;
    private boolean isActive;

    public Sucursal() {
        this.isActive = true;
    }

    public Sucursal(String codigo, String nombre, String ciudad, String direccion, 
                    int contadorCuentas, Double latitud, Double longitud, boolean isActive) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.contadorCuentas = contadorCuentas;
        this.latitud = latitud;
        this.longitud = longitud;
        this.isActive = isActive;
    }

    // Getters y Setters

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getContadorCuentas() {
        return contadorCuentas;
    }

    public void setContadorCuentas(int contadorCuentas) {
        this.contadorCuentas = contadorCuentas;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Sucursal{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", direccion='" + direccion + '\'' +
                ", contadorCuentas=" + contadorCuentas +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", isActive=" + isActive +
                '}';
    }
}
