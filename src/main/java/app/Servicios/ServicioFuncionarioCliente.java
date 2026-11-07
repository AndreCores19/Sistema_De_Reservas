package app.Servicios;

import app.DTO.FuncionarioDTO;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ServicioFuncionarioCliente extends ClienteBase {

    private static final Type TIPO_LISTA = new TypeToken<List<FuncionarioDTO>>() {}.getType();

    public List<FuncionarioDTO> listarTodos() throws Exception {
        return gson.fromJson(enviarPost("/funcionarios/listar", ""), TIPO_LISTA);
    }

    public FuncionarioDTO buscarPorId(String id) throws Exception {
        String json = enviarPost("/funcionarios/buscarPorId", "id=" + codificar(id));
        return gson.fromJson(json, FuncionarioDTO.class);
    }

    public List<FuncionarioDTO> buscarPorNombre(String texto) throws Exception {
        String json = enviarPost("/funcionarios/buscarPorNombre", "texto=" + codificar(texto));
        return gson.fromJson(json, TIPO_LISTA);
    }

    public void agregar(FuncionarioDTO nuevo) throws Exception {
        enviarPost("/funcionarios/agregar", desarmar(nuevo));
    }

    public void modificar(FuncionarioDTO actualizado) throws Exception {
        enviarPost("/funcionarios/modificar", desarmar(actualizado));
    }

    public void eliminar(String id) throws Exception {
        enviarPost("/funcionarios/eliminar", "id=" + codificar(id));
    }

    private String desarmar(FuncionarioDTO funcionario) {
        return "id=" + codificar(funcionario.getId())
                + "&nombre=" + codificar(funcionario.getNombre())
                + "&telefono=" + codificar(funcionario.getTelefono());
    }
}