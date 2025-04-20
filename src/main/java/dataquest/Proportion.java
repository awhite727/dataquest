package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

public class Proportion {
    private Field fieldA;
    private Field fieldB;
    private ArrayList<Double> valuesA;
    private ArrayList<Double> valuesB;
    private int nA;
    private int nB;
    private double successA; //defines value to compare to for pHat
    private double successB;
    private double pNull;  
    private double z;
    private double p;
    private double criticalValue;
    private double[] ci = new double[2];
    private int xA;
    private int xB;
    private double pHatA;
    private double pHatB; 
    private double alpha;
    private Direction direction;
    private Direction successADirection;
    private Direction successBDirection;
    private String testType = "";

    Proportion(Field fieldA, double success,Direction successADirection, double alpha, double pNull, Direction direction){
        this.fieldA = fieldA;
        this.valuesA = fieldA.getValues();
        this.pNull = pNull;
        this.nA = StatisticalSummary.getCount(valuesA);
        this.direction = direction;
        this.successA = success;
        this.alpha = alpha;
        this.successADirection = successADirection;
    }

    Proportion(Field fieldA, Field fieldB, double successA, Direction successADirection, double successB, Direction successBDirection, double alpha, Direction direction){
        this.fieldA = fieldA;
        this.fieldB = fieldB;
        valuesA = fieldA.getValues();
        valuesB = fieldB.getValues();
        //this.pNull = pNull;
        nA = StatisticalSummary.getCount(valuesA);
        nB = StatisticalSummary.getCount(valuesB);
        this.direction = direction;
        this.successA = successA;
        this.successB = successB;
        this.alpha = alpha; 
        this.successADirection = successADirection;
        this.successBDirection = successBDirection;

    }

    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }


    public void setOneProportion(){
        testType = "One Proportion Z-Test";
        setOneSamplePHat();
        setOnePropStat();
        setOnePropCI();
    }
    
    public void setTwoProportion(){
        testType = "Two Proportion Z-Test";
        setTwoSamplePHat();
        setTwoPropStat();
        setTwoPropCI();
    }

//Start of one-prop sub-setters

    private void setOneSamplePHat() {
        /* Formula: https://stats.libretexts.org/Bookshelves/Introductory_Statistics/Introductory_Statistics_(Shafer_and_Zhang)/06%3A_Sampling_Distributions/6.03%3A_The_Sample_Proportion
        * pHat = numPass/n -> numPass based on user-set success val  // Wrong -> pNull; pNull user-set
        */
        //NOTE: very slightly faster not to check every time, just makes code less messy 
        xA = 0;
        for (double d : valuesA) {
            switch(successADirection){
                case LESS_THAN:
                    if(d < successA) xA++;
                    continue;
                case GREATER_THAN:
                    if(d > successA) xA++;
                    continue;
                case EQUAL:
                    if(d == successA) xA++;
                    continue;
                default: 
                    System.out.println("ERROR: Unexpected error checking the direction of sample A");
                    break;
            }
        }
        pHatA = ((double)xA)/nA;
        
    }

    private void setOnePropStat() {
        /* Formula: https://www.uah.edu/images/administrative/student-success-center/resources/handouts/handouts_2019/business_stats_test_statistics_and_critical_value.pdf
         * z = (pHat - pNull)
         * /
         * sqrt(
         *      (pNull*(1-pNull))
         *          /n
         *      )
         * 
         */
        double denom = Math.sqrt((pNull*(1-pNull))/nA);
        z = (pHatA - pNull)/denom;
        p = StatisticalSummary.getPValue(z);
        switch (direction) {
            case LESS_THAN:
                //p /= 2;
                p = 1-p;
                criticalValue = -StatisticalSummary.getZStar(alpha*2);
                break;
            case GREATER_THAN:
                //p/=2;
                criticalValue = StatisticalSummary.getZStar(alpha*2);
                break;
            default: //EQUAL
                //p = 1 - p;
                p*=2;
                criticalValue = StatisticalSummary.getZStar(alpha);
                break;
        }
        p = round(p);
    }

    private void setOnePropCI() {
        /* Formula: https://stats.libretexts.org/Bookshelves/Introductory_Statistics/Introductory_Statistics_(Shafer_and_Zhang)/06%3A_Sampling_Distributions/6.03%3A_The_Sample_Proportion
         * pHat -+ crit*sqrt((pHat(1-pHatA))/n)
         */
        double criticalValue = StatisticalSummary.getZStar(alpha); //Needs to be the full version regardless of direction
        double sqrtPart = round(criticalValue* Math.sqrt((pHatA*(1-pHatA))/nA));
        ci[0] = round(pHatA - sqrtPart);
        ci[1] = round(pHatA + sqrtPart); 
    }
//End of one-prop sub-setters

//start of two-prop sub-setters

    //sets pHatA and pHatB
    //returns pHat of both 
    private double setTwoSamplePHat() {
        //NOTE: very slightly faster not to check every time, just makes code less messy 
        xA = 0;
        xB = 0;
        for (double d : valuesA) {
            switch(successADirection){
                case LESS_THAN:
                    if(d < successA) xA++;
                    continue;
                case GREATER_THAN:
                    if(d > successA) xA++;
                    continue;
                case EQUAL:
                    if(d == successA) xA++;
                    continue;
                default: 
                    System.out.println("ERROR: Unexpected error checking the direction of porportion test");
                    break;
            }
        }
        pHatA = ((double)xA)/nA;
        
        //NOTE: very slightly faster not to check every time, just makes code less messy 
        for (double d : valuesB) {
            switch(successBDirection){
                case LESS_THAN:
                    if(d < successB) xB++;
                    continue;
                case GREATER_THAN:
                    if(d > successB) xB++;
                    continue;
                case EQUAL:
                    if(d == successB) xB++;
                    continue;
                default: 
                    System.out.println("ERROR: Unexpected error checking the direction of porportion test");
                    break;
            }
        }
        pHatB = ((double)xB)/nB;
        return ((double)xA+xB)/(nA + nB);
    }

    private void setTwoPropStat() {
        /* OLD Formula: https://www.uah.edu/images/administrative/student-success-center/resources/handouts/handouts_2019/business_stats_test_statistics_and_critical_value.pdf
         * Formula: https://www.statisticshowto.com/probability-and-statistics/hypothesis-testing/z-test/
            * z = (pHatA - pHatB - 0)
            * /
            * sqrt(pHat(1-pHat) * (1/nA + 1/nB))
            * 
            */
        double pHat = setTwoSamplePHat();
        double denom = Math.sqrt((pHat*(1-pHat)) * (1.0/nA + 1.0/nB));
        //System.out.println("Denom: " + round(denom));
        z = (pHatA - pHatB)/denom;
        p = StatisticalSummary.getPValue(z);
        switch (direction) {
            case LESS_THAN:
                p = 1-p;
                criticalValue = -StatisticalSummary.getZStar(alpha*2);
                break;
            case GREATER_THAN:
                criticalValue = StatisticalSummary.getZStar(alpha*2);
                break;
            default: //EQUAL
                p*=2;
                criticalValue = StatisticalSummary.getZStar(alpha);
                break;
        }
        p = round(p);
    }

    private void setTwoPropCI() {
        /* Formula: https://www.statisticshowto.com/probability-and-statistics/hypothesis-testing/z-test/
         * (pHatA - pHatB) +- z* sqrt(
         *          (pHatA(1-pHatA))/nA) + (pHatB(1-pHatB))/nB)
         *          )
         */

        double meanDiff = pHatA - pHatB;
        //double zStar = StatisticalSummary.getZStar(alpha);
        System.out.println("MeanDiff: " + meanDiff);
        System.out.println("\tpHatA: " +pHatA);
        System.out.println("\tpHatB: " +pHatB);
        System.out.println("Z-star: " + round(criticalValue));
        double sqrtPart = Math.sqrt((pHatA*(1-pHatA)/nA) + (pHatB*(1-pHatB)/nB));
        System.out.println("Sqrt: " + sqrtPart);
        System.out.println("z*Sqrt: " + (criticalValue*sqrtPart));


        ci[0] = round(meanDiff - (Math.abs(criticalValue)*sqrtPart));
        ci[1] = round(meanDiff + (Math.abs(criticalValue)*sqrtPart));
    }
//end of two-prop sub-setters

    private String getConclusion(){
        String conclusion = "Conclusion: \n\tp";
        if(p < alpha) {
            conclusion += " < " + alpha;
            conclusion += "\n\tReject the null";
        } else {
            conclusion += " \u2265 " + alpha;
            conclusion += "\n\t Failed to reject the null";
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
                    return "p > p\u2080"; //Less/equal?
                case GREATER_THAN:
                    return "p < p\u2080";
                case EQUAL:
                    return "p \u2260 p\u2080";
                default:
                    break;
            }
        } else {
            switch (direction) {
                case LESS_THAN:
                    return "p\u2081 > p\u2082";
                case GREATER_THAN:
                    return "p\u2081 < p\u2082";
                case EQUAL:
                    return "p\u2081 \u2260 p\u2082";
            }
        }
        return null;
    }

    public String printBasic(){
        String result = "Test: " + testType + "\n";
        
        switch (successADirection) {
            case LESS_THAN:
                result += "Success for " + fieldA.getName() + ": x < " + successA;
                break;
            case GREATER_THAN:
                result += "Success for " + fieldA.getName() + ": x > " + successA;
                break;
            case EQUAL:
                result += "Success for " + fieldA.getName() + ": x = " + successA;
                break;        
            default:
                break;
        }
        result += "\n\t\u0070\u0302 = " + round(pHatA) + " (" + xA + "/" + nA + ")\n"; 
        if(testType.equals("One Proportion Z-Test")) {
            result += "P\u2080 = " + pNull;
        } else {
            switch (successBDirection) {
                case LESS_THAN:
                    result += "Success for " + fieldB.getName() + ": x < " + successB;
                    break;
                case GREATER_THAN:
                    result += "Success for " + fieldB.getName() + ": x > " + successB;
                    break;
                case EQUAL:
                    result += "Success for " + fieldB.getName() + ": x = " + successB;
                    break;        
                default:
                    break;
            }
            result += "\n\t\u0070\u0302 = " + round(pHatB) + " (" + xB + "/" + nB + ")";  
        }
        
        if(testType.equals("One Proportion Z-Test"))
        result += "\n\u03B1 = " + alpha;
        //Null and alt hypothesis 
        result += "\nHypothesis: ";
        result += "\n  H\u2080: " + getNullHypothesis();
        result += "\n  H\u2081: " + getAltHypothesis();
        result += "\nResults: ";
        result += "\n  z = "; 
        try {
            result += round(z);
        } catch (NumberFormatException e) { //if infinity
            result += z;
        }
        if(direction.equals(Direction.EQUAL)) {
            result += "\n  CI: ";
            result += Arrays.toString(ci);
        } 
        result +="\n  P-Value = "; 
        result += String.format("%,.5f", p);  
        result += "\n" + getConclusion();

        return result;
    }
}
