package app.Servicios;

import app.DTO.CeldaActividadDTO;

import java.time.LocalDate;

public class ServicioProgramacionCliente extends ClienteBase {

    public CeldaActividadDTO[][] matrizReservas(LocalDate fechaReferencia) throws Exception {
        String cuerpo = "fecha=" + codificar(fechaReferencia == null ? null : fechaReferencia.toString());
        String json = enviarPost("/calendarizacion/actividades", cuerpo);
        return gson.fromJson(json, CeldaActividadDTO[][].class);
    }
}