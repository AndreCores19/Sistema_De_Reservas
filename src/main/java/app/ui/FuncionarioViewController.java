package app.ui;

import app.Controllers.SesionActual;
import app.DTO.FuncionarioDTO;
import app.Servicios.ServicioFuncionarioCliente;
import app.ui.utilidades.ServicioImpresion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;


import java.util.List;

public class FuncionarioViewController {
    @FXML private TextField txtFieldBusqID;
    @FXML private TextField txtFieldBusqNombre;
    @FXML private Button btnBusc;
    @FXML private Button btnImpri;

    @FXML private TextField txtFIDFunc;
    @FXML private TextField txtFNombFunc;
    @FXML private TextField txtFTelfFunc;
    @FXML private Button btnGuardarFunc;
    @FXML private Button btnBorrarFunc;
    @FXML private Button btnLimpiarFunc;

    @FXML private Button btnCerrarSesion;

    @FXML private TableView<FuncionarioDTO> tvFuncionarios;
    @FXML private TableColumn<FuncionarioDTO, String> tcIDFunc;
    @FXML private TableColumn<FuncionarioDTO, String> tcNombFunc;
    @FXML private TableColumn<FuncionarioDTO, String> tcTelFunc;

    private final ServicioFuncionarioCliente servicioFuncionario = new ServicioFuncionarioCliente();
    private final ServicioImpresion servicioImpresion = new ServicioImpresion();
    private final ObservableList<FuncionarioDTO> datosTabla = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tcIDFunc.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcNombFunc.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tcTelFunc.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        tvFuncionarios.setItems(datosTabla);

        tvFuncionarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtFIDFunc.setText(seleccionado.getId());
                txtFNombFunc.setText(seleccionado.getNombre());
                txtFTelfFunc.setText(seleccionado.getTelefono());
            }
        });
        btnGuardarFunc.setOnAction(event -> guardar());
        btnBorrarFunc.setOnAction(event -> borrar());
        btnLimpiarFunc.setOnAction(event -> limpiar());
        btnBusc.setOnAction(event -> buscar());
        btnImpri.setOnAction(event -> {try {
            imprimir();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
        });
        btnCerrarSesion.setOnAction(event -> cerrarSesion(event));
    }

    private void cargarTabla() {
        try {
            List<FuncionarioDTO> lista = servicioFuncionario.listarTodos();
            datosTabla.setAll(lista);
            tvFuncionarios.setItems(datosTabla);
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void guardar() {
        String id = txtFIDFunc.getText();
        String nombre = txtFNombFunc.getText();
        String telefono = txtFTelfFunc.getText();

        try {
            FuncionarioDTO existente = null;
            try {
                existente = servicioFuncionario.buscarPorId(id);
            } catch (Exception ignorado) {
                // No existe, entonces es un alta nueva
            }

            if (existente == null) {
                servicioFuncionario.agregar(new FuncionarioDTO(id, null, null, nombre, telefono));
            } else {
                existente.setNombre(nombre);
                existente.setTelefono(telefono);
                servicioFuncionario.modificar(existente);
            }

            mostrarExito("Funcionario guardado correctamente.");
            limpiar();
            cargarTabla();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void borrar() {
        FuncionarioDTO seleccionado = tvFuncionarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Debe seleccionar un funcionario de la tabla para borrar.");
            return;
        }
        try {
            servicioFuncionario.eliminar(seleccionado.getId());
            mostrarExito("Funcionario eliminado.");
            limpiar();
            cargarTabla();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void limpiar() {
        txtFIDFunc.clear();
        txtFNombFunc.clear();
        txtFTelfFunc.clear();
        tvFuncionarios.getSelectionModel().clearSelection();
    }

    private void buscar() {
        String id = txtFieldBusqID.getText();
        String nombre = txtFieldBusqNombre.getText();

        try {
            if (!id.isEmpty()) {
                FuncionarioDTO encontrado = servicioFuncionario.buscarPorId(id);
                agregarSiNoExiste(encontrado);
            } else if (!nombre.isEmpty()) {
                List<FuncionarioDTO> resultado = servicioFuncionario.buscarPorNombre(nombre);
                for (FuncionarioDTO f : resultado) {
                    agregarSiNoExiste(f);
                }
            } else {
                cargarTabla();
            }
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void agregarSiNoExiste(FuncionarioDTO nuevo) {
        for (FuncionarioDTO f : datosTabla) {
            if (f.getId().equals(nuevo.getId())) {
                return;
            }
        }
        datosTabla.add(nuevo);
    }

    private void imprimir() {
        if (datosTabla.isEmpty()) {
            mostrarError("No hay funcionarios para imprimir.");
            return;
        }
        try {
            servicioImpresion.imprimirFuncionarios(datosTabla);
        } catch (Exception e) {
            mostrarError("No se pudo generar el reporte: " + e.getMessage());
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