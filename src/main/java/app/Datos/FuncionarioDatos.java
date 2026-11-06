package app.Datos;

import app.DTO.FuncionarioDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDatos {

    private static final String SELECT_BASE =
            "SELECT id, clave, rol, nombre, telefono FROM usuario WHERE rol = 'FUNCIONARIO' ";

    public List<FuncionarioDTO> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY nombre";
        List<FuncionarioDTO> funcionarios = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                funcionarios.add(crearFuncionario(resultado));
            }
        }
        return funcionarios;
    }

    public FuncionarioDTO buscarPorId(String id) throws SQLException {
        String sql = SELECT_BASE + "AND id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return crearFuncionario(resultado);
                }
            }
        }
        return null;
    }

    public List<FuncionarioDTO> buscarPorNombre(String texto) throws SQLException {
        String sql = SELECT_BASE + "AND nombre LIKE ? ORDER BY nombre";
        List<FuncionarioDTO> funcionarios = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, "%" + texto + "%");

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    funcionarios.add(crearFuncionario(resultado));
                }
            }
        }
        return funcionarios;
    }

    public boolean existeId(String id) throws SQLException {
        String sql = "SELECT 1 FROM usuario WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public void insertar(FuncionarioDTO funcionario) throws SQLException {
        String sql = "INSERT INTO usuario (id, clave, rol, nombre, telefono) VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, funcionario.getId());
            sentencia.setString(2, funcionario.getClave());
            sentencia.setString(3, funcionario.getRol());
            sentencia.setString(4, funcionario.getNombre());
            sentencia.setString(5, funcionario.getTelefono());

            sentencia.executeUpdate();
        }
    }

    public boolean actualizar(FuncionarioDTO funcionario) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, telefono = ? WHERE id = ? AND rol = 'FUNCIONARIO'";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, funcionario.getNombre());
            sentencia.setString(2, funcionario.getTelefono());
            sentencia.setString(3, funcionario.getId());

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ? AND rol = 'FUNCIONARIO'";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, id);

            return sentencia.executeUpdate() > 0;
        }
    }

    private FuncionarioDTO crearFuncionario(ResultSet resultado) throws SQLException {
        return new FuncionarioDTO(
                resultado.getString("id"),
                resultado.getString("clave"),
                resultado.getString("rol"),
                resultado.getString("nombre"),
                resultado.getString("telefono")
        );
    }
}