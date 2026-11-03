package app.Servidor;

import app.DTO.UsuarioDTO;
import app.Servicios.ServicioUsuario;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class UsuarioHandler implements HttpHandler {

    private final ServicioUsuario servicioUsuario = new ServicioUsuario();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.enviarError(exchange, 405, "Método no permitido");
                return;
            }

            String ruta = exchange.getRequestURI().getPath();
            switch (ruta) {
                case "/usuario/login"        -> login(exchange);
                case "/usuario/cambiarClave" -> cambiarClave(exchange);
                default -> HttpUtil.enviarError(exchange, 404, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 500, "Error interno del servidor");
        }
    }

    private void login(HttpExchange exchange) throws IOException {
        Map<String, String> parametros = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));

        UsuarioDTO usuario;
        try {
            usuario = servicioUsuario.iniciarSesion(parametros.get("id"), parametros.get("clave"));
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 401, e.getMessage());
            return;
        }

        usuario.setClave(null);
        HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(usuario));
    }

    private void cambiarClave(HttpExchange exchange) throws IOException {
        Map<String, String> parametros = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));

        try {
            servicioUsuario.cambiarClave(
                    parametros.get("id"),
                    parametros.get("claveActual"),
                    parametros.get("claveNueva"));
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 400, e.getMessage());
            return;
        }

        HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(Map.of("mensaje", "Clave actualizada correctamente.")));
    }
}