package app.ui.utilidades;
import app.DTO.*;


import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReporteLogica {

    public String generarReporteParaReservas(List<ReservaDTO> reservas) throws Exception {
        List<String> encabezados = List.of("ID", "Actividad", "Fecha", "Horario", "Recursos", "Estado");
        List<List<String>> filas = new ArrayList<>();
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");
        for (ReservaDTO r : reservas) {
            filas.add(List.of(r.getIdReserva(), r.getActividad(), r.getFecha().toString(), r.getHoraInicio().format(fmtHora) + " - " + r.getHoraFin().format(fmtHora), String.join(", ", r.getIdsRecursosAsignados()), r.getEstado()));
        }
        String rutaSalida = "Data/reporteMisReservas.pdf";
        try {
            GeneradorReportePDFLogica.generar("Mis Reservas", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de reservas: " + e.getMessage(), e);
        }
        return rutaSalida;
    }


    public String generarReporteMatrizRecursos(CategoriaDTO categoria, List<MatrizDTO> filasMatriz) throws Exception {
        List<RecursoDTO> recursosDeCategoria = new app.Servicios.ServicioRecurso().filtrarPorCategoria(categoria.getId());

        List<String> encabezados = new ArrayList<>();
        encabezados.add("Hora");
        for (RecursoDTO recurso : recursosDeCategoria) {
            encabezados.add(recurso.getDescripcion());
        }

        List<List<String>> filas = new ArrayList<>();
        for (MatrizDTO fila : filasMatriz) {
            List<String> filaTexto = new ArrayList<>();
            filaTexto.add(fila.getHora());
            for (RecursoDTO recurso : recursosDeCategoria) {
                filaTexto.add(fila.getEstadoPorRecurso().get(recurso.getNumActivo()));
            }
            filas.add(filaTexto);
        }

        String rutaSalida = "Data/reporteCalendarizacion.pdf";
        try {
            GeneradorReportePDFLogica.generar(
                    "Calendarización - " + categoria.getDescripcion(), encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de calendarización: " + e.getMessage(), e);
        }
        return rutaSalida;
    }

    public String generarReporteParaFuncionarios(List<FuncionarioDTO> funcionarios) throws Exception {
        List<String> encabezados = List.of("ID", "Nombre", "Teléfono");
        List<List<String>> filas = new java.util.ArrayList<>();

        for (FuncionarioDTO f : funcionarios) {
            filas.add(List.of(f.getId(), f.getNombre(), f.getTelefono()));
        }

        String rutaSalida = "Data/reporteFuncionarios.pdf";
        try {
            GeneradorReportePDFLogica.generar("Listado de Funcionarios", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de funcionarios: " + e.getMessage(), e);
        }
        return rutaSalida;
    }

    public String generarReporteEstadisticaDeActividades(List<ResultadoEstadisticaDTO> resultados) throws Exception {
        List<List<String>> filas = new ArrayList<>();
        for (ResultadoEstadisticaDTO a : resultados) {
            filas.add(List.of(a.getEtiqueta(), String.valueOf(a.getCantidad())));
        }
        List<String> encabezados = List.of("Semana", "Cantidad");
        String rutaSalida = "Data/estadisticas_actividades.pdf";

        try {
            GeneradorReportePDFLogica.generar("Estadísticas de Actividades", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de actividades: " + e.getMessage(), e);
        }
        return rutaSalida;
    }

    public String generarReporteEstadisticaDeRecursos(List<ResultadoEstadisticaDTO> resultados) throws Exception {
        List<List<String>> filas = new ArrayList<>();
        for (ResultadoEstadisticaDTO r : resultados) {
            filas.add(List.of(r.getEtiqueta(), String.valueOf(r.getCantidad())));
        }
        List<String> encabezados = List.of("Categoría", "Cantidad");
        String rutaSalida = "Data/estadisticas_recursos.pdf";
        try {
            GeneradorReportePDFLogica.generar("Estadísticas de Recursos", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de recursos: " + e.getMessage(), e);
        }
        return rutaSalida;
    }

    public String generarReporteCalendarioActividades(List<String> encabezados, List<List<String>> filas) throws Exception {
        String rutaSalida = "Data/programacion_actividades.pdf";
        try {
            GeneradorReportePDFLogica.generar("Programación de Actividades", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de actividades: " + e.getMessage(), e);
        }
        return rutaSalida;
    }

    public String generarReporteRecursos(List<RecursoDTO> recursos) throws Exception {

        List<String> encabezados = List.of("ID", "Categoría", "Descripción");
        List<List<String>> filas = new java.util.ArrayList<>();
        for (RecursoDTO r : recursos) {
            filas.add(List.of(r.getNumActivo(), r.getCategoria().getDescripcion(), r.getDescripcion()));
        }
        String rutaSalida = "Data/reporteRecursos.pdf";
        try {
            GeneradorReportePDFLogica.generar("Listado de Recursos", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("Error al generar el reporte: " + e.getMessage());
        }
        return rutaSalida;
    }

    public String generarReporteCategorias(List<String> encabezados, List<CategoriaDTO> resultado) throws Exception {
        List<List<String>> filas = new java.util.ArrayList<>();
        for (CategoriaDTO c : resultado) {
            filas.add(List.of(c.getId(), c.getDescripcion()));
        }

        String rutaSalida = "Data/reporteCategorias.pdf";
        try {
            GeneradorReportePDFLogica.generar("Listado de Categorías", encabezados, filas, rutaSalida);
        } catch (RuntimeException e) {
            throw new Exception("No se pudo generar el reporte de categorias: " + e.getMessage(), e);
        }
        return rutaSalida;
    }
}

