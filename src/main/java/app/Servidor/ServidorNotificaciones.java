package app.Servidor;

import app.DTO.GsonUtil;
import app.DTO.ReservaDTO;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServidorNotificaciones {
    public static final int PUERTO = 9090;
    private static final List<Conexion> conexiones = new CopyOnWriteArrayList<>();

    public static void iniciar() {
        Thread hilo = new Thread(() -> {
            try (ServerSocket servidor = new ServerSocket(PUERTO)) {
                System.out.println("Servidor de notificaciones en el puerto " + PUERTO);
                while (true) {
                    Socket socket = servidor.accept();
                    new Thread(() -> atender(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    private static void atender(Socket socket) {
        Conexion conexion = null;
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            PrintWriter salida = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String id = entrada.readLine();
            if (id == null) {
                return;
            }
            conexion = new Conexion(salida, id);
            conexiones.add(conexion);

            while (entrada.readLine() != null) { }
        } catch (IOException e) {
            // el cliente se desconectó
        } finally {
            if (conexion != null) {
                conexiones.remove(conexion);
            }
            try {
                socket.close();
            } catch (IOException ignorada) {
            }
        }
    }

    public static void notificarNuevaReserva(ReservaDTO reserva) {
        String mensaje = GsonUtil.GSON.toJson(reserva);

        for (Conexion c : conexiones) {
            if (c.idFuncionario.equals(reserva.getIdFuncionario())) {
                continue;
            }
            synchronized (c) {
                c.salida.println(mensaje);
                if (c.salida.checkError()) {
                    conexiones.remove(c);
                }
            }
        }
    }
}