package com.example.domain.progression;

import com.example.domain.model.TaskItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BossBattleCalculatorTest {

    @Test
    public void bossHpGrowth_matchesSpec() {
        assertEquals(200, BossBattleCalculator.bossHpForIndex(1));
        assertEquals(500, BossBattleCalculator.bossHpForIndex(2));
        assertEquals(1250, BossBattleCalculator.bossHpForIndex(3));
    }

    @Test
    public void coinsGrowth_matchesSpec() {
        assertEquals(200, BossBattleCalculator.coinsForBossIndex(1));
        assertEquals(240, BossBattleCalculator.coinsForBossIndex(2));
        assertEquals(288, BossBattleCalculator.coinsForBossIndex(3));
    }

    @Test
    public void successRate_excludesPausedAndCanceled() {
        TaskItem done = taskWithStatus(TaskItem.STATUS_DONE);
        TaskItem notDone = taskWithStatus(TaskItem.STATUS_NOT_DONE);
        TaskItem paused = taskWithStatus(TaskItem.STATUS_PAUSED);
        TaskItem canceled = taskWithStatus(TaskItem.STATUS_CANCELED);

        int successRate = BossBattleCalculator.calculateSuccessRate(Arrays.asList(done, notDone, paused, canceled));

        assertEquals(50, successRate);
    }

    @Test
    public void equipmentDrop_respectsChance() {
        Random noDropRandom = new Random(5);
        assertNull(BossBattleCalculator.rollEquipment(noDropRandom, false));
    }

    private TaskItem taskWithStatus(String status) {
        return new TaskItem("id", "task", "", "", "", "#FFF", TaskItem.TYPE_ONE_TIME,
                1, "DAY", 0L, 0L, 0L, "LAKO", "NORMALAN", 1, status, 0L);
    }
}
