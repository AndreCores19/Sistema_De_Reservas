package app.Logica;

import app.DTO.UsuarioDTO;
import app.Datos.UsuarioDatos;

public class UsuarioLogica {

    private final UsuarioDatos datos;

    public UsuarioLogica() {
        this.datos = new UsuarioDatos();
    }

    public UsuarioDTO iniciarSesion(String id, String clave) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("Debe ingresar el id.");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new Exception("Debe ingresar la clave.");
        }

        UsuarioDTO usuario = datos.buscarPorId(id.trim());
        if (usuario == null || !usuario.getClave().equals(clave)) {
            throw new Exception("El id o la clave son incorrectos.");
        }
        return usuario;
    }

    public void cambiarClave(UsuarioDTO usuario, String claveActual, String claveNueva) throws Exception {
        if (!usuario.getClave().equals(claveActual)) {
            throw new Exception("La clave actual no coincide.");
        }
        if (claveNueva == null || claveNueva.trim().isEmpty()) {
            throw new Exception("La clave nueva no puede estar vacía.");
        }
        if (claveNueva.equals(claveActual)) {
            throw new Exception("La clave nueva debe ser distinta a la actual.");
        }

        boolean actualizada = datos.actualizarClave(usuario.getId(), claveNueva);
        if (!actualizada) {
            throw new Exception("No se encontró el usuario para actualizar la clave.");
        }
        usuario.setClave(claveNueva);
    }
}