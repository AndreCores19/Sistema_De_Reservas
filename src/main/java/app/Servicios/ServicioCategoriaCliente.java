package app.Servicios;

import app.DTO.CategoriaDTO;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ServicioCategoriaCliente extends ClienteBase {

    private static final Type TIPO_LISTA = new TypeToken<List<CategoriaDTO>>() {}.getType();

    public List<CategoriaDTO> listarTodas() throws Exception {
        String json = enviarPost("/categorias/listar", "");
        return gson.fromJson(json, TIPO_LISTA);
    }

    public CategoriaDTO buscarPorId(String id) throws Exception {
        String json = enviarPost("/categorias/buscarPorId", "id=" + codificar(id));
        return gson.fromJson(json, CategoriaDTO.class);
    }

    public List<CategoriaDTO> buscarPorDescripcion(String texto) throws Exception {
        String json = enviarPost("/categorias/buscarPorDescripcion", "texto=" + codificar(texto));
        return gson.fromJson(json, TIPO_LISTA);
    }

    public void agregar(CategoriaDTO nueva) throws Exception {
        String json = enviarPost("/categorias/agregar", "descripcion=" + codificar(nueva.getDescripcion()));
        CategoriaDTO creada = gson.fromJson(json, CategoriaDTO.class);
        nueva.setId(creada.getId());
    }

    public void modificar(CategoriaDTO actualizada) throws Exception {
        String cuerpo = "id=" + codificar(actualizada.getId())
                + "&descripcion=" + codificar(actualizada.getDescripcion());
        enviarPost("/categorias/modificar", cuerpo);
    }

    public void eliminar(String id) throws Exception {
        enviarPost("/categorias/eliminar", "id=" + codificar(id));
    }
}