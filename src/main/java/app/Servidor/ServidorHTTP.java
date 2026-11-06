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
        servidor.createContext("/usuario", new UsuarioHandler());
        servidor.createContext("/categorias", new CategoriaHandler());
        servidor.createContext("/recursos", new RecursoHandler());
        servidor.createContext("/funcionarios", new FuncionarioHandler());
        servidor.createContext("/calendarizacion", new CalendarizacionHandler());
        servidor.createContext("/reserva", new ReservasHandler());
        servidor.createContext("/estadistica", new EstadisticasHandler());
        servidor.createContext("/gemini", new GeminiHandler());

        // executor
        servidor.setExecutor(Executors.newFixedThreadPool(10));

        // arrancar
        servidor.start();

        // mensajes de consola
        System.out.println("Servidor iniciado en el puerto " + PUERTO);
        System.out.println("POST /usuario/login");
    }
}
