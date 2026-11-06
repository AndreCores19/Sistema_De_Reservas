package app.Servidor;

import app.DTO.FuncionarioDTO;
import app.Servicios.ServicioFuncionario;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FuncionarioHandler implements HttpHandler {

    private final ServicioFuncionario servicio = new ServicioFuncionario();

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
                    case "/funcionarios/listar" ->
                            respuesta = HttpUtil.aJson(sinClaves(servicio.listarTodos()));

                    case "/funcionarios/buscarPorId" ->
                            respuesta = HttpUtil.aJson(sinClave(servicio.buscarPorId(parametros.get("id"))));

                    case "/funcionarios/buscarPorNombre" ->
                            respuesta = HttpUtil.aJson(sinClaves(servicio.buscarPorNombre(parametros.get("texto"))));

                    case "/funcionarios/agregar" -> {
                        servicio.agregar(armarFuncionario(parametros));
                        respuesta = mensaje("Funcionario agregado correctamente.");
                    }

                    case "/funcionarios/modificar" -> {
                        servicio.modificar(armarFuncionario(parametros));
                        respuesta = mensaje("Funcionario modificado correctamente.");
                    }

                    case "/funcionarios/eliminar" -> {
                        servicio.eliminar(parametros.get("id"));
                        respuesta = mensaje("Funcionario eliminado correctamente.");
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

    private FuncionarioDTO armarFuncionario(Map<String, String> parametros) {
        return new FuncionarioDTO(parametros.get("id"), null, null,
                parametros.get("nombre"), parametros.get("telefono"));
    }

    private FuncionarioDTO sinClave(FuncionarioDTO funcionario) {
        funcionario.setClave(null);
        return funcionario;
    }

    private List<FuncionarioDTO> sinClaves(List<FuncionarioDTO> funcionarios) {
        for (FuncionarioDTO f : funcionarios) {
            f.setClave(null);
        }
        return funcionarios;
    }

    private String mensaje(String texto) {
        return HttpUtil.aJson(Map.of("mensaje", texto));
    }
}