package dataquest;

import java.util.ArrayList;
//TODO: Make sure that it works multiple times in a row with different fields
public class Histogram {
    Field field; 
    ArrayList<Double> values;
    Histogram(Field field) {
        this.field = field;
        values = field.getValues();
    }
    //sets the number of bins if size of a bin is defined
    String binFromSize(double size) {
        double min = StatisticalSummary.getMin(values);
        double max = StatisticalSummary.getMax(values);
        int num = (int) Math.ceil((max-min)/size);

        return histogramText(num, size);
    }

    //sets the bins if the number of bins is defined
    String binFromNum(int num){
        //double min = StatisticalSummary.getMin(values);
        //double max = StatisticalSummary.getMax(values);
        //double size = (max-min)/num;
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

}
