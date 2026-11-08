package app.Servicios;

import app.DTO.ReservaDTO;
import app.DatosDePrueba;
import app.ServidorDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static app.DatosDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificacionesIT {

    private final ServicioReservasCliente reservas = new ServicioReservasCliente();
    private ServicioNotificacionesCliente oyente1;
    private ServicioNotificacionesCliente oyente2;
    private final BlockingQueue<ReservaDTO> recibidas1 = new LinkedBlockingQueue<>();
    private final BlockingQueue<ReservaDTO> recibidas2 = new LinkedBlockingQueue<>();

    @BeforeAll
    static void servidor() throws Exception { ServidorDePrueba.iniciar(); }

    @BeforeEach
    void preparar() throws Exception {
        DatosDePrueba.crear();
        oyente1 = new ServicioNotificacionesCliente(FUNCIONARIO_1);
        oyente2 = new ServicioNotificacionesCliente(FUNCIONARIO_2);
        oyente1.conectar(FUNCIONARIO_1, recibidas1::add);
        oyente2.conectar(FUNCIONARIO_2, recibidas2::add);
        Thread.sleep(500);
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (oyente1 != null)
            oyente1.detener();
        if (oyente2 != null)
            oyente2.detener();
        DatosDePrueba.borrar();
    }

    private ReservaDTO nueva(String funcionario, String categoria) {
        return new ReservaDTO(null, "Prueba alerta", LUNES.plusDays(10),
                LocalTime.of(14, 0), LocalTime.of(15, 0),
                List.of(categoria), funcionario, null, new ArrayList<>());
    }

    @Test
    void laReservaNuevaLeLlegaALosDemasPeroNoAlQueLaCreo() throws Exception {
        reservas.reservarEspacio(nueva(FUNCIONARIO_1, CAT_SALAS));

        ReservaDTO recibida = recibidas2.poll(3, TimeUnit.SECONDS);
        assertNotNull(recibida);
        assertEquals("Prueba alerta", recibida.getActividad());
        assertEquals(FUNCIONARIO_1, recibida.getIdFuncionario());

        assertNull(recibidas1.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void unaReservaRechazadaNoNotifica() {
        assertThrows(Exception.class,
                () -> reservas.reservarEspacio(nueva(FUNCIONARIO_1, CAT_SIN_RECURSOS)));
        assertNull(poll(recibidas2));
    }

    private ReservaDTO poll(BlockingQueue<ReservaDTO> cola) {
        try { return cola.poll(1, TimeUnit.SECONDS); }
        catch (InterruptedException e) { return null; }
    }
}
