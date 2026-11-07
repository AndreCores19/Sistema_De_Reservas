package app.Servidor;

import app.DTO.ResultadoEstadisticaDTO;
import app.Servicios.ServicioEstadisticas;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EstadisticasHandler implements HttpHandler {

    private final ServicioEstadisticas servicioEstadisticas = new ServicioEstadisticas();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.enviarError(exchange, 405, "Método no permitido");
                return;
            }

            String ruta = exchange.getRequestURI().getPath();
            switch (ruta) {
                case "/estadistica/recursos" -> recursos(exchange);
                case "/estadistica/actividades" -> actividades(exchange);
                default -> HttpUtil.enviarError(exchange, 404, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 500, "Error interno del servidor");
        }
    }

    private void recursos(HttpExchange exchange) throws IOException {
        Map<String, String> params = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));

        try {
            LocalDate desde = LocalDate.parse(params.get("desde"));
            LocalDate hasta = LocalDate.parse(params.get("hasta"));
            List<ResultadoEstadisticaDTO> lista =
                    servicioEstadisticas.obtenerEstadisticaRecursos(desde, hasta);
            HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(lista));
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 400, "Fechas inválidas");
        }
    }

    private void actividades(HttpExchange exchange) throws IOException {
        Map<String, String> params = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));

        try {
            LocalDate desde = LocalDate.parse(params.get("desde"));
            LocalDate hasta = LocalDate.parse(params.get("hasta"));
            List<ResultadoEstadisticaDTO> lista =
                    servicioEstadisticas.obtenerEstadisticaActividades(desde, hasta);
            HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(lista));
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 400, "Fechas inválidas");
        }
    }
}