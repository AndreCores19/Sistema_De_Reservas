package app.Servicios;

import app.DTO.ReservaDTO;

import java.util.function.Consumer;

public interface NotificacionesCliente {
    void registrarListener(Consumer<ReservaDTO> listener);

    void iniciar();

    void detener();
}
