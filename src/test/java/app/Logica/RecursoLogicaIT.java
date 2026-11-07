package app.Logica;

import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class RecursoLogicaIT {

    private final RecursoLogica logica = new RecursoLogica();

    @BeforeEach
    void prepararDatos() throws SQLException {
        app.DatosDePrueba.crear();
    }

    @AfterEach
    void borrarDatos() throws SQLException {
        app.DatosDePrueba.borrar();
    }

    private RecursoDTO recurso(String numActivo, String idCategoria, String descripcion) {
        CategoriaDTO categoria = idCategoria == null ? null : new CategoriaDTO(idCategoria, null);
        return new RecursoDTO(numActivo, categoria, descripcion);
    }


    @Test
    void filtrarPorCategoriaDebeEncontrarLasDosSalas() {
        assertEquals(2, logica.filtrarPorCategoria(CAT_SALAS).size());
    }

    @Test
    void filtrarPorCategoriaSinRecursosDebeRetornarVacio() {
        assertTrue(logica.filtrarPorCategoria(CAT_SIN_RECURSOS).isEmpty());
    }

    @Test
    void buscarPorNumActivoDebeTraerLaCategoriaCompleta() throws Exception {
        RecursoDTO encontrado = logica.buscarPorNumActivo(PROYECTOR);

        assertEquals("Proyector de prueba", encontrado.getDescripcion());
        assertEquals("Prueba Proyectores", encontrado.getCategoria().getDescripcion());
    }

    @Test
    void buscarPorNumActivoSiNoExisteDebeLanzarExcepcion() {
        assertThrows(Exception.class, () -> logica.buscarPorNumActivo("D-NO-EXISTE"));
    }


    @Test
    void agregarDebeGuardarElRecursoConSuCategoria() throws Exception {
        logica.agregar(recurso("D-NUEVO-1", CAT_SALAS, "Recurso nuevo"));

        assertEquals("Prueba Salas", logica.buscarPorNumActivo("D-NUEVO-1").getCategoria().getDescripcion());
    }

    @Test
    void agregarSiElNumActivoEsVacioDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.agregar(recurso("   ", CAT_SALAS, "Algo")));
        assertEquals("El número de activo no puede estar vacío.", ex.getMessage());
    }

    @Test
    void agregarSiElNumActivoEsMuyLargoDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.agregar(recurso("D-12345678901234", CAT_SALAS, "Algo")));
        assertEquals("El número de activo no puede tener más de 15 caracteres.", ex.getMessage());
    }

    @Test
    void agregarSinCategoriaDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.agregar(recurso("D-NUEVO-1", null, "Algo")));
        assertEquals("Debe seleccionar una categoría.", ex.getMessage());
    }

    @Test
    void agregarConUnaCategoriaInexistenteDebeLanzarExcepcion() {
        assertThrows(Exception.class, () -> logica.agregar(recurso("D-NUEVO-1", "CAT-999990", "Algo")));
    }

    @Test
    void agregarConUnNumActivoRepetidoDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.agregar(recurso(SALA_1, CAT_SALAS, "Repetido")));
        assertEquals("Ya existe un recurso con ese número de activo.", ex.getMessage());
    }


    @Test
    void modificarDebeActualizarDescripcionYCategoria() throws Exception {
        logica.modificar(recurso(SALA_2, CAT_LAPTOPS, "Sala convertida"));

        RecursoDTO modificado = logica.buscarPorNumActivo(SALA_2);
        assertEquals("Sala convertida", modificado.getDescripcion());
        assertEquals(CAT_LAPTOPS, modificado.getCategoria().getId());
    }

    @Test
    void modificarSiNoExisteDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class,
                () -> logica.modificar(recurso("D-NO-EXISTE", CAT_SALAS, "Algo")));
        assertEquals("No existe un recurso con ese número de activo.", ex.getMessage());
    }


    @Test
    void eliminarUnRecursoConReservasActivasDebeLanzarExcepcion() {
        Exception ex = assertThrows(Exception.class, () -> logica.eliminar(SALA_1));
        assertEquals("No se puede eliminar un recurso que tiene reservas activas.", ex.getMessage());
    }

    @Test
    void eliminarUnRecursoConSoloReservasCanceladasDebeRespetarElHistorial() {
        Exception ex = assertThrows(Exception.class, () -> logica.eliminar(LAPTOP_2));
        assertEquals("No se puede eliminar: el recurso aparece en el historial de reservas.", ex.getMessage());
    }

    @Test
    void eliminarUnRecursoSinReservasDebeBorrarlo() throws Exception {
        logica.eliminar(SALA_2);

        assertThrows(Exception.class, () -> logica.buscarPorNumActivo(SALA_2));
    }
}