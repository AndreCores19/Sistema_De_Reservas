package app.Servicios;

import app.DTO.UsuarioDTO;
import app.DatosDePrueba;
import app.ServidorDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static app.DatosDePrueba.FUNCIONARIO_1;
import static org.junit.jupiter.api.Assertions.*;

class ServicioUsuarioClienteIT {

    private final ServicioUsuarioCliente servicio = new ServicioUsuarioCliente();

    @BeforeAll
    static void servidor() throws Exception { ServidorDePrueba.iniciar(); }
    @BeforeEach
    void datos() throws SQLException { DatosDePrueba.crear(); }
    @AfterEach
    void limpiar() throws SQLException { DatosDePrueba.borrar(); }

    @Test
    void loginCorrectoDevuelveElUsuarioSinLaClave() throws Exception {
        UsuarioDTO usuario = servicio.iniciarSesion(FUNCIONARIO_1, FUNCIONARIO_1);
        assertEquals(FUNCIONARIO_1, usuario.getId());
        assertNull(usuario.getClave());
    }

    @Test
    void loginConClaveIncorrectaLanzaExcepcion() {
        assertThrows(Exception.class, () -> servicio.iniciarSesion(FUNCIONARIO_1, "mala"));
    }

    @Test
    void cambiarClaveConClaveActualIncorrectaLanzaExcepcion() {
        assertThrows(Exception.class,
                () -> servicio.cambiarClave(FUNCIONARIO_1, "mala", "OtraClave1"));
    }
}
