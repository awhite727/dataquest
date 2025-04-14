package dataquest;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class TDistribution extends Visualization {

    private int df; // degrees of freedom
    private XYSeriesCollection dataset;

    public TDistribution(String title, Color[] colors, int df, String direction, double a) {
        super(title, colors);
        this.df = df;
        XYSeries pointSeries = new XYSeries("Points");
        XYSeries shadedSeries = new XYSeries("Shaded");
        XYSeries upperSeries = new XYSeries("Upper Tail");


        ArrayList<Double> xList = new ArrayList<>();
        for (double x = -4; x <= 4; x += 0.1) {
            xList.add(x);
        }
        xList.add(a);
        xList.add(-a);
        List<Double> yList = StatisticalSummary.calculateTDistributionPDF(df, xList);

        switch (direction){
            case "<":
                shadedSeries.add(-4, 0);
                for (int i=0; i<xList.size(); i++) {
                    double x = xList.get(i);
                    double y = yList.get(i);
                    pointSeries.add(x, y);
                    if (x <= a) {
                        shadedSeries.add(x, y);
                    }
                }
                shadedSeries.add(a, 0);
                break;
            case ">":
                shadedSeries.add(a, 0);
                for (int i=0; i<xList.size(); i++) {
                    double x = xList.get(i);
                    double y = yList.get(i);
                    pointSeries.add(x, y);
                    if (x >= a) {
                        shadedSeries.add(x, y);
                    }
                }
                shadedSeries.add(4, 0);
                break;
            case "inside":
                a = Math.abs(a);
                shadedSeries.add(-a, 0);
                for (int i=0; i<xList.size(); i++) {
                    double x = xList.get(i);
                    double y = yList.get(i);
                    pointSeries.add(x, y);
                    if ((x >= -a && x <= a)) {
                        shadedSeries.add(x, y);
                    }
                }
                shadedSeries.add(a, 0);
                break;
            case "outside":
                a = Math.abs(a);
                shadedSeries.add(-4,0);
                shadedSeries.add(-a, 0);
                for (int i=0; i<xList.size(); i++) {
                    double x = xList.get(i);
                    double y = yList.get(i);
                    pointSeries.add(x,y);
                    if (x <= -a) {
                        shadedSeries.add(x, y);
                    }
                    if (x >= a) {
                        upperSeries.add(x, y);
                    }
                }
                upperSeries.add(a, 0);
                upperSeries.add(4, 0);
                break;
            default:
                System.out.println("Invalid direction: " + direction);
        }

        dataset = new XYSeriesCollection();
        dataset.addSeries(pointSeries);
        dataset.addSeries(shadedSeries);
        if (direction.equals("outside")) {
            dataset.addSeries(upperSeries);
        }
    }

    @Override
    public JPanel createChart() {
        // build an empty scatter plot
        JFreeChart chart = ChartFactory.createScatterPlot(
            "T Distribution (df = " + df + ")", 
            "X",             
            "Y",               
            null,                
            PlotOrientation.VERTICAL,
            false,  // no legend
            true,   // tooltips
            false   // no URLs
        );

        XYPlot plot = chart.getXYPlot();

        // 1) Series 0 = your pointSeries (the t‐curve)
        plot.setDataset(0, new XYSeriesCollection(dataset.getSeries(0)));
        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
        lineRenderer.setSeriesPaint(0, Color.RED);
        plot.setRenderer(0, lineRenderer);

        // 2) Series 1 = your shadedSeries (first tail or inside‐CI)
        plot.setDataset(1, new XYSeriesCollection(dataset.getSeries(1)));
        XYAreaRenderer areaRenderer = new XYAreaRenderer();
        // paint the first (and only) series in this dataset
        areaRenderer.setSeriesPaint(0, new Color(255, 0, 0, 64));
        plot.setRenderer(1, areaRenderer);

        // 3) Series 2 = upper tail (only for "outside")
        if (dataset.getSeriesCount() > 2) {
            plot.setDataset(2, new XYSeriesCollection(dataset.getSeries(2)));
            XYAreaRenderer areaRenderer2 = new XYAreaRenderer();
            areaRenderer2.setSeriesPaint(0, new Color(255, 0, 0, 64));
            plot.setRenderer(2, areaRenderer2);
        }

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        return new ChartPanel(chart);
    }


    @Override
    public JPanel updateChart() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}