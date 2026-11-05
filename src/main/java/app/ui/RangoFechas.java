package app.ui;

import java.time.LocalDate;

public final class RangoFechas {
    private RangoFechas() {
    }

    public static String validar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            return "Debe seleccionar las fechas desde y hasta.";
        }
        if (desde.isAfter(hasta)) {
            return "La fecha desde no puede ser posterior a la fecha hasta.";
        }
        return null;
    }
}
