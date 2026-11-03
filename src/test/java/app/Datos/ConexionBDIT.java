package app.Datos;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConexionBDIT {

    @Test
    void obtenerConexionDebeConectarConLaBaseDeDatos() throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            assertTrue(conexion.isValid(2));
        }
    }
}