package ec.edu.monster.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase de utilidad para gestionar la configuración de Google Maps
 * Lee la API Key desde el archivo googlemaps.properties
 * 
 * @author MONSTER
 */
public class GoogleMapsConfig {
    
    private static final String CONFIG_FILE = "googlemaps.properties";
    private static Properties properties;
    
    static {
        properties = new Properties();
        try (InputStream input = GoogleMapsConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("ERROR: No se encontró el archivo " + CONFIG_FILE);
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("ERROR al cargar configuración de Google Maps: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene la API Key de Google Maps
     * @return API Key configurada o un mensaje de error si no está configurada
     */
    public static String getApiKey() {
        String apiKey = properties.getProperty("google.maps.api.key", "");
        
        if (apiKey.isEmpty() || apiKey.equals("YOUR_GOOGLE_MAPS_API_KEY_HERE")) {
            System.err.println("ADVERTENCIA: Google Maps API Key no configurada. " +
                    "Por favor, edita el archivo googlemaps.properties");
            return "API_KEY_NOT_CONFIGURED";
        }
        
        return apiKey;
    }
    
    /**
     * Obtiene una propiedad de configuración específica
     * @param key Clave de la propiedad
     * @param defaultValue Valor por defecto si no se encuentra la propiedad
     * @return Valor de la propiedad o el valor por defecto
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Verifica si la API Key está configurada correctamente
     * @return true si está configurada, false en caso contrario
     */
    public static boolean isApiKeyConfigured() {
        String apiKey = properties.getProperty("google.maps.api.key", "");
        return !apiKey.isEmpty() && !apiKey.equals("YOUR_GOOGLE_MAPS_API_KEY_HERE");
    }
}
