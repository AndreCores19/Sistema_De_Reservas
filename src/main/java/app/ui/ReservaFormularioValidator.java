package app.ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ReservaFormularioValidator {
    private ReservaFormularioValidator() {
    }

    public static String validar(String actividad, LocalDate fecha, LocalTime horaInicio,
                                 LocalTime horaFin, List<String> categorias,
                                 LocalDate hoy, LocalTime ahora) {
        if (actividad == null || actividad.isBlank()) {
            return "La actividad no puede estar vacía.";
        }
        if (fecha == null) {
            return "Debe seleccionar una fecha.";
        }
        if (horaInicio == null) {
            return "Debe seleccionar una hora de inicio.";
        }
        if (horaFin == null) {
            return "Debe seleccionar una hora de fin.";
        }
        if (categorias == null || categorias.isEmpty()) {
            return "Debe seleccionar al menos una categoría.";
        }
        if (!horaFin.isAfter(horaInicio)) {
            return horaFin.equals(horaInicio)
                    ? "La hora de fin no puede ser igual a la hora de inicio."
                    : "La hora de fin no puede ser anterior a la hora de inicio.";
        }
        if (fecha.isBefore(hoy)) {
            return "La fecha de la reserva no puede ser anterior a la fecha actual.";
        }
        if (fecha.equals(hoy) && horaInicio.isBefore(ahora)) {
            return "La hora de inicio/fin no puede ser anterior a la hora actual.";
        }
        if (fecha.equals(hoy) && horaFin.isBefore(ahora)) {
            return "La hora de fin no puede ser anterior a la hora actual.";
        }
        return null;
    }
}
