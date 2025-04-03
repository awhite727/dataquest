package dataquest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import jxl.write.Font;
public class Histogram extends Visualization {
    Field field; 
    ArrayList<Double> values;
    int bins = -1;
    String title;
    String xLabel = "TEMPX";
    String yLabel = "TEMPY";
    public Histogram(String title, Color[] colors, Field field) {
        super(title, colors);
        this.field = field;
        this.title = title;
        values = field.getValues();
    }

    /* No longer needed: Will delete upon next commit 

    //sets the number of bins if size of a bin is defined
    String binFromSize(double size) {
        double min = StatisticalSummary.getMin(values);
        double max = StatisticalSummary.getMax(values);
        bins = (int) Math.ceil((max-min)/size);

        return histogramText(bins, size);
    }
    //sets the bins if the number of bins is defined
    String binFromNum(int num){
        //double min = StatisticalSummary.getMin(values);
        //double max = StatisticalSummary.getMax(values);
        //double size = (max-min)/num;
        bins = num;
        return histogramText(num, (((StatisticalSummary.getMax(values))-(StatisticalSummary.getMin(values)))/num));        
    }

    String histogramText(int totalBins, double binSize) {
        double min = StatisticalSummary.getMin(values);
        double max = StatisticalSummary.getMax(values);
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        @SuppressWarnings("unchecked")
        ArrayList<Double>[] bins = (ArrayList<Double>[]) new ArrayList[totalBins];
        
        //Instantiate all bins 
        for (int i = 0; i < bins.length; i++) {
            bins[i] = (new ArrayList<Double>());
        }
        values.stream().forEach(val -> {
            double shiftedVal = val-min; //To start the histogram at a specific num; without it bins get off center
            int tempBin;
            if(val == max) {
                tempBin = totalBins-1;
            }
            else { 
                tempBin = (int) Math.floor(shiftedVal/binSize);
            }
            bins[tempBin].add(val);
        });
        

        //Printing
        String text = "";
        for (int i = 0; i < bins.length; i++) {
            text+=String.format("%.2f",(binSize*i + min)) + ":\t";
            for (int j = 0; j < bins[i].size(); j++) {
                text+="*";
            }
            text+="\n";
        }

        return text;
    }
*/

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

    //TODO: Add default coloring so it doesn't look ugly 
    void setDefaultColors(ChartPanel chartPanel) {
        System.out.println("In default");

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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateChart'");
    }

}
