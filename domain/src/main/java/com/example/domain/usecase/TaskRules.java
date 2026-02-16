package com.example.domain.usecase;

import com.example.domain.model.TaskItem;

import java.util.Calendar;
import java.util.List;

public final class TaskRules {
    private static final long THREE_DAYS_MS = 3L * 24 * 60 * 60 * 1000;

    private TaskRules() {}

    public static int difficultyXp(String value) {
        if ("VEOMA_LAK".equals(value)) return 1;
        if ("LAK".equals(value)) return 3;
        if ("TEZAK".equals(value)) return 7;
        if ("EKSTREMNO_TEZAK".equals(value)) return 20;
        return 0;
    }

    public static int importanceXp(String value) {
        if ("NORMALAN".equals(value)) return 1;
        if ("VAZAN".equals(value)) return 3;
        if ("EKSTREMNO_VAZAN".equals(value)) return 10;
        if ("SPECIJALAN".equals(value)) return 100;
        return 0;
    }

    public static int xpValue(String difficulty, String importance) {
        return difficultyXp(difficulty) + importanceXp(importance);
    }

    public static String validateStatusTransition(TaskItem task, String newStatus, long nowMillis) {
        String currentStatus = task.getStatus();
        if (TaskItem.STATUS_NOT_DONE.equals(currentStatus)
                || TaskItem.STATUS_DONE.equals(currentStatus)
                || TaskItem.STATUS_CANCELED.equals(currentStatus)) {
            return "Status zadatka se više ne može menjati.";
        }

        if (TaskItem.STATUS_DONE.equals(newStatus)) {
            if (task.getExecuteAt() > nowMillis) {
                return "Zadatak zakazan u budućnosti ne može biti označen kao urađen.";
            }
        }

        if (TaskItem.STATUS_PAUSED.equals(newStatus)
                && !TaskItem.TYPE_REPEATING.equals(task.getType())) {
            return "Samo ponavljajući zadaci mogu biti pauzirani.";
        }
        return null;
    }

    public static boolean shouldAutoMarkNotDone(TaskItem task, long nowMillis) {
        return nowMillis - task.getExecuteAt() > THREE_DAYS_MS;
    }

    public static int quotaLimit(TaskItem task) {
        if ("VEOMA_LAK".equals(task.getDifficulty()) && "NORMALAN".equals(task.getImportance())) return 5;
        if ("LAK".equals(task.getDifficulty()) && "VAZAN".equals(task.getImportance())) return 5;
        if ("TEZAK".equals(task.getDifficulty()) && "EKSTREMNO_VAZAN".equals(task.getImportance())) return 2;
        if ("EKSTREMNO_TEZAK".equals(task.getDifficulty())) return 1;
        if ("SPECIJALAN".equals(task.getImportance())) return 1;
        return Integer.MAX_VALUE;
    }

    public static String quotaPeriod(TaskItem task) {
        if ("EKSTREMNO_TEZAK".equals(task.getDifficulty())) return "WEEK";
        if ("SPECIJALAN".equals(task.getImportance())) return "MONTH";
        return "DAY";
    }

    public static long periodStart(long timeMillis, String period) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeMillis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if ("WEEK".equals(period)) {
            c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
        } else if ("MONTH".equals(period)) {
            c.set(Calendar.DAY_OF_MONTH, 1);
        }
        return c.getTimeInMillis();
    }

    public static long periodEndExclusive(long startMillis, String period) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startMillis);
        if ("WEEK".equals(period)) c.add(Calendar.WEEK_OF_YEAR, 1);
        else if ("MONTH".equals(period)) c.add(Calendar.MONTH, 1);
        else c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static int doneCountInQuotaWindow(TaskItem currentTask, List<TaskItem> tasks, long nowMillis) {
        String period = quotaPeriod(currentTask);
        long start = periodStart(nowMillis, period);
        long end = periodEndExclusive(start, period);
        int count = 0;
        for (TaskItem item : tasks) {
            if (!TaskItem.STATUS_DONE.equals(item.getStatus())) continue;
            if (!item.getDifficulty().equals(currentTask.getDifficulty())) continue;
            if (!item.getImportance().equals(currentTask.getImportance())) continue;
            if (item.getExecuteAt() >= start && item.getExecuteAt() < end) count++;
        }
        return count;
    }
}
