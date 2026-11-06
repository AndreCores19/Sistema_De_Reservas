package app.Servicios;

import app.DTO.CeldaActividadDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class ServicioProgramacionIT {

    private final ServicioProgramacion servicio = new ServicioProgramacion();

    @BeforeEach
    void prepararDatos() throws SQLException {
        app.DatosDePrueba.crear();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        app.DatosDePrueba.borrar();
    }

    @Test
    void matrizReservasDebeUbicarLaSesionDeJuntaEnLunesHoras9y10() {
        CeldaActividadDTO[][] matriz = servicio.matrizReservas(LUNES);

        assertEquals("Sesion de Junta", matriz[9][0].getNombreActividad());
        assertEquals("Sesion de Junta", matriz[10][0].getNombreActividad());
    }

    @Test
    void matrizReservasDebeAsociarElNombreDelFuncionarioCorrecto() {
        CeldaActividadDTO[][] matriz = servicio.matrizReservas(LUNES);

        assertEquals("Ana Prueba", matriz[9][0].getNombreFuncionario());   // lunes 9:00
        assertEquals("Beto Prueba", matriz[8][1].getNombreFuncionario());  // martes 8:00
        assertEquals("Ana Prueba", matriz[10][3].getNombreFuncionario());  // jueves 10:00
    }

    @Test
    void matrizReservasNoDebeIncluirLaReservaCancelada() {
        CeldaActividadDTO[][] matriz = servicio.matrizReservas(LUNES);

        assertNull(matriz[9][1]); // la cancelada era el martes a las 9
    }

    @Test
    void matrizReservasNoDebeIncluirReservasDeOtraSemana() {
        CeldaActividadDTO[][] matriz = servicio.matrizReservas(LUNES);

        for (int hora = 0; hora < 24; hora++) {
            for (int dia = 0; dia < 7; dia++) {
                if (matriz[hora][dia] != null) {
                    assertNotEquals("Semana anterior", matriz[hora][dia].getNombreActividad());
                }
            }
        }
    }
}