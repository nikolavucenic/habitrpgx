package com.example.domain.progression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProgressionCalculatorTest {

    @Test
    public void requiredXpMatchesSpecExamples() {
        assertEquals(200, ProgressionCalculator.requiredXpForLevel(1));
        assertEquals(500, ProgressionCalculator.requiredXpForLevel(2));
        assertEquals(1250, ProgressionCalculator.requiredXpForLevel(3));
    }

    @Test
    public void ppRewardMatchesSpecExamples() {
        assertEquals(40, ProgressionCalculator.ppRewardForLevel(1));
        assertEquals(70, ProgressionCalculator.ppRewardForLevel(2));
        assertEquals(123, ProgressionCalculator.ppRewardForLevel(3));
    }

    @Test
    public void importanceScalingMatchesSpecTable() {
        assertEquals(2, ProgressionCalculator.scaledXpValue(1, 1));
        assertEquals(3, ProgressionCalculator.scaledXpValue(1, 2));

        assertEquals(5, ProgressionCalculator.scaledXpValue(3, 1));
        assertEquals(8, ProgressionCalculator.scaledXpValue(3, 2));

        assertEquals(15, ProgressionCalculator.scaledXpValue(10, 1));
        assertEquals(23, ProgressionCalculator.scaledXpValue(10, 2));

        assertEquals(150, ProgressionCalculator.scaledXpValue(100, 1));
        assertEquals(225, ProgressionCalculator.scaledXpValue(100, 2));
    }
}
