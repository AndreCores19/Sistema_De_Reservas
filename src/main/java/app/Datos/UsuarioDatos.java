package app.Datos;

import app.DTO.AdministradorDTO;
import app.DTO.FuncionarioDTO;
import app.DTO.UsuarioDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDatos {

    public UsuarioDTO buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, clave, rol, nombre, telefono FROM usuarios WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return crearUsuario(resultado);
                }
            }
        }
        return null;
    }

    public boolean actualizarClave(String id, String claveNueva) throws SQLException {
        String sql = "UPDATE usuarios SET clave = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, claveNueva);
            sentencia.setString(2, id);

            return sentencia.executeUpdate() > 0;
        }
    }

    private UsuarioDTO crearUsuario(ResultSet resultado) throws SQLException {
        String rol = resultado.getString("rol");

        if ("ADMIN".equals(rol)) {
            return new AdministradorDTO(
                    resultado.getString("nombre"),
                    resultado.getString("id"),
                    resultado.getString("clave"),
                    rol
            );
        }
        return new FuncionarioDTO(
                resultado.getString("id"),
                resultado.getString("clave"),
                rol,
                resultado.getString("nombre"),
                resultado.getString("telefono")
        );
    }
}