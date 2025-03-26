package dataquest;

import java.awt.Color;

import javax.swing.JPanel;

/*
    All graphs extend from visualization
    methods that call from layout need to be in visualization so all graphs contain them
 */
public abstract class Visualization extends JPanel{

    protected String title;
    protected Color[] colors;

    public Visualization(String title, Color[] colors) {
        this.title = title;
        this.colors = colors;
    }
    public void setColors(Color[] colors) {
        this.colors = colors;
    }
    public Color[] getColors() {
        return colors;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }    

    public abstract JPanel createChart();
    public abstract JPanel updateChart();

}

