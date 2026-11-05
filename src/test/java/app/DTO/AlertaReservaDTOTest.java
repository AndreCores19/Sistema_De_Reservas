package app.DTO;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertaReservaDTOTest {
    @Test
    void convierteUnaReservaEnLosDatosDeLaAlerta() {
        ReservaDTO reserva = new ReservaDTO(
                "RES-1",
                "Clase de prueba",
                LocalDate.of(2026, 11, 5),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                List.of("CAT-1"),
                "FUNC-1",
                "ACTIVA",
                List.of("REC-1")
        );

        AlertaReservaDTO alerta = new AlertaReservaDTO(reserva);

        assertEquals("Clase de prueba", alerta.getActividad());
        assertEquals("FUNC-1", alerta.getEncargado());
        assertEquals(LocalDate.of(2026, 11, 5), alerta.getFecha());
        assertEquals(LocalTime.of(8, 0), alerta.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), alerta.getHoraFin());
        assertEquals(List.of("REC-1"), alerta.getRecursos());
    }
}
