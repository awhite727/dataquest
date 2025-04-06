package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.apache.commons.math3.stat.inference.TTest;

//TODO: Make sure that it works multiple times in a row with different fields
//TODO: Add divide by 0 try/catch
public class MeanDiff {
        
    Field fieldA;
    Field fieldB; 
    //ArrayList<Double> valuesA;
    //ArrayList<Double> valuesB;
    double[] valuesA;
    double[] valuesB;

    TTest tTest = new TTest();

    private double meanA;
    private double sdA;
    private int nA;
    private double meanB;
    private double sdB;
    private int nB;
    private double difference; //(typically?) just do meanA - meanB - difference 
    private char hNull;
    private double alpha; 
    
    private double df;
    private double t;
    private double[] ci = new double[2];
    private double criticalValue;
    
    MeanDiff(Field fieldA, Field fieldB, double alpha, double difference, char hNull){
        this.fieldA = fieldA;
        ArrayList<Double>valuesA = fieldA.getValues();
        this.valuesA = new double[valuesA.size()];
        for (int i = 0; i < this.valuesA.length; i++) {
            this.valuesA[i] = valuesA.get(i);
        }
        this.fieldB = fieldB;
        ArrayList<Double>valuesB = fieldB.getValues();
        this.valuesB = new double[valuesB.size()];
        for (int i = 0; i < this.valuesB.length; i++) {
            this.valuesB[i] = valuesB.get(i);
        }
        meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        //sdA =  StatisticalSummary.getStandardDeviation(valuesA);
        sdA = StatisticalSummary.getSampleSD(valuesA);
        nA = StatisticalSummary.getCount(valuesA);
        //sdB = StatisticalSummary.getStandardDeviation(valuesB);
        sdB = StatisticalSummary.getSampleSD(valuesB);
        nB = StatisticalSummary.getCount(valuesB);
        System.out.println("MeanA: " + meanA);
        System.out.println("MeanB: " + meanB);
        System.out.println("VarA: " + Math.pow(sdA,2));
            System.out.println("\tsdA: " + sdA);
        System.out.println("VarB: " + Math.pow(sdB,2));
            System.out.println("\tsdB: " + sdB);
        System.out.println("nA: " + nA);
        System.out.println("nB: " + nB);


        this.alpha = alpha;

        this.hNull = hNull;
        this.difference = difference;
    }

    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }

//setters by type: Welch, Pooled, two-sample Z-test

    public void setWelch(){
        setWelchT();
        setWelchDF();
        setWelchCI();
    }
    public void setPooled(){
        setPooledT();
        setPooledDF();
        setPooledCI();
    }

    public void setZ(){
        /* Formula z: https://www.statology.org/two-sample-z-test/
         * CI: https://www.statskingdom.com/difference-confidence-interval-calculator.html 
         * SE = sqrt(oA^2/nA + oB^2/nB)
         * z = (meanA - meanB) / SE
         * criticalValue = StatisticalSummary.getZStar(alpha)
         * CI = meanDiff +- criticalValue*SE
         */

        double meanDiff = meanA - meanB;
        double sqrtPart = Math.sqrt(Math.pow(sdA,2)/nA + Math.pow(sdB,2)/nB);
        t = round(meanDiff/sqrtPart);

        criticalValue = StatisticalSummary.getZStar(alpha);
        ci[0] = round(meanDiff - criticalValue*sqrtPart);
        ci[1] = round(meanDiff + criticalValue*sqrtPart);
    }
    

//Welch sub-setters
    /* //NOTE: Alpha can't be above .5
    public double getPVal(){
        return tTest.tTest(valuesA, valuesB);
    } */

    //TODO: Allow checks with significance level, i.e. A-B < 10
    public void setWelchT(){
        t = round(tTest.t(valuesA, valuesB));
    }

    public void setWelchDF(){
        /* Formula:
         * (sdA^2/nA + sdB^2/nB)^2
         * /
         * (
         *  (sdA^4/(nA^2*(nA-1))
         * +
         *  (sdB^4/(nB^2*(nB-1))
         * )
        */
        //TODO: add divide by zero protection
        double numerator = Math.pow(sdA,2)/nA + Math.pow(sdB,2)/nB;
        numerator = Math.pow(numerator,2);
        double denomA = Math.pow(sdA,4)/(Math.pow(nA,2)*(nA-1));
        double denomB = Math.pow(sdB,4)/(Math.pow(nB,2)*(nB-1));
        double finalNum;
        try {
/*             System.out.println("Num: " + numerator);
            System.out.println("DenomA: " + denomA);
                System.out.println("\tnA -1: " + (nA-1));
            System.out.println("DenomB: " + denomB);
                System.out.println("\tnB -1: " + (nB-1));
 */
            finalNum = numerator/(denomA + denomB); 
            System.out.println(finalNum);
            df = round(finalNum); 
        } catch(Exception e) {
            e.printStackTrace();
            df = -1;
        }
    }

    public void setWelchCI(){
        /* Formula: https://online.stat.psu.edu/stat415/lesson/3/3.2
         * avgA - avgB +- t(alpha,df) * sqrt(sdA^2/nA + sdB^2/nB)
         * 
         */
        //double[] range = new double[2];
        criticalValue = StatisticalSummary.getTStar(alpha, df);
        double xbarDiff = round(meanA - meanB);
        double sqrtInterior = round(Math.pow(sdA,2)/nA) + round(Math.pow(sdB,2)/nB);
        double tPart = t * Math.sqrt(sqrtInterior);
        ci[0] = round(xbarDiff - tPart);
        ci[1] = round(xbarDiff + tPart);
    }
//End of welch sub-setters

//Pooled sub-setters
    private void setPooledT() {
        t = round(tTest.homoscedasticT(valuesA, valuesB));
    }

    private void setPooledDF() {
        df = nA + nB -2;
    }

    private void setPooledCI() {
        /* Formula: https://online.stat.psu.edu/stat415/lesson/3/3.1
         * (avgA - avgB) +- criticalVal * sqrt(pooledV *(1/nA + 1/nB))
         * 
         * pooledV = (((nA -1)*sdA^2) + ((nB - 1) * sdB^2))
         * /
         * df
         */
        double meanDiff = meanA - meanB;
        criticalValue = round(StatisticalSummary.getTStar(alpha, df));
        double pooledV = round((((nA-1)*Math.pow(sdA, 2)) + ((nB-1)*Math.pow(sdB,2))) 
                        /df);
        double sqrtInterior = pooledV* (1.0/nA + 1.0/nB);
        System.out.println("meanDiff: " + meanDiff);
        System.out.println("Pooled Sample Variance: " + pooledV);
        System.out.println("sqrtInterior: " + sqrtInterior);
        ci[0] = round(meanDiff - criticalValue*Math.sqrt(sqrtInterior));
        ci[1] = round(meanDiff + criticalValue*Math.sqrt(sqrtInterior));
    }
//End of pooled sub-setters

    //returns string with the results and explanation of what it means
    public String printTestWordy() {
        String result = "";
        //double[] muRange;
        boolean rejectNull; 
        //criticalValue = round(StatisticalSummary.getTStar(alpha, df));
        //double pval = round(getPVal());
        //System.out.println("CriticalVal: " + criticalVal);
        //System.out.println("WelchDF: " + df);



        //Difference in means
        //The true difference in means between fieldA and FieldB is estimated to be between [x,y] with Z% Confidence.
        result += "The true difference in means between ";
        result += fieldA.getName();
        result += " and ";
        result += fieldB.getName(); 
        /* try {
            muRange = ci;
        } catch (Exception e) {
            result += " could not be found do to an unexpected error";
            e.printStackTrace();
            return result;
        } */
        result += " is estimated to be between [";
        result += ci[0];
        result += ", ";
        result += ci[1];
        result += "] with ";
        result += ((1-alpha)*100);
        result += "% Confidence, where alpha is ";
        result += alpha;
        result += " and df is ";
        result += df;
        result += ". ";
        
        
        //Using a {Welch two-sample t-test} results in a t-value of 
        result += "\nUsing a ";
        result += "two-sample t-test "; //TODO: Replace with method getting the type
        result += "results in a t-value of "; 
        result += t;
        result += ". |";
        result += t;
        result +="| compared to a critical value of "; //p-value of ";
        result += criticalValue;
        result +=" finds that "; 
        switch(hNull) {
            case '<':
                
                break;
            case '>':
                break;
            case '=':
                break;
            default:
                System.out.println("ERROR: Not a valid H0");
        }

        result += "{t-value ><= critical value}";
        result += " therefore we {fail to }reject the null"; //TODO: add check 

        System.out.println(result);
        return result;
    }    
}
