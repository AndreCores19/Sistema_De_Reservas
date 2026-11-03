package app.Logica;

import app.DTO.FuncionarioDTO;
import app.Datos.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioLogicaIT {

    private static final String ID_FUNCIONARIO = "F-PRUEBA-1";
    private static final String ID_FUNCIONARIO_2 = "F-PRUEBA-2";
    private static final String ID_ADMIN = "F-PRUEBA-ADM";

    private final FuncionarioLogica logica = new FuncionarioLogica();

    @BeforeEach
    @AfterEach
    void borrarUsuariosDePrueba() throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id IN (?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, ID_FUNCIONARIO);
            sentencia.setString(2, ID_FUNCIONARIO_2);
            sentencia.setString(3, ID_ADMIN);
            sentencia.executeUpdate();
        }
    }

    private void insertarAdministrador(String id) throws SQLException {
        String sql = "INSERT INTO usuarios (id, clave, rol, nombre, telefono) VALUES (?, ?, 'ADMIN', 'Admin de prueba', NULL)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, id);
            sentencia.setString(2, id);
            sentencia.executeUpdate();
        }
    }

    private FuncionarioDTO nuevoFuncionario(String id, String nombre, String telefono) {
        return new FuncionarioDTO(id, null, null, nombre, telefono);
    }

    @Test
    void agregarDebeAsignarClaveIgualAlIdYRolFuncionario() throws Exception {
        logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Funcionario de prueba", "88880000"));

        FuncionarioDTO guardado = logica.buscarPorId(ID_FUNCIONARIO);
        assertEquals(ID_FUNCIONARIO, guardado.getClave());
        assertEquals("FUNCIONARIO", guardado.getRol());
        assertEquals("88880000", guardado.getTelefono());
    }

    @Test
    void agregarSiElIdEsVacioDebeLanzarException() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.agregar(nuevoFuncionario("", "Nombre", "88880000")));
        assertEquals("El id del funcionario no puede estar vacío.", ex.getMessage());
    }

    @Test
    void agregarSiElIdEsMuyLargoDebeLanzarException() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.agregar(nuevoFuncionario("1234567890123456", "Nombre", "88880000")));
        assertEquals("El id no puede tener más de 15 caracteres.", ex.getMessage());
    }

    @Test
    void agregarSiYaExisteElIdDebeLanzarException() throws Exception {
        logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Primero", "88880000"));

        Exception ex = assertThrows(Exception.class,
                () -> logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Repetido", "88881111")));
        assertEquals("Ya existe un usuario con ese id.", ex.getMessage());
    }

    @Test
    void agregarConElIdDeUnAdministradorDebeLanzarException() throws Exception {
        insertarAdministrador(ID_ADMIN);

        Exception ex = assertThrows(Exception.class,
                () -> logica.agregar(nuevoFuncionario(ID_ADMIN, "Nombre", "88880000")));
        assertEquals("Ya existe un usuario con ese id.", ex.getMessage());
    }

    @Test
    void listarTodosNoDebeIncluirAdministradores() throws Exception {
        insertarAdministrador(ID_ADMIN);
        logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Funcionario de prueba", "88880000"));

        List<FuncionarioDTO> funcionarios = logica.listarTodos();

        assertTrue(funcionarios.stream().anyMatch(f -> f.getId().equals(ID_FUNCIONARIO)));
        assertTrue(funcionarios.stream().noneMatch(f -> f.getId().equals(ID_ADMIN)));
    }

    @Test
    void buscarPorIdDeUnAdministradorDebeLanzarException() throws Exception {
        insertarAdministrador(ID_ADMIN);

        assertThrows(Exception.class, () -> logica.buscarPorId(ID_ADMIN));
    }

    @Test
    void buscarPorNombreNoDebeDistinguirMayusculas() throws Exception {
        logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Funcionario Prueba Zeta", "88880000"));

        List<FuncionarioDTO> resultado = logica.buscarPorNombre("PRUEBA ZETA");

        assertEquals(1, resultado.size());
        assertEquals(ID_FUNCIONARIO, resultado.get(0).getId());
    }

    @Test
    void modificarDebeActualizarNombreYTelefono() throws Exception {
        logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Nombre original", "88880000"));

        logica.modificar(nuevoFuncionario(ID_FUNCIONARIO, "Nombre nuevo", "60000000"));

        FuncionarioDTO modificado = logica.buscarPorId(ID_FUNCIONARIO);
        assertEquals("Nombre nuevo", modificado.getNombre());
        assertEquals("60000000", modificado.getTelefono());
    }

    @Test
    void modificarSiNoExisteDebeLanzarException() {
        assertThrows(Exception.class,
                () -> logica.modificar(nuevoFuncionario(ID_FUNCIONARIO_2, "Nombre", "88880000")));
    }

    @Test
    void eliminarDebeBorrarAlFuncionario() throws Exception {
        logica.agregar(nuevoFuncionario(ID_FUNCIONARIO, "Funcionario a borrar", "88880000"));

        logica.eliminar(ID_FUNCIONARIO);

        assertThrows(Exception.class, () -> logica.buscarPorId(ID_FUNCIONARIO));
    }

    @Test
    void eliminarSiNoExisteDebeLanzarException() {
        assertThrows(Exception.class, () -> logica.eliminar(ID_FUNCIONARIO_2));
    }
}