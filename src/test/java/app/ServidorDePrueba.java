package app;

import app.Servidor.ServidorHTTP;

public final class ServidorDePrueba {

    private static boolean iniciado = false;

    private ServidorDePrueba() { }

    public static synchronized void iniciar() throws Exception {
        if (iniciado) return;
        ServidorHTTP http = new ServidorHTTP();
        http.iniciar();   // también levanta las notificaciones
        Runtime.getRuntime().addShutdownHook(new Thread(http::detener));
        iniciado = true;
    }
}