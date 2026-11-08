package app.Servicios;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigCliente {

    public static final String HOST_SERVIDOR = leerHost();
    public static final String URL_SERVIDOR = "http://" + HOST_SERVIDOR + ":8080";
    public static final int PUERTO_NOTIFICACIONES = 9090;

    private static String leerHost() {
        Path archivo = Path.of("servidor.properties");

        if (Files.exists(archivo)) {
            Properties propiedades = new Properties();
            try (InputStream entrada = Files.newInputStream(archivo)) {
                propiedades.load(entrada);
                String host = propiedades.getProperty("host");
                if (host != null && !host.isBlank()) {
                    return host.trim();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return System.getenv().getOrDefault("SERVIDOR_HOST", "localhost");
    }
}