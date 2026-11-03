package app.Logica;

import app.DTO.FuncionarioDTO;
import app.Datos.FuncionarioDatos;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class FuncionarioLogica {

    private static final int LARGO_MAXIMO_ID = 15;
    private static final int LARGO_MAXIMO_TELEFONO = 15;
    private static final int LARGO_MAXIMO_NOMBRE = 100;

    private final FuncionarioDatos datos;
    private final LogicaReservas reservaLogica;

    public FuncionarioLogica() {
        this.datos = new FuncionarioDatos();
        this.reservaLogica = new LogicaReservas();
    }

    @Deprecated
    public FuncionarioLogica(String rutaArchivo) {
        this();
    }

    public List<FuncionarioDTO> listarTodos() {
        try {
            return datos.listarTodos();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar los funcionarios.", e);
        }
    }

    public FuncionarioDTO buscarPorId(String id) throws Exception {
        FuncionarioDTO funcionario = datos.buscarPorId(id);
        if (funcionario == null) {
            throw new Exception("No existe un funcionario con id: " + id);
        }
        return funcionario;
    }

    public List<FuncionarioDTO> buscarPorNombre(String texto) {
        try {
            return datos.buscarPorNombre(texto);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron buscar los funcionarios.", e);
        }
    }

    public void agregar(FuncionarioDTO nuevo) throws Exception {
        if (nuevo.getId() == null || nuevo.getId().trim().isEmpty()) {
            throw new Exception("El id del funcionario no puede estar vacío.");
        }
        if (nuevo.getId().trim().length() > LARGO_MAXIMO_ID) {
            throw new Exception("El id no puede tener más de " + LARGO_MAXIMO_ID + " caracteres.");
        }
        validarNombreYTelefono(nuevo);

        nuevo.setId(nuevo.getId().trim());
        if (datos.existeId(nuevo.getId())) {
            throw new Exception("Ya existe un usuario con ese id.");
        }

        nuevo.setClave(nuevo.getId());
        nuevo.setRol("FUNCIONARIO");
        datos.insertar(nuevo);
    }

    public void modificar(FuncionarioDTO actualizado) throws Exception {
        validarNombreYTelefono(actualizado);

        boolean encontrado = datos.actualizar(actualizado);
        if (!encontrado) {
            throw new Exception("No existe un funcionario con ese id.");
        }
    }

    public void eliminar(String id) throws Exception {
        if (datos.buscarPorId(id) == null) {
            throw new Exception("No existe un funcionario con ese id.");
        }
        if (reservaLogica.tieneReservasActivas(id)) {
            throw new Exception("No se puede eliminar: el funcionario tiene reservas activas.");
        }

        try {
            datos.eliminar(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new Exception("No se puede eliminar: el funcionario aparece en el historial de reservas.");
        }
    }

    private void validarNombreYTelefono(FuncionarioDTO funcionario) throws Exception {
        if (funcionario.getNombre() == null || funcionario.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre no puede estar vacío.");
        }
        if (funcionario.getNombre().length() > LARGO_MAXIMO_NOMBRE) {
            throw new Exception("El nombre no puede tener más de " + LARGO_MAXIMO_NOMBRE + " caracteres.");
        }
        if (funcionario.getTelefono() == null || funcionario.getTelefono().trim().isEmpty()) {
            throw new Exception("El teléfono no puede estar vacío.");
        }
        if (funcionario.getTelefono().length() > LARGO_MAXIMO_TELEFONO) {
            throw new Exception("El teléfono no puede tener más de " + LARGO_MAXIMO_TELEFONO + " caracteres.");
        }
    }
}