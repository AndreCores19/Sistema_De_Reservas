package app.Controllers;

import app.DTO.UsuarioDTO;
import app.Servicios.NotificacionesCliente;

public class SesionActual {

    private static UsuarioDTO usuarioActual;
    private static NotificacionesCliente notificacionesCliente;

    public static UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(UsuarioDTO usuario) {
        usuarioActual = usuario;
    }

    public static void iniciarNotificaciones(NotificacionesCliente cliente) {
        detenerNotificaciones();
        notificacionesCliente = cliente;
    }

    public static void detenerNotificaciones() {
        if (notificacionesCliente != null) {
            notificacionesCliente.detener();
            notificacionesCliente = null;
        }
    }

    public static void cerrarSesion() {
        detenerNotificaciones();
        usuarioActual = null;
    }
}