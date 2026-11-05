package app.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos que el frontend muestra cuando recibe una reserva nueva.
 */
public class AlertaReservaDTO {
    private String actividad;
    private String encargado;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<String> recursos;

    public AlertaReservaDTO() {
    }

    public AlertaReservaDTO(String actividad, String encargado, LocalDate fecha,
                            LocalTime horaInicio, LocalTime horaFin,
                            List<String> recursos) {
        this.actividad = actividad;
        this.encargado = encargado;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.recursos = recursos;
    }

    public AlertaReservaDTO(ReservaDTO reserva) {
        this(
                reserva.getActividad(),
                reserva.getIdFuncionario(),
                reserva.getFecha(),
                reserva.getHoraInicio(),
                reserva.getHoraFin(),
                reserva.getIdsRecursosAsignados()
        );
    }

    public String getActividad() {
        return actividad;
    }

    public String getEncargado() {
        return encargado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public List<String> getRecursos() {
        return recursos;
    }
}
