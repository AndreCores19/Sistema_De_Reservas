package app.Servicios;

import app.DTO.UsuarioDTO;

public class ServicioUsuarioCliente extends ClienteBase {

    public UsuarioDTO iniciarSesion(String id, String clave) throws Exception {
        String cuerpo = "id=" + codificar(id) + "&clave=" + codificar(clave);
        String json = enviarPost("/usuario/login", cuerpo);
        return gson.fromJson(json, UsuarioDTO.class);
    }

    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        String cuerpo = "id=" + codificar(id)
                + "&claveActual=" + codificar(claveActual)
                + "&claveNueva=" + codificar(claveNueva);
        enviarPost("/usuario/cambiarClave", cuerpo);
    }
}