package dataquest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;



public class StatisticalSummary {
    /* public static void main(String[] args) {
        System.out.println(getTStar(0.1, 20,18));

    } */
    // gets t star for two-tailed t table
    // if one-tailed t table is needed, divide alpha by two before passing
    public static double getTStar(double alpha, int sampleSize) {
        if (alpha <= 0) {
            return 0;
        }
        int df = sampleSize - 1;
        double tStar = new TDistribution(df).inverseCumulativeProbability(1 - alpha / 2);
        return tStar;
    }

    public static double getTStar(double alpha, double df){
        if (alpha <= 0 || df <= 0) {
            return 0;
        }
        double tStar = new TDistribution(df).inverseCumulativeProbability(1 - alpha / 2);
        System.out.println("TStar: " + tStar);
        return tStar;
    }

    public static double getZStar(double alpha) {
        if (alpha <= 0) {
            return 0;
        }
        double zStar = new NormalDistribution().inverseCumulativeProbability(1- alpha / 2);
        return zStar;
    }

    public static double getPValue(double value) {
        return new NormalDistribution().cumulativeProbability(value);
    }

    public static double getPValue(double value, Direction direction) {
        double p = new NormalDistribution().cumulativeProbability(value);
        switch (direction) {
            case EQUAL:
                return p*2;
            case LESS_THAN:
                return p;
            case GREATER_THAN:
                return 1-p;
        }
        return -1;
    }

    // used by TDistribution visualization to get the points
   public static List<Double> calculateTDistributionPDF(int df, List<Double> xList) {
    TDistribution tDistribution = new TDistribution(df);
    List<Double> yList = new ArrayList<>();
    for (double x: xList) {
        yList.add(tDistribution.density(x));
    }
    return yList;
    
}

    public static double getMean(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static double getMedian(List<Double> data) {
        int size = data.size();
        List<Double> sortedData = data.stream().sorted().toList();
        if (size % 2 == 0) {
            return (sortedData.get(size / 2 - 1) + sortedData.get(size / 2)) / 2.0;
        } else {
            return sortedData.get(size / 2);
        }
    }

    public static double getStandardDeviation(List<Double> data) {
        double mean = getMean(data);
        double variance = data.stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum();
        variance = variance/(getCount(data) - 1);
        return Math.sqrt(variance);
    }

    // takes the fields for anova and outputs information about the anova test
    public static String getAnova(Field numerical, Field category) {
        if (numerical == null || category == null) {
            return "Error selecting fields.";
        }
        String [] levels = category.getLevels();
        Collection<double[]> dataSplit = new ArrayList<>();     // data split by category
        ArrayList<Object> dataArray = numerical.getTypedArray();
        for (String level : levels) {
            ArrayList<Integer> indexes = category.getIndexOfLevel(level);
            if (indexes.size() == 1) {
                String out = "All levels must have two or more values, " + level + " has 1 at line " + indexes.get(0);
                return out;
            }
            List<Double> dataLevel = new ArrayList<>();
            for(int i:indexes) {
                if (dataArray.get(i) instanceof Number number) {
                    dataLevel.add(number.doubleValue());
                }
            }
            double[] levelArray = new double[dataLevel.size()]; // needs to be array of double
            for (int i=0; i<levelArray.length;i++) {
                levelArray[i]=dataLevel.get(i);
            }
            dataSplit.add(levelArray);
        }

        // generate string output
        OneWayAnova model = new OneWayAnova();
        double fStat = model.anovaFValue(dataSplit);
        int dfR = levels.length - 1;
        int dfE = dataArray.size() - levels.length;
        double pValue = model.anovaPValue(dataSplit);
        StringBuilder output = new StringBuilder();
        output.append("\tOne Way ANOVA Test");
        output.append("\nComparing " + numerical.getName() +" by " + category.getName());
        output.append("\nLevels: \n   ");
        output.append(levels[0]);
        for (int i = 1; i< levels.length; i++) {
            output.append(", " + levels[i]);
        }
        output.append("\nF-Statistic: " + String.format("%.4f", fStat));
        output.append("\nDegrees of freedom: ");
        output.append("\n   Regression: " + dfR);
        output.append("    Error: " + dfE);
        output.append("\np-value: " + String.format("%.4f", pValue));
        return output.toString();
    }

    // takes the fields of the linear regression and outputs a string of information about the model
    public static String getLinearRegression(Field target, Field[] parameters) {
        if (target == null || parameters == null || parameters.length ==0) {
            return "There was a problem computing the linear regression";
        }
        Field[] variables = new Field[parameters.length + 1];
        variables[0] = target;
        for (int i=1; i< variables.length; i++) {
            variables[i] = parameters[i-1];
        }
        ArrayList<ArrayList<Double>> values = Dataset.matchFields(variables);
        if (values.get(0).size() <= 1) {
            return null;//return "Too many missing values to compute linear regression.";
        }
        ArrayList<Double> depVar = values.get(0);
        values.remove(0); // gives dependent variables
        // simple linear regression output
        if (values.size() == 1) {
            ArrayList<Double> indVar = values.get(0);
            SimpleRegression model = findSimpleLinearRegression(indVar, depVar);
            double fstat = model.getRegressionSumSquares()/model.getMeanSquareError();
            StringBuilder output = new StringBuilder();
            output.append("\tSimple Linear Regression\n");
            output.append("Dependent variable: " + target.getName() + "\n");
            output.append("Independent variable: " + parameters[0].getName() + "\n");
            output.append("Equation: y = " + String.format("%.4f", model.getIntercept()) + 
                        " + " + String.format("%.4f", model.getSlope()) + "x\n");
            output.append("Significance: " + String.format("%.4f", model.getSignificance()));
            output.append("\tF-statistic: " + String.format("%.4f", fstat));
            output.append("\nR-Squared: " + String.format("%.4f", model.getRSquare()) + "\n");

            return output.toString();
        }
        // multiple linear regression output
        else {
            OLSMultipleLinearRegression  model = findMultipleLinearRegression(values, depVar);
            double meanSquareRegression = model.calculateTotalSumOfSquares()/parameters.length;
            int dfe = depVar.size()-parameters.length;
            if (dfe<=0) {
                dfe = 1;
            }
            double meanSquareResidual = model.calculateResidualSumOfSquares()/dfe;
            if(meanSquareResidual <=0) {
                System.out.println("Error calculating f statistic");
                meanSquareResidual = 1;
            }
            double fstat = meanSquareRegression/meanSquareResidual;
            StringBuilder output = new StringBuilder();
            output.append("\tMultiple Linear Regression\n");
            output.append("Target field: " + target.getName() + "\n");
            output.append("Parameters: " + parameters[0].getName());
            for (int i=1; i<parameters.length; i++) {
                if (i%3 == 0) {
                    output.append("," + "\n" + "\t" + parameters[i].getName());
                }
                else {
                    output.append(", " + parameters[i].getName());
                }
            }
            double[] betas = model.estimateRegressionParameters();
            output.append("\nEquation: " + String.format("%.4f", betas[0]));
            for (int i = 1; i < betas.length; i++) {
                output.append(" + " + String.format("%.4f", betas[i]) + "x" + i);
                if (i % 5 == 0) {
                    output.append("\n" + "\t");
                }
            }
            output.append("\nF-Statistic: " + String.format("%.4f", fstat));
            output.append("\nR-Squared: " + String.format("%.4f", model.calculateRSquared()) + "\n");
            return output.toString();
        }
    }
    // indVar: independent variable
    // depVar: dependent variable
    // returns model
    public static SimpleRegression findSimpleLinearRegression(ArrayList<Double> indVar, ArrayList<Double> depVar) {
        if (indVar == null || indVar.size() <= 1 || depVar == null || depVar.size() <= 1) {
            System.out.println("Invalid arguments");
            throw new IllegalArgumentException();
        }
        if (indVar.size() != depVar.size()) {
            System.out.println("Error: Variables must match in size.");
            System.out.println("Independent size: " + indVar.size());
            System.out.println("Dependent size: " + depVar.size());
            throw new IllegalArgumentException();
        }

        // populate model
        SimpleRegression model = new SimpleRegression();
        for (int i=0; i<indVar.size(); i++) {
            model.addData(indVar.get(i), depVar.get(i));
        }

        return model;
    }

    // indVars: independent variables
    // depVar: dependent variables
    // returns model
    public static OLSMultipleLinearRegression findMultipleLinearRegression(ArrayList<ArrayList<Double>> indVars, List<Double> depVar) {
        if(indVars == null || indVars.size() <= 1 || depVar == null || depVar.size() <= 1) {
            System.out.println("Invalid arguments");
            throw new IllegalArgumentException();
        }
        int size = depVar.size();
        for (List<Double> indVar: indVars) {
            if (indVar.size() != size) {
                System.out.println("Error: Variables must match in size");
                System.out.println("Independent size: " + indVar.size());
                System.out.println("Dependent size: " + size);
                throw new IllegalArgumentException();
            }
        }

        // convert lists to arrays
        int numPredictors = indVars.size();
        int numObservations = indVars.get(0).size();
        double[][] indValues = new double[numObservations][numPredictors];
        for (int i = 0; i < numPredictors; i++) {
            List<Double> predictor = indVars.get(i);
            for (int j = 0; j < numObservations; j++) {
                indValues[j][i] = predictor.get(j);
            }
        }

        double[] depValues = new double[depVar.size()];
        for (int i = 0; i < depVar.size(); i++) {
            depValues[i] = depVar.get(i);
        }

        // populate model
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(depValues, indValues);
        return model;
    }

    public static double getMin(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    public static double getMax(List<Double> data) {
        return data.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    public static double getQuartile(List<Double> data, int quartile) {
        List<Double> sortedData = data.stream().sorted().toList();
        int size = sortedData.size();
        switch (quartile) {
            case 1: // Q1
                return sortedData.get(size / 4);
            case 2: // Q2 (Median)
                return getMedian(data);
            case 3: // Q3
                return sortedData.get(3 * size / 4);
            default:
                throw new IllegalArgumentException("Invalid quartile. Use 1, 2, or 3.");
        }
    }

    public static int getCount(List<Double> data) {
        int count = data.stream().filter(e -> e != null).collect(Collectors.toList()).size();
        //return data.size();
        return count;
    }

    public static double getSum(List<Double> data) {
        double total = 0;
        for (double val : data) {
            total+= val;   
        }
        return total;
    }

    public static String getSummary(Field field) {
        List<Double> data = field.getValues();
        StringBuilder output = new StringBuilder();
        output.append("\n\tSummary of " + field.getName());
        output.append("\nMean: " + String.format("%.4f", getMean(data)));
        output.append("\nStandard Deviation: " + String.format("%.4f",getStandardDeviation(data)));
        output.append("\nMinimum: " + String.format("%.4f", getMin(data)));
        output.append("\nFirst Quartile: " + String.format("%.4f", getQuartile(data, 1)));
        output.append("\nMedian: " + String.format("%.4f", getMedian(data)));
        output.append("\nThird Quartile: " + String.format("%.4f", getQuartile(data, 3)));
        output.append("\nMaximum: " + String.format("%.4f", getMax(data)));
        output.append("\nCount: " + getCount(data));
        return output.toString();
    }

}