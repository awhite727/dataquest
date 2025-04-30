package dataquest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

public class Histogram extends Visualization {
    Field field; 
    ArrayList<Double> values;
    int bins = -1;
    String title;
    String xLabel = "Value";
    String yLabel = "Count";
    public Histogram(String title, Color[] colors, Field field) {
        super(title, colors);
        this.field = field;
        this.title = title;
        values = field.getValues();
    }

//sets the number of bins needed
    //If the double input is the number of bins, isNum is assigned true
    //If it is the size, it is assigned false
    void setBins(boolean isNum, double input){
        if(isNum){
            bins = (int)input;
        } else {
            double min = StatisticalSummary.getMin(values);
            double max = StatisticalSummary.getMax(values);
            bins = (int) Math.ceil((max-min)/input);
        }
    }

    void setDefaultColors(ChartPanel chartPanel) {
        //System.out.println("In default");
    }
    @Override
    public JPanel createChart() {
        HistogramDataset dataset = new HistogramDataset();
        PlotOrientation plotOrientation = PlotOrientation.VERTICAL;
        //double[] vals = (double[])values.toArray(); //can't directly cast Object to double bc primative
        double[] vals = new double[values.size()];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = values.get(i);
        }
        dataset.addSeries(TOOL_TIP_TEXT_KEY, vals, bins);
        
        JFreeChart chart = ChartFactory.createHistogram(title, xLabel, yLabel,
        dataset, plotOrientation, true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart);        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        setDefaultColors(chartPanel);
        panel.setVisible(true);
        return panel;
    }
    @Override
    public JPanel updateChart() {
        throw new UnsupportedOperationException("Unimplemented method 'updateChart'");
    }

}
