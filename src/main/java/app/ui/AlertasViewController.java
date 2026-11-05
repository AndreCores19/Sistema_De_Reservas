package app.ui;

import app.DTO.AlertaReservaDTO;
import app.DTO.ReservaDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;

import java.time.format.DateTimeFormatter;

public class AlertasViewController {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TableView<AlertaReservaDTO> tablaAlertas;
    @FXML private TableColumn<AlertaReservaDTO, String> columnaActividad;
    @FXML private TableColumn<AlertaReservaDTO, String> columnaFecha;
    @FXML private TableColumn<AlertaReservaDTO, Void> columnaVer;
    @FXML private TableColumn<AlertaReservaDTO, Void> columnaDescartar;

    private final ObservableList<AlertaReservaDTO> alertas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        columnaActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        columnaFecha.setCellValueFactory(alerta -> new SimpleStringProperty(alerta.getValue().getFecha() == null ? ""
                : alerta.getValue().getFecha().toString()));
        configurarBoton(columnaVer, "Ver", this::verAlerta);
        configurarBoton(columnaDescartar, "Descartar", this::descartarAlerta);
        tablaAlertas.setItems(alertas);
    }


     //Se agrega una alerta desde el hilo

    public void agregarAlerta(AlertaReservaDTO alerta) {
        if (alerta != null) {
            alertas.add(alerta);
        }
    }

    //Punto de entrada para el hilo que reciba notificaciones del backend.

    public void recibirAlerta(AlertaReservaDTO alerta) {
        if (alerta != null) {
            Platform.runLater(() -> agregarAlerta(alerta));
        }
    }

    public void agregarReserva(ReservaDTO reserva) {
        if (reserva != null) {
            agregarAlerta(new AlertaReservaDTO(reserva));
        }
    }

    public void recibirReserva(ReservaDTO reserva) {
        if (reserva != null) {
            recibirAlerta(new AlertaReservaDTO(reserva));
        }
    }

    private void configurarBoton(TableColumn<AlertaReservaDTO, Void> columna,
                                 String texto, java.util.function.Consumer<AlertaReservaDTO> accion) {
        columna.setCellFactory(ignored -> new TableCell<>() {
            private final Button boton = new Button(texto);

            {
                boton.setOnAction(event -> accion.accept(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : boton);
            }
        });
    }

    private void verAlerta(AlertaReservaDTO alerta) {
        String horario = alerta.getHoraInicio() == null || alerta.getHoraFin() == null
                ? "No indicado"
                : alerta.getHoraInicio().format(FORMATO_HORA) + " - "
                + alerta.getHoraFin().format(FORMATO_HORA);
        String recursos = alerta.getRecursos() == null
                ? "No indicados"
                : String.join(", ", alerta.getRecursos());

        Alert detalle = new Alert(Alert.AlertType.INFORMATION);
        detalle.setTitle("Detalle de reserva");
        detalle.setHeaderText(alerta.getActividad());
        detalle.setContentText(
                "Encargado: " + valor(alerta.getEncargado()) + "\n"
                        + "Fecha: " + valor(alerta.getFecha()) + "\n"
                        + "Horas: " + horario + "\n"
                        + "Recursos: " + recursos
        );
        detalle.showAndWait();
    }

    private void descartarAlerta(AlertaReservaDTO alerta) {
        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Desea descartar esta alerta?",
                ButtonType.CANCEL,
                ButtonType.OK
        );
        confirmacion.setTitle("Descartar alerta");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                alertas.remove(alerta);
            }
        });
    }

    private String valor(Object valor) {
        return valor == null ? "No indicado" : valor.toString();
    }
}
