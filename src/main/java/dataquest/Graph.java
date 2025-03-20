package dataquest;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Graph implements Serializable {
    //NOTE: I think we need chart here to make sure the right chart info 
    //is tied to the right graph info, but can remove all chart related and leave in 
    //layout if another way is found
    transient JFreeChart chart; 
    String title;
    String xLabel;
    String yLabel;
    Color[] colorPalette;
    XYSeriesCollection data;
    Color[] graphPalette;

    Graph(){
        colorPalette = new Color[]{
            new Color(0x264653), new Color(0x2A9D8F), new Color(0xE9C46A),
            new Color(0xF4A261), new Color(0xE76F51), new Color(0x023047),
            new Color(0x219EBC), new Color(0xFFB703), new Color(0xFB8500)
        };
        System.out.println(colorPalette[0]);

        title = "Wavy-Graph";
        xLabel = "X";
        yLabel = "Y";
        data = new XYSeriesCollection();
        chart = createEmptyChart();
        graphPalette = new Color[]{(Color)chart.getPlot().getBackgroundPaint(),
            (Color)chart.getPlot().getOutlinePaint()};
        
        chart.addChangeListener(new ChartChangeListener() {
            @Override
            public void chartChanged(ChartChangeEvent event) {
                if (!chart.getTitle().getText().equals(title)) {
                    System.out.println("Updated: " + chart.getTitle().getText());
                    setTitle(chart.getTitle().getText());  
                }
                if (!((Color)chart.getBackgroundPaint()).equals(graphPalette[0])
                    ||!((Color)chart.getBorderPaint()).equals(graphPalette[1])) {
                        System.out.println("Updated: graph paint");
                        setGraphPalette(new Color[]{
                            (Color)chart.getBackgroundPaint(),
                            (Color)chart.getBorderPaint()
                        });
                }
            }
        });
    }

    public JFreeChart getChart(){
        return chart;
    }
    private JFreeChart createEmptyChart() {
        chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, data);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    }

    public JFreeChart updateChart(){
        System.out.println("updateChart reached");
        chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, data);
        applyColorPalette();
        
        //TODO: replace with the saved background elements
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        return chart;       
    }

    public void setColorPalette(Color[] colorPalette) {
        this.colorPalette = colorPalette;
    }
    
    public void setTitle(String title){
        this.title = title;
    }

    //Sets the colors for background and axis
    public void setGraphPalette(Color[] colors) {
        graphPalette[0] = colors[0];
        graphPalette[1] = colors[1];
    }

    public void applyColorPalette() {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < plot.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colorPalette[i % colorPalette.length]);
            renderer.setSeriesShapesVisible(i, true);
        }
        plot.setRenderer(renderer);
    }
    
    //NOTE: Not sure what this does, likely has errors as it looks like 
    //even columns were saved to one graph and odd to the other?
    //Just assumes that the it should take the evens
    public void updateCharts(DefaultTableModel tableModel) {
        XYSeriesCollection dataset1 = new XYSeriesCollection();
        //XYSeriesCollection dataset2 = new XYSeriesCollection();
        
        for (int col = 0; col < tableModel.getColumnCount(); col++) {
            XYSeries series = new XYSeries("Series " + (col + 1));
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Object value = tableModel.getValueAt(row, col);
                if (value instanceof Number) {
                    series.add(row, ((Number) value).doubleValue());
                }
            }
            if (!series.isEmpty()) {
                if (col % 2 == 0) {
                    dataset1.addSeries(series);
                } /* else {
                    dataset2.addSeries(series);
                } */
            }
        }
        try {
            chart.getXYPlot().setDataset(dataset1);   
            applyColorPalette();

        } catch (NullPointerException e) {
            System.out.println("ERROR: No dataset to fill in Graph");
        }
    }
    
}
