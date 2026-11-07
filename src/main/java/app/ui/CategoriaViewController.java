package app.ui;

import app.DTO.CategoriaDTO;
import app.Servicios.ServicioCategoriaCliente;
import app.ui.utilidades.ServicioImpresion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import app.Controllers.SesionActual;

import java.util.List;

public class CategoriaViewController {

    @FXML private TextField txtFieldBusqDesc;
    @FXML private Button btnBuscDesc;
    @FXML private Button btnImpriDesc;

    @FXML private TextField txtFIDCateg;
    @FXML private TextField txtFDesCateg;
    @FXML private Button btnGuardarFunc;
    @FXML private Button btnBorrarFunc;
    @FXML private Button btnLimpiarFunc;

    @FXML private Button btnCerrarSesion;

    @FXML private TableView<CategoriaDTO> tvCategorias;
    @FXML private TableColumn<CategoriaDTO, String> tcIDCateg;
    @FXML private TableColumn<CategoriaDTO, String> tcDesCateg;

    private final ServicioCategoriaCliente servicioCategoria = new ServicioCategoriaCliente();
    private final ServicioImpresion servicioImpresion = new ServicioImpresion();
    private final ObservableList<CategoriaDTO> datosTabla = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tcIDCateg.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcDesCateg.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        tvCategorias.setItems(datosTabla);

        tvCategorias.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtFIDCateg.setText(seleccionado.getId());
                txtFDesCateg.setText(seleccionado.getDescripcion());
            }
        });

        btnGuardarFunc.setOnAction(event -> guardar());
        btnBorrarFunc.setOnAction(event -> borrar());
        btnLimpiarFunc.setOnAction(event -> limpiar());
        btnBuscDesc.setOnAction(event -> buscar());
        btnImpriDesc.setOnAction(event -> imprimir());
        btnCerrarSesion.setOnAction(event -> cerrarSesion(event));
    }

    private void guardar() {
        String id = txtFIDCateg.getText();
        String descripcion = txtFDesCateg.getText();

        try {
            if (id == null || id.trim().isEmpty()) {
                servicioCategoria.agregar(new CategoriaDTO(null, descripcion));
            } else {
                CategoriaDTO actualizada = servicioCategoria.buscarPorId(id);
                actualizada.setDescripcion(descripcion);
                servicioCategoria.modificar(actualizada);
            }
            mostrarExito("Categoría guardada correctamente.");
            limpiar();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void borrar() {
        CategoriaDTO seleccionada = tvCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una categoría de la tabla para poder borrarlo borrar.");
            return;
        }
        try {
            servicioCategoria.eliminar(seleccionada.getId());
            mostrarExito("Categoría eliminada.");
            datosTabla.remove(seleccionada);
            limpiar();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void limpiar() {
        txtFIDCateg.clear();
        txtFDesCateg.clear();
        tvCategorias.getSelectionModel().clearSelection();
    }

    private void buscar() {
        String descripcion = txtFieldBusqDesc.getText();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return;
        }

        try {
            List<CategoriaDTO> resultado = servicioCategoria.buscarPorDescripcion(descripcion);
            for (CategoriaDTO c : resultado) {
                agregarSiNoExiste(c);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void agregarSiNoExiste(CategoriaDTO nueva) {
        for (CategoriaDTO c : datosTabla) {
            if (c.getId().equals(nueva.getId())) {
                return;
            }
        }
        datosTabla.add(nueva);
    }

    private void imprimir() {
        if(datosTabla.isEmpty()) {
            mostrarError("No hay categorías para imprimir.");
            return;
        }
        List<String> encabezados = List.of("ID", "Descripción");
        try {
            servicioImpresion.imprimirCategorias(encabezados, datosTabla);
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
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
}