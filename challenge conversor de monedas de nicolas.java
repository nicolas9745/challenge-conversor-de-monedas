import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.text.DecimalFormat;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Conversor de Monedas - Proyecto Completo en un solo archivo
 * 
 * Características:
 * - Consulta en tiempo real a ExchangeRate-API
 * - Soporte para 6 monedas latinoamericanas
 * - Validación de entradas
 * - Formateo de resultados
 * - Manejo de errores
 */
public class ConversorDeMonedas {
    
    // Constantes del proyecto
    private static final String[] MONEDAS_SOPORTADAS = {
        "USD", // Dólar estadounidense
        "ARS", // Peso argentino
        "BOB", // Boliviano boliviano
        "BRL", // Real brasileño
        "CLP", // Peso chileno
        "COP"  // Peso colombiano
    };
    
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String API_KEY = "eb7f9020aa329a6776fef1b8"; // Reemplaza con tu clave
    
    private static final Gson gson = new Gson();
    private static final DecimalFormat formateador = new DecimalFormat("#,##0.00");
    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * Clase para mapear la respuesta JSON de la API
     */
    private static class RespuestaTasaCambio {
        @SerializedName("result")
        private String resultado;
        
        @SerializedName("conversion_rate")
        private double tasaConversion;
        
        @SerializedName("base_code")
        private String monedaBase;
        
        @SerializedName("target_code")
        private String monedaDestino;
        
        public double getTasaConversion() {
            return tasaConversion;
        }
        
        public String getResultado() {
            return resultado;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("💱 CONVERSOR DE MONEDAS LATAM 💰");
        System.out.println("Monedas disponibles: " + String.join(", ", MONEDAS_SOPORTADAS));
        
        try {
            String monedaOrigen = obtenerMoneda("Moneda origen: ");
            String monedaDestino = obtenerMoneda("Moneda destino: ");
            double cantidad = obtenerCantidad();
            
            double resultado = convertir(monedaOrigen, monedaDestino, cantidad);
            
            System.out.printf("\n🔁 %s %s = %s %s\n",
                formateador.format(cantidad), monedaOrigen,
                formateador.format(resultado), monedaDestino);
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Obtiene y valida la moneda ingresada por el usuario
     */
    private static String obtenerMoneda(String mensaje) throws IllegalArgumentException {
        System.out.print(mensaje);
        String moneda = scanner.nextLine().trim().toUpperCase();
        
        for (String soportada : MONEDAS_SOPORTADAS) {
            if (soportada.equals(moneda)) {
                return moneda;
            }
        }
        
        throw new IllegalArgumentException("Moneda no soportada: " + moneda);
    }
    
    /**
     * Obtiene y valida la cantidad a convertir
     */
    private static double obtenerCantidad() {
        while (true) {
            System.out.print("Monto a convertir: ");
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Ingrese un número válido.");
            }
        }
    }
    
    /**
     * Realiza la conversión de monedas usando la API
     */
    private static double convertir(String deMoneda, String aMoneda, double cantidad) throws Exception {
        HttpClient cliente = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
        
        String url = API_URL + API_KEY + "/pair/" + deMoneda + "/" + aMoneda;
        
        HttpRequest solicitud = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> respuesta = cliente.send(
            solicitud, 
            HttpResponse.BodyHandlers.ofString()
        );
        
        if (respuesta.statusCode() != 200) {
            throw new Exception("Error al conectar con la API: " + respuesta.statusCode());
        }
        
        RespuestaTasaCambio tasa = gson.fromJson(respuesta.body(), RespuestaTasaCambio.class);
        
        if (!"success".equals(tasa.getResultado())) {
            throw new Exception("Error en la respuesta de la API");
        }
        
        return cantidad * tasa.getTasaConversion();
    }
}