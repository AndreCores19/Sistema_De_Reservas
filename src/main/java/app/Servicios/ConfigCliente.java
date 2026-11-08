package app.Servicios;

public class ConfigCliente {
    public static final String HOST_SERVIDOR = System.getenv().getOrDefault("SERVIDOR_HOST", "localhost");
    public static final String URL_SERVIDOR = "http://" + HOST_SERVIDOR + ":8080";
    public static final int PUERTO_NOTIFICACIONES = 9090;
}
