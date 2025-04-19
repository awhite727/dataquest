package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;



//TODO: Add divide by 0 try/catch
public class TwoSample {
        
    private Field fieldA;
    private Field fieldB; 
    private double[] valuesA;
    private double[] valuesB;
    private String testType = "";
    private TTest tTest = new TTest();
    private Direction direction; 
    private double meanA;
    private double meanB;
    private StatisticalSummaryValues statsA; 
    private StatisticalSummaryValues statsB; //NOTE: The mean stored in statsB is the mean+difference
    private double difference; //(typically?) just do meanA - meanB - difference 
    private double alpha; 
    
    private double df;
    private double testStat;
    private double[] ci = new double[2];
    private double criticalValue;
    private double p;
    
    //TODO: Add new constructors for custom sds; one field; one field and custom sds
    TwoSample(Field fieldA, Field fieldB, double alpha, double difference, Direction direction){
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
        meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        this.difference = difference;
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
        this.direction = direction;
    }

    //for pooled sample 
    TwoSample(Field fieldA, Field fieldB, double popSDA, double popSDB, double alpha, double difference, Direction direction){
        this.fieldA = fieldA;
        ArrayList<Double>valuesA = fieldA.getValues();
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
        meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        this.difference = difference;
        statsA = new StatisticalSummaryValues(meanA, Math.pow(popSDA,2), StatisticalSummary.getCount(valuesA),StatisticalSummary.getMax(valuesA),StatisticalSummary.getMin(valuesA),StatisticalSummary.getSum(valuesA));
        statsB = new StatisticalSummaryValues(meanB+difference, Math.pow(popSDB,2), StatisticalSummary.getCount(valuesB),StatisticalSummary.getMax(valuesB),StatisticalSummary.getMin(valuesB),StatisticalSummary.getSum(valuesB));

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

        this.direction = direction;
        this.difference = difference;
    }

    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }

//setters by type: Welch, Pooled, two-sample Z-test

    public void setWelch(){
        testType = "Welch Two-Sample T-Test";
        setWelchStat();
        setWelchDF();
        setWelchCI();
    }
    public void setPooled(){
        testType = "Pooled Two-Sample T-Test";
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
        testType = "Two-Sample Z-Test";
        double meanDiff = statsA.getMean() - statsB.getMean(); //NOTE: Different for CI and stat
        double sqrtPart = Math.sqrt(statsA.getVariance()/statsA.getN() + statsB.getVariance()/statsB.getN());
        testStat = round(meanDiff/sqrtPart);

        p = StatisticalSummary.getPValue(testStat);
        //NOTE: Handled differently because getPValue returns cummulative probability p(x<=T) 
        switch (direction) {
            case LESS_THAN:
                //p /= 2;
                p = 1-p;
                break;
            case GREATER_THAN:
                //p/=2;
                break;
            default: //EQUAL
                p = 1 - p;
                p*=2;
                break;
        }
        p = round(p);

        criticalValue = StatisticalSummary.getZStar(alpha);
        meanDiff = meanA - meanB;
        ci[0] = round(meanDiff - criticalValue*sqrtPart);
        ci[1] = round(meanDiff + criticalValue*sqrtPart);
    }
    
    public void setPaired(){
        testType = "Paired Sample T-Test";
        ArrayList<Double> paired = alignPaired();
        setPairedStat(paired);
        setPairedDF();
        setPairedCI(paired);
    }

    //TODO: Allow checks with significance level, i.e. A-B < 10
    private void setWelchStat(){
        testStat = round(tTest.t(statsA, statsB));
        p = tTest.tTest(statsA, statsB);
        switch (direction) {
            case GREATER_THAN:
                p /= 2;
                p = 1-p;
                break;
            case LESS_THAN:
                p/=2;
                break;
            default: //EQUAL
                break;
        }
        p = round(p);
    }

    private void setWelchDF(){
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

    private void setWelchCI(){
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
        p = tTest.homoscedasticTTest(statsA,statsB);
        switch (direction) {
            case GREATER_THAN:
                p /= 2;
                p = 1-p;
                break;
            case LESS_THAN:
                p/=2;
                break;
            default: //EQUAL
                break;
        }
        p = round(p);
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
        //testStat = tTest.pairedT(valuesA, valuesB);  //doesn't allow difference
        double diffBar = StatisticalSummary.getMean(paired);
        double diffSD = StatisticalSummary.getSampleSD(paired);
        testStat = (diffBar - difference)/(diffSD/Math.sqrt(paired.size()));
        p = tTest.pairedTTest(valuesA, valuesB);
        switch (direction) {
            case GREATER_THAN:
                p /= 2;
                p = 1-p;
                break;
            case LESS_THAN:
                p/=2;
                break;
            default: //EQUAL
                break;
        }
        p = round(p);
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

private String getConclusion(){
    String conclusion = "Conclusion: \n\tp";
    if(p < alpha) {
        conclusion += " < " + alpha;
        conclusion += "\n\tReject the null";
    } else {
        conclusion += " \u2265 " + alpha;
        conclusion += "\n\tFailed to reject the null";
    }
    return conclusion;
}

//helper method to make print basic less ugly
private String getNullHypothesis() {
    if(testType.equals("One Proportion Z-Test")) {
        switch (direction) {
            case LESS_THAN:
                return "p < p\u2080";
            case GREATER_THAN:
                return "p > p\u2080"; // \u2265 is >=
            case EQUAL:
                return "p = p\u2080";
            default:
                break;
        }
    } else {
        switch (direction) {
            case LESS_THAN:
                return "p\u2081 < p\u2082";
            case GREATER_THAN:
                return "p\u2081 > p\u2082";
            case EQUAL:
                return "p\u2081 = p\u2082";
        }
    }
    return null;
}

//helper method to make print basic less ugly
private String getAltHypothesis() {
    if(testType.equals("One Proportion Z-Test")) {
        switch (direction) {
            case LESS_THAN:
                return fieldA.getName() + " > " + difference;
            case GREATER_THAN:
            return fieldA.getName() + " < " + difference;
            case EQUAL:
            return fieldA.getName() + " = " + difference;
            default:
                break;
        }
    } else {
        switch (direction) {
            case LESS_THAN:
                return fieldA.getName() + " + " + fieldB.getName() + " > " + difference;
            case GREATER_THAN:
            return fieldA.getName() + " + " + fieldB.getName() + " < " + difference;
            case EQUAL:
            return fieldA.getName() + " + " + fieldB.getName() + " = " + difference;
        }
    }
    return null;
}

//TODO: add the null and alternative to conclusion
public String printBasic(){
        String result = "Test: " + testType; 
        result += "\nmean " + fieldA.getName()+ " = " + round(meanA);
        result += "\nmean " + fieldB.getName() + " = " + round(meanB);
        result += "\nd = " + difference; //TODO: What to call it? 
        result += "\n\u03B1 = " + alpha;
        //if t
        if(testType.equals("Two-Sample Z-Test")) {
            result += "\n  z = ";
            result += round(testStat);        
        } else {
            result += "\n  t = ";
            result += round(testStat);
            result += "\n  df = ";
            result += round(df);
        }
        result +="\n  P-Value = ";
        result += p;
        if(direction == Direction.EQUAL) {
            result += "\n  CI: ";
            result += Arrays.toString(ci);
        }
        result += "\n" + getConclusion();
        return result;
    }
}
