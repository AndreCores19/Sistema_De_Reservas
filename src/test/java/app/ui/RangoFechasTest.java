package app.ui;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RangoFechasTest {
    @Test
    void aceptaElMismoDia() {
        LocalDate fecha = LocalDate.of(2026, 11, 5);

        assertNull(RangoFechas.validar(fecha, fecha));
    }

    @Test
    void rechazaFechasIncompletas() {
        assertEquals(
                "Debe seleccionar las fechas inicial y final.",
                RangoFechas.validar(null, LocalDate.of(2026, 11, 5))
        );
    }

    @Test
    void rechazaUnRangoInvertido() {
        assertEquals(
                "La fecha inicial seleccionada no puede ser posterior a la fecha final seleccionada.",
                RangoFechas.validar(
                        LocalDate.of(2026, 11, 6),
                        LocalDate.of(2026, 11, 5)
                )
        );
    }
}
