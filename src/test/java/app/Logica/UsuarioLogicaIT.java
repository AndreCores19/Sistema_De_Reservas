package app.Logica;

import app.DTO.AdministradorDTO;
import app.DTO.FuncionarioDTO;
import app.DTO.UsuarioDTO;
import app.Datos.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioLogicaIT {

    private static final String ID_ADMIN = "U-PRUEBA-ADM";
    private static final String ID_FUNCIONARIO = "U-PRUEBA-FUN";

    private final UsuarioLogica logica = new UsuarioLogica();

    @BeforeEach
    void crearUsuariosDePrueba() throws SQLException {
        borrarUsuariosDePrueba();
        insertarUsuario(ID_ADMIN, "claveAdmin", "ADMIN", "Admin de prueba", null);
        insertarUsuario(ID_FUNCIONARIO, "claveFunc", "FUNCIONARIO", "Funcionario de prueba", "88888888");
    }

    @AfterEach
    void borrarUsuariosDePrueba() throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id IN (?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, ID_ADMIN);
            sentencia.setString(2, ID_FUNCIONARIO);
            sentencia.executeUpdate();
        }
    }

    private void insertarUsuario(String id, String clave, String rol, String nombre, String telefono) throws SQLException {
        String sql = "INSERT INTO usuarios (id, clave, rol, nombre, telefono) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, id);
            sentencia.setString(2, clave);
            sentencia.setString(3, rol);
            sentencia.setString(4, nombre);
            sentencia.setString(5, telefono);
            sentencia.executeUpdate();
        }
    }

    @Test
    void iniciarSesionDebeAutenticarAlAdministrador() throws Exception {
        UsuarioDTO usuario = logica.iniciarSesion(ID_ADMIN, "claveAdmin");

        assertInstanceOf(AdministradorDTO.class, usuario);
        assertEquals("ADMIN", usuario.getRol());
    }

    @Test
    void iniciarSesionDebeAutenticarAlFuncionario() throws Exception {
        UsuarioDTO usuario = logica.iniciarSesion(ID_FUNCIONARIO, "claveFunc");

        assertInstanceOf(FuncionarioDTO.class, usuario);
        assertEquals("88888888", ((FuncionarioDTO) usuario).getTelefono());
    }

    @Test
    void iniciarSesionSiElIdEsVacioDebeLanzarException() {
        Exception ex = assertThrows(Exception.class, () -> logica.iniciarSesion("", "claveFunc"));
        assertEquals("Debe ingresar el id.", ex.getMessage());
    }

    @Test
    void iniciarSesionSiLaClaveEsVaciaDebeLanzarException() {
        Exception ex = assertThrows(Exception.class, () -> logica.iniciarSesion(ID_FUNCIONARIO, ""));
        assertEquals("Debe ingresar la clave.", ex.getMessage());
    }

    @Test
    void iniciarSesionSiLaClaveEsIncorrectaDebeLanzarException() {
        Exception ex = assertThrows(Exception.class, () -> logica.iniciarSesion(ID_FUNCIONARIO, "otraClave"));
        assertEquals("El id o la clave son incorrectos.", ex.getMessage());
    }

    @Test
    void iniciarSesionSiElIdNoExisteDebeLanzarException() {
        Exception ex = assertThrows(Exception.class, () -> logica.iniciarSesion("NO-EXISTE", "claveFunc"));
        assertEquals("El id o la clave son incorrectos.", ex.getMessage());
    }

    @Test
    void iniciarSesionDebeDistinguirMayusculasEnLaClave() {
        assertThrows(Exception.class, () -> logica.iniciarSesion(ID_FUNCIONARIO, "CLAVEFUNC"));
    }

    @Test
    void cambiarClaveDebeActualizarLaClaveEnLaBase() throws Exception {
        logica.cambiarClave(ID_FUNCIONARIO, "claveFunc", "claveNueva");

        assertNotNull(logica.iniciarSesion(ID_FUNCIONARIO, "claveNueva"));
        assertThrows(Exception.class, () -> logica.iniciarSesion(ID_FUNCIONARIO, "claveFunc"));
    }

    @Test
    void cambiarClaveSiLaClaveActualNoCoincideDebeLanzarException() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.cambiarClave(ID_FUNCIONARIO, "incorrecta", "claveNueva"));
        assertEquals("El id o la clave son incorrectos.", ex.getMessage());
    }

    @Test
    void cambiarClaveSiLaClaveNuevaEsVaciaDebeLanzarException() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.cambiarClave(ID_FUNCIONARIO, "claveFunc", ""));
        assertEquals("La clave nueva no puede estar vacía.", ex.getMessage());
    }
}



