package app.ui;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReservaFormularioValidatorTest {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 7);
    private static final LocalTime AHORA = LocalTime.of(10, 0);

    @Test
    void aceptaUnaReservaValida() {
        assertNull(ReservaFormularioValidator.validar(
                "Clase", HOY.plusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0),
                List.of("CAT-1"), HOY, AHORA));
    }

    @Test
    void rechazaCamposObligatorios() {
        assertEquals("La actividad no puede estar vacía.",
                ReservaFormularioValidator.validar("", HOY, LocalTime.of(8, 0),
                        LocalTime.of(9, 0), List.of("CAT-1"), HOY, AHORA));
    }

    @Test
    void rechazaHorarioInvertido() {
        assertEquals("La hora de fin no puede ser anterior a la hora de inicio.",
                ReservaFormularioValidator.validar("Clase", HOY.plusDays(1),
                        LocalTime.of(10, 0), LocalTime.of(9, 0), List.of("CAT-1"), HOY, AHORA));
    }

    @Test
    void rechazaHoraPasadaParaHoy() {
        assertEquals("La hora de inicio/fin no puede ser anterior a la hora actual.",
                ReservaFormularioValidator.validar("Clase", HOY, LocalTime.of(9, 0),
                        LocalTime.of(11, 0), List.of("CAT-1"), HOY, AHORA));
    }
}
