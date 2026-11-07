package app.ui;

import app.Controllers.SesionActual;
import app.DTO.RecursoDTO;
import app.Servicios.ServicioRecursoCliente;
import app.DTO.CategoriaDTO;
import app.DTO.MatrizDTO;
import app.Servicios.ServicioCalendarizacionRecursosCliente;
import app.Servicios.ServicioCategoriaCliente;
import app.ui.utilidades.ServicioImpresion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarizacionRecursosViewController {


    @FXML private ComboBox<CategoriaDTO> cbxCategoria;
    @FXML private DatePicker dpFechafiltros;
    @FXML private Button btnCargarFiltros;
    @FXML private Button btnImprimirEnFiltros;
    @FXML private Button btnCerrarSesion;
    @FXML private TableView<MatrizDTO> tvCalendarizacion;
    @FXML private TableColumn<MatrizDTO, String> tcHora;

    private final ServicioCalendarizacionRecursosCliente servicioCalendarizacion = new ServicioCalendarizacionRecursosCliente();
    private final ServicioCategoriaCliente servicioCategoria = new ServicioCategoriaCliente();
    private final ServicioImpresion servicioImpresion = new ServicioImpresion();
    private final ServicioRecursoCliente servicioRecurso = new ServicioRecursoCliente();

    @FXML
    public void initialize() {
        tcHora.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hora"));

        List<CategoriaDTO> categorias;
        try {
            categorias = servicioCategoria.listarTodas();
        } catch (Exception e) {
            mostrarError("No se pudieron cargar las categorías: " + e.getMessage());
            categorias = new ArrayList<>();
        }

        cbxCategoria.setItems(FXCollections.observableArrayList(categorias));
        cbxCategoria.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(CategoriaDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDescripcion());
            }
        });
        cbxCategoria.setButtonCell(cbxCategoria.getCellFactory().call(null));
        btnCerrarSesion.setOnAction(event -> cerrarSesion(event));
        btnCargarFiltros.setOnAction(event -> cargarMatriz());
        btnImprimirEnFiltros.setOnAction(event -> {
            try {
                imprimirMatriz();
            } catch (Exception e) {
                mostrarError("Error al imprimir la matriz: " + e.getMessage());
            }
        });
    }

    private void cargarMatriz() {
        CategoriaDTO categoria = cbxCategoria.getValue();
        LocalDate fecha = dpFechafiltros.getValue();

        if (categoria == null || fecha == null) {
            mostrarError("Por favor, seleccione una categoría y una fecha.");
            return;
        }
        try {
            List<MatrizDTO> filas = servicioCalendarizacion.obtenerMatrizDeRecursos(categoria.getId(), fecha);
            if (!construirColumnas(categoria)) {
                return;
            }
            tvCalendarizacion.setItems(FXCollections.observableArrayList(filas));
        } catch (Exception e) {
            mostrarError("Error al cargar la matriz: " + e.getMessage());
        }
    }

    private boolean construirColumnas(CategoriaDTO categoria) {
        tvCalendarizacion.getColumns().setAll(tcHora);

        List<RecursoDTO> recursosDeCategoria;
        try {
            recursosDeCategoria = servicioRecurso.filtrarPorCategoria(categoria.getId());
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los recursos: " + e.getMessage());
            return false;
        }

        for (var recurso : recursosDeCategoria) {
            TableColumn<MatrizDTO, String> columnaRecurso = new TableColumn<>(recurso.getDescripcion());
            String numActivo = recurso.getNumActivo();
            columnaRecurso.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstadoPorRecurso().get(numActivo)));

            tvCalendarizacion.getColumns().add(columnaRecurso);
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void cerrarSesion(javafx.event.ActionEvent event) {
        SesionActual.setUsuarioActual(null);
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

    private void imprimirMatriz() {
        CategoriaDTO categoria = cbxCategoria.getValue();
        LocalDate fecha = dpFechafiltros.getValue();
        if (categoria == null || fecha == null) {
            mostrarError("Por favor, seleccione una categoría y una fecha.");
            return;
        }
        try {
            List<MatrizDTO> matriz = servicioCalendarizacion.obtenerMatrizDeRecursos(categoria.getId(), fecha);
            servicioImpresion.imprimirMatrizRecursos(categoria, matriz);
        } catch (Exception e) {
            mostrarError("Error al obtener e imprimir el calendario: " + e.getMessage());
        }
    }

}