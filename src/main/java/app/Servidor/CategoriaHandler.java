package app.Servidor;

import app.DTO.CategoriaDTO;
import app.Servicios.ServicioCategoria;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class CategoriaHandler implements HttpHandler {

    private final ServicioCategoria servicio = new ServicioCategoria();

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
                    case "/categorias/listar" ->
                            respuesta = HttpUtil.aJson(servicio.listarTodas());

                    case "/categorias/buscarPorId" ->
                            respuesta = HttpUtil.aJson(servicio.buscarPorId(parametros.get("id")));

                    case "/categorias/buscarPorDescripcion" ->
                            respuesta = HttpUtil.aJson(servicio.buscarPorDescripcion(parametros.get("texto")));

                    case "/categorias/agregar" -> {
                        CategoriaDTO nueva = new CategoriaDTO(null, parametros.get("descripcion"));
                        servicio.agregar(nueva);
                        respuesta = HttpUtil.aJson(nueva);
                    }

                    case "/categorias/modificar" -> {
                        servicio.modificar(new CategoriaDTO(parametros.get("id"), parametros.get("descripcion")));
                        respuesta = mensaje("Categoría modificada correctamente.");
                    }

                    case "/categorias/eliminar" -> {
                        servicio.eliminar(parametros.get("id"));
                        respuesta = mensaje("Categoría eliminada correctamente.");
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

    private String mensaje(String texto) {
        return HttpUtil.aJson(Map.of("mensaje", texto));
    }
}