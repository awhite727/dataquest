package dataquest;

import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;

public class StatisticalSummary {

    // gets t star for two-tailed t table
    // if one-tailed t table is needed, divide alpha by two before passing
    public static double getTStar(double alpha, int sampleSize) {
        int df = sampleSize - 1;
        double tStar = new TDistribution(df).inverseCumulativeProbability(1 - alpha / 2);
        return tStar;
    }

    public static double getZStar(double alpha) {
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