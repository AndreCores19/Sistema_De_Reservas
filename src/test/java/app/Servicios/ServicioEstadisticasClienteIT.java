package app.Servicios;

import app.DTO.CeldaActividadDTO;
import app.DTO.ResultadoEstadisticaDTO;
import org.junit.jupiter.api.Test;
import java.util.List;
import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;


public class ServicioEstadisticasClienteIT {
    @Test
    void estadisticaDeRecursosCuentaCadaCategoriaSinLasCanceladas() throws Exception {
        List<ResultadoEstadisticaDTO> r = new ServicioEstadisticasCliente()
                .obtenerEstadisticaRecursos(LUNES, LUNES.plusDays(6));
        assertEquals(3, r.size());
        assertTrue(r.stream().allMatch(x -> x.getCantidad() == 1));
    }

    @Test
    void estadisticaDeActividadesAgrupaPorSemana() throws Exception {
        List<ResultadoEstadisticaDTO> r = new ServicioEstadisticasCliente()
                .obtenerEstadisticaActividades(LUNES, LUNES.plusDays(6));
        assertEquals(1, r.size());
        assertEquals(3, r.get(0).getCantidad());
    }

    @Test
    void matrizDeProgramacionTiene24HorasY7Dias() throws Exception {
        CeldaActividadDTO[][] matriz = new ServicioProgramacionCliente().matrizReservas(LUNES);
        assertEquals(24, matriz.length);
        assertEquals(7, matriz[0].length);
    }
}
