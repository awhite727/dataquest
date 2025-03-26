package dataquest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;



public class Boxplot extends Visualization {
    
    private Field data;
    private Field category;


    public Boxplot(String title, Color[] colors, Field data, Field category) {
        super(title, colors);
        this.data = data;
        if (category == null) {
            this.category = null;
        }
        else if (category.isCategorical()) {
            this.category = category;
        }
        else {
            this.category = null;
        }
        ArrayList<Double> values = data.getValues();
    }

    @Override
    public JPanel createChart() {
        DefaultBoxAndWhiskerCategoryDataset dataset = createDataset();

        // Create the chart
        String categoryName;
        if (category == null) {
            categoryName = "";
        }
        else {
            categoryName = category.getName();
        }
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                title, categoryName, data.getName(), dataset, true);

        
        BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setMaximumBarWidth(0.5);      // ensures that boxplots are not extremely wide  

        // Set the customized renderer on your plot
        chart.getCategoryPlot().setRenderer(renderer);
        
        // to do: fix outlier and mean shapes

        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }
    
    @Override
    public JPanel updateChart() {
        return null;
    }

    // chart calculates boxplot values with data
    private DefaultBoxAndWhiskerCategoryDataset createDataset() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        if (data == null || !data.getType().equalsIgnoreCase("float")) {
            System.out.println("Error creating dataset with field " + data.getName() + ". Invalid type.");
            return null;
        }
        if (category == null) {
            dataset.add(data.getValues(), "Data", data.getName());  // "Data" is a placeholder, I don't honestly know why it needs to be there
        }
        else {
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
        return dataset;
    }
}