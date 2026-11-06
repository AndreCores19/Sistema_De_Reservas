package app.Servidor;

import app.DTO.ExtraccionIADTO;
import app.Servicios.ServicioGemini;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class GeminiHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.enviarError(exchange, 405, "Método no permitido");
                return;
            }

            String ruta = exchange.getRequestURI().getPath();
            switch (ruta) {
                case "/gemini/extraer" -> extraer(exchange);
                default -> HttpUtil.enviarError(exchange, 404, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 500, "Error interno del servidor");
        }
    }

    private void extraer(HttpExchange exchange) throws IOException {
        Map<String, String> params = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));
        String frase = params.get("frase");

        if (frase == null || frase.isBlank()) {
            HttpUtil.enviarError(exchange, 400, "Debe indicar una frase.");
            return;
        }

        try {
            ExtraccionIADTO extraido = ServicioGemini.extraerDatosDeLaIA(frase);
            HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(extraido));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 502, "No se pudo consultar el servicio de IA.");
        }
    }
}