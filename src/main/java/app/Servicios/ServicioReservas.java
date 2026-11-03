package app.Servicios;

import app.DTO.ReservaDTO;
import app.Logica.LogicaReservas;
import com.google.gson.Gson;

import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ServicioReservas {
    private final LogicaReservas logicaReservas = new LogicaReservas();

    public void reservarEspacio(ReservaDTO reservaDTO) throws Exception {
        logicaReservas.agregarReserva(reservaDTO);
    }
    public List<ReservaDTO> listarReservas(ReservaDTO reservaDTO) throws Exception {
        return logicaReservas.listarTodas();
    }

    public List<ReservaDTO> listarReservasPorFuncionario(String idFuncionario) throws Exception {
        return logicaReservas.listarReservasPorFuncionario(idFuncionario);
    }

    public void cancelarReserva(String idReserva) throws Exception {
        logicaReservas.cancelarReserva(idReserva);
    }

}
