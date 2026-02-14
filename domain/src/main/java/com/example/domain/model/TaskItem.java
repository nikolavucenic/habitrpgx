package com.example.domain.model;

public class TaskItem {
    public static final String TYPE_ONE_TIME = "ONE_TIME";
    public static final String TYPE_REPEATING = "REPEATING";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_NOT_DONE = "NOT_DONE";
    public static final String STATUS_PAUSED = "PAUSED";
    public static final String STATUS_CANCELED = "CANCELED";

    private final String id;
    private final String title;
    private final String description;
    private final String categoryId;
    private final String categoryName;
    private final String categoryColorHex;
    private final String type;
    private final int repeatInterval;
    private final String repeatUnit;
    private final long repeatStartAt;
    private final long repeatEndAt;
    private final long executeAt;
    private final String difficulty;
    private final String importance;
    private final int xpValue;
    private final String status;
    private final long createdAt;

    public TaskItem(String id,
                    String title,
                    String description,
                    String categoryId,
                    String categoryName,
                    String categoryColorHex,
                    String type,
                    int repeatInterval,
                    String repeatUnit,
                    long repeatStartAt,
                    long repeatEndAt,
                    long executeAt,
                    String difficulty,
                    String importance,
                    int xpValue,
                    String status,
                    long createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColorHex = categoryColorHex;
        this.type = type;
        this.repeatInterval = repeatInterval;
        this.repeatUnit = repeatUnit;
        this.repeatStartAt = repeatStartAt;
        this.repeatEndAt = repeatEndAt;
        this.executeAt = executeAt;
        this.difficulty = difficulty;
        this.importance = importance;
        this.xpValue = xpValue;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryColorHex() { return categoryColorHex; }
    public String getType() { return type; }
    public int getRepeatInterval() { return repeatInterval; }
    public String getRepeatUnit() { return repeatUnit; }
    public long getRepeatStartAt() { return repeatStartAt; }
    public long getRepeatEndAt() { return repeatEndAt; }
    public long getExecuteAt() { return executeAt; }
    public String getDifficulty() { return difficulty; }
    public String getImportance() { return importance; }
    public int getXpValue() { return xpValue; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
}
