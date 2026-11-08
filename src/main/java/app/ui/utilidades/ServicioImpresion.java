package app.ui.utilidades;

import app.DTO.*;
import app.Servicios.ServicioPDF;

import java.util.List;

public class ServicioImpresion {
    private final ReporteLogica reporteLogica = new ReporteLogica();
    private final ServicioPDF servicioPDF = new ServicioPDF();

    public void imprimirReservas(List<ReservaDTO> reservas) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteParaReservas(reservas));

    }
    public void  imprimirFuncionarios(List<FuncionarioDTO> funcionarios) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteParaFuncionarios(funcionarios));
    }

    public void imprimirMatrizRecursos(CategoriaDTO categoria, List<RecursoDTO> recursos, List<MatrizDTO> filasMatriz) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteMatrizRecursos(categoria, recursos, filasMatriz));
    }

    public void imprimirEstadisticaActividades(List<ResultadoEstadisticaDTO> resultados) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteEstadisticaDeActividades(resultados));
    }

    public void imprimirEstadisticaRecursos(List<ResultadoEstadisticaDTO> resultados) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteEstadisticaDeRecursos(resultados));
    }

    public void imprimirCalendario(List<String> encabezados, List<List<String>> filas) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteCalendarioActividades(encabezados, filas));
    }

    public void imprimirRecursos(List<RecursoDTO> recursos) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteRecursos(recursos));
    }

    public void imprimirCategorias(List<String> encabezados, List<CategoriaDTO> resultado) throws Exception {
        servicioPDF.abrirReporte(reporteLogica.generarReporteCategorias(encabezados, resultado));
    }
}