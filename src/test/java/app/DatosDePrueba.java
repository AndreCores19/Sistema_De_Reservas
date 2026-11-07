package app;

import app.Datos.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/* Crea en MySQL un escenario fijo para las pruebas y lo borra cuando termina
 Usa fechas de 2099 e ids reconocibles para no mezclarse con los datos reales
 */
public final class DatosDePrueba {

    public static final String FUNCIONARIO_1 = "D-FUN-1";
    public static final String FUNCIONARIO_2 = "D-FUN-2";

    public static final String CAT_SALAS = "CAT-999901";
    public static final String CAT_LAPTOPS = "CAT-999902";
    public static final String CAT_PROYECTORES = "CAT-999903";
    public static final String CAT_SIN_RECURSOS = "CAT-999904";

    public static final String SALA_1 = "D-SALA-1";
    public static final String SALA_2 = "D-SALA-2";
    public static final String LAPTOP_1 = "D-LAP-1";
    public static final String LAPTOP_2 = "D-LAP-2";
    public static final String PROYECTOR = "D-PROY-1";

    /** Lunes de la semana de prueba. */
    public static final LocalDate LUNES = LocalDate.of(2099, 1, 5);

    private DatosDePrueba() {
    }

    public static void crear() throws SQLException {
        borrar();
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            funcionario(conexion, FUNCIONARIO_1, "Ana Prueba");
            funcionario(conexion, FUNCIONARIO_2, "Beto Prueba");

            categoria(conexion, CAT_SALAS, "Prueba Salas");
            categoria(conexion, CAT_LAPTOPS, "Prueba Laptops");
            categoria(conexion, CAT_PROYECTORES, "Prueba Proyectores");
            categoria(conexion, CAT_SIN_RECURSOS, "Prueba Sin Recursos");

            recurso(conexion, SALA_1, CAT_SALAS, "Sala de prueba 1");
            recurso(conexion, SALA_2, CAT_SALAS, "Sala de prueba 2");
            recurso(conexion, LAPTOP_1, CAT_LAPTOPS, "Laptop de prueba 1");
            recurso(conexion, LAPTOP_2, CAT_LAPTOPS, "Laptop de prueba 2");
            recurso(conexion, PROYECTOR, CAT_PROYECTORES, "Proyector de prueba");

            // lunes 09:00-11:00, proyector
            reserva(conexion, "RES-999901", "Sesion de Junta", LUNES, "09:00", "11:00",
                    FUNCIONARIO_1, "ACTIVA", CAT_PROYECTORES, PROYECTOR);
            // martes 08:00-09:00, laptop 1
            reserva(conexion, "RES-999902", "Charla", LUNES.plusDays(1), "08:00", "09:00",
                    FUNCIONARIO_2, "ACTIVA", CAT_LAPTOPS, LAPTOP_1);
            // martes 09:00-10:00, laptop 2, CANCELADA
            reserva(conexion, "RES-999903", "Reunion cancelada", LUNES.plusDays(1), "09:00", "10:00",
                    FUNCIONARIO_2, "CANCELADA", CAT_LAPTOPS, LAPTOP_2);
            // jueves 10:00-11:00, sala 1
            reserva(conexion, "RES-999904", "Entrevista", LUNES.plusDays(3), "10:00", "11:00",
                    FUNCIONARIO_1, "ACTIVA", CAT_SALAS, SALA_1);
            // lunes de la semana anterior, sala 1
            reserva(conexion, "RES-999905", "Semana anterior", LUNES.minusDays(7), "10:00", "12:00",
                    FUNCIONARIO_1, "ACTIVA", CAT_SALAS, SALA_1);
        }
    }

    public static void borrar() throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            ejecutar(conexion, "DELETE FROM reserva WHERE id_reserva LIKE 'RES-99990%'");
            ejecutar(conexion, "DELETE FROM recurso WHERE num_activo LIKE 'D-%'");
            ejecutar(conexion, "DELETE FROM categoria WHERE id LIKE 'CAT-99990%'");
            ejecutar(conexion, "DELETE FROM usuario WHERE id LIKE 'D-%'");
        }
    }

    private static void funcionario(Connection conexion, String id, String nombre) throws SQLException {
        ejecutar(conexion, "INSERT INTO usuario (id, clave, rol, nombre, telefono) VALUES (?, ?, 'FUNCIONARIO', ?, '80000000')",
                id, id, nombre);
    }

    private static void categoria(Connection conexion, String id, String descripcion) throws SQLException {
        ejecutar(conexion, "INSERT INTO categoria (id, descripcion) VALUES (?, ?)", id, descripcion);
    }

    private static void recurso(Connection conexion, String numActivo, String idCategoria, String descripcion) throws SQLException {
        ejecutar(conexion, "INSERT INTO recurso (num_activo, id_categoria, descripcion) VALUES (?, ?, ?)",
                numActivo, idCategoria, descripcion);
    }

    private static void reserva(Connection conexion, String id, String actividad, LocalDate fecha,
                                String inicio, String fin, String funcionario, String estado,
                                String idCategoria, String numActivo) throws SQLException {
        ejecutar(conexion, "INSERT INTO reserva (id_reserva, actividad, fecha, hora_inicio, hora_fin, id_funcionario, estado) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, actividad, fecha.toString(), inicio, fin, funcionario, estado);
        ejecutar(conexion, "INSERT INTO reserva_categoria (id_reserva, id_categoria) VALUES (?, ?)", id, idCategoria);
        ejecutar(conexion, "INSERT INTO reserva_recurso (id_reserva, num_activo) VALUES (?, ?)", id, numActivo);
    }

    private static void ejecutar(Connection conexion, String sql, String... parametros) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                sentencia.setString(i + 1, parametros[i]);
            }
            sentencia.executeUpdate();
        }
    }
}