package dataquest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Graph extends Visualization implements Serializable {
    //NOTE: I think we need chart here to make sure the right chart info 
    //is tied to the right graph info, but can remove all chart related and leave in 
    //layout if another way is found
    String xLabel;
    String yLabel;
    XYSeriesCollection data;

    Field categoryField;

    final BasicStroke regressionLine = new BasicStroke(2.5f);
    final Shape[] pointShapes = {
        new Ellipse2D.Double(-4, -4, 4, 4),
        new Rectangle.Double(-4, -4, 4, 4),
        new Polygon(new int[]{0, -4, 4}, new int[]{-4, 4, 4}, 3),       // triangle
        new Polygon(new int[]{0, -4, 0, 4}, new int[]{-4, 0, 4, 0}, 4) // diamond
    };


    public Graph(String title, Color[] colors, Field xField, Field yField, Field categoryField){
        super(title, colors);
        xLabel = xField.getName();
        yLabel = yField.getName();
        data = new XYSeriesCollection();
        Field[] fields = {xField, yField};
        if (categoryField == null) {
            ArrayList<ArrayList<Double>> values = Dataset.matchFields(fields);
            XYSeries series = new XYSeries("Data");
            for (int i=0; i<values.get(0).size(); i++) {
                series.add(values.get(0).get(i), values.get(1).get(i)); // adds matching x and y values
            }
            data.addSeries(series);
        }
        // can't directly use matchFields, have to incorporate the level matching as well
        // the else statement is matchFields, but instead of returning a list, it matches by level and adds to the collection
        else {
            String[] levels = categoryField.getLevels();
            Set<Integer> missingIndex = new HashSet<>();
            for (Field f : fields) {
                List<Integer> missing = f.getMissing(); 
                missingIndex.addAll(missing);  
            }
            for (String level : levels) {
                List<Integer> indexOfLevel = categoryField.getIndexOfLevel(level);
                if (indexOfLevel.size() <= 1) {
                    continue;
                }
                XYSeries series = new XYSeries(level);
                for (int i: indexOfLevel) {
                    if (!missingIndex.contains(i)) {    // skips if at least one field has a missing at that value
                        Object valueX = xField.getTypedAtIndex(i);
                        Object valueY = yField.getTypedAtIndex(i);
                        if (valueX instanceof Number numberX && valueY instanceof Number numberY) {
                            series.add((float) numberX, (float) numberY);
                        }
                        else {
                            throw new IllegalArgumentException("Values must be numerical: " + valueX + " " + valueY + " " + i);
                        }
                    }
                }
                data.addSeries(series);
            }
        }

        this.categoryField = categoryField;
        computeRegression();
    }

    public JPanel createChart(){
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Scatter Plot", 
            xLabel,             
            yLabel,               
            data,                
            PlotOrientation.VERTICAL,
            true,                   // include legend
            true,                   // include tooltips
            false                   // exclude url
        );

        XYPlot plot = (XYPlot) chart.getPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        if (categoryField == null) {
            // set original dataset to points
            renderer.setSeriesLinesVisible(0, false);
            renderer.setSeriesShapesVisible(0, true);

            // set regression line to line
            renderer.setSeriesLinesVisible(1, true);
            renderer.setSeriesShapesVisible(1, false);
            renderer.setSeriesPaint(1, Color.BLACK);
        }
        else {
            String[] levels = categoryField.getLevels();
            //Color[] colors = {Color.BLACK, Color.GREEN, Color.MAGENTA, Color.ORANGE,Color.CYAN, Color.YELLOW, Color.BLUE, Color.PINK};
            for (int i=0; i<levels.length; i++) {
                // set original dataset to points
                Color color = generateColor(i);
                renderer.setSeriesLinesVisible(i, false);
                renderer.setSeriesShapesVisible(i, true);
                //renderer.setSeriesPaint(i, colors[i%8]);
                renderer.setSeriesShape(i, pointShapes[i%4]);
                renderer.setSeriesPaint(i, color);

                // set regression line to line
                renderer.setSeriesLinesVisible(i + levels.length, true);
                renderer.setSeriesShapesVisible(i + levels.length, false);
                //renderer.setSeriesPaint(i + levels.length, colors[i%8]);
                renderer.setSeriesPaint(i+levels.length, color);
                renderer.setSeriesVisibleInLegend(i + levels.length, false);
            }
        }

        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        return chartPanel;
    }

    public JPanel updateChart() {
        JPanel panel = new JPanel();
        return panel;
    }

    // regression is added to the dataset
    private void computeRegression() {
        if (categoryField == null) {
            double[] coefficients = Regression.getOLSRegression(data, 0); // 0 is the series index
            // coefficients[0] = intercept, coefficients[1] = slope

            XYSeries regressionLine = new XYSeries("Regression Line");

            // Choose min/max X values from the data
            XYSeries series = data.getSeries(0);
            double xMin = series.getMinX();
            double xMax = series.getMaxX();

            // Compute Y = a + bX at min/max to draw the line
            regressionLine.add(xMin, coefficients[0] + coefficients[1] * xMin);
            regressionLine.add(xMax, coefficients[0] + coefficients[1] * xMax);

            // Add regression line to dataset
            data.addSeries(regressionLine);
        }
        else {
            String[] levels = categoryField.getLevels();
            XYSeriesCollection regressionCollection = new XYSeriesCollection();
            for (int i = 0; i < data.getSeriesCount(); i++) {
                double[] coef = Regression.getOLSRegression(data, i);
                XYSeries reg = new XYSeries(levels[i] + " Line");
                double minX = data.getSeries(i).getMinX();
                double maxX = data.getSeries(i).getMaxX();
                reg.add(minX, coef[0] + coef[1] * minX);
                reg.add(maxX, coef[0] + coef[1] * maxX);
                regressionCollection.addSeries(reg);
            }
            for (int i=0; i<regressionCollection.getSeriesCount(); i++) {
                data.addSeries(regressionCollection.getSeries(i));
            }
        }
    }
    private Color generateColor(int index) {
        // Define warm and cool color arrays
        Color[] warmColors = new Color[] {
            new Color(255, 99, 71),   
            new Color(255, 165, 0),  
            new Color(255, 215, 0),  
            new Color(255, 105, 180)   
        };

        Color[] coolColors = new Color[] {
            new Color(30, 144, 255),   
            new Color(0, 191, 255),   
            new Color(60, 179, 113),   
            new Color(138, 43, 226)    
        };

        boolean useCool = (index % 2 == 0);
        int paletteIndex = index / 2; 

        if (useCool) {
            return coolColors[paletteIndex % coolColors.length];
        } else {
            return warmColors[paletteIndex % warmColors.length];
        }
    }



    // maybe delete
    private JFreeChart createEmptyChart() {
        JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, data);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    }
    
}
