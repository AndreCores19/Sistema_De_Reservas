package app.Servicios;

import app.DTO.GsonUtil;
import app.DTO.AlertaReservaDTO;
import app.DTO.FuncionarioDTO;
import app.DTO.RecursoDTO;
import app.DTO.ReservaDTO;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ServicioNotificacionesCliente implements NotificacionesCliente {
    private Socket socket;
    private final String idFuncionario;
    private Consumer<AlertaReservaDTO> listener;

    public ServicioNotificacionesCliente(String idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    @Override
    public void registrarListener(Consumer<AlertaReservaDTO> listener) {
        this.listener = listener;
    }

    @Override
    public void iniciar() {
        if (listener == null) {
            throw new IllegalStateException("No se registró el listener de notificaciones.");
        }
        try {
            conectar(idFuncionario, this::pasarANombres);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo conectar al servidor de notificaciones.", e);
        }
    }

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

    private void pasarANombres(ReservaDTO reserva) {
        if (reserva == null || listener == null) {
            return;
        }

        String encargado = reserva.getIdFuncionario();
        try {
            FuncionarioDTO funcionario = new ServicioFuncionarioCliente()
                    .buscarPorId(reserva.getIdFuncionario());
            if (funcionario != null && funcionario.getNombre() != null
                    && !funcionario.getNombre().isBlank()) {
                encargado = funcionario.getNombre();
            }
        } catch (Exception ignorada) {
            // Se conserva el id del funcionario como respaldo uwu
        }

        java.util.List<String> recursos = new java.util.ArrayList<>();
        if (reserva.getIdsRecursosAsignados() != null) {
            for (String idRecurso : reserva.getIdsRecursosAsignados()) {
                String descripcion = idRecurso;
                try {
                    RecursoDTO recurso = new ServicioRecursoCliente()
                            .buscarPorNumActivo(idRecurso);
                    if (recurso != null && recurso.getDescripcion() != null
                            && !recurso.getDescripcion().isBlank()) {
                        descripcion = recurso.getDescripcion();
                    }
                } catch (Exception ignorada) {
                    // Se conserva el id del recurso como respaldo uwu
                }
                recursos.add(descripcion);
            }
        }

        listener.accept(new AlertaReservaDTO(reserva.getActividad(), encargado, reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin(), recursos));
    }


    public void detener() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignorada) {
        }
    }
}