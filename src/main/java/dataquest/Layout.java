package dataquest;
import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public class Layout extends JFrame {
    private JTable spreadsheet;
    private DefaultTableModel tableModel;
    private JTextArea output;
    //private JFreeChart chart1, chart2;
    //private ChartPanel chartPanel1, chartPanel2;
    //private Color[] colorPalette;
    private JButton addRowButton, addColumnButton, importingButton, 
        handleMissingButton, statisticalSummaryButton, 
        histogramButton, boxplotButton, linearRegressionButton,
        meanDiffButton;

    private Dataset dataset;
    //private Graph graph1;
    //private Graph graph2;
    private Visualization visual1;
    private Visualization visual2;
    private JPanel visualPanel1;
    private JPanel visualPanel2;

    private final int visualPanelWidth = 400;
    private final int visualPanelHeight = 300;

    public Layout() {
        setTitle("DataQuest");
        setSize(800, 600);
        //setIconImage(new ImageIcon("src\\main\\resources\\icon.png").getImage()); //Just changes icon at the bottom when it's running to whatever icon.png we have in resources
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dataset = new Dataset();
        loadSavedWorkspace();
        /*
        if (graph1 == null) {
            graph1 = new Graph();
        } 
        if (graph2 ==null){
            graph2 = new Graph();
        } */

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int exitChoice = JOptionPane.showConfirmDialog (null, "Would you like to save your workspace?",null, JOptionPane.YES_NO_OPTION);                
                if(exitChoice == JOptionPane.YES_OPTION){
                    //TODO: Add loading bar; takes a good bit
                    System.out.println("Loading");
                    ArrayList<Object> workspace = new ArrayList<>();
                    //Graph[] graphs = new Graph[]{graph1,graph2};
                    //workspace.add(graphs);
                    Serialization ser = new Serialization();
                    ser.saveProject(workspace);
                } if(exitChoice == JOptionPane.YES_OPTION || exitChoice == JOptionPane.NO_OPTION) {
                    System.exit(0);
                }
            }  
        });
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Create buttons
        JPanel buttonPanel = new JPanel();
        addRowButton = new JButton("Add Row");
        addColumnButton = new JButton("Add Column");
        importingButton = new JButton("Import Dataset");
        handleMissingButton = new JButton("Handle Missing");
        statisticalSummaryButton = new JButton("Statistical Summary");

        histogramButton = new JButton("Histogram");
        boxplotButton = new JButton("Boxplot");
        linearRegressionButton = new JButton("Linear Regression");
        meanDiffButton = new JButton("Mean comparison");

        buttonPanel.add(addRowButton);
        buttonPanel.add(addColumnButton);
        buttonPanel.add(importingButton);
        buttonPanel.add(handleMissingButton);
        buttonPanel.add(statisticalSummaryButton);
        buttonPanel.add(histogramButton);
        buttonPanel.add(boxplotButton);
        buttonPanel.add(linearRegressionButton);
        buttonPanel.add(meanDiffButton);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weighty = 0.1;
        add(buttonPanel, gbc);

        // Create spreadsheet
        tableModel = new DefaultTableModel(5, 3);
        spreadsheet = new JTable(tableModel);
        JTableHeader header = spreadsheet.getTableHeader();
        // allows for editing column names
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new EditableHeaderRenderer(header.getDefaultRenderer()));
        header.addMouseListener(new HeaderEditorListener(spreadsheet));

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
        /* colorPalette = new Color[]{
            new Color(0x264653), new Color(0x2A9D8F), new Color(0xE9C46A),
            new Color(0xF4A261), new Color(0xE76F51), new Color(0x023047),
            new Color(0x219EBC), new Color(0xFFB703), new Color(0xFB8500)
        }; */

        //TODO: POSSIBLE BREAK POINT; make sure that updates to Graph chart affect charts here
        // Create empty charts
        //chart1 = createEmptyChart("Wavy Grpah-1");
        //chart2 = createEmptyChart("Wavy Graph-2");
        
        /*chartPanel1 = new ChartPanel(graph1.getChart());
        chartPanel2 = new ChartPanel(graph2.getChart());
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weighty = 0.5;
        add(chartPanel1, gbc);
        gbc.gridx = 1;
        add(chartPanel2, gbc); */

        visualPanel1 = new JPanel();
        visualPanel1.setLayout(new BorderLayout()); // most charts need to be centered
        visualPanel1.setPreferredSize(new Dimension(visualPanelWidth, visualPanelHeight));
        visualPanel1.add(new JLabel("Visualization 1"));    // placeholder label for default look
        visualPanel2 = new JPanel();
        visualPanel2.setLayout(new BorderLayout()); // most charts need to be centered
        visualPanel2.setPreferredSize(new Dimension(visualPanelWidth, visualPanelHeight));
        visualPanel2.add(new JLabel("Visualization 2"));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weighty = 0.5;
        add(visualPanel1, gbc);
        gbc.gridx = 1;
        add(visualPanel2, gbc);


        // Add listeners
        addRowButton.addActionListener(e -> addRow());
        addColumnButton.addActionListener(e -> addColumn());
        importingButton.addActionListener(e -> importAssist());
        handleMissingButton.addActionListener(e -> {
            if(Dataset.dataArray != null) {
                ChoiceMenu.missingValueMenu(this);
                updateSpreadsheet();
                System.out.println("Missing handled successfully.");
            }
        });
        statisticalSummaryButton.addActionListener(e -> {
            if(Dataset.dataArray != null) {
                String textOutput = ChoiceMenu.statisticalSummaryMenu(this);
                output.append(textOutput);
            }
        });
        histogramButton.addActionListener(e-> {
            if(Dataset.dataArray != null) {
                //String textOutput = ChoiceMenu.histogramMenu(this);
                //if(textOutput != null) output.append(textOutput+"---\n");
                visual1 = ChoiceMenu.histogramMenu(this);
                if(visual1!= null){
                    JPanel newPanel = visual1.createChart();
                    visualPanel1.removeAll();
                    //newPanel.add(new JLabel("Testing Layout"));
                    visualPanel1.add(newPanel, BorderLayout.CENTER);
                    visualPanel1.revalidate();
                    visualPanel1.repaint();
                }
            }
          //TODO: create actual graph
        });
        boxplotButton.addActionListener(e -> {
            if (Dataset.dataArray != null) {
                visual1 = ChoiceMenu.boxplotMenu(this);
                JPanel newPanel = visual1.createChart();

                // remove old panel and add new one
                visualPanel1.removeAll();
                visualPanel1.add(newPanel, BorderLayout.CENTER);

                visualPanel1.revalidate();
                visualPanel1.repaint();
            }
        });
        linearRegressionButton.addActionListener (e -> {
            if (Dataset.dataArray != null) {
                String info = ChoiceMenu.linearRegression(this);
                output.append(info);
            }
        });
        meanDiffButton.addActionListener(e -> {
            if (Dataset.dataArray != null) {
                String info = ChoiceMenu.meanDiffMenu(this);
                output.append(info);
            }
        });
        tableModel.addTableModelListener(e -> updateCharts());
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    if (row >=0 && column >= 0) {
                        Object updatedValue = tableModel.getValueAt(row, column);
                        if (updatedValue != null) {
                            updateValue(updatedValue, row, column);
                        }
                        System.out.println("Value changed at [" + row + ", " + column + "] to: " + updatedValue);
                    }
                }
            }
        });
    

    if (dataset == null) {
        dataset = new Dataset();
    } else {
        updateSpreadsheet();
    }

    /* private JFreeChart createEmptyChart(String title) {
        XYSeriesCollection data = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(title, "X", "Y", data);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        return chart;
    } */
    }
    @SuppressWarnings("unchecked")
    private void loadSavedWorkspace() {
        //TODO: Include a loading bar; takes a bit to load in
        System.out.println("Loading");
        Serialization ser = new Serialization();
        ArrayList<Object> state = ser.openProject();
        if(state.size() != 2){return;}
        
        //TODO: Add any other serialized objects to loadSavedWorkspace
        try { 
            ArrayList<Object> directState = (ArrayList<Object>)state.get(0);
            for (Object object : directState) {
                if (object instanceof Graph[]) {
                    Graph[] graphs = (Graph[])object;
                    for (int i = 0; i < graphs.length; i++){
                    }
                    //graph1 = graphs[0];
                    //graph2 = graphs[1];
                } else {
                    System.out.println(object.getClass());
                }
            }
            //graph1 = (Graph)((Graph[])directState.get(0))[0];
            //graph2 = (Graph)((Graph[])directState.get(0))[1];
            //graph1.updateChart();
            //graph2.updateChart();

            dataset.setDataArray((ArrayList<Field>)state.get(1));
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }
    
    //Opens the importing window
    //Checks if the user has python properly installed
    //If so, it opens the modern importing window
    //TODO: Add notification if they don't have it
    //Once file selected, calls the repesective Dataset's csvReading, xlsReading, or xlsxReading
   //Returns true if the file was found and properly added to Dataset.dataArray, returns false if not 
    private void importAssist(){
        String pythonPath = "src\\main\\resources\\PythonAssist.py";
        String selectedPath = "";
        File file = null;
        ProcessBuilder pb;
        Process p;
        BufferedReader bf;
        try {
            p = Runtime.getRuntime().exec("python --version");
            bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor();
            System.out.println(bf.readLine()); //prints the python version read
            bf.close();
        } catch (Exception e) {
            //Python isn't present
            System.out.println("ERROR: Python not installed.");
            JOptionPane.showMessageDialog(null, "ERROR: Python not properly installed. Please install to import data");
            return;
        }

        pb = new ProcessBuilder()
            .command("python","-u", pythonPath, "openFile");
      
      try {
         //run the process from process builder; 
         p = pb.start();
         bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
         selectedPath = bf.readLine();
         p.waitFor();
         bf.close();
         file = new File(selectedPath);
      } catch (IOException e) {
         System.out.println("ERROR: " + pythonPath + " could not be found");
         e.printStackTrace();
      } catch (InterruptedException e) {
         //Process p interupted by another thread
         e.printStackTrace();
      } catch (NullPointerException e){
         //file selection canceled 
      }
        try {
            if(file.getName().equals("")){//nothing selected
                return;
            } else if(file.getName().endsWith(".txt")) {
                System.out.println("txt");
                //TODO: Call choice menu to determine the delim
                String delim = ChoiceMenu.importingDelimMenu(this);
                if(delim != null) dataset.csvReading(file, delim);
            } else if(file.getName().endsWith(".csv")) {
                System.out.println("csv");
                dataset.csvReading(file); 
            } else if(file.getName().endsWith(".xlsx")) {
                System.out.println("xlsx");
                dataset.xlsxReading(file);
            } else if(file.getName().endsWith(".xls")) {
                System.out.println("xls");
                dataset.xlsReading(file);
            } else {
                System.out.println("Not a valid file type: " + file.getName());
                return;
            }
        } catch(IOException e) {
            System.out.println("File not found: ");
            System.out.println(file);
            return;
        } catch(Exception e) {
            System.out.println("ERROR: Unknown error in Dataset.gui()");
            e.printStackTrace();
            return;
        }
        updateSpreadsheet();
    }

    // call if dataset is updated
    private void updateSpreadsheet() {
        ArrayList<Field> data = dataset.getDataArray();
        if(data == null){return;} // handles empty dataset
        //int rows = data.get(0).getTypedArray().size();
        int rows = 0;
        // for non-rectangular dataArrays, gets the largest row size
        for (int i = 0; i < data.size(); i++ ) {
            int rowSize = data.get(i).getTypedArray().size();
            if (rowSize > rows) {
                rows = rowSize;
            }
        }
        int columns = data.size();
        // save listeners before recreating tableModel
        TableModelListener[] listeners = tableModel.getTableModelListeners();
        for (TableModelListener listener : listeners) {
            tableModel.removeTableModelListener(listener);
        }
        tableModel = new DefaultTableModel(rows + 1, columns + 1); // Ensure extra row & column
        spreadsheet.setModel(tableModel); // update model
        // cache columns to reduce method calls
        ArrayList<ArrayList<?>> cachedColumns = new ArrayList<>();
        for (Field field : data) { 
            cachedColumns.add(field.getTypedArray()); 
        }

        // populate table, data must be populated by row
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                ArrayList<?> column = cachedColumns.get(j);
                Object value = (i < column.size()) ? column.get(i) : null; // handles non-rectangular datasets
                tableModel.setValueAt(value, i, j);
            }
        }
        // set the correct names for the table
        String[] identifiers = Dataset.getFields();
        String[] identifiersWithBlank = new String[identifiers.length + 1];
        for (int i=0; i<identifiers.length;i++) {
            identifiersWithBlank[i] = identifiers[i];
        }
        String blankColumn = "Column " + identifiersWithBlank.length;
        identifiersWithBlank[identifiersWithBlank.length-1] = blankColumn;
        tableModel.setColumnIdentifiers(identifiersWithBlank);
        // read listeners after tableModel has been recreated
        for (TableModelListener listener : listeners) {
            tableModel.addTableModelListener(listener);
        }
    }

    private void addRow() {
        tableModel.addRow(new Object[tableModel.getColumnCount()]);
    }

    // be sure to call this if you are adding a column, never add directly
    private void addColumn() {
        int columnCount = tableModel.getColumnCount();
        String[] columnNames = new String[columnCount + 1];

        // gets names of columns
        if (Dataset.dataArray !=  null) {
            String[] fieldNames = Dataset.getFields();
            for (int i=0; i< fieldNames.length; i++) {
                columnNames[i] = fieldNames[i];
            }
            for (int i= fieldNames.length; i<columnNames.length -1; i++) {
                columnNames[i] = tableModel.getColumnName(i);
            }
        }
        else {
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = tableModel.getColumnName(i);
            }
        }

        // Add a new column name
        columnNames[columnCount] = "Column " + (columnCount + 1);
        tableModel.addColumn("Column " + (tableModel.getColumnCount() + 1));
        tableModel.setColumnIdentifiers(columnNames);
    }

    // manual entry 
    private void updateValue(Object value, int row, int col) {
        if (Dataset.dataArray == null) {
            Dataset.dataArray = new ArrayList<>();
        }

        // Ensure dataset has enough columns
        while (Dataset.dataArray.size() <= col) {
            if (Dataset.getPattern(value.toString()).equals("missing")) {   // if a blank value is accidentally added to a new column
                return;
            }
            Dataset.dataArray.add(new Field(tableModel.getColumnName(Dataset.dataArray.size())));
        }

        // Ensure correct field type
        Field field = Dataset.dataArray.get(col);
        if (field.getType() == null) {
            field.setType(Dataset.getPattern(value.toString()));
        }
        boolean success = field.setCell(row, value.toString().strip());

        // temporarily remove listener before updating model
        // without removing listeners, infinite recursion occurs
        TableModelListener[] listeners = tableModel.getTableModelListeners();
        for (TableModelListener listener : listeners) {
            tableModel.removeTableModelListener(listener);
        }
        if (success) {
            tableModel.setValueAt(value, row, col);
        }

        // re-add listeners
        for (TableModelListener listener : listeners) {
            tableModel.addTableModelListener(listener);
        }

        // Ensure an extra empty row and column
        if (tableModel.getRowCount() <= row + 1) {
            addRow();
        }
        if (tableModel.getColumnCount() <= col + 1) {
            addColumn();
        }
    }


    private void updateCharts() {
        //graph1.updateCharts(tableModel);
        //graph2.updateCharts(tableModel);
        /* XYSeriesCollection dataset1 = new XYSeriesCollection();
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
        

        //TODO: POSSIBLE BREAK POINT; make sure the color is properly set
        applyColorPalette(graph1.getChart());
        applyColorPalette(graph2.getChart());
        */
        //output.setText("Data updated: " + java.time.LocalDateTime.now());
    }

    /* private void applyColorPalette(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < plot.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colorPalette[i % colorPalette.length]);
            renderer.setSeriesShapesVisible(i, true);
        }
        plot.setRenderer(renderer);
    }
*/
    // custom renderer to make headers look normal even while editing
    static class EditableHeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer defaultRenderer;

        public EditableHeaderRenderer(TableCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    // listener to handle header editing
    static class HeaderEditorListener extends MouseAdapter {
        private final JTable table;
        private final JTextField editor = new JTextField();
        private int editingColumn = -1;

        public HeaderEditorListener(JTable table) {
            this.table = table;

            editor.addActionListener(e -> stopEditing());
            editor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    stopEditing();
                }
            });
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            JTableHeader header = table.getTableHeader();
            int column = header.columnAtPoint(e.getPoint());

            if (column != -1) {
                startEditing(column);
            }
        }

        private void startEditing(int column) {
            editingColumn = column;
            JTableHeader header = table.getTableHeader();
            TableColumnModel colModel = table.getColumnModel();
            TableColumn col = colModel.getColumn(column);

            editor.setText(col.getHeaderValue().toString());
            editor.setBounds(header.getHeaderRect(column));
            header.add(editor);
            editor.requestFocus();
            editor.selectAll();
        }

        private void stopEditing() {
            if (editingColumn != -1) {
                JTableHeader header = table.getTableHeader();
                TableColumnModel colModel = table.getColumnModel();
                TableColumn col = colModel.getColumn(editingColumn);

                String newName = editor.getText();
                col.setHeaderValue(newName);
                header.repaint();
                header.remove(editor);
                Dataset.dataArray.get(editingColumn).setName(newName); // sets name of field
                editingColumn = -1;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Layout layout = new Layout();
            layout.setVisible(true);
        });
        
    }
}