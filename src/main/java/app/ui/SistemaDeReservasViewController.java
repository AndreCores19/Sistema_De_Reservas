package app.ui;

import app.DTO.ReservaDTO;
import app.Servicios.NotificacionesCliente;
import javafx.fxml.FXML;

public class SistemaDeReservasViewController {
    @FXML
    private AlertasViewController alertasController;

    private NotificacionesCliente notificacionesCliente;

    public void configurarNotificaciones(NotificacionesCliente cliente) {
        if (notificacionesCliente != null) {
            notificacionesCliente.detener();
        }
        notificacionesCliente = cliente;
        if (notificacionesCliente != null) {
            notificacionesCliente.registrarListener(this::recibirReserva);
            notificacionesCliente.iniciar();
        }
    }

    private void recibirReserva(ReservaDTO reserva) {
        alertasController.recibirReserva(reserva);
    }

    public AlertasViewController getAlertasController() {
        return alertasController;
    }
}
