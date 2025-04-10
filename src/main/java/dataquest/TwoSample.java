package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;



//TODO: Make sure that it works multiple times in a row with different fields
//TODO: Add divide by 0 try/catch
//TODO: Finish/test Z
//TODO: Finish paired
//NOTE: Name not great; includes one sample t-test too but includes two-sample z-test so can't be called t-test 

public class TwoSample {
        
    Field fieldA;
    Field fieldB; 
    //ArrayList<Double> valuesA;
    //ArrayList<Double> valuesB;
    double[] valuesA;
    double[] valuesB;

    TTest tTest = new TTest();

    private double meanA;
    //private double sdA;
    //private int nA;
    private double meanB;
    //private double sdB;
    //private int nB;
    StatisticalSummaryValues statsA; 
    StatisticalSummaryValues statsB; //NOTE: The mean stored in statsB is the mean+difference
    private double difference; //(typically?) just do meanA - meanB - difference 
    private char hNull;
    private double alpha; 
    
    private double df;
    private double testStat;
    private double[] ci = new double[2];
    private double criticalValue;
    private double p;
    
    //TODO: Add new constructors for custom sds; one field; one field and custom sds
    TwoSample(Field fieldA, Field fieldB, double alpha, double difference, char hNull){
        this.fieldA = fieldA;
        ArrayList<Double>valuesA = fieldA.getValues(); //TODO: handle differently for paired sample 
        this.valuesA = new double[valuesA.size()];
        for (int i = 0; i < this.valuesA.length; i++) {
            this.valuesA[i] = valuesA.get(i);
        }
        this.fieldB = fieldB;
        ArrayList<Double>valuesB = fieldB.getValues(); //handle differently for paired sample
        this.valuesB = new double[valuesB.size()];
        for (int i = 0; i < this.valuesB.length; i++) {
            this.valuesB[i] = valuesB.get(i);
        }
        //StatisticalSummaryValues(double mean, double variance, long n, double max, double min, double sum) 
        meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        this.difference = difference;
        /* sdA = StatisticalSummary.getSampleSD(valuesA);
        nA = StatisticalSummary.getCount(valuesA);
        sdB = StatisticalSummary.getSampleSD(valuesB);
        nB = StatisticalSummary.getCount(valuesB); */
        statsA = new StatisticalSummaryValues(meanA, Math.pow(StatisticalSummary.getSampleSD(valuesA),2), StatisticalSummary.getCount(valuesA),StatisticalSummary.getMax(valuesA),StatisticalSummary.getMin(valuesA),StatisticalSummary.getSum(valuesA));
        statsB = new StatisticalSummaryValues(meanB+difference, Math.pow(StatisticalSummary.getSampleSD(valuesB),2), StatisticalSummary.getCount(valuesB),StatisticalSummary.getMax(valuesB),StatisticalSummary.getMin(valuesB),StatisticalSummary.getSum(valuesB));

        System.out.println("MeanA: " + meanA);
            System.out.println("\tstatsA meanA: " + statsA.getMean());
        System.out.println("MeanB: " + meanB);
            System.out.println("\tMeanB+difference: " + statsB.getMean());
        System.out.println("VarA: " + statsA.getVariance());
            System.out.println("\tsdA: " + statsA.getStandardDeviation());
        System.out.println("VarB: " + statsB.getVariance());
            System.out.println("\tsdB: " + statsB.getStandardDeviation());
        System.out.println("nA: " + statsA.getN());
        System.out.println("nB: " + statsB.getN());


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
        setWelchStat();
        setWelchDF();
        setWelchCI();
    }
    public void setPooled(){
        setPooledStat();
        setPooledDF();
        setPooledCI();
    }

    //TODO: Make users input the population SDs (currently just assumes it's equal to the sample sd, which is an inaccurate assumption. If the samples are the whole populations, there's no need for a test) 
    public void setZ(){
        /* Formula z: https://www.statology.org/two-sample-z-test/
         * CI: https://www.statskingdom.com/difference-confidence-interval-calculator.html 
         * SE = sqrt(oA^2/nA + oB^2/nB)
         * z = (meanA - meanB) / SE
         * criticalValue = StatisticalSummary.getZStar(alpha)
         * CI = meanDiff +- criticalValue*SE
         */

        double meanDiff = statsA.getMean() - statsB.getMean(); //NOTE: Different for CI and stat
        double sqrtPart = Math.sqrt(statsA.getVariance()/statsA.getN() + statsB.getVariance()/statsB.getN());
        testStat = round(meanDiff/sqrtPart);

        criticalValue = StatisticalSummary.getZStar(alpha);
        meanDiff = meanA - meanB;
        ci[0] = round(meanDiff - criticalValue*sqrtPart);
        ci[1] = round(meanDiff + criticalValue*sqrtPart);
    }
    
    public void setPaired(){
        ArrayList<Double> paired = alignPaired();
        setPairedStat(paired);
        setPairedDF();
        setPairedCI(paired);
    }

    //uses difference as mu in constructor
    public void setOneSampleT(){
        /* Formula: https://www.uah.edu/images/administrative/student-success-center/resources/handouts/handouts_2019/business_stats_test_statistics_and_critical_value.pdf
         * t = (xbar - mu)
         *      /
         *      (sd/sqrt(n))
         */
        testStat = tTest.t(difference, statsA);
        p = round(tTest.tTest(difference,statsA));
        criticalValue = round(StatisticalSummary.getTStar(alpha, statsA.getN()));
        double moe = criticalValue * (statsA.getStandardDeviation()/Math.sqrt(statsA.getN()));
        df = statsA.getN()-1;
        ci[0] = round(meanA - moe);
        ci[1] = round(meanA + moe);
    }

    //TODO: Allow checks with significance level, i.e. A-B < 10
    public void setWelchStat(){
        testStat = round(tTest.t(statsA, statsB));
        p = round(tTest.tTest(statsA, statsB));
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
        double numerator = statsA.getVariance()/statsA.getN() + statsB.getVariance()/statsB.getN();
        numerator = Math.pow(numerator,2);
        double denomA = Math.pow(statsA.getVariance(),2)/(Math.pow(statsA.getN(),2)*(statsA.getN()-1));
        double denomB = Math.pow(statsB.getVariance(),2)/(Math.pow(statsB.getN(),2)*(statsB.getN()-1));
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
         * meanA - meanB +- t(alpha,df) * sqrt(sdA^2/nA + sdB^2/nB)
         * 
         */
        //double[] range = new double[2];
        criticalValue = StatisticalSummary.getTStar(alpha, df);
        double xbarDiff = round(meanA - meanB);
        double sqrtInterior = round(statsA.getVariance()/statsA.getN()) + round(statsB.getVariance()/statsB.getN());
        //System.out.println("CI ")
        double tPart = criticalValue * Math.sqrt(sqrtInterior);
        ci[0] = round(xbarDiff - tPart);
        ci[1] = round(xbarDiff + tPart);
    }
//End of welch sub-setters

//Pooled sub-setters
    private void setPooledStat() {
        testStat = round(tTest.homoscedasticT(statsA, statsB));
        p = round(tTest.homoscedasticTTest(statsA,statsB));
    }

    private void setPooledDF() {
        df = statsA.getN() + statsB.getN() -2;
    }

    private void setPooledCI() {
        /* Formula: https://online.stat.psu.edu/stat415/lesson/3/3.1
         * (meanA - meanB) +- criticalVal * sqrt(pooledV *(1/nA + 1/nB))
         * 
         * pooledV = (((nA -1)*sdA^2) + ((nB - 1) * sdB^2))
         * /
         * df
         */
        double meanDiff = meanA - meanB;
        criticalValue = round(StatisticalSummary.getTStar(alpha, df));
        double pooledV = round((((statsA.getN()-1)*statsA.getVariance()) + ((statsB.getN()-1)*statsB.getVariance())) 
                        /df);
        double sqrtInterior = pooledV* (1.0/statsA.getN() + 1.0/statsB.getN());
        System.out.println("meanDiff: " + meanDiff);
        System.out.println("Pooled Sample Variance: " + pooledV);
        System.out.println("sqrtInterior: " + sqrtInterior);
        ci[0] = round(meanDiff - criticalValue*Math.sqrt(sqrtInterior));
        ci[1] = round(meanDiff + criticalValue*Math.sqrt(sqrtInterior));
    }
//End of pooled sub-setters

//paired sub-setters

//Get the full arraylists then if either is null skip for both
//set valuesA and valuesB to the corrected versions and return ArrayList<Double> of the difference of each x
    //NOTE: currently uses obA as "after" and obB as "before" if it's that kind of relationship
    //Just switch obA and obB in paired if need other way around
    private ArrayList<Double> alignPaired(){
        ArrayList<Object> objectsA = fieldA.getTypedArray();
        ArrayList<Object> objectsB = fieldB.getTypedArray();
        ArrayList<Double> valuesA = new ArrayList<>();
        ArrayList<Double> valuesB = new ArrayList<>();
        ArrayList<Double> paired = new ArrayList<>();
        //Made sure all are floats to get to this point, so just removing both where one is null to keep aligned
        for (int i=0; i<objectsA.size(); i++) { //if valuesA.size() smaller than valuesB, the rest would be null
            // removes nulls from both as well as handles errors
            // if v is null, v will not be added
            Object obA = objectsA.get(i);
            Object obB = objectsB.get(i);
            try {
                if (obA instanceof Number && obB instanceof Number) {     
                    valuesA.add(((Number) obA).doubleValue());
                    valuesB.add(((Number) obB).doubleValue());
                    paired.add(((Number) obA).doubleValue() - ((Number) obB).doubleValue());
                }
            } catch (IndexOutOfBoundsException e) {
                break; //if valuesB.size() smaller than valuesA the rest would be null
            }
        }
        
        this.valuesA = valuesA.stream().mapToDouble(a -> a).toArray(); 
        this.valuesB = valuesB.stream().mapToDouble(b -> b).toArray(); 
        return paired;
    }
    private void setPairedStat(ArrayList<Double> paired) {
        /* Formula: https://sites.msudenver.edu/ngrevsta/wp-content/uploads/sites/416/2020/01/3240chapter9.pdf
         * t = (diffBar - difference)/sdMeanDiff
         * sdmeanDiff = diffSD/sqrt(n)
         */
        //testStat = tTest.pairedT(valuesA, valuesB); 
        double diffBar = StatisticalSummary.getMean(paired);
        double diffSD = StatisticalSummary.getSampleSD(paired);
        testStat = (diffBar - difference)/(diffSD/Math.sqrt(paired.size()));
        p = tTest.pairedTTest(valuesA, valuesB);
    }

    private void setPairedDF() {
        df = statsA.getN()-1;
    }

    private void setPairedCI(ArrayList<Double> paired) {
        /* Formula: https://sites.msudenver.edu/ngrevsta/wp-content/uploads/sites/416/2020/01/3240chapter9.pdf (page 14, 12)
         * diffBar -+ t(alpha,df(n-1))*sdMeanDiff
         * sdmeanDiff = diffSD/sqrt(n)
         */
        
        double diffBar = StatisticalSummary.getMean(paired);
        double diffSD = StatisticalSummary.getSampleSD(paired);
        criticalValue = StatisticalSummary.getTStar(alpha, paired.size());
        ci[0] = round(diffBar - criticalValue*(diffSD/Math.sqrt(paired.size())));
        ci[1] = round(diffBar + criticalValue*(diffSD/Math.sqrt(paired.size())));
    }

//End of paired sub-setters



//TODO: Check if t or z test to include or leave out the df
//TODO: Separate and complete oneSampleT
    public String printBasic(){
        String result = ""; 
        result += "mean " + fieldA.getName()+ " = " + round(meanA);
        result += "\nmean " + fieldB.getName() + " = " + round(meanB);
        result += "\nd = " + difference; //TODO: What to call it? 
        result += "\nalpha = " + alpha;
        result += "\nTest: ";
        result += "[fill]"; 
        //if t
        result += "\n  t = "; 
        result += round(testStat);
        result += "\n  df = ";
        result += round(df);
        //else z = round(testStat)
        result += "\n  Critical Value: " + round(criticalValue);
        result +="\n  P-Value = ";
        result += round(p);
        result += "\n  CI: ";
        result += Arrays.toString(ci);
        result += "\nConclusion: ";
        result += "[fill]";

        return result;
    }
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
        result += testStat;
        result += ". |";
        result += testStat;
        result +="| compared to a critical value of "; //p-value of ";
        result += round(criticalValue); //NOTE: if |testStat| > criticalValue, ALWAYS outside of acceptance region
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
