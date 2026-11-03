package app.Datos;

import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecursoDatos {

    private static final String SELECT_BASE =
            "SELECT r.num_activo, r.descripcion, "
                    + "c.id AS id_categoria, c.descripcion AS descripcion_categoria "
                    + "FROM recursos r "
                    + "INNER JOIN categorias c ON r.id_categoria = c.id ";

    public List<RecursoDTO> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY r.num_activo";
        List<RecursoDTO> recursos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                recursos.add(crearRecurso(resultado));
            }
        }
        return recursos;
    }

    public RecursoDTO buscarPorNumActivo(String numActivo) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.num_activo = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, numActivo);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return crearRecurso(resultado);
                }
            }
        }
        return null;
    }

    public List<RecursoDTO> filtrarPorCategoria(String idCategoria) throws SQLException {
        String sql = SELECT_BASE + "WHERE r.id_categoria = ? ORDER BY r.num_activo";
        List<RecursoDTO> recursos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, idCategoria);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    recursos.add(crearRecurso(resultado));
                }
            }
        }
        return recursos;
    }

    public void insertar(RecursoDTO recurso) throws SQLException {
        String sql = "INSERT INTO recursos (num_activo, id_categoria, descripcion) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, recurso.getNumActivo());
            sentencia.setString(2, recurso.getCategoria().getId());
            sentencia.setString(3, recurso.getDescripcion());

            sentencia.executeUpdate();
        }
    }

    public boolean actualizar(RecursoDTO recurso) throws SQLException {
        String sql = "UPDATE recursos SET id_categoria = ?, descripcion = ? WHERE num_activo = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, recurso.getCategoria().getId());
            sentencia.setString(2, recurso.getDescripcion());
            sentencia.setString(3, recurso.getNumActivo());

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminar(String numActivo) throws SQLException {
        String sql = "DELETE FROM recursos WHERE num_activo = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, numActivo);

            return sentencia.executeUpdate() > 0;
        }
    }

    private RecursoDTO crearRecurso(ResultSet resultado) throws SQLException {
        CategoriaDTO categoria = new CategoriaDTO(
                resultado.getString("id_categoria"),
                resultado.getString("descripcion_categoria")
        );
        return new RecursoDTO(
                resultado.getString("num_activo"),
                categoria,
                resultado.getString("descripcion")
        );
    }
}