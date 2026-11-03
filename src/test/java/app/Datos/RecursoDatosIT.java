package app.Datos;

import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursoDatosIT {

    private static final String CAT_PRUEBA_1 = "CAT-999998";
    private static final String CAT_PRUEBA_2 = "CAT-999999";

    private static final String RECURSO_1 = "PRUEBA-01";
    private static final String RECURSO_2 = "PRUEBA-02";
    private static final String RECURSO_3 = "PRUEBA-03";
    private static final String RECURSO_INEXISTENTE = "PRUEBA-99";

    private final RecursoDatos datos = new RecursoDatos();
    private final CategoriaDatos categoriaDatos = new CategoriaDatos();

    @BeforeEach
    void crearCategoriasDePrueba() throws SQLException {
        borrarDatosDePrueba();
        categoriaDatos.insertar(new CategoriaDTO(CAT_PRUEBA_1, "Categoria de prueba uno"));
        categoriaDatos.insertar(new CategoriaDTO(CAT_PRUEBA_2, "Categoria de prueba dos"));
    }

    @AfterEach
    void borrarDatosDePrueba() throws SQLException {
        datos.eliminar(RECURSO_1);
        datos.eliminar(RECURSO_2);
        datos.eliminar(RECURSO_3);
        categoriaDatos.eliminar(CAT_PRUEBA_1);
        categoriaDatos.eliminar(CAT_PRUEBA_2);
    }

    private RecursoDTO nuevoRecurso(String numActivo, String idCategoria, String descripcion) {
        return new RecursoDTO(numActivo, new CategoriaDTO(idCategoria, null), descripcion);
    }

    @Test
    void listarTodosDebeIncluirLosRecursosInsertados() throws SQLException {
        int cantidadAntes = datos.listarTodos().size();

        datos.insertar(nuevoRecurso(RECURSO_1, CAT_PRUEBA_1, "Recurso uno"));
        datos.insertar(nuevoRecurso(RECURSO_2, CAT_PRUEBA_1, "Recurso dos"));

        assertEquals(cantidadAntes + 2, datos.listarTodos().size());
    }

    @Test
    void buscarPorNumActivoDebeTraerLaCategoriaCompleta() throws SQLException {
        datos.insertar(nuevoRecurso(RECURSO_1, CAT_PRUEBA_1, "Recurso uno"));

        RecursoDTO encontrado = datos.buscarPorNumActivo(RECURSO_1);

        assertNotNull(encontrado);
        assertEquals("Recurso uno", encontrado.getDescripcion());
        assertEquals(CAT_PRUEBA_1, encontrado.getCategoria().getId());
        assertEquals("Categoria de prueba uno", encontrado.getCategoria().getDescripcion());
    }

    @Test
    void buscarPorNumActivoSiNoExisteDebeRetornarNull() throws SQLException {
        assertNull(datos.buscarPorNumActivo(RECURSO_INEXISTENTE));
    }

    @Test
    void filtrarPorCategoriaDebeTraerSoloLosDeEsaCategoria() throws SQLException {
        datos.insertar(nuevoRecurso(RECURSO_1, CAT_PRUEBA_1, "Recurso uno"));
        datos.insertar(nuevoRecurso(RECURSO_2, CAT_PRUEBA_1, "Recurso dos"));
        datos.insertar(nuevoRecurso(RECURSO_3, CAT_PRUEBA_2, "Recurso tres"));

        List<RecursoDTO> resultado = datos.filtrarPorCategoria(CAT_PRUEBA_1);

        assertEquals(2, resultado.size());
    }

    @Test
    void actualizarDebeCambiarCategoriaYDescripcion() throws SQLException {
        datos.insertar(nuevoRecurso(RECURSO_1, CAT_PRUEBA_1, "Descripcion original"));

        boolean actualizado = datos.actualizar(nuevoRecurso(RECURSO_1, CAT_PRUEBA_2, "Descripcion nueva"));

        assertTrue(actualizado);
        RecursoDTO encontrado = datos.buscarPorNumActivo(RECURSO_1);
        assertEquals("Descripcion nueva", encontrado.getDescripcion());
        assertEquals(CAT_PRUEBA_2, encontrado.getCategoria().getId());
    }

    @Test
    void actualizarSiNoExisteDebeRetornarFalse() throws SQLException {
        assertFalse(datos.actualizar(nuevoRecurso(RECURSO_INEXISTENTE, CAT_PRUEBA_1, "No importa")));
    }

    @Test
    void eliminarDebeBorrarElRecurso() throws SQLException {
        datos.insertar(nuevoRecurso(RECURSO_1, CAT_PRUEBA_1, "Recurso a borrar"));

        assertTrue(datos.eliminar(RECURSO_1));
        assertNull(datos.buscarPorNumActivo(RECURSO_1));
    }

    @Test
    void eliminarSiNoExisteDebeRetornarFalse() throws SQLException {
        assertFalse(datos.eliminar(RECURSO_INEXISTENTE));
    }
}