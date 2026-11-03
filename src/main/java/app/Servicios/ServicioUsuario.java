package app.Servicios;

import app.DTO.UsuarioDTO;
import app.Logica.UsuarioLogica;

public class ServicioUsuario {
    private final UsuarioLogica usuarioLogica = new UsuarioLogica();

    public UsuarioDTO iniciarSesion(String id, String clave) throws Exception {
        return usuarioLogica.iniciarSesion(id, clave);
    }

    public void cambiarClave(String id, String claveActual, String nuevaClave) throws Exception {
        usuarioLogica.cambiarClave(id, claveActual, nuevaClave);
    }
}
