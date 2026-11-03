package app.Servidor;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServidorHTTP {

    public static final int PUERTO = 8080;
    private HttpServer servidor;

    public void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", PUERTO), 0);

        //registrar rutas
        servidor.createContext("/usuario/login", new LoginHandler());
        servidor.createContext("/usuario/cambiarClave", new CambiarClaveHandler());

        // executor
        servidor.setExecutor(Executors.newFixedThreadPool(10));

        // arrancar
        servidor.start();

        // mensajes de consola
        System.out.println("Servidor iniciado en el puerto " + PUERTO);
        System.out.println("POST /usuario/login");
    }
}
