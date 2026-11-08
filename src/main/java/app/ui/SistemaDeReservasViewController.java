package app.ui;

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

    private void recibirReserva(app.DTO.AlertaReservaDTO alerta) {
        alertasController.recibirAlerta(alerta);
    }

    public AlertasViewController getAlertasController() {
        return alertasController;
    }
}
