package dataquest;

import java.util.ArrayList;
//TODO: Make sure that it works multiple times in a row with different fields
public class MeanDiff {
    Field fieldA;
    Field fieldB; 
    ArrayList<Double> valuesA;
    ArrayList<Double> valuesB;

    MeanDiff(Field fieldA, Field fieldB) {
        this.fieldA = fieldA;
        valuesA = fieldA.getValues();
        this.fieldB = fieldB;
        valuesB = fieldB.getValues();
        /* Formula: 
         * (avgA - avgB) +- t* sqrt(
         *                      (sdA^2/nA) + (sdB^2/nB)
         *                      )
         */
    }

    //TODO: Implement Ashen's t-val getter
    public double[] calculateDifference(double t) {
        double[] range = new double[2];
        double xbarDiff = StatisticalSummary.getMean(valuesA) - StatisticalSummary.getMean(valuesB);
        double sqrtInterior = (Math.pow(StatisticalSummary.getStandardDeviation(valuesA),2)/StatisticalSummary.getCount(valuesA))
                        + (Math.pow(StatisticalSummary.getStandardDeviation(valuesB),2)/StatisticalSummary.getCount(valuesB));
        double tPart = t * Math.sqrt(sqrtInterior);
        range[0] = xbarDiff + tPart;
        range[1] = xbarDiff - tPart;
        return range;
    }

    public void t_Test() {
        
    }

    public void printTest() {

    }
}
