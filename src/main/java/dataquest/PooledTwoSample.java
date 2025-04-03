package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class PooledTwoSample {

    Field fieldA;
    Field fieldB; 
    ArrayList<Double> valuesA;
    ArrayList<Double> valuesB;

    private double meanA;
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
    /* public static void main(String[] args) {
        PooledTwoSample ps = new PooledTwoSample();
        //ps.commonSDEstimate();
        //ps.calculateTStatistic();
        ps.printAnalysis();
    } */
    PooledTwoSample(){
        /* nA = 10;
        sdA = .683;
        nB = 10;
        sdB = .750;
        significance = 0;
        meanA = 42.14;
        meanB = 43.23;
        alpha = 0.05;
        //alpha = 0.1; 
        hNull = '>';  

        nA = 10;
        sdA = .750;
        nB = 10;
        sdB = .683;
        significance = 0;
        meanA = 43.23;
        meanB = 42.14;
        alpha = 0.05;
        //alpha = 0.1; 
        hNull = '<'; //TODO: Handle two-tail vs one-tail left vs open-tail right differences (alpha differences, critical value differences)
*/
        /* nA = 10;
        sdA = 5.32;
        nB = 13;
        sdB = 6.84; //Note: website checking off had incorrect sd^2; still slightly off but closer
        //sdB = 6.84251416;
        significance = 0;
        meanA = 22.29;
        meanB = 14.95;
        alpha = 0.05;
        hNull = '=';  */

        nA = 65;
        nB = 65;
        meanA = 98.105;
        meanB = 98.394;
        sdA = .699;
        sdB = .743;
        alpha = 0.05;
        hNull = '=';

    }
    
    //significance is the value set by the user that indicates if they care if there's a difference
    //So if fieldA - fieldB < 10 reject 
    //hNull is what we want to disprove
        //H0: fieldA = fieldB, '='
        //H0: fieldA > fieldB, '>'
        //H0: fieldA < fieldB, '<'
    PooledTwoSample(Field fieldA, Field fieldB, double alpha, double significance, char hNull){
        this.fieldA = fieldA;
        valuesA = fieldA.getValues();
        this.fieldB = fieldB;
        valuesB = fieldB.getValues();
        meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        sdA =  StatisticalSummary.getStandardDeviation(valuesA);
        nA = StatisticalSummary.getCount(valuesA);
        sdB = StatisticalSummary.getStandardDeviation(valuesB);
        nB = StatisticalSummary.getCount(valuesB);
        this.hNull = hNull;
        this.significance = significance;  
    }


    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }

    private double commonSDEstimate(){
        /*  Sp = 
         * sqrt(
         *      ((nA - 1)*sA^2   +   (nB - 1)*sB^2)
         *  /
         *      (nA + nB - 2)
         * )
         */
        double numeratorA = round((nA - 1)*Math.pow(sdA,2));
        double numeratorB = round((nB - 1)*Math.pow(sdB,2));
        df = nA + nB - 2;
        double commonSD = round(Math.sqrt(((numeratorA+numeratorB)/df)));
        System.out.println("commonSD: " + commonSD);
        System.out.println("\t sqrt(" + round((numeratorA+numeratorB)/df) + ")");
        System.out.println("\t(nA-1*sdA^2): " + numeratorA);
        System.out.println("\t\tsdA^2: " + round(Math.pow(sdA,2)));

        System.out.println("\t(nB-1*sdB^2): " + numeratorB);
        System.out.println("\t\tsdB^2: " + round(Math.pow(sdB,2)));

        System.out.println("\t(nA + nB - 2): " + df);

        return commonSD;
    }
    private double calculateTStatistic(){
        /* t* = 
         * (avgA - avgB - significance)
         * / 
         * ((commonSDEstimate)*sqrt(1/sdA + 1/sdB))
         */
        double numerator = round(meanA - meanB - significance);
        double sqrt = round(Math.sqrt((1.0/nA + 1.0/nB)));
        double tStat = round(numerator/(commonSDEstimate()*sqrt));
        System.out.println("Numerator: " + numerator);
        System.out.println("sqrt: " + sqrt);
        System.out.println("\tInterior: (1/" + nA + " + 1/" + nB + ") = " + (1.0/nA + 1.0/nB));
        System.out.println("TStat: " + tStat);
        return tStat;
    }

    public double[] calculateCI(){
        /* Formula: 
         * (avgA - avgB) +- t* sqrt(
         *                      (sdA^2/nA) + (sdB^2/nB)
         *                      )
         */
        double[] ci = new double[2];
        //double t = StatisticalSummary.getTStar(alpha, 64);
        double t = StatisticalSummary.getTStar(alpha, df);
        double xbarDiff = round(meanA - meanB); //TODO: include margin here? like if they don't care if the difference is less than 10
        double sqrtInterior = round((Math.pow(sdA,2)/nA) + (Math.pow(sdB,2)/nB));
        double tPart = round(t * Math.sqrt(sqrtInterior));
        ci[0] = round(xbarDiff - tPart);
        ci[1] = round(xbarDiff + tPart);

        return ci;
    }

    public void printAnalysis(){
        double tStat = calculateTStatistic();
        double criticalVal;
        if(hNull == '>'){
            criticalVal = round(-StatisticalSummary.getTStar(alpha*2, df));
        } else if(hNull == '<'){
            criticalVal = round(StatisticalSummary.getTStar(alpha*2, df));
        } else if(hNull == '='){
            criticalVal = round(StatisticalSummary.getTStar(alpha, df));
        } else {
            System.out.println("ERROR: " + tail + " is not a valid tail");
            return;
        }
        System.out.println("tStat: "+ tStat);
        System.out.println("criticalVal: " + criticalVal); 
        System.out.print("Conclusion: ");
        switch (hNull) {
            case '<':
                if(tStat <= criticalVal) {
                    System.out.println("Fail to reject");
                } else {
                    System.out.println("Reject null; fieldA is greater than fieldB"); 
                }
                break;
            case '>':
                if(tStat >= criticalVal) {
                    System.out.println("Fail to reject");
                } else {
                    System.out.println("Reject null; fieldA is less than fieldB");
                }
                break;
            case '=':
                //Case where null just equal - CI thing? 
                //Just the same seemed to work too, idk if I need to run both
                //Probably? to determine the significance of the findings? 
                double[] ci = calculateCI();
                System.out.print("[" + ci[0]+", "+ci[1]+"] ");
                if(ci[0] >=0 && ci[1] <=0) {
                    System.out.println(" contains 0, therefore we fail to reject the null");
                } else {
                    System.out.println(" does not contain 0, therefore we reject the null");
                    System.out.println("There is a significant difference in means between the fields");
                }
                break;
            default:
                System.out.println("ERROR: H null choice a valid character");
                break;
        }
    }
    
}
