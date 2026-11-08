package app.ui;

import app.Controllers.SesionActual;
import app.DTO.CategoriaDTO;
import app.DTO.RecursoDTO;
import app.Servicios.ServicioCategoriaCliente;
import app.Servicios.ServicioRecursoCliente;
import app.ui.utilidades.ServicioImpresion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

public class RecursoViewController {

    @FXML private ComboBox<CategoriaDTO> CombBRecur;
    @FXML private TextField txtFieldDescRecur;
    @FXML private Button btnBuscRecur;
    @FXML private Button btnImpriRecur;

    @FXML private TextField txtFIDRecur;
    @FXML private ComboBox<CategoriaDTO> CombRecur;
    @FXML private TextField txtFRecur;
    @FXML private Button btnGuardarFunc;
    @FXML private Button btnBorrarFunc;
    @FXML private Button btnLimpiarFunc;

    @FXML private Button btnCerrarSesion;

    @FXML private TableView<RecursoDTO> tvRecursos;
    @FXML private TableColumn<RecursoDTO, String> tcIDRecur;
    @FXML private TableColumn<RecursoDTO, String> tccategRecur;
    @FXML private TableColumn<RecursoDTO, String> tcDesRecur;

    private final ServicioRecursoCliente servicioRecurso = new ServicioRecursoCliente();
    private final ServicioCategoriaCliente servicioCategoria = new ServicioCategoriaCliente();
    private final ServicioImpresion servicioImpresion = new ServicioImpresion();
    private final ObservableList<RecursoDTO> datosTabla = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tcIDRecur.setCellValueFactory(new PropertyValueFactory<>("numActivo"));
        tccategRecur.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoria().getDescripcion())
        );
        tcDesRecur.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        tvRecursos.setItems(datosTabla);

        configurarComboBox(CombBRecur);
        configurarComboBox(CombRecur);
        cargarCategoriasEnCombos();

        tvRecursos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtFIDRecur.setText(seleccionado.getNumActivo());
                txtFRecur.setText(seleccionado.getDescripcion());
                CombRecur.setValue(seleccionado.getCategoria());
            }
        });

        btnGuardarFunc.setOnAction(event -> guardar());
        btnBorrarFunc.setOnAction(event -> borrar());
        btnLimpiarFunc.setOnAction(event -> limpiar());
        btnBuscRecur.setOnAction(event -> buscar());
        btnImpriRecur.setOnAction(event -> imprimir());
        btnCerrarSesion.setOnAction(event -> cerrarSesion(event));
    }

    private void configurarComboBox(ComboBox<CategoriaDTO> combo) {
        combo.setConverter(new StringConverter<CategoriaDTO>() {
            @Override
            public String toString(CategoriaDTO categoria) {
                return categoria == null ? "" : categoria.getDescripcion();
            }
            @Override
            public CategoriaDTO fromString(String string) {
                return null; // no hace falta, el usuario no escribe texto libre acá
            }
        });
    }

    private void cargarCategoriasEnCombos() {
        try {
            List<CategoriaDTO> categorias = servicioCategoria.listarTodas();
            ObservableList<CategoriaDTO> observable = FXCollections.observableArrayList(categorias);
            CombBRecur.setItems(observable);
            CombRecur.setItems(observable);
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void guardar() {
        String id = txtFIDRecur.getText();
        String descripcion = txtFRecur.getText();
        CategoriaDTO categoriaElegida = CombRecur.getValue();

        try {
            RecursoDTO existente = null;
            try {
                existente = servicioRecurso.buscarPorNumActivo(id);
            } catch (Exception ignorado) {
                // Ignorar excepción si el recurso no existe
            }

            if (existente == null) {
                servicioRecurso.agregar(new RecursoDTO(id, categoriaElegida, descripcion));
            } else {
                existente.setDescripcion(descripcion);
                existente.setCategoria(categoriaElegida);
                servicioRecurso.modificar(existente);
            }

            mostrarExito("Recurso guardado correctamente.");
            limpiar();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void borrar() {
        RecursoDTO seleccionado = tvRecursos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un recurso de la tabla para borrar.");
            return;
        }
        try {
            servicioRecurso.eliminar(seleccionado.getNumActivo());
            mostrarExito("Recurso eliminado.");
            datosTabla.remove(seleccionado);
            limpiar();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void limpiar() {
        txtFIDRecur.clear();
        txtFRecur.clear();
        CombRecur.setValue(null);
        tvRecursos.getSelectionModel().clearSelection();
    }

    private void buscar() {
        CategoriaDTO categoriaFiltro = CombBRecur.getValue();
        String descripcion = txtFieldDescRecur.getText();

        try {
            List<RecursoDTO> resultado;
            if (categoriaFiltro != null) {
                resultado = servicioRecurso.filtrarPorCategoria(categoriaFiltro.getId());
            } else {
                resultado = servicioRecurso.listarTodos();
            }

            for (RecursoDTO r : resultado) {
                if (descripcion == null || descripcion.trim().isEmpty()
                        || r.getDescripcion().toLowerCase().contains(descripcion.toLowerCase())) {
                    agregarSiNoExiste(r);
                }
            }
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void agregarSiNoExiste(RecursoDTO nuevo) {
        for (RecursoDTO r : datosTabla) {
            if (r.getNumActivo().equals(nuevo.getNumActivo())) {
                return;
            }
        }
        datosTabla.add(nuevo);
    }

    private void imprimir() {
        if(datosTabla.isEmpty()) {
            mostrarError("No hay recursos para imprimir.");
            return;
        }
        try {
            servicioImpresion.imprimirRecursos(datosTabla);
        } catch (Exception e) {
            mostrarError("Error al imprimir los recursos: " + e.getMessage());
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