package dataquest;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

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
    private JButton addRowButton, addColumnButton, importingButton;

    private Dataset dataset;

    

    public Layout() {
        setTitle("DataQuest");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        dataset = new Dataset();

        // Create buttons
        JPanel buttonPanel = new JPanel();
        addRowButton = new JButton("Add Row");
        addColumnButton = new JButton("Add Column");
        importingButton = new JButton("Import Dataset");
        
        buttonPanel.add(addRowButton);
        buttonPanel.add(addColumnButton);
        buttonPanel.add(importingButton);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weighty = 0.1;
        add(buttonPanel, gbc);

        // Create spreadsheet
        tableModel = new DefaultTableModel(5, 3);
        spreadsheet = new JTable(tableModel);

        // Disable JTable auto-resizing to force horizontal scrolling
        spreadsheet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // hover for full text if too long
        spreadsheet.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = spreadsheet.rowAtPoint(e.getPoint());
                int col = spreadsheet.columnAtPoint(e.getPoint());
                if (row != -1 && col != -1) {
                    Object value = spreadsheet.getValueAt(row, col);
                    spreadsheet.setToolTipText(value != null ? value.toString() : null);
                }
            }
        });
        // hover for full text in column headers
        spreadsheet.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = spreadsheet.columnAtPoint(e.getPoint());
                if (col != -1) {
                    String columnName = spreadsheet.getColumnName(col);
                    spreadsheet.getTableHeader().setToolTipText(columnName);
                }
            }
        });

        // Wrap JTable in JScrollPane with horizontal scrolling enabled
        JScrollPane scrollPane = new JScrollPane(spreadsheet);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Add JScrollPane instead of JTable directly
        gbc.gridx = 0; gbc.gridy = 0; 
        gbc.gridwidth = 1; gbc.weightx = 1; gbc.weighty = 0.4; 
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollPane, gbc);


        // Create output area
        output = new JTextArea();
        output.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        add(new JScrollPane(output), gbc);

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
        importingButton.addActionListener(e -> importDataset());
        tableModel.addTableModelListener(e -> updateCharts());
    }

    private JFreeChart createEmptyChart(String title) {
        XYSeriesCollection data = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(title, "X", "Y", data);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    }

    // called from button
    private void importDataset() {
        dataset.gui();
        ArrayList<Field> data = dataset.getDataArray();
        int rows = data.get(0).getTypedArray().size();
        int columns = data.size();
        tableModel = new DefaultTableModel(rows, columns);
        spreadsheet.setModel(tableModel); // update model
        // cache columns to reduce method calls
        ArrayList<ArrayList<?>> cachedColumns = new ArrayList<>();
        for (Field field : data) { 
            cachedColumns.add(field.getTypedArray()); 
        }

        // populate table, data must be populated by row
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                tableModel.setValueAt(cachedColumns.get(j).get(i), i, j);
            }
        }
        tableModel.setColumnIdentifiers(dataset.getFields());
        System.out.println("Import Successful");
    }

    private void addRow() {
        tableModel.addRow(new Object[tableModel.getColumnCount()]);
    }

    private void addColumn() {
        tableModel.addColumn("Column " + (tableModel.getColumnCount() + 1));
    }

    private void addColumn(String name) {
        tableModel.addColumn(name + (tableModel.getColumnCount() + 1));
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