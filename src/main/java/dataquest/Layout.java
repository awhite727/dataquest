package dataquest;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Layout extends JFrame {
    private JTable spreadsheet;
    private DefaultTableModel tableModel;
    private JTextArea output;
    private JFreeChart chart1, chart2;
    private ChartPanel chartPanel1, chartPanel2;
    private Color[] colorPalette;
    private JButton addRowButton, addColumnButton;

    public Layout() {
        setTitle("DataQuest");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Create spreadsheet
        tableModel = new DefaultTableModel(5, 3);
        spreadsheet = new JTable(tableModel);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 1; gbc.weighty = 0.4; gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(spreadsheet), gbc);

        // Create output area
        output = new JTextArea();
        output.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        add(new JScrollPane(output), gbc);

        // Create buttons
        JPanel buttonPanel = new JPanel();
        addRowButton = new JButton("Add Row");
        addColumnButton = new JButton("Add Column");
        
        buttonPanel.add(addRowButton);
        buttonPanel.add(addColumnButton);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 0.1;
        add(buttonPanel, gbc);

        // Set color palette
        colorPalette = new Color[]{
            new Color(0x264653), new Color(0x2A9D8F), new Color(0xE9C46A),
            new Color(0xF4A261), new Color(0xE76F51), new Color(0x023047),
            new Color(0x219EBC), new Color(0xFFB703), new Color(0xFB8500)
        };

        // Create empty charts
        chart1 = createEmptyChart("Wavy Grpah-1");
        chart2 = createEmptyChart("Wavy Graph-2");
        chartPanel1 = new ChartPanel(chart1);
        chartPanel2 = new ChartPanel(chart2);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weighty = 0.5;
        add(chartPanel1, gbc);
        gbc.gridx = 1;
        add(chartPanel2, gbc);

        // Add listeners
        addRowButton.addActionListener(e -> addRow());
        addColumnButton.addActionListener(e -> addColumn());
        tableModel.addTableModelListener(e -> updateCharts());
    }

    private JFreeChart createEmptyChart(String title) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(title, "X", "Y", dataset);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    }

    private void addRow() {
        tableModel.addRow(new Object[tableModel.getColumnCount()]);
    }

    private void addColumn() {
        tableModel.addColumn("Column " + (tableModel.getColumnCount() + 1));
    }

    private void updateCharts() {
        XYSeriesCollection dataset1 = new XYSeriesCollection();
        XYSeriesCollection dataset2 = new XYSeriesCollection();
        
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
                } else {
                    dataset2.addSeries(series);
                }
            }
        }
        
        chart1.getXYPlot().setDataset(dataset1);
        chart2.getXYPlot().setDataset(dataset2);
        
        applyColorPalette(chart1);
        applyColorPalette(chart2);
        
        output.setText("Data updated: " + java.time.LocalDateTime.now());
    }

    private void applyColorPalette(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < plot.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colorPalette[i % colorPalette.length]);
            renderer.setSeriesShapesVisible(i, true);
        }
        plot.setRenderer(renderer);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Layout layout = new Layout();
            layout.setVisible(true);
        });
    }
}