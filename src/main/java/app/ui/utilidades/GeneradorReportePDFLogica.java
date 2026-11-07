package app.ui.utilidades;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import java.util.List;

public class GeneradorReportePDFLogica {
    public static void generar(String titulo, List<String> encabezados, List<List<String>> filas, String rutaSalida) throws RuntimeException {
        if (encabezados == null || encabezados.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un encabezado");
        }
        if (filas == null) {
            throw new IllegalArgumentException("La lista de filas no puede ser null");
        }
        for (List<String> fila : filas) {
            if (fila.size() != encabezados.size()) {
                throw new IllegalArgumentException(
                        "Cada fila debe tener " + encabezados.size() + " columnas, pero una tiene " + fila.size());
            }
        }
        try {
            PdfWriter writer = new PdfWriter(rutaSalida);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(titulo).setBold().setFontSize(18));
            Table table = new Table(encabezados.size());
            table.setWidth(UnitValue.createPercentValue(100));
            for (String enc : encabezados) {
                Cell celda = new Cell().add(new Paragraph(enc)).setBackgroundColor(ColorConstants.RED).setFontColor(ColorConstants.WHITE).setBold();
                table.addHeaderCell(celda);
            }
            for (List<String> fila : filas) {
                for (String valor : fila) {
                    table.addCell(new Paragraph(valor));
                }
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF: " + rutaSalida, e);
        }
    }
}
