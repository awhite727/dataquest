package dataquest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

public class Proportion {
    Field fieldA;
    Field fieldB;
    ArrayList<Double> valuesA;
    ArrayList<Double> valuesB;
    int nA;
    int nB;
    double successA; //defines value to compare to for pHat
    double successB;
    double pNull;  
    double z;
    double p;
    double criticalValue;
    double[] ci = new double[2];
    int xA;
    int xB;
    double pHatA;
    double pHatB; 
    double alpha;
    Direction direction;
    
    public enum Direction {
        LESS_THAN,      //value < success       pHat < pNull
        GREATER_THAN,   //value > success
        EQUAL           //value == success
    }

    Proportion(Field fieldA, double success, double alpha, double pNull, Direction direction){
        this.fieldA = fieldA;
        valuesA = fieldA.getValues();
        this.pNull = pNull;
        nA = StatisticalSummary.getCount(valuesA);
        this.direction = direction;
        successA = success;
        this.alpha = alpha;
    }

    Proportion(Field fieldA, Field fieldB, double successA, double successB, double alpha, double pNull, Direction direction){
        this.fieldA = fieldA;
        this.fieldB = fieldB;
        valuesA = fieldA.getValues();
        valuesB = fieldB.getValues();
        this.pNull = pNull;
        nA = StatisticalSummary.getCount(valuesA);
        nB = StatisticalSummary.getCount(valuesB);
        this.direction = direction;
        this.successA = successA;
        this.successB = successB;
        this.alpha = alpha; 
    }

    //used because of inexactness of floats causing slight but noticeable differences in t tests 
    //Automatically does to 5 places
    private double round(double input){
        return BigDecimal.valueOf(input).setScale(5,RoundingMode.HALF_UP).doubleValue();
    }


    public void setOneProportion(){
        setOneSamplePHat();
        setOnePropStat();
        setOnePropCI();
    }
    
    public void setTwoProportion(){
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
            switch(direction){
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
        if(direction == Direction.EQUAL) 
            criticalValue = StatisticalSummary.getZStar(alpha);
        else if(direction == Direction.LESS_THAN)
            criticalValue = -StatisticalSummary.getZStar(alpha*2);
        else 
            criticalValue = StatisticalSummary.getZStar(alpha*2);
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
            switch(direction){
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
            switch(direction){
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
        //System.out.println("pHatA-pHatB: " + (pHatA-pHatB));
        //System.out.println("z: " + round(z));
        if(direction == Direction.EQUAL)
            criticalValue = StatisticalSummary.getZStar(alpha);
        else if(direction == Direction.LESS_THAN)
            criticalValue = -StatisticalSummary.getZStar(alpha*2);
        else
            criticalValue = StatisticalSummary.getZStar(alpha*2);

    }

    private void setTwoPropCI() {
        /* Formula: https://www.statisticshowto.com/probability-and-statistics/hypothesis-testing/z-test/
         * (pHatA - pHatB) +- z* sqrt(
         *          (pHatA(1-pHatA))/nA) + (pHatB(1-pHatB))/nB)
         *          )
         */

        //NOTE: site didn't include pNull in CI; I think that's correct but wanna note bc tired 
        double meanDiff = pHatA - pHatB;
        //double zStar = StatisticalSummary.getZStar(alpha);
        System.out.println("MeanDiff: " + meanDiff);
        System.out.println("\tpHatA: " +pHatA);
        System.out.println("\tpHatB: " +pHatB);
        System.out.println("Z-star: " + round(criticalValue));
        double sqrtPart = Math.sqrt((pHatA*(1-pHatA)/nA) + (pHatB*(1-pHatB)/nB));
        System.out.println("Sqrt: " + sqrtPart);
        System.out.println("z*Sqrt: " + (criticalValue*sqrtPart));


        ci[0] = round(meanDiff - (Math.abs(criticalValue)*sqrtPart)); //TODO: Check if left/right? calculator changes like [-1,-.4] for -.45 +- 0.015
        ci[1] = round(meanDiff + (Math.abs(criticalValue)*sqrtPart));
    }
//end of two-prop sub-setters

    public String printBasic(){
        String result = ""; 
        result += "Threshold " + fieldA.getName()+ ": " + successA;
        result += "\nThreshold " + fieldB.getName() + ": " + successB;
        result += "\nExpected: " + pNull; //TODO: What to call it? 
        result += "\nalpha = " + alpha;
        result += "\nTest: ";
        result += "[fill]"; 
        result += "\n  p^A = " + round(pHatA) + " (" + xA + "/" + nA + ")"; 
        result += "\n  p^B = " + round(pHatB) + " (" + xB + "/" + nB + ")"; 
        result += "\n  z = "; 
        try {
            result += round(z);
        } catch (NumberFormatException e) { //if infinity
            result += z;
        }
        result += "\n  Critical Value = " + round(criticalValue);
        result +="\n  P-Value = "; //NOTE: In the calculator, it multiplied P(x<=Z) by something (pNull?); should we? 
        result += String.format("%,.5f", p); //round(p); //For some reason was formatting as scino?? 
        System.out.printf("p: %.5f \t (p(x<=Z) = %.5f)", (p*pNull), p); //not the same for pregnancy, success = 2, alpha = 0.2, pNull = .2, Direction.LESS_THAN)
        result += "\n  CI: ";
        result += Arrays.toString(ci); // Normal approximation
        result += "\nConclusion: ";
        result += "[fill]";

        return result;
    }
}
