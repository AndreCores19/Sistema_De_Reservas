package app.ui;

import java.time.LocalDate;

public final class RangoFechas {
    private RangoFechas() {
    }

    public static String validar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            return "Debe seleccionar las fechas inicial y final.";
        }
        if (desde.isAfter(hasta)) {
            return "La fecha inicial seleccionada no puede ser posterior a la fecha final seleccionada.";
        }
        return null;
    }
}
