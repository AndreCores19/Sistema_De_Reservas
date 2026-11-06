package app.Datos;

import app.DTO.ReservaDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDatos {

    private static final String SELECT_BASE =
            "SELECT id_reserva, actividad, fecha, hora_inicio, hora_fin, id_funcionario, estado "
                    + "FROM reserva ";

    private static final String ORDEN = "ORDER BY fecha, hora_inicio";


    public List<ReservaDTO> listarTodas() throws SQLException {
        return consultar(SELECT_BASE + ORDEN);
    }

    public List<ReservaDTO> listarActivasPorRango(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_BASE + "WHERE fecha BETWEEN ? AND ? AND estado = 'ACTIVA' " + ORDEN;
        return consultar(sql, desde, hasta);
    }

    public List<ReservaDTO> listarPorFuncionario(String idFuncionario) throws SQLException {
        String sql = SELECT_BASE + "WHERE id_funcionario = ? " + ORDEN;
        return consultar(sql, idFuncionario);
    }

    public ReservaDTO buscarPorId(String idReserva) throws SQLException {
        List<ReservaDTO> resultado = consultar(SELECT_BASE + "WHERE id_reserva = ?", idReserva);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public boolean tieneActivasPorFuncionario(String idFuncionario) throws SQLException {
        String sql = "SELECT 1 FROM reserva WHERE id_funcionario = ? AND estado = 'ACTIVA' LIMIT 1";
        return existe(sql, idFuncionario);
    }

    public boolean tieneActivasPorRecurso(String numActivo) throws SQLException {
        String sql = "SELECT 1 FROM reserva r "
                + "INNER JOIN reserva_recurso rr ON r.id_reserva = rr.id_reserva "
                + "WHERE rr.num_activo = ? AND r.estado = 'ACTIVA' LIMIT 1";
        return existe(sql, numActivo);
    }

    public String obtenerUltimoId() throws SQLException {
        String sql = "SELECT MAX(id_reserva) AS ultimo FROM reserva";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {

            if (filas.next()) {
                return filas.getString("ultimo");
            }
        }
        return null;
    }


    public void insertar(ReservaDTO reserva) throws SQLException {
        String sqlReserva = "INSERT INTO reserva "
                + "(id_reserva, actividad, fecha, hora_inicio, hora_fin, id_funcionario, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCategoria = "INSERT INTO reserva_categoria (id_reserva, id_categoria) VALUES (?, ?)";
        String sqlRecurso = "INSERT INTO reserva_recurso (id_reserva, num_activo) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(sqlReserva)) {
                    sentencia.setString(1, reserva.getIdReserva());
                    sentencia.setString(2, reserva.getActividad());
                    sentencia.setObject(3, reserva.getFecha());
                    sentencia.setObject(4, reserva.getHoraInicio());
                    sentencia.setObject(5, reserva.getHoraFin());
                    sentencia.setString(6, reserva.getIdFuncionario());
                    sentencia.setString(7, reserva.getEstado());
                    sentencia.executeUpdate();
                }

                insertarIds(conexion, sqlCategoria, reserva.getIdReserva(), reserva.getIdsCategoriaSolicitada());
                insertarIds(conexion, sqlRecurso, reserva.getIdReserva(), reserva.getIdsRecursosAsignados());

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public boolean actualizarEstado(String idReserva, String estado) throws SQLException {
        String sql = "UPDATE reserva SET estado = ? WHERE id_reserva = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setString(2, idReserva);

            return sentencia.executeUpdate() > 0;
        }
    }


    private List<ReservaDTO> consultar(String sql, Object... parametros) throws SQLException {
        List<ReservaDTO> resultado = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                sentencia.setObject(i + 1, parametros[i]);
            }

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    resultado.add(crearReserva(filas));
                }
            }

            for (ReservaDTO reserva : resultado) {
                cargarListas(conexion, reserva);
            }
        }
        return resultado;
    }

    private void cargarListas(Connection conexion, ReservaDTO reserva) throws SQLException {
        reserva.setIdsCategoriaSolicitada(consultarIds(conexion,
                "SELECT id_categoria FROM reserva_categoria WHERE id_reserva = ? ORDER BY id_categoria",
                reserva.getIdReserva()));

        reserva.setIdsRecursosAsignados(consultarIds(conexion,
                "SELECT num_activo FROM reserva_recurso WHERE id_reserva = ? ORDER BY num_activo",
                reserva.getIdReserva()));
    }

    private List<String> consultarIds(Connection conexion, String sql, String idReserva) throws SQLException {
        List<String> ids = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, idReserva);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    ids.add(filas.getString(1));
                }
            }
        }
        return ids;
    }

    private void insertarIds(Connection conexion, String sql, String idReserva, List<String> ids) throws SQLException {
        if (ids == null) {
            return;
        }
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (String id : ids) {
                sentencia.setString(1, idReserva);
                sentencia.setString(2, id);
                sentencia.executeUpdate();
            }
        }
    }

    private boolean existe(String sql, String parametro) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, parametro);

            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next();
            }
        }
    }

    private ReservaDTO crearReserva(ResultSet fila) throws SQLException {
        return new ReservaDTO(
                fila.getString("id_reserva"),
                fila.getString("actividad"),
                fila.getObject("fecha", LocalDate.class),
                fila.getObject("hora_inicio", LocalTime.class),
                fila.getObject("hora_fin", LocalTime.class),
                new ArrayList<>(),
                fila.getString("id_funcionario"),
                fila.getString("estado"),
                new ArrayList<>()
        );
    }
}