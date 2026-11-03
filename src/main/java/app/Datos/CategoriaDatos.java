package app.Datos;

import app.DTO.CategoriaDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDatos {

    public List<CategoriaDTO> listarTodas() throws SQLException {
        String sql = "SELECT id, descripcion FROM categorias ORDER BY id";
        List<CategoriaDTO> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                categorias.add(crearCategoria(resultado));
            }
        }
        return categorias;
    }

    public CategoriaDTO buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, descripcion FROM categorias WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return crearCategoria(resultado);
                }
            }
        }
        return null;
    }

    public List<CategoriaDTO> buscarPorDescripcion(String texto) throws SQLException {
        String sql = "SELECT id, descripcion FROM categorias WHERE descripcion LIKE ? ORDER BY id";
        List<CategoriaDTO> categorias = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, "%" + texto + "%");

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    categorias.add(crearCategoria(resultado));
                }
            }
        }
        return categorias;
    }

    public void insertar(CategoriaDTO categoria) throws SQLException {
        String sql = "INSERT INTO categorias (id, descripcion) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, categoria.getId());
            sentencia.setString(2, categoria.getDescripcion());

            sentencia.executeUpdate();
        }
    }

    public boolean actualizar(CategoriaDTO categoria) throws SQLException {
        String sql = "UPDATE categorias SET descripcion = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, categoria.getDescripcion());
            sentencia.setString(2, categoria.getId());

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, id);

            return sentencia.executeUpdate() > 0;
        }
    }

    public String obtenerUltimoId() throws SQLException {
        String sql = "SELECT MAX(id) AS ultimo FROM categorias";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            if (resultado.next()) {
                return resultado.getString("ultimo");
            }
        }
        return null;
    }

    private CategoriaDTO crearCategoria(ResultSet resultado) throws SQLException {
        return new CategoriaDTO(
                resultado.getString("id"),
                resultado.getString("descripcion")
        );
    }
}