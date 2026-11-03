package app.Logica;

import app.DTO.RecursoDTO;
import app.DTO.ReservaDTO;
import app.DTO.ResultadoDeAsignacionDTO;
import app.Datos.ReservaDatos;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LogicaReservas {

    private static final Object CANDADO_RESERVAS = new Object();
    private static final int LARGO_MAXIMO_ACTIVIDAD = 100;

    private final ReservaDatos reservaDatos;
    private final CategoriaLogica categoriaLogica;

    public LogicaReservas() {
        this.reservaDatos = new ReservaDatos();
        this.categoriaLogica = new CategoriaLogica();
    }

    @Deprecated
    public LogicaReservas(String rutaReservas) {
        this();
    }

    public List<ReservaDTO> listarTodas() {
        try {
            return reservaDatos.listarTodas();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar las reservas.", e);
        }
    }

    public List<ReservaDTO> listarPorRango(LocalDate desde, LocalDate hasta) {
        try {
            return reservaDatos.listarActivasPorRango(desde, hasta);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar las reservas del rango.", e);
        }
    }

    public List<ReservaDTO> listarReservasPorFuncionario(String idFuncionario) {
        try {
            return reservaDatos.listarPorFuncionario(idFuncionario);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron consultar las reservas del funcionario.", e);
        }
    }

    public boolean tieneReservasActivas(String idFuncionario) {
        try {
            return reservaDatos.tieneActivasPorFuncionario(idFuncionario);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudieron revisar las reservas del funcionario.", e);
        }
    }

    public boolean tieneReservasActivasPorRecurso(String numActivo) throws Exception {
        return reservaDatos.tieneActivasPorRecurso(numActivo);
    }


    public void agregarReserva(ReservaDTO nueva) throws Exception {
        validarReserva(nueva);

        synchronized (CANDADO_RESERVAS) {
            ResultadoDeAsignacionDTO resultado = estaDisponible(nueva);
            if (!resultado.isExitosa()) {
                throw new Exception("No hay recursos disponibles en ese horario para: "
                        + nombresDeCategorias(resultado.getIdsCategoriasNoDisponibles()));
            }

            nueva.setIdsRecursosAsignados(resultado.getIdsRecursosAsignados());
            nueva.setIdReserva(generarSiguienteId());
            nueva.setEstado("ACTIVA");
            reservaDatos.insertar(nueva);
        }
    }

    public void cancelarReserva(String idReserva) throws Exception {
        ReservaDTO reserva = reservaDatos.buscarPorId(idReserva);
        if (reserva == null) {
            throw new Exception("No se encontró la reserva con el ID proporcionado.");
        }
        if ("CANCELADA".equals(reserva.getEstado())) {
            throw new Exception("La reserva ya está cancelada.");
        }
        if (!esFechaFutura(reserva.getFecha(), reserva.getHoraInicio())) {
            throw new Exception("No se puede cancelar una reserva pasada.");
        }
        reservaDatos.actualizarEstado(idReserva, "CANCELADA");
    }

    private void validarReserva(ReservaDTO reserva) throws Exception {
        if (reserva.getActividad() == null || reserva.getActividad().trim().isEmpty()) {
            throw new Exception("Debe ingresar el nombre de la actividad.");
        }
        if (reserva.getActividad().length() > LARGO_MAXIMO_ACTIVIDAD) {
            throw new Exception("La actividad no puede tener más de " + LARGO_MAXIMO_ACTIVIDAD + " caracteres.");
        }
        if (reserva.getFecha() == null || reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new Exception("Debe indicar la fecha y las horas de la reserva.");
        }
        if (!reserva.getHoraFin().isAfter(reserva.getHoraInicio())) {
            throw new Exception("La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (reserva.getIdsCategoriaSolicitada() == null || reserva.getIdsCategoriaSolicitada().isEmpty()) {
            throw new Exception("Debe seleccionar al menos una categoría.");
        }
        if (reserva.getIdFuncionario() == null || reserva.getIdFuncionario().trim().isEmpty()) {
            throw new Exception("La reserva debe tener un funcionario.");
        }
        if (!esFechaFutura(reserva.getFecha(), reserva.getHoraInicio())) {
            throw new Exception("La fecha y hora de la reserva deben ser futuras.");
        }
    }

    private ResultadoDeAsignacionDTO estaDisponible(ReservaDTO reserva) throws Exception {
        List<ReservaDTO> reservasDelDia = reservaDatos.listarActivasPorRango(reserva.getFecha(), reserva.getFecha());
        RecursoLogica recursoLogica = new RecursoLogica();

        List<String> idsCategoriasNoDisponibles = new ArrayList<>();
        List<String> idsRecursosAsignados = new ArrayList<>();

        for (String idCategoria : reserva.getIdsCategoriaSolicitada()) {
            categoriaLogica.buscarPorId(idCategoria); // lanza excepción si la categoría no existe

            String recursoLibre = null;
            for (RecursoDTO recurso : recursoLogica.filtrarPorCategoria(idCategoria)) {
                if (!estaOcupado(recurso.getNumActivo(), reserva, reservasDelDia)) {
                    recursoLibre = recurso.getNumActivo();
                    break;
                }
            }

            if (recursoLibre != null) {
                idsRecursosAsignados.add(recursoLibre);
            } else {
                idsCategoriasNoDisponibles.add(idCategoria);
            }
        }
        return new ResultadoDeAsignacionDTO(idsCategoriasNoDisponibles.isEmpty(),
                idsCategoriasNoDisponibles, idsRecursosAsignados);
    }

    private boolean estaOcupado(String numActivo, ReservaDTO nueva, List<ReservaDTO> reservasDelDia) {
        for (ReservaDTO existente : reservasDelDia) {
            if (existente.getIdsRecursosAsignados().contains(numActivo)
                    && hayConflictoHoras(nueva.getHoraInicio(), nueva.getHoraFin(),
                    existente.getHoraInicio(), existente.getHoraFin())) {
                return true;
            }
        }
        return false;
    }

    private String nombresDeCategorias(List<String> idsCategorias) {
        List<String> nombres = new ArrayList<>();
        for (String id : idsCategorias) {
            try {
                nombres.add(categoriaLogica.buscarPorId(id).getDescripcion());
            } catch (Exception e) {
                nombres.add(id);
            }
        }
        return String.join(", ", nombres);
    }

    private String generarSiguienteId() throws SQLException {
        String ultimoId = reservaDatos.obtenerUltimoId();
        int numero = 0;
        if (ultimoId != null && ultimoId.startsWith("RES-")) {
            numero = Integer.parseInt(ultimoId.substring(4));
        }
        return String.format("RES-%06d", numero + 1);
    }

    public boolean hayConflictoHoras(LocalTime horaInicioA, LocalTime horaFinA, LocalTime horaInicioB, LocalTime horaFinB) {
        return horaInicioA.isBefore(horaFinB) && horaInicioB.isBefore(horaFinA);
    }

    private boolean esFechaFutura(LocalDate fecha, LocalTime horaInicio) {
        return fecha.isAfter(LocalDate.now())
                || (fecha.equals(LocalDate.now()) && horaInicio.isAfter(LocalTime.now()));
    }
}