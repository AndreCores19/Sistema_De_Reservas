package app.Servicios;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import app.DTO.ResultadoEstadisticaDTO;
import java.time.LocalDate;


public class ServicioEstadisticasCliente extends ClienteBase {

    public List<ResultadoEstadisticaDTO> obtenerEstadisticaRecursos(LocalDate desde, LocalDate hasta) throws Exception {
        String cuerpo = "desde=" + codificar(desde.toString()) + "&hasta=" + codificar(hasta.toString());
        String json = enviarPost("/estadistica/recursos", cuerpo);

        Type tipoLista = new TypeToken<List<ResultadoEstadisticaDTO>>() {}.getType();
        return gson.fromJson(json, tipoLista);
    }

    public List<ResultadoEstadisticaDTO> obtenerEstadisticaActividades(LocalDate desde, LocalDate hasta) throws Exception {
        String cuerpo = "desde=" + codificar(desde.toString()) + "&hasta=" + codificar(hasta.toString());
        String json = enviarPost("/estadistica/actividades", cuerpo);

        Type tipoLista = new TypeToken<List<ResultadoEstadisticaDTO>>() {}.getType();
        return gson.fromJson(json, tipoLista);
    }
}
