package app.Logica;

import app.DTO.UsuarioDTO;
import app.DTO.AdministradorDTO;
import app.DTO.FuncionarioDTO;
import app.Datos.UsuarioDatos;
import app.Datos.AdministradorDatos;
import app.Datos.FuncionarioDatos;

public class UsuarioLogica {

    private String rutaArchivoAdmins;
    private String rutaArchivoFuncionarios;

    public UsuarioLogica() {
        this("Data/administradores.json", "Data/funcionarios.json");
    }

    public UsuarioLogica(String rutaArchivoAdmins, String rutaArchivoFuncionarios) {
        this.rutaArchivoAdmins = rutaArchivoAdmins;
        this.rutaArchivoFuncionarios = rutaArchivoFuncionarios;
    }

    public UsuarioDTO iniciarSesion(String id, String clave) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("Debe ingresar el id.");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new Exception("Debe ingresar la clave.");
        }

        UsuarioDatos usuarioDatos = new UsuarioDatos();
        usuarioDatos.setRutaArchivoAdmins(rutaArchivoAdmins);
        usuarioDatos.setRutaArchivoFuncionarios(rutaArchivoFuncionarios);
        UsuarioDTO usuario = usuarioDatos.iniciarSesion(id, clave);
        if (usuario == null) {
            throw new Exception("El id o la clave son incorrectos.");
        }
        return usuario;
    }

    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        UsuarioDTO usuario = iniciarSesion(id, claveActual);

        if (claveNueva == null || claveNueva.trim().isEmpty()) {
            throw new Exception("La clave nueva no puede estar vacía.");
        }
        if (claveNueva.equals(claveActual)) {
            throw new Exception("La clave nueva debe ser distinta a la actual.");
        }

        if ("ADMIN".equals(usuario.getRol())) {
            AdministradorDatos adminDatos = new AdministradorDatos();
            adminDatos.setRutaArchivo(rutaArchivoAdmins);
            adminDatos.deserializar();
            boolean encontrado = false;
            for (AdministradorDTO admin : adminDatos.getListado()) {
                if (admin.getId().equals(usuario.getId())) {
                    admin.setClave(claveNueva);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                throw new Exception("No se encontró el administrador para actualizar la clave.");
            }
            adminDatos.serializar();

        } else if ("FUNCIONARIO".equals(usuario.getRol())) {
            FuncionarioDatos funcionarioDatos = new FuncionarioDatos();
            funcionarioDatos.setRutaArchivo(rutaArchivoFuncionarios);
            funcionarioDatos.deserializar();
            boolean encontrado = false;
            for (FuncionarioDTO func : funcionarioDatos.getListado()) {
                if (func.getId().equals(usuario.getId())) {
                    func.setClave(claveNueva);
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                throw new Exception("No se encontró el funcionario para actualizar la clave.");
            }
            funcionarioDatos.serializar();
        } else {
            throw new Exception("Rol desconocido.");
        }
    }
}