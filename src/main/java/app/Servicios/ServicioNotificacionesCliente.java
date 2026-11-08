package app.Servicios;

import app.DTO.GsonUtil;
import app.DTO.ReservaDTO;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ServicioNotificacionesCliente {
    private Socket socket;

    public void conectar(String idFuncionario, Consumer<ReservaDTO> alRecibir) throws IOException {
        socket = new Socket(ConfigCliente.HOST_SERVIDOR, ConfigCliente.PUERTO_NOTIFICACIONES);

        PrintWriter salida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        salida.println(idFuncionario);

        Thread hilo = new Thread(() -> escuchar(alRecibir), "Hilo-Notificaciones");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void escuchar(Consumer<ReservaDTO> alRecibir) {
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            while ((linea = entrada.readLine()) != null) {
                ReservaDTO reserva = GsonUtil.GSON.fromJson(linea, ReservaDTO.class);
                alRecibir.accept(reserva);
            }
        } catch (IOException e) {
            // el servidor se desconectó
        }
    }

    public void desconectar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignorada) {
        }
    }
}