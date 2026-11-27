package com.example.analyzer.aggregate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class Stats {

    private Stats() {}

    public static double average(List<Double> values) {
        requireNotEmpty(values);
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    public static double median(List<Double> sorted) {
        requireNotEmpty(sorted);
        int n = sorted.size();
        int middle = n / 2;
        return n % 2 == 1 ? sorted.get(middle) : (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
    }

    public static double populationStdDev(List<Double> values) {
        double mean = average(values);
        double sumOfSquares = 0;
        for (double v : values) {
            sumOfSquares += (v - mean) * (v - mean);
        }
        return Math.sqrt(sumOfSquares / values.size());
    }

    public static <T> T nearestRank(List<T> sorted, int percentile) {
        requireNotEmpty(sorted);
        if (percentile <= 0 || percentile > 100) {
            throw new IllegalArgumentException("percentile must be in (0, 100]: " + percentile);
        }
        long n = sorted.size();
        long rank = (percentile * n + 99) / 100;
        return sorted.get((int) rank - 1);
    }

    public static Number round1(double value) {
        BigDecimal rounded = BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
        if (rounded.stripTrailingZeros().scale() <= 0) {
            return rounded.longValueExact();
        }
        return rounded.doubleValue();
    }

    private static void requireNotEmpty(List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
    }
}
