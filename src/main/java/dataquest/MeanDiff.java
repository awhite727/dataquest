package dataquest;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;

//TODO: Make sure that it works multiple times in a row with different fields
//TODO: Add divide by 0 try/catch
public class MeanDiff extends Visualization {
    Field fieldA;
    Field fieldB; 
    ArrayList<Double> valuesA;
    ArrayList<Double> valuesB;
    String tail;

    private final double meanA;
    private final double sdA;
    private final int nA;
    private final double meanB;
    private final double sdB;
    private final int nB;

    MeanDiff(){        
        super("title", new Color[1]);
        meanA = 98.105;
        sdA = 0.699;
        nA = 65;
        meanB = 98.394;
        sdB = 0.743;
        nB = 65;
    };
    MeanDiff(String title, Color[] colors,Field fieldA, Field fieldB, String tail) {
        super(title, colors);
        this.fieldA = fieldA;
        valuesA = fieldA.getValues();
        this.fieldB = fieldB;
        valuesB = fieldB.getValues();
        this.tail = tail;
        meanA = StatisticalSummary.getMean(valuesA);
        meanB = StatisticalSummary.getMean(valuesB);
        sdA =  StatisticalSummary.getStandardDeviation(valuesA);
        nA = StatisticalSummary.getCount(valuesA);
        sdB = StatisticalSummary.getStandardDeviation(valuesB);
        nB = StatisticalSummary.getCount(valuesB);
    }

    public double calculateWelchDF(){
        /* Formula: df = 
         * (sdA^2/nA + sdB^2/nB)^2
         * /
         * (
         *  sdA^4/(nA^2*(nA-1))
         * + sdB^4/(nB^2*(nB-1))
         * )
         */
        double numerator = Math.pow((Math.pow(sdA,2)/nA) + (Math.pow(sdB,2)/nB),2);
        double denomA = Math.pow(sdA,4)/(Math.pow(nA,2)*(nA-1));
        double denomB = Math.pow(sdB,4)/(Math.pow(nB,2)*(nB-1));
        double df = numerator/(denomA + denomB);
        return df;
    }

    public double getDifference(){
        return (meanA-meanB);
    }

    public double[] calculateMuCI(double t) {
        /* Formula: 
         * (avgA - avgB) +- t* sqrt(
         *                      (sdA^2/nA) + (sdB^2/nB)
         *                      )
         */
        double[] range = new double[2];
        
        double xbarDiff = meanA - meanB;
        double sqrtInterior = (Math.pow(sdA,2)/nA) + (Math.pow(sdB,2)/nB);
        double tPart = t * Math.sqrt(sqrtInterior);
        range[0] = xbarDiff - tPart;
        range[1] = xbarDiff + tPart;

        return range;
    }

    //TODO: Make sure formula understood and correct
    public double calculateTtest(double alpha, int n) {
        /* Formula: 
         * ((avgA - avgB)                                   //- (muA - muB))
         * /
         * sqrt(
         *      sdA^2/nA + sdB^2/nB
         * )
         */

        double xbarDiff = meanA - meanB;
        //double t = StatisticalSummary.getTStar(alpha, n);
        //double[] muDiffRange = calculateMuCI(t);//TODO: CHECK IF CORRECT
        double sqrtPart = Math.sqrt(
                            (Math.pow(sdA,2)/nA) + (Math.pow(sdB,2)/nB)
                        );
        //double[] tTestResult = new double[2];
        //tTestResult[0] = (xbarDiff - muDiffRange[0])/sqrtPart;
        //tTestResult[1] = (xbarDiff - muDiffRange[1])/sqrtPart;
        double tVal = xbarDiff/sqrtPart;
        System.out.printf("tVal: %.3f", tVal);
        return tVal;
    }

    //returns string with the results and explanation of what it means
    public String printTestWordy(double alpha, int n) {
        String result = "";
        double[] muRange;
        double tTest; 
        double pVal = StatisticalSummary.getTStar(alpha, 64);

        //Difference in means
        result += "The true difference in means between ";
        //result += fieldA.getName();
        result += " and ";
        //result += fieldB.getName(); 
        try {
            muRange = calculateMuCI(pVal);
            tTest = calculateTtest(alpha, n);
        } catch (Exception e) {
            result += " could not be found do to an unexpected error";
            return result;
        }
        result += " is estimated to be between [";
        result += muRange[0];
        result += ", ";
        result += muRange[1];
        result += "] with ";
        result += ((1-alpha)*100);
        result += "% Confidence. As the difference in X-bars is ";
        double difference = getDifference();
        result += difference;
        result += " and this is "; 
        
        if(!((difference >= muRange[0]) && (difference <= muRange[1]))) {
            result += " not ";
        }

        
        
        
        result += "\n";
        result += "A two-sample t-test results in a t-value of "; 
        result += tTest;
        result += ". |";
        result += tTest;
        result +="| is "; 
        if (Math.abs(tTest) > pVal) {

            result += "greater than our critical value (";
            result += pVal;
            result += "). Therefore, we reject the null"; 
        } else {
            result += "not greater than our alpha (";
            result += alpha;
            result += "). Therefore, we fail to reject the null"; 
        }

        
        

        return result;
    }

    //use histogram to overlap two?

    @Override
    public JPanel createChart() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createChart'");
        
    }

    @Override
    public JPanel updateChart() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateChart'");
    }
}
