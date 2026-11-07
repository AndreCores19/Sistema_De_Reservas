package app.Servicios;

import app.DTO.ExtraccionIADTO;

public class ServicioGeminiCliente extends ClienteBase {

    public ExtraccionIADTO extraerDatosDeLaIA(String frase) throws Exception {
        String json = enviarPost("/gemini/extraer", "frase=" + codificar(frase));
        return gson.fromJson(json, ExtraccionIADTO.class);
    }
}
