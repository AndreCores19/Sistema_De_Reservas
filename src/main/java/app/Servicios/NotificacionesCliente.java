package app.Servicios;

import app.DTO.AlertaReservaDTO;

import java.util.function.Consumer;

public interface NotificacionesCliente {
    void registrarListener(Consumer<AlertaReservaDTO> listener);

    void iniciar();

    void detener();
}
