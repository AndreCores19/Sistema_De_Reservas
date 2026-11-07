package app.Servicios;

import app.DTO.CategoriaDTO;
import app.DTO.FuncionarioDTO;
import app.DTO.GsonUtil;
import app.DTO.RecursoDTO;
import app.DTO.ResultadoEstadisticaDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiciosClienteTest {
    private HttpServer servidor;

    @BeforeEach
    void iniciarServidorFalso() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress(0), 0);
        servidor.createContext("/", this::responder);
        servidor.start();
        System.setProperty("sistema.reservas.urlServidor",
                "http://localhost:" + servidor.getAddress().getPort());
    }

    @AfterEach
    void detenerServidorFalso() {
        servidor.stop(0);
        System.clearProperty("sistema.reservas.urlServidor");
    }

    @Test
    void servicioEstadisticasDeserializaLaRespuestaDelServidor() throws Exception {
        List<ResultadoEstadisticaDTO> resultado =
                new ServicioEstadisticasCliente().obtenerEstadisticaRecursos(
                        java.time.LocalDate.of(2026, 11, 1),
                        java.time.LocalDate.of(2026, 11, 7));

        assertEquals("Sala", resultado.get(0).getEtiqueta());
        assertEquals(3, resultado.get(0).getCantidad());
    }

    @Test
    void serviciosDeMantenimientoDeserializanSusRespuestas() throws Exception {
        CategoriaDTO categoria = new ServicioCategoriaCliente().listarTodas().get(0);
        FuncionarioDTO funcionario = new ServicioFuncionarioCliente().buscarPorId("F-1");
        RecursoDTO recurso = new ServicioRecursoCliente().buscarPorNumActivo("R-1");

        assertEquals("CAT-1", categoria.getId());
        assertEquals("F-1", funcionario.getId());
        assertEquals("R-1", recurso.getNumActivo());
    }

    @Test
    void servicioClientePropagaElErrorDelServidor() {
        Exception error = assertThrows(Exception.class,
                () -> new ServicioCategoriaCliente().buscarPorId("invalido"));

        assertEquals("Categoría no encontrada.", error.getMessage());
    }

    private void responder(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        if (ruta.equals("/categorias/buscarPorId")) {
            enviar(exchange, 400, "{\"error\":\"Categoría no encontrada.\"}");
        } else if (ruta.equals("/categorias/listar")) {
            enviar(exchange, 200, GsonUtil.GSON.toJson(
                    List.of(new CategoriaDTO("CAT-1", "Sala"))));
        } else if (ruta.equals("/funcionarios/buscarPorId")) {
            enviar(exchange, 200, GsonUtil.GSON.toJson(
                    new FuncionarioDTO("F-1", null, "FUNCIONARIO", "Ana", "2222")));
        } else if (ruta.equals("/recursos/buscarPorNumActivo")) {
            enviar(exchange, 200, GsonUtil.GSON.toJson(
                    new RecursoDTO("R-1", new CategoriaDTO("CAT-1", "Sala"), "Proyector")));
        } else if (ruta.equals("/estadistica/recursos")) {
            enviar(exchange, 200, GsonUtil.GSON.toJson(
                    List.of(new ResultadoEstadisticaDTO("Sala", 3))));
        } else {
            enviar(exchange, 404, "{\"error\":\"Ruta no encontrada.\"}");
        }
    }

    private void enviar(HttpExchange exchange, int estado, String cuerpo) throws IOException {
        byte[] datos = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(estado, datos.length);
        try (var salida = exchange.getResponseBody()) {
            salida.write(datos);
        }
    }
}
