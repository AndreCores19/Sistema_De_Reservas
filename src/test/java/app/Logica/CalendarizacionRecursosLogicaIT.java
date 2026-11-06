package app.Logica;

import app.DTO.MatrizDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class CalendarizacionRecursosLogicaIT {

    private final CalendarizacionRecursosLogica calendarizacion = new CalendarizacionRecursosLogica();

    @BeforeEach
    void prepararDatos() throws SQLException {
        app.DatosDePrueba.crear();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        app.DatosDePrueba.borrar();
    }

    private MatrizDTO bloque(List<MatrizDTO> matriz, String hora) {
        return matriz.stream()
                .filter(m -> m.getHora().equals(hora))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el bloque de hora " + hora));
    }

    @Test
    void construirMatrizDebeTenerVeintiochoBloquesDeMediaHoraEntreLasSieteYLasNueveDeLaNoche() throws Exception {
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_SALAS, LUNES);

        assertEquals(28, matriz.size());
        assertEquals("07:00", matriz.get(0).getHora());
        assertEquals("20:30", matriz.get(matriz.size() - 1).getHora());
    }

    @Test
    void construirMatrizDebeIncluirUnaColumnaPorCadaRecursoDeLaCategoria() throws Exception {
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_SALAS, LUNES);

        assertEquals(2, matriz.get(0).getEstadoPorRecurso().size());
        assertTrue(matriz.get(0).getEstadoPorRecurso().containsKey(SALA_1));
        assertTrue(matriz.get(0).getEstadoPorRecurso().containsKey(SALA_2));
    }

    @Test
    void construirMatrizDebeMarcarOcupadoSoloElRangoExactoDeLaReserva() throws Exception {
        // RES-999901: lunes, proyector, de 09:00 a 11:00
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_PROYECTORES, LUNES);

        assertEquals("Libre", bloque(matriz, "08:30").getEstadoPorRecurso().get(PROYECTOR));
        assertEquals("Ocupado", bloque(matriz, "09:00").getEstadoPorRecurso().get(PROYECTOR));
        assertEquals("Ocupado", bloque(matriz, "10:30").getEstadoPorRecurso().get(PROYECTOR));
        // termina a las 11:00 en punto: ese bloque ya no está ocupado
        assertEquals("Libre", bloque(matriz, "11:00").getEstadoPorRecurso().get(PROYECTOR));
    }

    @Test
    void construirMatrizNoDebeMezclarLaOcupacionEntreRecursosDeLaMismaCategoria() throws Exception {
        // RES-999904: jueves, solo la sala 1, de 10:00 a 11:00
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_SALAS, LUNES.plusDays(3));

        assertEquals("Ocupado", bloque(matriz, "10:00").getEstadoPorRecurso().get(SALA_1));
        assertEquals("Libre", bloque(matriz, "10:00").getEstadoPorRecurso().get(SALA_2));
    }

    @Test
    void construirMatrizSoloDebeConsiderarReservasDeLaFechaSolicitada() throws Exception {
        // la sala 1 está ocupada el jueves y el lunes anterior, pero no este lunes
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_SALAS, LUNES);

        for (MatrizDTO fila : matriz) {
            assertEquals("Libre", fila.getEstadoPorRecurso().get(SALA_1));
        }
    }

    @Test
    void construirMatrizNoDebeConsiderarUnaReservaCancelada() throws Exception {
        // RES-999903: martes, laptop 2, de 09:00 a 10:00, CANCELADA
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_LAPTOPS, LUNES.plusDays(1));

        assertEquals("Libre", bloque(matriz, "09:00").getEstadoPorRecurso().get(LAPTOP_2));
    }

    @Test
    void construirMatrizDebeConsiderarLaOtraReservaActivaDelMismoDia() throws Exception {
        // RES-999902: martes, laptop 1, de 08:00 a 09:00, ACTIVA
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_LAPTOPS, LUNES.plusDays(1));

        assertEquals("Ocupado", bloque(matriz, "08:00").getEstadoPorRecurso().get(LAPTOP_1));
        assertEquals("Libre", bloque(matriz, "08:00").getEstadoPorRecurso().get(LAPTOP_2));
    }

    @Test
    void construirMatrizSiLaCategoriaNoExisteDebeLanzarException() {
        assertThrows(Exception.class, () -> calendarizacion.construirMatriz("CAT-999990", LUNES));
    }

    @Test
    void construirMatrizSiLaCategoriaNoTieneRecursosDebeDevolverFilasSinColumnas() throws Exception {
        List<MatrizDTO> matriz = calendarizacion.construirMatriz(CAT_SIN_RECURSOS, LUNES);

        assertEquals(28, matriz.size());
        assertTrue(matriz.get(0).getEstadoPorRecurso().isEmpty());
    }
}