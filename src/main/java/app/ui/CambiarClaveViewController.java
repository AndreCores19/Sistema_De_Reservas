package app.ui;

import app.Controllers.SesionActual;
import app.DTO.UsuarioDTO;
import app.Servicios.ServicioUsuarioCliente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;

public class CambiarClaveViewController {

    @FXML private PasswordField pwdActual;
    @FXML private PasswordField pwdNueva;
    @FXML private PasswordField pwdConf;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancel;

    private ServicioUsuarioCliente servicioUsuario = new ServicioUsuarioCliente();

    @FXML
    private void initialize() {
        btnAceptar.setOnAction(event -> intentarCambiar(event));
        btnCancel.setOnAction(event -> volverAlLogin(event));
    }

    private void intentarCambiar(javafx.event.ActionEvent event) {
        UsuarioDTO usuario = SesionActual.getUsuarioActual();
        String claveActual = pwdActual.getText();
        String claveNueva = pwdNueva.getText();
        String confirmacion = pwdConf.getText();

        if (!claveNueva.equals(confirmacion)) {
            mostrarError("La nueva clave y la confirmación no coinciden.");
            return;
        }

        try {
            servicioUsuario.cambiarClave(usuario.getId(), claveActual, claveNueva);
            mostrarExito("Clave actualizada correctamente.");
            volverAlLogin(event);
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Éxito");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void volverAlLogin(javafx.event.ActionEvent event) {
        SesionActual.setUsuarioActual(null);
        try {
            javafx.scene.Parent raiz = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/app/ui/login-view.fxml")
            );
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(raiz);
            stage.getScene().setRoot(raiz);
            stage.setWidth(348);
            stage.setHeight(363);
            stage.centerOnScreen();
        } catch (Exception e) {
            mostrarError(e.getMessage() != null ? e.getMessage() : "No se pudo volver al inicio de sesión.");
        }
    }
}