package app.ui.utilidades;
import app.DTO.ResultadoEstadisticaDTO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class GeneradorGraficoLogica {
    public static Image generar(String titulo, String ejex, String ejey, List<ResultadoEstadisticaDTO> datos) {
        if (datos == null || datos.isEmpty()) {
            throw new IllegalArgumentException("No hay datos para graficar");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ResultadoEstadisticaDTO r : datos) {
            if (r.getEtiqueta() == null) {
                throw new IllegalArgumentException("Un resultado no tiene etiqueta asignada");
            }
            dataset.addValue(r.getCantidad(), "Cantidad", r.getEtiqueta());
        }

        JFreeChart chart = ChartFactory.createBarChart(titulo, ejex, ejey, dataset);
        BufferedImage bufferedImage = chart.createBufferedImage(600, 400);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.07);
        renderer.setSeriesPaint(0, new Color(0, 121, 107));
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
