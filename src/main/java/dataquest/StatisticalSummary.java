package dataquest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;


public class StatisticalSummary {

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

    public static double getZStar(double alpha) {
        if (alpha <= 0) {
            return 0;
        }
        double zStar = new NormalDistribution().inverseCumulativeProbability(1- alpha / 2);
        return zStar;
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
        double variance = data.stream().mapToDouble(x -> Math.pow(x - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
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
            return "Too many missing values to compute linear regression.";
        }
        ArrayList<Double> depVar = values.get(0);
        values.remove(0); // gives dependent variables
        // simple linear regression output
        if (values.size() == 1) {
            ArrayList<Double> indVar = values.get(0);
            SimpleRegression model = findSimpleLinearRegression(indVar, depVar);
            StringBuilder output = new StringBuilder();
            output.append("\tSimple Linear Regression\n");
            output.append("Dependent variable: " + target.getName() + "\n");
            output.append("Independent variable: " + parameters[0].getName() + "\n");
            output.append("Equation: y=" + model.getIntercept() + " + " + model.getSlope() + "x\n");
            output.append("Significance: " + model.getSignificance());
            output.append("\tR-Squared: " + model.getRSquare()  + "\n");
            return output.toString();
        }
        // multiple linear regression output
        else {
            OLSMultipleLinearRegression  model = findMultipleLinearRegression(values, depVar);
            StringBuilder output = new StringBuilder();
            output.append("\tMultiple Linear Regression\n");
            output.append("Target field: " + target.getName() + "\n");
            output.append("Parameters: " + parameters[0]);
            for (int i=1; i<parameters.length; i++) {
                output.append(", " + parameters[i].getName());
                if (i%3 == 0) {
                    output.append("\n" + "\t");
                }
            }
            double[] betas = model.estimateRegressionParameters();
            output.append("Equation: " + betas[0]);
            for (int i=1; i<betas.length; i++) {
                output.append(" + " + betas[i] + "x" + i);
                if (i%5 == 0) {
                    output.append("\n" + "\t");
                }
            }
            output.append("\nR-Squared: " + model.calculateRSquared() + "\n");
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
        int numPredictors = indVars.get(0).size();
        double[][] indValues = new double[indVars.size()][numPredictors];
        for (int i = 0; i < indVars.size(); i++) {
            List<Double> predictors = indVars.get(i);
            for (int j = 0; j < numPredictors; j++) {
                indValues[i][j] = predictors.get(j);
            }
        }
        double[] depValues = new double[depVar.size()];
        for (int i=0; i<depVar.size(); i++) {
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
        return data.size();
    }

    public static String getSummary(List<Double> data) {
        return String.format(
            "Mean: %.2f\nMedian: %.2f\nStandard Deviation: %.2f\nMin: %.2f\nMax: %.2f\nQ1: %.2f\nQ3: %.2f\nCount: %d\n",
            getMean(data), getMedian(data), getStandardDeviation(data),
            getMin(data), getMax(data), getQuartile(data, 1), getQuartile(data, 3), getCount(data)
        );
    }

}