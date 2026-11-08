package app.ui;

import app.DTO.AlertaReservaDTO;
import app.DTO.ReservaDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class AlertasViewController {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TableView<AlertaReservaDTO> tablaAlertas;
    @FXML private TableColumn<AlertaReservaDTO, String> columnaActividad;
    @FXML private TableColumn<AlertaReservaDTO, Void> columnaVer;
    @FXML private TableColumn<AlertaReservaDTO, Void> columnaDescartar;

    private final AlertasModel modelo = new AlertasModel();

    @FXML
    private void initialize() {
        columnaActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        configurarBoton(columnaVer, "\uD83D\uDCC1", "Ver detalle de la alerta", null, this::verAlerta);
        configurarBoton(columnaDescartar, "\u2296", "Descartar alerta",
                "-fx-text-fill: #d32f2f; -fx-font-weight: bold;", this::descartarAlerta);
        tablaAlertas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaAlertas.setItems(modelo.getAlertas());
    }


     //Se agrega una alerta desde el hilo

    public void agregarAlerta(AlertaReservaDTO alerta) {
        modelo.agregar(alerta);
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
                                 String icono, String descripcion,
                                 String estilo,
                                 java.util.function.Consumer<AlertaReservaDTO> accion) {
        columna.setCellFactory(ignored -> new TableCell<>() {
            private final Button boton = new Button(icono);

            {
                boton.setTooltip(new Tooltip(descripcion));
                boton.setAccessibleText(descripcion);
                if (estilo != null) {
                    boton.setStyle(estilo);
                }
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
        Stage detalle = new Stage();
        detalle.setTitle("Detalle de reserva");
        detalle.initModality(Modality.APPLICATION_MODAL);

        Label titulo = new Label(valor(alerta.getActividad()));
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane datos = new GridPane();
        datos.setHgap(12);
        datos.setVgap(8);
        datos.add(new Label("Encargado:"), 0, 0);
        datos.add(new Label(valor(alerta.getEncargado())), 1, 0);
        datos.add(new Label("Fecha:"), 0, 1);
        datos.add(new Label(valor(alerta.getFecha())), 1, 1);
        datos.add(new Label("Horas:"), 0, 2);
        datos.add(new Label(horario), 1, 2);
        datos.add(new Label("Recursos:"), 0, 3);

        ListView<String> listaRecursos = new ListView<>();
        listaRecursos.getItems().setAll(alerta.getRecursos() == null
                ? java.util.List.of("No indicados")
                : alerta.getRecursos());
        listaRecursos.setPrefHeight(100);
        datos.add(listaRecursos, 1, 3);

        Button cerrar = new Button("Cerrar");
        cerrar.setOnAction(event -> detalle.close());

        VBox contenido = new VBox(14, titulo, datos, cerrar);
        contenido.setPadding(new Insets(16));
        contenido.setMinWidth(360);
        VBox.setMargin(cerrar, new Insets(4, 0, 0, 0));

        detalle.setScene(new Scene(contenido));
        detalle.setResizable(false);
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
                modelo.descartar(alerta);
            }
        });
    }

    private String valor(Object valor) {
        return valor == null ? "No indicado" : valor.toString();
    }
}
