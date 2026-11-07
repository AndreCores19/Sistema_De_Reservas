package app.Servidor;

import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;
import app.Servicios.ServicioRecurso;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class RecursoHandler implements HttpHandler {

    private final ServicioRecurso servicio = new ServicioRecurso();

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
                    case "/recursos/listar" ->
                            respuesta = HttpUtil.aJson(servicio.listarTodos());

                    case "/recursos/filtrarPorCategoria" ->
                            respuesta = HttpUtil.aJson(servicio.filtrarPorCategoria(parametros.get("idCategoria")));

                    case "/recursos/buscarPorNumActivo" ->
                            respuesta = HttpUtil.aJson(servicio.buscarPorNumActivo(parametros.get("numActivo")));

                    case "/recursos/agregar" -> {
                        servicio.agregar(armarRecurso(parametros));
                        respuesta = mensaje("Recurso agregado correctamente.");
                    }

                    case "/recursos/modificar" -> {
                        servicio.modificar(armarRecurso(parametros));
                        respuesta = mensaje("Recurso modificado correctamente.");
                    }

                    case "/recursos/eliminar" -> {
                        servicio.eliminar(parametros.get("numActivo"));
                        respuesta = mensaje("Recurso eliminado correctamente.");
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

    private RecursoDTO armarRecurso(Map<String, String> parametros) {
        String idCategoria = parametros.get("idCategoria");
        CategoriaDTO categoria = (idCategoria == null || idCategoria.isBlank())
                ? null
                : new CategoriaDTO(idCategoria, null);

        return new RecursoDTO(parametros.get("numActivo"), categoria, parametros.get("descripcion"));
    }

    private String mensaje(String texto) {
        return HttpUtil.aJson(Map.of("mensaje", texto));
    }
}