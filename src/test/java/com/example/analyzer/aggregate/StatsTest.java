package com.example.analyzer.aggregate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class StatsTest {

    @Test
    void medianOfOddCountIsMiddleValue() {
        assertEquals(2.0, Stats.median(List.of(1.0, 2.0, 9.0)));
    }

    @Test
    void medianOfEvenCountAveragesTheTwoMiddleValues() {
        assertEquals(2.5, Stats.median(List.of(1.0, 2.0, 3.0, 100.0)));
    }

    @Test
    void medianOfSingleValue() {
        assertEquals(7.0, Stats.median(List.of(7.0)));
    }

    @Test
    void averageOfValues() {
        assertEquals(2.5, Stats.average(List.of(1.0, 2.0, 3.0, 4.0)));
    }

    @Test
    void populationStdDevDividesByN() {
        assertEquals(2.0, Stats.populationStdDev(List.of(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)), 1e-9);
    }

    @Test
    void stdDevOfSingleValueIsZero() {
        assertEquals(0.0, Stats.populationStdDev(List.of(42.0)));
    }

    @Test
    void nearestRankUsesCeilingOfPercentTimesN() {
        List<Long> sorted = List.of(18L, 28L, 30L, 35L);
        assertEquals(28L, Stats.nearestRank(sorted, 50));
        assertEquals(35L, Stats.nearestRank(sorted, 90));
        assertEquals(35L, Stats.nearestRank(sorted, 99));
    }

    @Test
    void nearestRankFiftiethCanDifferFromMedian() {
        List<Long> sorted = List.of(10L, 20L, 30L, 40L);
        assertEquals(20L, Stats.nearestRank(sorted, 50));
        assertEquals(25.0, Stats.median(List.of(10.0, 20.0, 30.0, 40.0)));
    }

    @Test
    void nearestRankAvoidsFloatingPointOvershoot() {
        List<Long> sorted = LongStream.rangeClosed(1, 30).boxed().toList();
        assertEquals(27L, Stats.nearestRank(sorted, 90));
    }

    @Test
    void nearestRankOfSingleValue() {
        assertEquals(5L, Stats.nearestRank(List.of(5L), 99));
    }

    @Test
    void emptyInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Stats.median(List.of()));
        assertThrows(IllegalArgumentException.class, () -> Stats.nearestRank(List.<Long>of(), 50));
    }

    @ParameterizedTest
    @CsvSource({
            "21.25, 21.3",
            "21.24, 21.2",
            "18.5, 18.5",
            "2.7386, 2.7",
            "-1.25, -1.3"
    })
    void round1RoundsHalfUpToOneDecimal(double input, double expected) {
        assertEquals(expected, Stats.round1(input));
    }

    @ParameterizedTest
    @CsvSource({
            "40.0, 40",
            "44.96, 45",
            "0.0, 0",
            "-0.0, 0",
            "-3.0, -3"
    })
    void round1WritesWholeNumbersAsLongs(double input, long expected) {
        assertEquals(expected, Stats.round1(input));
    }
}
