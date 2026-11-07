package app.ui;

import app.DTO.AlertaReservaDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AlertasModel {
    private final ObservableList<AlertaReservaDTO> alertas = FXCollections.observableArrayList();

    public ObservableList<AlertaReservaDTO> getAlertas() {
        return alertas;
    }

    public void agregar(AlertaReservaDTO alerta) {
        if (alerta != null) {
            alertas.add(alerta);
        }
    }

    public void descartar(AlertaReservaDTO alerta) {
        alertas.remove(alerta);
    }
}
