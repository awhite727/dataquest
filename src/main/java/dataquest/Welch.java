package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.apache.commons.math3.stat.inference.TTest; 
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class Welch {
    
    Field fieldA;
    Field fieldB; 
    ArrayList<Double> valuesA;
    ArrayList<Double> valuesB;
    //double[] valuesA;
    //double[] valuesB;

    /* private double meanA;
    private double sdA;
    private int nA;
    private double meanB;
    private double sdB;
    private int nB;
    private double significance;
    private double alpha; 
    private double df;
    private char hNull;
    private String tail;
    
    Welch(){
        nA = 65;
        nB = 65;
        meanA = 98.105;
        meanB = 98.394;
        sdA = .699;
        sdB = .743;
        alpha = 0.05;
        hNull = '=';
    } */
    
    //significance is the value set by the user that indicates if they care if there's a difference
    //So if fieldA - fieldB < 10 reject 
    //hNull is what we want to disprove
        //H0: fieldA = fieldB, '='
        //H0: fieldA > fieldB, '>'
        //H0: fieldA < fieldB, '<'
    Welch(Field fieldA, Field fieldB, double alpha, double significance, char hNull){
        this.fieldA = fieldA;
        /* ArrayList<Double> */valuesA = fieldA.getValues();
        /*this.valuesA = new double[valuesA.size()];
        for (int i = 0; i < this.valuesA.length; i++) {
            this.valuesA[i] = valuesA.get(i);
        } */
        this.fieldB = fieldB;
        /* ArrayList<Double> */valuesB = fieldB.getValues();
        /*this.valuesB = new double[valuesB.size()];
        for (int i = 0; i < this.valuesB.length; i++) {
            this.valuesB[i] = valuesB.get(i);
        } */
        /* meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        sdA =  StatisticalSummary.getStandardDeviation(valuesA);
        nA = StatisticalSummary.getCount(valuesA);
        sdB = StatisticalSummary.getStandardDeviation(valuesB);
        nB = StatisticalSummary.getCount(valuesB);
        this.hNull = hNull;
        this.significance = significance;   */
    }

    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }

    public double getSignificance(){
        TTest tTest = new TTest();
        System.out.println("Field A: " + fieldA.getName() + "\tField B: " + fieldB.getName());
        //System.out.println(tTest.tTest(valuesA, valuesB));
        StatisticalSummaryValues ssA = new StatisticalSummaryValues(StatisticalSummary.getMean(valuesA),Math.pow(StatisticalSummary.getStandardDeviation(valuesA),2),StatisticalSummary.getCount(valuesA), StatisticalSummary.getMax(valuesA), StatisticalSummary.getMin(valuesA), valuesA.stream().mapToDouble(a->a).sum());
        StatisticalSummaryValues ssB = new StatisticalSummaryValues(StatisticalSummary.getMean(valuesB),Math.pow(StatisticalSummary.getStandardDeviation(valuesB),2),StatisticalSummary.getCount(valuesB), StatisticalSummary.getMax(valuesB), StatisticalSummary.getMin(valuesB), valuesB.stream().mapToDouble(a->a).sum());
        System.out.println(StatisticalSummary.getSummary(valuesB));
        //System.out.println(tTest.tTest(1.0,1.0,1.0,1.0,1.0,1.0));
        return tTest.tTest(ssA, ssB);
    }
    
}
