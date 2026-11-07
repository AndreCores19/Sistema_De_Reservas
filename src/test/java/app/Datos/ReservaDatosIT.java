package app.Datos;

import app.DTO.ReservaDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaDatosIT {

    private static final String FUNCIONARIO = "R-PRUEBA-FUN";
    private static final String CATEGORIA = "CAT-999999";
    private static final String RECURSO = "R-PRUEBA-01";
    private static final String RESERVA_1 = "RES-999998";
    private static final String RESERVA_2 = "RES-999999";
    private static final LocalDate FECHA = LocalDate.of(2099, 1, 5);

    private final ReservaDatos datos = new ReservaDatos();

    @BeforeEach
    void crearDatosDePrueba() throws SQLException {
        borrarDatosDePrueba();
        ejecutar("INSERT INTO usuario (id, clave, rol, nombre, telefono) VALUES (?, ?, 'FUNCIONARIO', 'Funcionario reservas', '80000000')",
                FUNCIONARIO, FUNCIONARIO);
        ejecutar("INSERT INTO categoria (id, descripcion) VALUES (?, 'Categoria reservas')", CATEGORIA);
        ejecutar("INSERT INTO recurso (num_activo, id_categoria, descripcion) VALUES (?, ?, 'Recurso reservas')",
                RECURSO, CATEGORIA);
    }

    @AfterEach
    void borrarDatosDePrueba() throws SQLException {
        ejecutar("DELETE FROM reserva WHERE id_reserva IN (?, ?)", RESERVA_1, RESERVA_2);
        ejecutar("DELETE FROM recurso WHERE num_activo = ?", RECURSO);
        ejecutar("DELETE FROM categoria WHERE id = ?", CATEGORIA);
        ejecutar("DELETE FROM usuario WHERE id = ?", FUNCIONARIO);
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

    private ReservaDTO nuevaReserva(String id, String estado, List<String> recursos) {
        return new ReservaDTO(id, "Actividad de prueba", FECHA,
                LocalTime.of(9, 0), LocalTime.of(10, 30),
                List.of(CATEGORIA), FUNCIONARIO, estado, recursos);
    }

    @Test
    void insertarYBuscarPorIdDebenGuardarYLeerLasTresTablas() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_1, "ACTIVA", List.of(RECURSO)));

        ReservaDTO leida = datos.buscarPorId(RESERVA_1);

        assertNotNull(leida);
        assertEquals("Actividad de prueba", leida.getActividad());
        assertEquals(FECHA, leida.getFecha());
        assertEquals(LocalTime.of(9, 0), leida.getHoraInicio());
        assertEquals(LocalTime.of(10, 30), leida.getHoraFin());
        assertEquals(List.of(CATEGORIA), leida.getIdsCategoriaSolicitada());
        assertEquals(List.of(RECURSO), leida.getIdsRecursosAsignados());
    }

    @Test
    void insertarSiFallaUnRecursoNoDebeGuardarNada() throws SQLException {
        ReservaDTO conRecursoInexistente = nuevaReserva(RESERVA_1, "ACTIVA", List.of(RECURSO, "NO-EXISTE"));

        assertThrows(SQLException.class, () -> datos.insertar(conRecursoInexistente));

        assertNull(datos.buscarPorId(RESERVA_1));
    }

    @Test
    void listarActivasPorRangoNoDebeIncluirCanceladas() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_1, "ACTIVA", List.of(RECURSO)));
        datos.insertar(nuevaReserva(RESERVA_2, "CANCELADA", List.of(RECURSO)));

        List<ReservaDTO> resultado = datos.listarActivasPorRango(FECHA, FECHA);

        assertEquals(1, resultado.size());
        assertEquals(RESERVA_1, resultado.get(0).getIdReserva());
    }

    @Test
    void listarPorFuncionarioDebeIncluirActivasYCanceladas() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_1, "ACTIVA", List.of(RECURSO)));
        datos.insertar(nuevaReserva(RESERVA_2, "CANCELADA", List.of(RECURSO)));

        assertEquals(2, datos.listarPorFuncionario(FUNCIONARIO).size());
    }

    @Test
    void tieneActivasDebeIgnorarLasCanceladas() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_1, "CANCELADA", List.of(RECURSO)));

        assertFalse(datos.tieneActivasPorFuncionario(FUNCIONARIO));
        assertFalse(datos.tieneActivasPorRecurso(RECURSO));
    }

    @Test
    void tieneActivasDebeDetectarUnaReservaActiva() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_1, "ACTIVA", List.of(RECURSO)));

        assertTrue(datos.tieneActivasPorFuncionario(FUNCIONARIO));
        assertTrue(datos.tieneActivasPorRecurso(RECURSO));
    }

    @Test
    void actualizarEstadoDebeCancelarLaReserva() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_1, "ACTIVA", List.of(RECURSO)));

        assertTrue(datos.actualizarEstado(RESERVA_1, "CANCELADA"));
        assertEquals("CANCELADA", datos.buscarPorId(RESERVA_1).getEstado());
    }

    @Test
    void obtenerUltimoIdDebeRetornarElMayor() throws SQLException {
        datos.insertar(nuevaReserva(RESERVA_2, "ACTIVA", List.of(RECURSO)));

        assertEquals(RESERVA_2, datos.obtenerUltimoId());
    }
}