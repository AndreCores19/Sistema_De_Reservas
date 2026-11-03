package app.Servidor;

import app.Servicios.ServicioUsuario;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CambiarClaveHandler implements HttpHandler {
    private final ServicioUsuario servicioUsuario = new ServicioUsuario();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.enviarError(exchange, 405, "Método no permitido");
                return;
            }

            String cuerpo = HttpUtil.leerCuerpo(exchange);
            Map<String, String> parametros = HttpUtil.obtenerParametros(cuerpo);
            String id = parametros.get("id");
            String claveActual = parametros.get("claveActual");
            String claveNueva = parametros.get("claveNueva");

            try {
                servicioUsuario.cambiarClave(id, claveActual, claveNueva);
            } catch (Exception e) {
                HttpUtil.enviarError(exchange, 400, e.getMessage());
                return;
            }

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Clave actualizada correctamente.");
            HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(respuesta));

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 500, "Error interno del servidor.");
        }
    }
}