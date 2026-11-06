package app.Servidor;

import app.Servicios.ServicioCalendarizacionRecursos;
import app.Servicios.ServicioProgramacion;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class CalendarizacionHandler implements HttpHandler {

    private final ServicioCalendarizacionRecursos servicioRecursos = new ServicioCalendarizacionRecursos();
    private final ServicioProgramacion servicioProgramacion = new ServicioProgramacion();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.enviarError(exchange, 405, "Método no permitido");
                return;
            }

            String ruta = exchange.getRequestURI().getPath();
            Map<String, String> parametros = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));

            String respuesta;
            try {
                switch (ruta) {
                    case "/calendarizacion/recursos" -> {
                        LocalDate fecha = leerFecha(parametros);
                        respuesta = HttpUtil.aJson(
                                servicioRecursos.obtenerMatrizDeRecursos(parametros.get("idCategoria"), fecha));
                    }

                    case "/calendarizacion/actividades" -> {
                        LocalDate fecha = leerFecha(parametros);
                        respuesta = HttpUtil.aJson(servicioProgramacion.matrizReservas(fecha));
                    }

                    default -> {
                        HttpUtil.enviarError(exchange, 404, "Ruta no encontrada: " + ruta);
                        return;
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                HttpUtil.enviarError(exchange, 400, e.getMessage());
                return;
            }

            HttpUtil.enviarRespuesta(exchange, 200, respuesta);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 500, "Error interno del servidor.");
        }
    }

    private LocalDate leerFecha(Map<String, String> parametros) throws Exception {
        String texto = parametros.get("fecha");
        if (texto == null || texto.isBlank()) {
            throw new Exception("Debe indicar una fecha.");
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new Exception("La fecha no es válida. Use el formato aaaa-mm-dd.");
        }
    }
}