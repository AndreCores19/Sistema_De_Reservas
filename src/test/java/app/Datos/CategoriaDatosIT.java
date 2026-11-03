package app.Datos;

import app.DTO.CategoriaDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaDatosIT {

    private static final String ID_PRUEBA_1 = "CAT-999997";
    private static final String ID_PRUEBA_2 = "CAT-999998";
    private static final String ID_PRUEBA_3 = "CAT-999999";
    private static final String ID_INEXISTENTE = "CAT-999990";

    private final CategoriaDatos datos = new CategoriaDatos();

    @AfterEach
    void borrarDatosDePrueba() throws SQLException {
        datos.eliminar(ID_PRUEBA_1);
        datos.eliminar(ID_PRUEBA_2);
        datos.eliminar(ID_PRUEBA_3);
    }

    @Test
    void listarTodasDebeIncluirLasCategoriasInsertadas() throws SQLException {
        int cantidadAntes = datos.listarTodas().size();

        datos.insertar(new CategoriaDTO(ID_PRUEBA_1, "Categoria de prueba uno"));
        datos.insertar(new CategoriaDTO(ID_PRUEBA_2, "Categoria de prueba dos"));

        assertEquals(cantidadAntes + 2, datos.listarTodas().size());
    }

    @Test
    void insertarYBuscarPorIdDebenEncontrarLaCategoria() throws SQLException {
        datos.insertar(new CategoriaDTO(ID_PRUEBA_1, "Categoria de prueba"));

        CategoriaDTO encontrada = datos.buscarPorId(ID_PRUEBA_1);

        assertNotNull(encontrada);
        assertEquals("Categoria de prueba", encontrada.getDescripcion());
    }

    @Test
    void buscarPorIdSiNoExisteDebeRetornarNull() throws SQLException {
        assertNull(datos.buscarPorId(ID_INEXISTENTE));
    }

    @Test
    void buscarPorDescripcionNoDebeDistinguirMayusculas() throws SQLException {
        datos.insertar(new CategoriaDTO(ID_PRUEBA_1, "Categoria Prueba Zeta"));

        List<CategoriaDTO> resultado = datos.buscarPorDescripcion("PRUEBA ZETA");

        assertEquals(1, resultado.size());
        assertEquals(ID_PRUEBA_1, resultado.get(0).getId());
    }

    @Test
    void actualizarDebeCambiarLaDescripcion() throws SQLException {
        datos.insertar(new CategoriaDTO(ID_PRUEBA_1, "Descripcion original"));

        boolean actualizada = datos.actualizar(new CategoriaDTO(ID_PRUEBA_1, "Descripcion nueva"));

        assertTrue(actualizada);
        assertEquals("Descripcion nueva", datos.buscarPorId(ID_PRUEBA_1).getDescripcion());
    }

    @Test
    void actualizarSiNoExisteDebeRetornarFalse() throws SQLException {
        assertFalse(datos.actualizar(new CategoriaDTO(ID_INEXISTENTE, "No importa")));
    }

    @Test
    void eliminarDebeBorrarLaCategoria() throws SQLException {
        datos.insertar(new CategoriaDTO(ID_PRUEBA_1, "Categoria a borrar"));

        assertTrue(datos.eliminar(ID_PRUEBA_1));
        assertNull(datos.buscarPorId(ID_PRUEBA_1));
    }

    @Test
    void eliminarSiNoExisteDebeRetornarFalse() throws SQLException {
        assertFalse(datos.eliminar(ID_INEXISTENTE));
    }

    @Test
    void obtenerUltimoIdDebeRetornarElMayor() throws SQLException {
        datos.insertar(new CategoriaDTO(ID_PRUEBA_3, "Categoria con el id mas alto"));

        assertEquals(ID_PRUEBA_3, datos.obtenerUltimoId());
    }
}