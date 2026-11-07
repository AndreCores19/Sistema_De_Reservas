package app.Logica;

import app.ui.utilidades.GeneradorReportePDFLogica;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorReportePDFLogicaTest {

    private List<String> encabezadosValidos;
    private List<List<String>> filasValidas;

    @BeforeEach
    void setUp() {
        encabezadosValidos = List.of("Categoría", "Cantidad");
        filasValidas = List.of(List.of("Sala de Juntas", "2"));
    }

    @Test
    void generarConEncabezadoNullDebeLanzarException() {
        assertThrows(IllegalArgumentException.class, () ->
                GeneradorReportePDFLogica.generar("Titulo", null, filasValidas, "Data_Test/test_salida.pdf"));
    }

    @Test
    void generarConEncabezadosVaciosDebeLanzarException() {
        assertThrows(IllegalArgumentException.class, () ->
                GeneradorReportePDFLogica.generar("Titulo", List.of(), filasValidas, "Data_Test/test_salida.pdf"));
    }

    @Test
    void generarConFilasNullDebeLanzarException() {
        assertThrows(IllegalArgumentException.class, () ->
                GeneradorReportePDFLogica.generar("Titulo", encabezadosValidos, null, "Data_Test/test_salida.pdf"));
    }

    @Test
    void generarConFilaMalFormadaDebeLanzarException() {
        List<List<String>> filaMalFormada = List.of(List.of("Solo una columna"));

        assertThrows(IllegalArgumentException.class, () ->
                GeneradorReportePDFLogica.generar("Titulo", encabezadosValidos, filaMalFormada, "Data_Test/test_salida.pdf"));
    }

    @Test
    void generarConDatosValidosDebeCrearElArchivoConContenido(@TempDir Path carpetaTemporal) throws Exception {
        Path rutaSalida = carpetaTemporal.resolve("reporte.pdf");

        GeneradorReportePDFLogica.generar("Reporte de prueba", encabezadosValidos, filasValidas, rutaSalida.toString());

        assertTrue(Files.exists(rutaSalida));
        assertTrue(Files.size(rutaSalida) > 0);
    }

    @Test
    void generarConDatosValidosDebeEscribirElTituloLosEncabezadosYLasFilasEnElPdf(@TempDir Path carpetaTemporal) throws Exception {
        Path rutaSalida = carpetaTemporal.resolve("reporte.pdf");

        GeneradorReportePDFLogica.generar("Reporte de prueba", encabezadosValidos, filasValidas, rutaSalida.toString());

        String texto;
        try (PdfDocument pdf = new PdfDocument(new PdfReader(rutaSalida.toString()))) {
            texto = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
        }

        assertTrue(texto.contains("Reporte de prueba"));
        assertTrue(texto.contains("Categoría"));
        assertTrue(texto.contains("Cantidad"));
        assertTrue(texto.contains("Sala de Juntas"));
        assertTrue(texto.contains("2"));
    }

    @Test
    void generarConVariasFilasDebeEscribirlasTodas(@TempDir Path carpetaTemporal) throws Exception {
        Path rutaSalida = carpetaTemporal.resolve("reporte.pdf");
        List<List<String>> variasFilas = List.of(
                List.of("Sala de Juntas", "2"),
                List.of("Laptop windows 11", "5"),
                List.of("Proyector", "1")
        );

        GeneradorReportePDFLogica.generar("Reporte de prueba", encabezadosValidos, variasFilas, rutaSalida.toString());

        String texto;
        try (PdfDocument pdf = new PdfDocument(new PdfReader(rutaSalida.toString()))) {
            texto = PdfTextExtractor.getTextFromPage(pdf.getPage(1));
        }

        assertTrue(texto.contains("Laptop windows 11"));
        assertTrue(texto.contains("Proyector"));
    }

    @Test
    void generarConFilasVaciasNoDebeLanzarExcepcionYDebeCrearElPdfSoloConEncabezados(@TempDir Path carpetaTemporal) throws Exception {
        Path rutaSalida = carpetaTemporal.resolve("reporte.pdf");

        assertDoesNotThrow(() ->
                GeneradorReportePDFLogica.generar("Reporte vacío", encabezadosValidos, List.of(), rutaSalida.toString()));

        assertTrue(Files.exists(rutaSalida));
    }

    @Test
    void generarConRutaDeSalidaInvalidaDebeLanzarRuntimeException(@TempDir Path carpetaTemporal) {
        // La carpeta "carpetaQueNoExiste" no se crea a propósito: PdfWriter no puede
        // abrir el archivo y esa falla debe llegar envuelta en RuntimeException.
        String rutaInvalida = carpetaTemporal.resolve("carpetaQueNoExiste").resolve("reporte.pdf").toString();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                GeneradorReportePDFLogica.generar("Titulo", encabezadosValidos, filasValidas, rutaInvalida));

        assertTrue(ex.getMessage().contains("No se pudo generar el PDF"));
        assertNotNull(ex.getCause());
    }
}