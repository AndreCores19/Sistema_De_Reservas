package app.Servicios;

import app.DTO.ReservaDTO;
import app.DTO.UsuarioDTO;
import app.DatosDePrueba;
import app.ServidorDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class ServicioReservasClienteIT {

    private final ServicioReservasCliente servicio = new ServicioReservasCliente();

    @BeforeAll static void servidor() throws Exception { ServidorDePrueba.iniciar(); }
    @BeforeEach void datos() throws SQLException { DatosDePrueba.crear(); }
    @AfterEach void limpiar() throws SQLException { DatosDePrueba.borrar(); }

    private ReservaDTO nueva(String funcionario, String categoria, int diasDespuesDelLunes) {
        return new ReservaDTO(null, "Prueba", LUNES.plusDays(diasDespuesDelLunes),
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                List.of(categoria), funcionario, null, new ArrayList<>());
    }

    @Test
    void listarPorFuncionarioDevuelveSoloSusReservas() throws Exception {
        List<ReservaDTO> reservas = servicio.listarReservasPorFuncionario(FUNCIONARIO_1);
        assertEquals(3, reservas.size());
        assertTrue(reservas.stream().allMatch(r -> r.getIdFuncionario().equals(FUNCIONARIO_1)));
    }

    @Test
    void crearReservaAsignaIdEstadoYRecurso() throws Exception {
        servicio.reservarEspacio(nueva(FUNCIONARIO_1, CAT_SALAS, 10));

        List<ReservaDTO> reservas = servicio.listarReservasPorFuncionario(FUNCIONARIO_1);
        assertEquals(4, reservas.size());
        ReservaDTO creada = reservas.stream()
                .filter(r -> "Prueba".equals(r.getActividad())).findFirst().orElseThrow();
        assertEquals("ACTIVA", creada.getEstado());
        assertTrue(creada.getIdReserva().startsWith("RES-"));
        assertEquals(1, creada.getIdsRecursosAsignados().size());
        assertTrue(List.of(SALA_1, SALA_2).contains(creada.getIdsRecursosAsignados().get(0)));
    }

    @Test
    void sinRecursosLibresLaReservaSeRechazaConMensaje() throws Exception {
        servicio.reservarEspacio(nueva(FUNCIONARIO_1, CAT_SALAS, 10));
        servicio.reservarEspacio(nueva(FUNCIONARIO_2, CAT_SALAS, 10));   // hay 2 salas

        Exception e = assertThrows(Exception.class,
                () -> servicio.reservarEspacio(nueva(FUNCIONARIO_1, CAT_SALAS, 10)));
        assertTrue(e.getMessage().contains("No hay recursos disponibles"));
    }

    @Test
    void categoriaSinRecursosSeRechaza() {
        assertThrows(Exception.class,
                () -> servicio.reservarEspacio(nueva(FUNCIONARIO_1, CAT_SIN_RECURSOS, 10)));
    }

    @Test
    void funcionarioInexistenteSeRechaza() {
        Exception e = assertThrows(Exception.class,
                () -> servicio.reservarEspacio(nueva("NO-EXISTE", CAT_SALAS, 10)));
        assertTrue(e.getMessage().contains("No existe un funcionario"));
    }

    @Test
    void fechaPasadaSeRechaza() {
        ReservaDTO pasada = new ReservaDTO(null, "Prueba", LocalDate.of(2020, 1, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                List.of(CAT_SALAS), FUNCIONARIO_1, null, new ArrayList<>());
        assertThrows(Exception.class, () -> servicio.reservarEspacio(pasada));
    }

    @Test
    void cancelarMarcaCanceladaYNoSePuedeCancelarDosVeces() throws Exception {
        servicio.cancelarReserva("RES-999904");   // jueves de 2099

        ReservaDTO cancelada = servicio.listarReservasPorFuncionario(FUNCIONARIO_1).stream()
                .filter(r -> r.getIdReserva().equals("RES-999904")).findFirst().orElseThrow();
        assertEquals("CANCELADA", cancelada.getEstado());

        Exception e = assertThrows(Exception.class, () -> servicio.cancelarReserva("RES-999904"));
        assertTrue(e.getMessage().contains("ya está cancelada"));
    }

    @Test
    void cancelarReservaInexistenteLanzaExcepcion() {
        assertThrows(Exception.class, () -> servicio.cancelarReserva("RES-000000"));
    }
}
