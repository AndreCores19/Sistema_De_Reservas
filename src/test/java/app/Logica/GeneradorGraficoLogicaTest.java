package app.Logica;

import app.DTO.ResultadoEstadisticaDTO;
import app.ui.utilidades.GeneradorGraficoLogica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneradorGraficoLogicaTest {

    private List<ResultadoEstadisticaDTO> datosValidos;

    @BeforeEach
    void setUp() {
        datosValidos = List.of(new ResultadoEstadisticaDTO("Sala de Juntas", 2));
    }

    @Test
    void generarConDatosNullDebeLanzarException() {
        assertThrows(IllegalArgumentException.class, () ->
                GeneradorGraficoLogica.generar("Titulo", "Categoría", "Cantidad", null));
    }

    @Test
    void generarConDatosVaciosDebeLanzarException() {
        assertThrows(IllegalArgumentException.class, () ->
                GeneradorGraficoLogica.generar("Titulo", "Categoría", "Cantidad", List.of()));
    }
    @Test
    void generarConEtiquetaNullDebeLanzarException() {
        List<ResultadoEstadisticaDTO> datosConEtiquetaNull = List.of(new ResultadoEstadisticaDTO(null, 2));
        assertThrows(IllegalArgumentException.class, () ->
                GeneradorGraficoLogica.generar("Titulo", "Categoría", "Cantidad", datosConEtiquetaNull));
    }
}