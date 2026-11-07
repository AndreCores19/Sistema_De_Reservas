package app.Servicios;

import app.DTO.RecursoDTO;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ServicioRecursoCliente extends ClienteBase {

    private static final Type TIPO_LISTA = new TypeToken<List<RecursoDTO>>() {}.getType();

    public List<RecursoDTO> listarTodos() throws Exception {
        return gson.fromJson(enviarPost("/recursos/listar", ""), TIPO_LISTA);
    }

    public List<RecursoDTO> filtrarPorCategoria(String categoriaId) throws Exception {
        String json = enviarPost("/recursos/filtrarPorCategoria", "idCategoria=" + codificar(categoriaId));
        return gson.fromJson(json, TIPO_LISTA);
    }

    public RecursoDTO buscarPorNumActivo(String numActivo) throws Exception {
        String json = enviarPost("/recursos/buscarPorNumActivo", "numActivo=" + codificar(numActivo));
        return gson.fromJson(json, RecursoDTO.class);
    }

    public void agregar(RecursoDTO nuevo) throws Exception {
        enviarPost("/recursos/agregar", desarmar(nuevo));
    }

    public void modificar(RecursoDTO actualizado) throws Exception {
        enviarPost("/recursos/modificar", desarmar(actualizado));
    }

    public void eliminar(String id) throws Exception {
        enviarPost("/recursos/eliminar", "numActivo=" + codificar(id));
    }

    private String desarmar(RecursoDTO recurso) {
        String idCategoria = recurso.getCategoria() == null ? null : recurso.getCategoria().getId();
        return "numActivo=" + codificar(recurso.getNumActivo())
                + "&idCategoria=" + codificar(idCategoria)
                + "&descripcion=" + codificar(recurso.getDescripcion());
    }
}