package dataquest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;


public class Boxplot extends Visualization {
    
    private Field data;
    private double min;
    private double firstQuartile;
    private double median;
    private double thirdQuartile;
    private double max;
    private Field category;


    public Boxplot(String title, Color[] colors, Field data, Field category) {
        super(title, colors);
        this.data = data;
        this.category = category;
        ArrayList<Double> values = data.getValues();
        min = StatisticalSummary.getMin(values);
        firstQuartile = StatisticalSummary.getQuartile(values, 1);
        median = StatisticalSummary.getMedian(values);
        thirdQuartile = StatisticalSummary.getQuartile(values, 3);
        max = StatisticalSummary.getMax(values);
    }

    @Override
    public JPanel createChart() {
        DefaultBoxAndWhiskerCategoryDataset dataset = createDataset();

        // Create the chart
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                "Box Plot Example", "Category", "Value", dataset, true);

        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }
    
    @Override
    public JPanel updateChart() {
        return new JPanel();
    }

    private DefaultBoxAndWhiskerCategoryDataset createDataset() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        if (data == null || !data.getType().equalsIgnoreCase("float")) {
            System.out.println("Error creating dataset with field " + data.getName() + ". Invalid type.");
            return null;
        }
        if (category == null) {
            dataset.add(data.getValues(), "Data", data.getName());
        }
        else if(category.isCategorical()) {
            String [] levels = category.getLevels();
            for (String level : levels) {
                ArrayList<Integer> indexes = category.getIndexOfLevel(level);
                ArrayList<Object> dataArray = data.getTypedArray();
                List<Float> dataLevels = new ArrayList<>();
                for(int i:indexes) {
                    if (dataArray.get(i) instanceof Number) {
                        dataLevels.add((float) dataArray.get(i));
                    }
                }
                dataset.add(dataLevels, "Data", level);
            }
        }
        else {
            System.out.println("Error creating dataset with field " + category.getName() +". Not categorical.");
            return null;
        }
        return dataset;
    }
}