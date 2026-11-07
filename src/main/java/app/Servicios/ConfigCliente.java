package app.Servicios;

public class ConfigCliente {
    public static final String URL_SERVIDOR = "http://localhost:8080";

    private ConfigCliente() {
    }

    static String urlServidor() {
        return System.getProperty("sistema.reservas.urlServidor", URL_SERVIDOR);
    }
}
