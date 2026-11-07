package app.Servicios;

import app.DTO.MatrizDTO;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class ServicioCalendarizacionRecursosCliente extends ClienteBase {

    private static final Type TIPO_LISTA = new TypeToken<List<MatrizDTO>>() {}.getType();

    public List<MatrizDTO> obtenerMatrizDeRecursos(String idCategoria, LocalDate fecha) throws Exception {
        String cuerpo = "idCategoria=" + codificar(idCategoria)
                + "&fecha=" + codificar(fecha == null ? null : fecha.toString());
        String json = enviarPost("/calendarizacion/recursos", cuerpo);
        return gson.fromJson(json, TIPO_LISTA);
    }
}