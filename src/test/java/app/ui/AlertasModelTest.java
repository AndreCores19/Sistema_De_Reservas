package app.ui;

import app.DTO.AlertaReservaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertasModelTest {
    @Test
    void agregaYDescartaAlertasDeFormaLocal() {
        AlertasModel modelo = new AlertasModel();
        AlertaReservaDTO alerta = new AlertaReservaDTO();

        modelo.agregar(alerta);
        assertEquals(1, modelo.getAlertas().size());

        modelo.descartar(alerta);
        assertEquals(0, modelo.getAlertas().size());
    }

    @Test
    void ignoraAlertasNulas() {
        AlertasModel modelo = new AlertasModel();

        modelo.agregar(null);

        assertEquals(0, modelo.getAlertas().size());
    }
}
