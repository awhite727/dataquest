package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.inference.TTest;

public class OneSampleTTest {
    Field fieldA;
    double[] valuesA;

    private TTest tTest = new TTest();
    //private Direction direction; 
    private double meanA;
    StatisticalSummaryValues statsA; 
    private double mu; //(typically?) just do meanA - meanB - difference 
    private double alpha; 
    
    private double df;
    private double testStat;
    private double[] ci = new double[2];
    private double criticalValue;
    private double p;
    
    OneSampleTTest(Field fieldA, double alpha, double mu, Direction direction){
        this.fieldA = fieldA;
        ArrayList<Double>valuesA = fieldA.getValues();
        this.valuesA = new double[valuesA.size()];
        for (int i = 0; i < this.valuesA.length; i++) {
            this.valuesA[i] = valuesA.get(i);
        }
        meanA = StatisticalSummary.getMean(valuesA);
        this.mu = mu;
        statsA = new StatisticalSummaryValues(meanA, Math.pow(StatisticalSummary.getStandardDeviation(valuesA),2), StatisticalSummary.getCount(valuesA),StatisticalSummary.getMax(valuesA),StatisticalSummary.getMin(valuesA),StatisticalSummary.getSum(valuesA));
        
        System.out.println("MeanA: " + meanA);
            System.out.println("\tstatsA meanA: " + statsA.getMean());
        System.out.println("VarA: " + statsA.getVariance());
            System.out.println("\tsdA: " + statsA.getStandardDeviation());
        System.out.println("nA: " + statsA.getN());

        this.alpha = alpha;
        //this.direction = direction;
    }

    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }

    public void setOneSampleT(){
        /* Formula: https://www.uah.edu/images/administrative/student-success-center/resources/handouts/handouts_2019/business_stats_test_statistics_and_critical_value.pdf
        * t = (xbar - mu)
        *      /
        *      (sd/sqrt(n))
        */
        testStat = tTest.t(mu, statsA);
        p = round(tTest.tTest(mu,statsA));
        criticalValue = round(StatisticalSummary.getTStar(alpha, statsA.getN()));
        double moe = criticalValue * (statsA.getStandardDeviation()/Math.sqrt(statsA.getN()));
        df = statsA.getN()-1;
        ci[0] = round(meanA - moe);
        ci[1] = round(meanA + moe);
    } 

        public String printBasic(){
            String result = ""; 
            result += "mean " + fieldA.getName()+ " = " + round(meanA);
            result += "\n\u03BC = " + mu; 
            result += "\nalpha = " + alpha;
            result += "\nTest: ";
            result += "[fill]"; 
            //if t
            result += "\n  t = "; 
            result += round(testStat);
            result += "\n  df = ";
            result += round(df);
            result +="\n  P-Value = ";
            result += round(p);
            result += "\n  CI: ";
            result += Arrays.toString(ci);
            result += "\nConclusion: ";
            result += "[fill]";

            return result;
        }
}
