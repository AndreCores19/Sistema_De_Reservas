package app.ui;

import app.Controllers.SesionActual;
import app.DTO.ResultadoEstadisticaDTO;
import app.ui.utilidades.ServicioGeneradorGrafico;
import app.Servicios.ServicioEstadisticasCliente;
import app.ui.utilidades.ServicioImpresion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasViewController {
    //objetos Recursos
    @FXML private DatePicker dteDesdeR;
    @FXML private DatePicker dteHastaR;
    @FXML private TableView<ResultadoEstadisticaDTO> tblRecursos;
    @FXML private TableColumn<ResultadoEstadisticaDTO, String> tblCategoriaR;
    @FXML private TableColumn<ResultadoEstadisticaDTO, String> tblCantidadR;
    @FXML private ImageView imgGraficoR;

    // objetos Actividades
    @FXML private DatePicker dteDesdeA;
    @FXML private DatePicker dteHastaA;
    @FXML private TableView<ResultadoEstadisticaDTO> tblActividades;
    @FXML private TableColumn<ResultadoEstadisticaDTO, String> tblSemanaA;
    @FXML private TableColumn<ResultadoEstadisticaDTO, String> tblCantidadA;
    @FXML private ImageView imgGraficoA;

    @FXML private Button btnCerrarSesion;


    private final ServicioEstadisticasCliente servicioEstadisticas = new ServicioEstadisticasCliente();
    private final ServicioImpresion servicioImpresion = new ServicioImpresion();
    private final ServicioGeneradorGrafico servicioGeneradorGrafico = new ServicioGeneradorGrafico();

    @FXML
    public void initialize() {
        tblCategoriaR.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        tblCantidadR.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tblSemanaA.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        tblCantidadA.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        btnCerrarSesion.setOnAction(event -> cerrarSesion(event));
    }
    @FXML
    private void cargarRecursos() {
        LocalDate desde = dteDesdeR.getValue();
        LocalDate hasta = dteHastaR.getValue();

        String errorRango = RangoFechas.validar(desde, hasta);
        if (errorRango != null) {
            mostrarError(errorRango);
            return;
        }
        try {
            List<ResultadoEstadisticaDTO> resultados = servicioEstadisticas.obtenerEstadisticaRecursos(desde, hasta);
            if (resultados.isEmpty()) {
                mostrarInformacion("No hay reservas en el rango de fechas seleccionado.");
                return;
            }
            tblRecursos.setItems(FXCollections.observableArrayList(resultados));
            imgGraficoR.setImage(servicioGeneradorGrafico.generar("Recursos Usados", "Categoría", "Cantidad", resultados));
        } catch (Exception e) {
            mostrarError("No se pudieron cargar las estadísticas de recursos: " + mensajeError(e));
        }
    }

    @FXML
    private void cargarActividades() {
        LocalDate desde = dteDesdeA.getValue();
        LocalDate hasta = dteHastaA.getValue();

        String errorRango = RangoFechas.validar(desde, hasta);
        if (errorRango != null) {
            mostrarError(errorRango);
            return;
        }
        try {
            List<ResultadoEstadisticaDTO> resultados = servicioEstadisticas.obtenerEstadisticaActividades(desde, hasta);
            if (resultados.isEmpty()) {
                mostrarInformacion("No hay reservas en el rango de fechas seleccionado.");
                return;
            }
            tblActividades.setItems(FXCollections.observableArrayList(resultados));
            imgGraficoA.setImage(servicioGeneradorGrafico.generar("Actividades", "Semana", "Cantidad", resultados));
        } catch (Exception e) {
            mostrarError("No se pudieron cargar las estadísticas de actividades: " + mensajeError(e));
        }
    }

    @FXML
    private void imprimirRecursos() {
        List<ResultadoEstadisticaDTO> resultados = new ArrayList<>(tblRecursos.getItems());
        if (resultados.isEmpty()) {
            // avisar al usuario que no hay datos cargados
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No hay datos cargados. Presione 'Cargar' primero.");
            alerta.showAndWait();
            return;
        }
        try{
            servicioImpresion.imprimirEstadisticaRecursos(resultados);
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, "Error al imprimir los recursos: " + e.getMessage());
            alerta.showAndWait();
        }
    }

    @FXML
    private void imprimirActividades() {
        List<ResultadoEstadisticaDTO> resultados = new ArrayList<>(tblActividades.getItems());
        if (resultados.isEmpty()) {
            // avisar al usuario que no hay datos cargados
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No hay datos cargados. Presione 'Cargar' primero.");
            alerta.showAndWait();
            return;
        }
        try{
            servicioImpresion.imprimirEstadisticaActividades(resultados);
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, "Error al imprimir las actividades: " + e.getMessage());
            alerta.showAndWait();
        }
    }

    @FXML
    private void limpiarRecursos() {
        dteDesdeR.setValue(null);
        dteHastaR.setValue(null);
        tblRecursos.getItems().clear();
        imgGraficoR.setImage(null);
    }

    @FXML
    private void limpiarActividades() {
        dteDesdeA.setValue(null);
        dteHastaA.setValue(null);
        tblActividades.getItems().clear();
        imgGraficoA.setImage(null);
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

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.setTitle("Información");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private String mensajeError(Exception excepcion) {
        return excepcion.getMessage() == null || excepcion.getMessage().isBlank()
                ? "Error inesperado."
                : excepcion.getMessage();
    }
}
