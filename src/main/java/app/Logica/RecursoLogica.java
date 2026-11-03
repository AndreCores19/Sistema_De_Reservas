package app.Logica;

import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;
import app.Datos.RecursoDatos;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class RecursoLogica {

    private final RecursoDatos datos;
    private final CategoriaLogica categoriaLogica;
    private final LogicaReservas reservaLogica;

    public RecursoLogica() {
        this.datos = new RecursoDatos();
        this.categoriaLogica = new CategoriaLogica();
        this.reservaLogica = new LogicaReservas();
    }

    @Deprecated
    public RecursoLogica(String rutaArchivo) {
        this();
    }

    public List<RecursoDTO> listarTodos() {
        try {
            return datos.listarTodos();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar los recursos.", e);
        }
    }

    public List<RecursoDTO> filtrarPorCategoria(String categoriaId) {
        try {
            return datos.filtrarPorCategoria(categoriaId);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar los recursos de la categoría.", e);
        }
    }

    public RecursoDTO buscarPorNumActivo(String numActivo) throws Exception {
        RecursoDTO recurso = datos.buscarPorNumActivo(numActivo);
        if (recurso == null) {
            throw new Exception("No existe un recurso con número de activo: " + numActivo);
        }
        return recurso;
    }

    public void agregar(RecursoDTO nuevo) throws Exception {
        if (nuevo.getNumActivo() == null || nuevo.getNumActivo().trim().isEmpty()) {
            throw new Exception("El número de activo no puede estar vacío.");
        }
        validarDescripcionYCategoria(nuevo);

        if (datos.buscarPorNumActivo(nuevo.getNumActivo()) != null) {
            throw new Exception("Ya existe un recurso con ese número de activo.");
        }

        datos.insertar(nuevo);
    }

    public void modificar(RecursoDTO actualizado) throws Exception {
        validarDescripcionYCategoria(actualizado);

        boolean encontrado = datos.actualizar(actualizado);
        if (!encontrado) {
            throw new Exception("No existe un recurso con ese número de activo.");
        }
    }

    public void eliminar(String numActivo) throws Exception {
        if (datos.buscarPorNumActivo(numActivo) == null) {
            throw new Exception("No existe un recurso con ese número de activo.");
        }
        if (reservaLogica.tieneReservasActivasPorRecurso(numActivo)) {
            throw new Exception("No se puede eliminar un recurso que tiene reservas activas.");
        }

        try {
            datos.eliminar(numActivo);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("No se puede eliminar: el recurso aparece en el historial de reservas.");
        }
    }

    private void validarDescripcionYCategoria(RecursoDTO recurso) throws Exception {
        if (recurso.getDescripcion() == null || recurso.getDescripcion().trim().isEmpty()) {
            throw new Exception("La descripción no puede estar vacía.");
        }
        if (recurso.getCategoria() == null || recurso.getCategoria().getId() == null) {
            throw new Exception("Debe seleccionar una categoría.");
        }
        CategoriaDTO categoriaReal = categoriaLogica.buscarPorId(recurso.getCategoria().getId());
        recurso.setCategoria(categoriaReal);
    }
}