package app.Servicios;

import app.DTO.ReservaDTO;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class ServicioReservasCliente extends ClienteBase {
    public void reservarEspacio(ReservaDTO reserva) throws Exception {
        enviarPostJson("/reserva/crear", reserva);
    }

    public List<ReservaDTO> listarReservasPorFuncionario(String idFuncionario) throws Exception {
        String json = enviarPost("/reserva/listarPorFuncionario",
                "idFuncionario=" + codificar(idFuncionario));

        Type tipoLista = new TypeToken<List<ReservaDTO>>() {}.getType();
        return gson.fromJson(json, tipoLista);
    }

    public void cancelarReserva(String idReserva) throws Exception {
        enviarPost("/reserva/cancelar", "idReserva=" + codificar(idReserva));
    }
}