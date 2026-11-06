package app.Logica;

import app.DTO.CategoriaDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class CategoriaLogicaIT {

    private final CategoriaLogica logica = new CategoriaLogica();

    @BeforeEach
    void prepararDatos() throws SQLException {
        app.DatosDePrueba.crear();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        app.DatosDePrueba.borrar();
    }

    @Test
    void listarTodasDebeIncluirLasCategoriasDePrueba() {
        List<String> ids = logica.listarTodas().stream().map(CategoriaDTO::getId).toList();

        assertTrue(ids.containsAll(List.of(CAT_SALAS, CAT_LAPTOPS, CAT_PROYECTORES, CAT_SIN_RECURSOS)));
    }

    @Test
    void buscarPorIdDebeEncontrarLaCategoriaCorrecta() throws Exception {
        assertEquals("Prueba Salas", logica.buscarPorId(CAT_SALAS).getDescripcion());
    }

    @Test
    void buscarPorIdSiNoExisteDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.buscarPorId("CAT-999990"));
        assertEquals("No existe una categoría con id: CAT-999990", ex.getMessage());
    }

    @Test
    void buscarPorDescripcionNoDebeDistinguirMayusculas() {
        List<CategoriaDTO> resultado = logica.buscarPorDescripcion("PRUEBA SALAS");

        assertEquals(1, resultado.size());
        assertEquals(CAT_SALAS, resultado.get(0).getId());
    }

    @Test
    void agregarDebeAsignarUnIdConFormatoCorrectoYGuardarla() throws Exception {
        CategoriaDTO nueva = new CategoriaDTO(null, "Prueba nueva");

        logica.agregar(nueva);
        try {
            assertTrue(nueva.getId().matches("CAT-\\d{6}"));
            assertEquals("Prueba nueva", logica.buscarPorId(nueva.getId()).getDescripcion());
        } finally {
            logica.eliminar(nueva.getId());
        }
    }

    @Test
    void agregarSiLaDescripcionEsVaciaDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.agregar(new CategoriaDTO(null, "   ")));
        assertEquals("La descripción no puede estar vacía.", ex.getMessage());
    }

    @Test
    void modificarDebeActualizarLaDescripcion() throws Exception {
        logica.modificar(new CategoriaDTO(CAT_SALAS, "Salas renovadas"));

        assertEquals("Salas renovadas", logica.buscarPorId(CAT_SALAS).getDescripcion());
    }

    @Test
    void modificarSiElIdNoExisteDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.modificar(new CategoriaDTO("CAT-999990", "No importa")));
        assertEquals("No existe una categoría con ese id.", ex.getMessage());
    }

    @Test
    void eliminarUnaCategoriaConRecursosDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.eliminar(CAT_SALAS));
        assertEquals("No se puede eliminar: la categoría tiene recursos asociados.", ex.getMessage());
    }

    @Test
    void eliminarUnaCategoriaSinRecursosDebeBorrarla() throws Exception {
        logica.eliminar(CAT_SIN_RECURSOS);

        assertThrows(Exception.class, () -> logica.buscarPorId(CAT_SIN_RECURSOS));
    }
}