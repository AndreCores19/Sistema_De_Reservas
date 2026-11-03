package app.Logica;

import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;
import app.Datos.CategoriaDatos;

import java.sql.SQLException;
import java.util.List;

public class CategoriaLogica {

    private final CategoriaDatos datos;

    public CategoriaLogica() {
        this.datos = new CategoriaDatos();
    }

    @Deprecated
    public CategoriaLogica(String rutaArchivo) {
        this();
    }

    public List<CategoriaDTO> listarTodas() {
        try {
            return datos.listarTodas();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar las categorías.", e);
        }
    }

    public CategoriaDTO buscarPorId(String id) throws Exception {
        CategoriaDTO categoria = datos.buscarPorId(id);
        if (categoria == null) {
            throw new Exception("No existe una categoría con id: " + id);
        }
        return categoria;
    }

    public List<CategoriaDTO> buscarPorDescripcion(String texto) {
        try {
            return datos.buscarPorDescripcion(texto);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron buscar las categorías.", e);
        }
    }

    public void agregar(CategoriaDTO nueva) throws Exception {
        validarDescripcion(nueva);
        nueva.setId(generarSiguienteId());
        datos.insertar(nueva);
    }

    public void modificar(CategoriaDTO actualizada) throws Exception {
        validarDescripcion(actualizada);
        boolean encontrada = datos.actualizar(actualizada);
        if (!encontrada) {
            throw new Exception("No existe una categoría con ese id.");
        }
    }

    public void eliminar(String id) throws Exception {
        if (datos.buscarPorId(id) == null) {
            throw new Exception("No existe una categoría con ese id.");
        }

        RecursoLogica recursoLogica = new RecursoLogica();
        List<RecursoDTO> recursosAsociados = recursoLogica.filtrarPorCategoria(id);
        if (!recursosAsociados.isEmpty()) {
            throw new Exception("No se puede eliminar: la categoría tiene recursos asociados.");
        }

        datos.eliminar(id);
    }

    private void validarDescripcion(CategoriaDTO categoria) throws Exception {
        if (categoria.getDescripcion() == null || categoria.getDescripcion().trim().isEmpty()) {
            throw new Exception("La descripción no puede estar vacía.");
        }
    }

    private String generarSiguienteId() throws SQLException {
        String ultimoId = datos.obtenerUltimoId();
        int numero = 0;
        if (ultimoId != null) {
            numero = Integer.parseInt(ultimoId.replace("CAT-", ""));
        }
        return String.format("CAT-%06d", numero + 1);
    }
}