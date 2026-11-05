package app.Servidor;

import app.DTO.GsonUtil;
import app.DTO.ReservaDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import app.Servicios.ServicioReservas;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReservasHandler implements HttpHandler {
    private final ServicioReservas servicioReservas = new ServicioReservas();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                HttpUtil.enviarError(exchange, 405, "Método no permitido");
                return;
            }
            String ruta = exchange.getRequestURI().getPath();
            switch (ruta) {
                case "/reserva/crear" -> crear(exchange);
                case "/reserva/listarPorFuncionario" -> listarPorFuncionario(exchange);
                case "/reserva/cancelar" -> cancelar(exchange);
                default -> HttpUtil.enviarError(exchange, 404, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.enviarError(exchange, 500, "Error interno del servidor");
        }
    }

    private void crear(HttpExchange exchange) throws IOException {
        String cuerpo = HttpUtil.leerCuerpo(exchange);
        ReservaDTO reserva = GsonUtil.GSON.fromJson(cuerpo, ReservaDTO.class);
        try {
            servicioReservas.reservarEspacio(reserva);
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 400, e.getMessage());
            return;
        }

        // ServidorNotificaciones.notificarNuevaReserva(reserva);

        HttpUtil.enviarRespuesta(exchange, 200,
                HttpUtil.aJson(Map.of("mensaje", "Reserva creada correctamente.")));
    }

    private void listarPorFuncionario(HttpExchange exchange) throws IOException {
        Map<String, String> params = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));
        String idFuncionario = params.get("idFuncionario");

        try {
            List<ReservaDTO> lista = servicioReservas.listarReservasPorFuncionario(idFuncionario);
            HttpUtil.enviarRespuesta(exchange, 200, HttpUtil.aJson(lista));
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 400, e.getMessage());
        }
    }

    private void cancelar(HttpExchange exchange) throws IOException {
        Map<String, String> params = HttpUtil.obtenerParametros(HttpUtil.leerCuerpo(exchange));
        String idReserva = params.get("idReserva");

        try {
            servicioReservas.cancelarReserva(idReserva);
        } catch (Exception e) {
            HttpUtil.enviarError(exchange, 400, e.getMessage());
            return;
        }

        HttpUtil.enviarRespuesta(exchange, 200,
                HttpUtil.aJson(Map.of("mensaje", "Reserva cancelada correctamente.")));
    }
}
