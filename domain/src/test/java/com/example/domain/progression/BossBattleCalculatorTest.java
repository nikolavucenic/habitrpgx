package com.example.domain.progression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BossBattleCalculatorTest {

    @Test
    public void hpMatchesSpecExamples() {
        assertEquals(200, BossBattleCalculator.hpForBoss(1));
        assertEquals(500, BossBattleCalculator.hpForBoss(2));
        assertEquals(1250, BossBattleCalculator.hpForBoss(3));
    }

    @Test
    public void coinsMatchesSpecExamples() {
        assertEquals(200, BossBattleCalculator.coinsForBoss(1));
        assertEquals(240, BossBattleCalculator.coinsForBoss(2));
        assertEquals(288, BossBattleCalculator.coinsForBoss(3));
    }
}
