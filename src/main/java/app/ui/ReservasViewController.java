package app.ui;

import app.Controllers.SesionActual;
import app.DTO.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import app.Servicios.ServicioReservasCliente;
import app.Servicios.ServicioCategoriaCliente;
import app.Servicios.ServicioGeminiCliente;
import app.ui.utilidades.ServicioImpresion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservasViewController {
    @FXML private TextField txtFFrase;
    @FXML private TextField txtFActividad;
    @FXML private Button btnExtraerAI;

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbxHoraInicio;
    @FXML private ComboBox<String> cbxHoraFin;

    @FXML private Button btnCatReservar;
    @FXML private Button btnCatCancelarSeleccionada;
    @FXML private Button btnLimpiarSeleccion;
    @FXML private ListView <CategoriaDTO> lvCategorias;

    @FXML private Button btnCerrarSesion;

    @FXML private TableView <ReservaDTO> tvMisReservas;
    @FXML private TableColumn <ReservaDTO,String> tcReservasId;
    @FXML private TableColumn <ReservaDTO,String> tcReservasActividad;
    @FXML private TableColumn <ReservaDTO,String> tcReservasFecha;
    @FXML private TableColumn <ReservaDTO,String> tcReservasHorario;
    @FXML private TableColumn <ReservaDTO,String> tcReservasRecursos;
    @FXML private TableColumn <ReservaDTO,String> tcReservasEstado;
    @FXML private Button btnImprimirReservas;

    private final ServicioReservasCliente servicioReservas = new ServicioReservasCliente();
    private final ServicioCategoriaCliente servicioCategoria = new ServicioCategoriaCliente();
    private final ServicioImpresion servicioImpresion = new ServicioImpresion();
    private String idFuncionarioActual;
    private final ServicioGeminiCliente servicioGemini = new ServicioGeminiCliente();


    @FXML
    public void initialize() {
        idFuncionarioActual = SesionActual.getUsuarioActual().getId();
        btnExtraerAI.setOnAction(event -> extraerIA());
        btnCatReservar.setOnAction(event -> reservarCategoria());
        btnCatCancelarSeleccionada.setOnAction(event -> cancelarReservaSeleccionada());
        btnLimpiarSeleccion.setOnAction(event -> limpiarSeleccion());
        btnImprimirReservas.setOnAction(event -> {
            try {
                imprimirReservas();
            } catch (Exception e) {
                mostrarError("No se pudo generar el reporte: " + mensajeError(e));
            }
        });
        tvMisReservas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtFActividad.setText(seleccionado.getActividad());
                dpFecha.setValue(seleccionado.getFecha());
                cbxHoraInicio.setValue(seleccionado.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
                cbxHoraFin.setValue(seleccionado.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        });
        btnCerrarSesion.setOnAction(event -> cerrarSesion(event));

        cargarHorario();
        configurarFecha();
        configurarTablaReservas();
        cargarCategorias();
        cargarMisReservas();
    }

    private void extraerIA() {
        String frase = txtFFrase.getText();
        if (frase.isEmpty()) {
            mostrarError("Ingrese una frase para extraer la actividad.");
            return;
        }
        try {
            ExtraccionIADTO extraido = servicioGemini.extraerDatosDeLaIA(frase);
            limpiarSeleccion();
            txtFActividad.setText(frase);
            if (extraido.getActividad() != null) txtFActividad.setText(extraido.getActividad());
            if (extraido.getFecha() != null) dpFecha.setValue(extraido.getFecha());
            if (extraido.getHoraInicio() != null) cbxHoraInicio.setValue(extraido.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
            if (extraido.getHoraFin() != null) cbxHoraFin.setValue(extraido.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")));
            seleccionarCategoriasSugeridas(extraido.getCategoriasSugeridas());
        } catch (Exception e) {
            mostrarError("No se pudo interpretar la frase. Completa el formulario manualmente.");
        }
    }
    private void seleccionarCategoriasSugeridas(List<String> categoriasSugeridas) {
        if (categoriasSugeridas == null || categoriasSugeridas.isEmpty()) {
            return;
        }
        for(CategoriaDTO categoria : lvCategorias.getItems()) {
            for (String sugeridas : categoriasSugeridas) {
                if (categoria.getDescripcion().toLowerCase().contains(sugeridas.toLowerCase()) || sugeridas.toLowerCase().contains(categoria.getDescripcion().toLowerCase())) {
                    lvCategorias.getSelectionModel().select(categoria);
                    break;
                }
            }
        }
    }

    private void cargarHorario() {
        ObservableList<String> horas = FXCollections.observableArrayList();
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        LocalDate fechaSeleccionada = dpFecha.getValue();
        if (fechaSeleccionada == null) {
            cbxHoraInicio.getItems().clear();
            cbxHoraFin.getItems().clear();
            return;
        }
        for (int i = 7; i < 21; i++) {
            LocalTime horaEnPunto = LocalTime.of(i, 0);
            LocalTime horaMedia = LocalTime.of(i, 30);
            if (!fechaSeleccionada.equals(hoy) || horaEnPunto.isAfter(ahora)) {
                horas.add(String.format("%02d:00", i));
            }
            if (!fechaSeleccionada.equals(hoy) || horaMedia.isAfter(ahora)) {
                horas.add(String.format("%02d:30", i));
            }
        }
        cbxHoraInicio.setItems(horas);
        cbxHoraFin.setItems(horas);
    }


    private void configurarFecha() {
        LocalDate hoy = LocalDate.now();
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                if (fecha.isBefore(hoy)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #72303f;");
                }
            }
        });

        dpFecha.valueProperty().addListener((obs, fechaAnterior, fechaNueva) -> {
            cargarHorario();
        });
    }

    private void configurarTablaReservas() {
        tcReservasId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));
        tcReservasActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        tcReservasFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFecha().toString()));
        tcReservasHorario.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + data.getValue().getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm"))));
        tcReservasRecursos.setCellValueFactory(data -> new SimpleStringProperty(String.join(", ", data.getValue().getIdsRecursosAsignados())));
        tcReservasEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void imprimirReservas() {
        List<ReservaDTO> reservas = tvMisReservas.getItems();
        if (reservas.isEmpty()) {
            mostrarError("No hay reservas para imprimir.");
            return;
        }
        try {
            servicioImpresion.imprimirReservas(reservas);
        } catch (Exception e) {
            mostrarError("No se pudo generar el reporte: " + e.getMessage());
        }
    }

    private void cargarCategorias() {
        List<CategoriaDTO> categorias;
        try {
            categorias = servicioCategoria.listarTodas();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            categorias = new ArrayList<>();
        }

        ObservableList<CategoriaDTO> datosCategorias = FXCollections.observableArrayList(categorias);
        lvCategorias.setItems(datosCategorias);
        lvCategorias.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        lvCategorias.setCellFactory(lv -> new ListCell<CategoriaDTO>() {
            @Override
            protected void updateItem(CategoriaDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " - " + item.getDescripcion());
                }
            }
        });
    }

    private void cargarMisReservas() {
        if(idFuncionarioActual == null) return;
        try {
            List<ReservaDTO> reservas = servicioReservas.listarReservasPorFuncionario(idFuncionarioActual);
            ObservableList<ReservaDTO> misReservas = FXCollections.observableArrayList(reservas);
            tvMisReservas.setItems(misReservas);
        } catch (Exception e) {
            mostrarError("No se pudieron cargar las reservas: " + e.getMessage());
        }
    }

    private void reservarCategoria() {
        try {
        List<String> idsCategorias = lvCategorias.getSelectionModel().getSelectedItems().stream().map(CategoriaDTO::getId).collect(Collectors.toList());
        LocalTime horaInicio = cbxHoraInicio.getValue() == null
                ? null : LocalTime.parse(cbxHoraInicio.getValue());
        LocalTime horaFin = cbxHoraFin.getValue() == null
                ? null : LocalTime.parse(cbxHoraFin.getValue());
        String errorValidacion = ReservaFormularioValidator.validar(
                txtFActividad.getText(),
                dpFecha.getValue(),
                horaInicio,
                horaFin,
                idsCategorias,
                LocalDate.now(),
                LocalTime.now()
        );
        if (errorValidacion != null) {
           mostrarError(errorValidacion);
           return;
        }
        ReservaDTO nuevaReserva =
                new ReservaDTO(
                        null,
                        txtFActividad.getText(),
                        dpFecha.getValue(),
                        horaInicio,
                        horaFin,
                        idsCategorias,
                        idFuncionarioActual,
                        "ACTIVA",
                        new ArrayList<>()
                );
         servicioReservas.reservarEspacio(nuevaReserva);
         mostrarExito("Reserva realizada con éxito"); //Si llego aqui es porque el catch no capturo ninguna excepcion y la reserva fue exitosa

        limpiarSeleccion();
        cargarMisReservas();
        } catch (Exception e) {
            mostrarError("No se pudo realizar la reserva: " + e.getMessage());
        }
    }

    private void cancelarReservaSeleccionada() {
        ReservaDTO seleccionada = tvMisReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Seleccione una reserva de la tabla para cancelar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de que desea cancelar la reserva seleccionada?");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    servicioReservas.cancelarReserva(seleccionada.getIdReserva());
                    mostrarExito("Reserva cancelada con exito.");
                    cargarMisReservas();
                } catch (Exception e) {
                    mostrarError("No se pudo cancelar la reserva: " + e.getMessage());
                }
            }
        });
    }

    private void limpiarSeleccion() {
        txtFActividad.clear();
        txtFFrase.clear();
        dpFecha.setValue(null);
        cbxHoraInicio.setValue(null);
        cbxHoraFin.setValue(null);
        lvCategorias.getSelectionModel().clearSelection();

    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private String mensajeError(Exception excepcion) {
        return excepcion.getMessage() == null || excepcion.getMessage().isBlank()
                ? "Error inesperado."
                : excepcion.getMessage();
    }

    private void mostrarExito(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarSesion(javafx.event.ActionEvent event) {
        SesionActual.cerrarSesion();
        try {
            javafx.scene.Parent raiz = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/app/ui/login-view.fxml")
            );
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(raiz);
        } catch (Exception e) {
            mostrarError(e.getMessage() != null ? e.getMessage() : "No se pudo volver al inicio de sesión.");
        }
    }
}
