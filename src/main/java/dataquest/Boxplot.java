package dataquest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;



public class Boxplot extends Visualization {
    
    private Field data;
    private Field category;
    private double min;
    private double q1;
    private double median;
    private double q3;
    private double max;


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
        min = StatisticalSummary.getMin(values);
        q1 = StatisticalSummary.getQuartile(values, 1);
        median = StatisticalSummary.getMedian(values);
        q3 = StatisticalSummary.getQuartile(values, 3);
        max = StatisticalSummary.getMax(values);
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

        
        CustomBoxAndWhiskerRenderer renderer = new CustomBoxAndWhiskerRenderer();
        renderer.setMaximumBarWidth(0.5);      // ensures that boxplots are not extremely wide  
        renderer.setOutlierRadius(8.0); // custom size
        renderer.setMeanVisible(false); // no mean circle

        // Set the customized renderer on your plot
        chart.getCategoryPlot().setRenderer(renderer);

        CategoryPlot plot = chart.getCategoryPlot();
        ValueAxis rangeAxis = plot.getRangeAxis();
        // keeps from cutting off outliers
        rangeAxis.setAutoRange(false);
        double padding = (max - min) / 10;
        rangeAxis.setRange(min - padding, max + padding);

        // set color of title
        TextTitle title = chart.getTitle();
        title.setBackgroundPaint(UIManager.getColor("Label.background"));
        title.setPaint(UIManager.getColor("Label.foreground"));

    
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(chartPanel, BorderLayout.CENTER);
        return panel;
    }
    
    // is this necessary?
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
                    try {
                        if (dataArray.get(i) instanceof Number) {
                            dataLevels.add((float) dataArray.get(i));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("ERROR: dataArray is smaller than indexes of the levels");
                        e.printStackTrace();
                    }
                }
                dataset.add(dataLevels, "Data", level);
            }
        }
        return dataset;
    }

    // code modified from here: https://stackoverflow.com/questions/33334206/how-to-remove-outlierssmall-circle-from-box-and-whisker-chart-in-jfreechart
    static class CustomBoxAndWhiskerRenderer extends BoxAndWhiskerRenderer {

    private Double outlierRadius;

    public Double getOutlierRadius() {
        return outlierRadius;
    }

    public void setOutlierRadius(Double outlierRadius) {
        this.outlierRadius = outlierRadius;
    }

    @Override
    public void drawVerticalItem(Graphics2D g2, CategoryItemRendererState state,
                                 Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                                 ValueAxis rangeAxis, CategoryDataset dataset, int row, int column) {

        BoxAndWhiskerCategoryDataset bawDataset
                = (BoxAndWhiskerCategoryDataset) dataset;
        
        

        double categoryEnd = domainAxis.getCategoryEnd(column,
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column,
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = categoryEnd - categoryStart;

        double xx = categoryStart;
        int seriesCount = getRowCount();
        int categoryCount = getColumnCount();

        if (seriesCount > 1) {
            double seriesGap = dataArea.getWidth() * getItemMargin()
                    / (categoryCount * (seriesCount - 1));
            double usedWidth = (state.getBarWidth() * seriesCount)
                    + (seriesGap * (seriesCount - 1));
            // offset the start of the boxes if the total width used is smaller
            // than the category width
            double offset = (categoryWidth - usedWidth) / 2;
            xx = xx + offset + (row * (state.getBarWidth() + seriesGap));
        }
        else {
            // offset the start of the box if the box width is smaller than the
            // category width
            double offset = (categoryWidth - state.getBarWidth()) / 2;
            xx = xx + offset;
        }

        double yyAverage;
        double yyOutlier;

        Paint itemPaint = getItemPaint(row, column);
        g2.setPaint(itemPaint);
        Stroke s = getItemStroke(row, column);
        g2.setStroke(s);

        double aRadius = 0;                 // average radius

        RectangleEdge location = plot.getRangeAxisEdge();

        Number yQ1 = bawDataset.getQ1Value(row, column);
        Number yQ3 = bawDataset.getQ3Value(row, column);
        Number yMax = bawDataset.getMaxRegularValue(row, column);
        Number yMin = bawDataset.getMinRegularValue(row, column);
        Shape box = null;
        if (yQ1 != null && yQ3 != null && yMax != null && yMin != null) {

            double yyQ1 = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea,
                    location);
            double yyQ3 = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea,
                    location);
            double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(),
                    dataArea, location);
            double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(),
                    dataArea, location);
            double xxmid = xx + state.getBarWidth() / 2.0;
            double halfW = (state.getBarWidth() / 2.0) * getWhiskerWidth();

            // draw the body...
            box = new Rectangle2D.Double(xx, Math.min(yyQ1, yyQ3),
                    state.getBarWidth(), Math.abs(yyQ1 - yyQ3));
            if (getFillBox()) {
                g2.fill(box);
            }

            Paint outlinePaint = getItemOutlinePaint(row, column);
            if (getUseOutlinePaintForWhiskers()) {
                g2.setPaint(outlinePaint);
            }
            // draw the upper shadow...
            g2.draw(new Line2D.Double(xxmid, yyMax, xxmid, yyQ3));
            g2.draw(new Line2D.Double(xxmid - halfW, yyMax, xxmid + halfW, yyMax));

            // draw the lower shadow...
            g2.draw(new Line2D.Double(xxmid, yyMin, xxmid, yyQ1));
            g2.draw(new Line2D.Double(xxmid - halfW, yyMin, xxmid + halfW, yyMin));

            g2.setStroke(getItemOutlineStroke(row, column));
            g2.setPaint(outlinePaint);
            g2.draw(box);
        }

        g2.setPaint(getArtifactPaint());

        // draw mean - SPECIAL AIMS REQUIREMENT...
        if (isMeanVisible()) {
            Number yMean = bawDataset.getMeanValue(row, column);
            if (yMean != null) {
                yyAverage = rangeAxis.valueToJava2D(yMean.doubleValue(),
                        dataArea, location);
                aRadius = state.getBarWidth() / 4;
                // here we check that the average marker will in fact be
                // visible before drawing it...
                if ((yyAverage > (dataArea.getMinY() - aRadius))
                        && (yyAverage < (dataArea.getMaxY() + aRadius))) {
                    Ellipse2D.Double avgEllipse = new Ellipse2D.Double(
                            xx + aRadius, yyAverage - aRadius, aRadius * 2,
                            aRadius * 2);
                    g2.fill(avgEllipse);
                    g2.draw(avgEllipse);
                }
            }
        }

        // draw median...
        if (isMedianVisible()) {
            Number yMedian = bawDataset.getMedianValue(row, column);
            if (yMedian != null) {
                double yyMedian = rangeAxis.valueToJava2D(
                        yMedian.doubleValue(), dataArea, location);
                g2.draw(new Line2D.Double(xx, yyMedian,
                        xx + state.getBarWidth(), yyMedian));
            }
        }

        // draw yOutliers...
        double maxAxisValue = rangeAxis.valueToJava2D(
                rangeAxis.getUpperBound(), dataArea, location) + aRadius;
        double minAxisValue = rangeAxis.valueToJava2D(
                rangeAxis.getLowerBound(), dataArea, location) - aRadius;

        g2.setPaint(itemPaint);

        // draw outliers
        double oRadius = outlierRadius == null? state.getBarWidth()/3: outlierRadius;    // outlier radius
        List<Outlier> outliers = new ArrayList<>();
        OutlierListCollection outlierListCollection
                = new OutlierListCollection();

        List yOutliers = bawDataset.getOutliers(row, column);
        if (yOutliers != null) {
            //System.out.println(yOutliers.toString());
            for (int i = 0; i < yOutliers.size(); i++) {
                double outlier = ((Number) yOutliers.get(i)).doubleValue();
                Number minOutlier = bawDataset.getMinOutlier(row, column);
                Number maxOutlier = bawDataset.getMaxOutlier(row, column);
                Number minRegular = bawDataset.getMinRegularValue(row, column);
                Number maxRegular = bawDataset.getMaxRegularValue(row, column);

                // Check for far-out values first.
                if (outlier > maxOutlier.doubleValue()) {
                    outlierListCollection.setHighFarOut(true);
                }
                else if (outlier < minOutlier.doubleValue()) {
                    outlierListCollection.setLowFarOut(true);
                }
                // For outliers between regular and far-out values, add them to the list.
                else if (outlier > maxRegular.doubleValue() || outlier < minRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0, yyOutlier, oRadius));
                }
            }

            // Draw each outlier individually without grouping them.
            for (Outlier outlier : outliers) {
                Point2D point = outlier.getPoint();
                drawEllipse(point, oRadius, g2);
            }

            // Draw far-out indicators if flagged.
            if (outlierListCollection.isHighFarOut()) {
                drawHighFarOut(outlierRadius / 2.0, g2, xx + state.getBarWidth() / 2.0, maxAxisValue);
            }
            if (outlierListCollection.isLowFarOut()) {
                drawLowFarOut(outlierRadius / 2.0, g2, xx + state.getBarWidth() / 2.0, minAxisValue);
            }

        }
        // collect entity and tool tip information...
        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, box);
            }
        }

    }


    /**
     * Draws two dots to represent the average value of more than one outlier.
     *
     * @param point  the location
     * @param boxWidth  the box width.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    private void drawMultipleEllipse(Point2D point, double boxWidth,
                                     double oRadius, Graphics2D g2)  {

        Ellipse2D dot1 = new Ellipse2D.Double(point.getX() - (boxWidth / 2)
                + oRadius, point.getY(), oRadius, oRadius);
        Ellipse2D dot2 = new Ellipse2D.Double(point.getX() + (boxWidth / 2),
                point.getY(), oRadius, oRadius);
        g2.draw(dot1);
        g2.draw(dot2);
    }


    /**
     * Draws a dot to represent an outlier.
     *
     * @param point  the location.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    private void drawEllipse(Point2D point, double oRadius, Graphics2D g2) {
        Ellipse2D dot = new Ellipse2D.Double(point.getX() + oRadius / 2,
                point.getY(), oRadius, oRadius);
        g2.draw(dot);
    }

    /**
     * Draws a triangle to indicate the presence of far-out values.
     *
     * @param aRadius  the radius.
     * @param g2  the graphics device.
     * @param xx  the x coordinate.
     * @param m  the y coordinate.
     */
    private void drawHighFarOut(double aRadius, Graphics2D g2, double xx,
                                double m) {
        double side = aRadius * 2;
        g2.draw(new Line2D.Double(xx - side, m + side, xx + side, m + side));
        g2.draw(new Line2D.Double(xx - side, m + side, xx, m));
        g2.draw(new Line2D.Double(xx + side, m + side, xx, m));
    }


    /**
     * Draws a triangle to indicate the presence of far-out values.
     *
     * @param aRadius  the radius.
     * @param g2  the graphics device.
     * @param xx  the x coordinate.
     * @param m  the y coordinate.
     */
    private void drawLowFarOut(double aRadius, Graphics2D g2, double xx,
                               double m) {
        double side = aRadius * 2;
        g2.draw(new Line2D.Double(xx - side, m - side, xx + side, m - side));
        g2.draw(new Line2D.Double(xx - side, m - side, xx, m));
        g2.draw(new Line2D.Double(xx + side, m - side, xx, m));
    }

}
}