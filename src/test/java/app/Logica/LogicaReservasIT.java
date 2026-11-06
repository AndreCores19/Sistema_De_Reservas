package app.Logica;

import app.DTO.ReservaDTO;
import app.Datos.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LogicaReservasIT {

    private static final String FUNCIONARIO = "LR-PRUEBA-FUN";
    private static final String CATEGORIA = "CAT-999999";
    private static final String RECURSO = "LR-PRUEBA-01";
    private static final LocalDate FECHA_FUTURA = LocalDate.of(2099, 1, 5);

    private final LogicaReservas logica = new LogicaReservas();

    @BeforeEach
    void crearDatosDePrueba() throws SQLException {
        borrarDatosDePrueba();
        ejecutar("INSERT INTO usuarios (id, clave, rol, nombre, telefono) VALUES (?, ?, 'FUNCIONARIO', 'Funcionario logica', '80000000')",
                FUNCIONARIO, FUNCIONARIO);
        ejecutar("INSERT INTO categorias (id, descripcion) VALUES (?, 'Categoria con un recurso')", CATEGORIA);
        ejecutar("INSERT INTO recursos (num_activo, id_categoria, descripcion) VALUES (?, ?, 'Unico recurso')",
                RECURSO, CATEGORIA);
    }

    @AfterEach
    void borrarDatosDePrueba() throws SQLException {
        ejecutar("DELETE FROM reservas WHERE id_funcionario = ?", FUNCIONARIO);
        ejecutar("DELETE FROM recursos WHERE num_activo = ?", RECURSO);
        ejecutar("DELETE FROM categorias WHERE id = ?", CATEGORIA);
        ejecutar("DELETE FROM usuarios WHERE id = ?", FUNCIONARIO);
    }

    private void ejecutar(String sql, String... parametros) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                sentencia.setString(i + 1, parametros[i]);
            }
            sentencia.executeUpdate();
        }
    }

    private ReservaDTO nuevaReserva(LocalDate fecha, int horaInicio, int horaFin) {
        return new ReservaDTO(null, "Actividad de prueba", fecha,
                LocalTime.of(horaInicio, 0), LocalTime.of(horaFin, 0),
                new ArrayList<>(List.of(CATEGORIA)), FUNCIONARIO, null, new ArrayList<>());
    }

    @Test
    void agregarReservaDebeAsignarElRecursoYUnIdConFormato() throws Exception {
        ReservaDTO reserva = nuevaReserva(FECHA_FUTURA, 9, 10);

        logica.agregarReserva(reserva);

        assertTrue(reserva.getIdReserva().matches("RES-\\d{6}"));
        assertEquals("ACTIVA", reserva.getEstado());
        assertEquals(List.of(RECURSO), reserva.getIdsRecursosAsignados());
        assertEquals(1, logica.listarReservasPorFuncionario(FUNCIONARIO).size());
    }

    @Test
    void agregarSiNoHayRecursoLibreDebeLanzarExceptionYNoGuardar() throws Exception {
        logica.agregarReserva(nuevaReserva(FECHA_FUTURA, 9, 10));

        Exception ex = assertThrows(Exception.class,
                () -> logica.agregarReserva(nuevaReserva(FECHA_FUTURA, 9, 10)));

        assertTrue(ex.getMessage().startsWith("No hay recursos disponibles"));
        assertEquals(1, logica.listarReservasPorFuncionario(FUNCIONARIO).size());
    }

    @Test
    void agregarEnUnHorarioQueNoSeTraslapaDebeReutilizarElRecurso() throws Exception {
        logica.agregarReserva(nuevaReserva(FECHA_FUTURA, 9, 10));

        ReservaDTO siguiente = nuevaReserva(FECHA_FUTURA, 10, 11);
        logica.agregarReserva(siguiente);

        assertEquals(List.of(RECURSO), siguiente.getIdsRecursosAsignados());
    }

    @Test
    void unaReservaCanceladaDebeLiberarElRecurso() throws Exception {
        ReservaDTO primera = nuevaReserva(FECHA_FUTURA, 9, 10);
        logica.agregarReserva(primera);
        logica.cancelarReserva(primera.getIdReserva());

        ReservaDTO segunda = nuevaReserva(FECHA_FUTURA, 9, 10);
        logica.agregarReserva(segunda);

        assertEquals(List.of(RECURSO), segunda.getIdsRecursosAsignados());
    }

    @Test
    void agregarConFechaPasadaDebeLanzarException() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.agregarReserva(nuevaReserva(LocalDate.of(2020, 1, 1), 9, 10)));
        assertEquals("La fecha y hora de la reserva deben ser futuras.", ex.getMessage());
    }

    @Test
    void agregarConHoraFinAntesDeLaInicialDebeLanzarException() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.agregarReserva(nuevaReserva(FECHA_FUTURA, 10, 9)));
        assertEquals("La hora de fin debe ser posterior a la hora de inicio.", ex.getMessage());
    }

    @Test
    void agregarSinCategoriasDebeLanzarException() {
        ReservaDTO sinCategorias = nuevaReserva(FECHA_FUTURA, 9, 10);
        sinCategorias.setIdsCategoriaSolicitada(new ArrayList<>());

        Exception ex = assertThrows(Exception.class, () -> logica.agregarReserva(sinCategorias));
        assertEquals("Debe seleccionar al menos una categoría.", ex.getMessage());
    }

    @Test
    void cancelarReservaDebeCambiarElEstado() throws Exception {
        ReservaDTO reserva = nuevaReserva(FECHA_FUTURA, 9, 10);
        logica.agregarReserva(reserva);

        logica.cancelarReserva(reserva.getIdReserva());

        assertEquals("CANCELADA", logica.listarReservasPorFuncionario(FUNCIONARIO).get(0).getEstado());
        assertFalse(logica.tieneReservasActivas(FUNCIONARIO));
    }

    @Test
    void cancelarUnaReservaYaCanceladaDebeLanzarException() throws Exception {
        ReservaDTO reserva = nuevaReserva(FECHA_FUTURA, 9, 10);
        logica.agregarReserva(reserva);
        logica.cancelarReserva(reserva.getIdReserva());

        Exception ex = assertThrows(Exception.class, () -> logica.cancelarReserva(reserva.getIdReserva()));
        assertEquals("La reserva ya está cancelada.", ex.getMessage());
    }

    @Test
    void cancelarUnaReservaInexistenteDebeLanzarException() {
        assertThrows(Exception.class, () -> logica.cancelarReserva("RES-000000"));
    }


    @Test
    void dosReservasSimultaneasNoDebenRecibirElMismoRecurso() throws Exception {
        AtomicInteger exitosas = new AtomicInteger(0);

        Runnable intentarReservar = () -> {
            try {
                new LogicaReservas().agregarReserva(nuevaReserva(FECHA_FUTURA, 9, 10));
                exitosas.incrementAndGet();
            } catch (Exception e) {
                // la segunda reserva debe fallar porque el único recurso ya está ocupado
            }
        };

        Thread hilo1 = new Thread(intentarReservar);
        Thread hilo2 = new Thread(intentarReservar);
        hilo1.start();
        hilo2.start();
        hilo1.join();
        hilo2.join();

        assertEquals(1, exitosas.get());
        assertEquals(1, logica.listarReservasPorFuncionario(FUNCIONARIO).size());
    }
}