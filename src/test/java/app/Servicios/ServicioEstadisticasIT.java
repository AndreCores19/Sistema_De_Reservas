package app.Servicios;

import app.DTO.ResultadoEstadisticaDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class ServicioEstadisticasIT {

    private final ServicioEstadisticas servicio = new ServicioEstadisticas();

    @BeforeEach
    void prepararDatos() throws SQLException {
        app.DatosDePrueba.crear();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        app.DatosDePrueba.borrar();
    }

    @Test
    void obtenerEstadisticaRecursosDebeContarCadaCategoriaSinLasCanceladas() {
        List<ResultadoEstadisticaDTO> resultado = servicio.obtenerEstadisticaRecursos(LUNES, LUNES.plusDays(6));

        // activas en la semana: una de proyectores, una de laptops y una de salas
        // (la de laptops cancelada no cuenta)
        assertEquals(3, resultado.size());
        for (ResultadoEstadisticaDTO r : resultado) {
            assertEquals(1, r.getCantidad());
        }

        List<String> etiquetas = resultado.stream().map(ResultadoEstadisticaDTO::getEtiqueta).toList();
        assertTrue(etiquetas.containsAll(List.of("Prueba Salas", "Prueba Laptops", "Prueba Proyectores")));
        assertFalse(etiquetas.contains("Desconocida"));
    }

    @Test
    void obtenerEstadisticaActividadesDebeAgruparLasReservasDeLaSemana() {
        List<ResultadoEstadisticaDTO> resultado = servicio.obtenerEstadisticaActividades(LUNES, LUNES.plusDays(6));

        assertEquals(1, resultado.size());
        assertEquals(LUNES.toString(), resultado.get(0).getEtiqueta());
        assertEquals(3, resultado.get(0).getCantidad());
    }

    @Test
    void obtenerEstadisticaActividadesDebeSepararPorSemana() {
        List<ResultadoEstadisticaDTO> resultado =
                servicio.obtenerEstadisticaActividades(LUNES.minusDays(7), LUNES.plusDays(6));

        assertEquals(2, resultado.size());
        assertEquals(LUNES.minusDays(7).toString(), resultado.get(0).getEtiqueta());
        assertEquals(1, resultado.get(0).getCantidad());
        assertEquals(LUNES.toString(), resultado.get(1).getEtiqueta());
        assertEquals(3, resultado.get(1).getCantidad());
    }
}