package com.example.data.repository;

import com.example.domain.core.Result;
import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
import com.example.domain.progression.ProgressionCalculator;
import com.example.domain.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import android.content.SharedPreferences;
import javax.inject.Singleton;

@Singleton
public class TaskRepositoryImpl implements TaskRepository {

    private static final String KEY_PENDING_BOSS_ENCOUNTER = "pending_boss_encounter";
    private static final String KEY_LAST_RESOLVED_BOSS_ENCOUNTER_LEVEL = "last_resolved_boss_encounter_level";
    private static final String KEY_BOSS_NUMBER = "boss_number";
    private static final String KEY_BOSS_HP = "boss_hp";

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final SharedPreferences sharedPreferences;

    @Inject
    public TaskRepositoryImpl(FirebaseFirestore db, SharedPreferences sharedPreferences) {
        this.auth = FirebaseAuth.getInstance();
        this.db = db;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public boolean isPendingBossEncounter() {
        return sharedPreferences.getBoolean(KEY_PENDING_BOSS_ENCOUNTER, false);
    }

    @Override
    public void setPendingBossEncounter(boolean pending) {
        sharedPreferences.edit().putBoolean(KEY_PENDING_BOSS_ENCOUNTER, pending).apply();
    }

    @Override
    public int getLastResolvedBossEncounterLevel() {
        return sharedPreferences.getInt(KEY_LAST_RESOLVED_BOSS_ENCOUNTER_LEVEL, 0);
    }

    @Override
    public void saveLastResolvedBossEncounterLevel(int encounterLevel) {
        sharedPreferences.edit().putInt(KEY_LAST_RESOLVED_BOSS_ENCOUNTER_LEVEL, encounterLevel).apply();
    }

    @Override
    public int getBossNumber() {
        return sharedPreferences.getInt(KEY_BOSS_NUMBER, 0);
    }

    @Override
    public int getBossHp() {
        return sharedPreferences.getInt(KEY_BOSS_HP, 0);
    }

    @Override
    public void saveBossState(int bossNumber, int hp) {
        sharedPreferences.edit()
                .putInt(KEY_BOSS_NUMBER, bossNumber)
                .putInt(KEY_BOSS_HP, hp)
                .apply();
    }

    @Override
    public CompletableFuture<Result<List<TaskCategory>>> getCategories() {
        CompletableFuture<Result<List<TaskCategory>>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        db.collection("users").document(user.getUid()).collection("categories").get()
                .addOnSuccessListener(snapshot -> {
                    List<TaskCategory> categories = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> categories.add(new TaskCategory(
                            doc.getId(),
                            doc.getString("name") == null ? "" : doc.getString("name"),
                            doc.getString("colorHex") == null ? "#3F51B5" : doc.getString("colorHex")
                    )));
                    future.complete(new Result.Success<>(categories));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> createCategory(String name, String colorHex) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        db.collection("users").document(user.getUid()).collection("categories")
                .whereEqualTo("colorHex", colorHex).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        future.complete(new Result.Error<>("Boja kategorije mora biti jedinstvena."));
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("colorHex", colorHex);
                    db.collection("users").document(user.getUid()).collection("categories").document().set(data)
                            .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                            .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> updateCategory(String categoryId, String name, String colorHex) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        db.collection("users").document(user.getUid()).collection("categories")
                .whereEqualTo("colorHex", colorHex).get()
                .addOnSuccessListener(query -> {
                    boolean usedByOther = query.getDocuments().stream().anyMatch(doc -> !doc.getId().equals(categoryId));
                    if (usedByOther) {
                        future.complete(new Result.Error<>("Boja kategorije mora biti jedinstvena."));
                        return;
                    }
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", name);
                    updates.put("colorHex", colorHex);
                    db.collection("users").document(user.getUid()).collection("categories").document(categoryId)
                            .update(updates)
                            .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                            .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> deleteCategory(String categoryId) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        db.collection("users").document(user.getUid()).collection("tasks")
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("status", TaskItem.STATUS_ACTIVE)
                .get()
                .addOnSuccessListener(activeTasks -> {
                    if (!activeTasks.isEmpty()) {
                        future.complete(new Result.Error<>("Kategorija se ne može obrisati jer ima aktivne zadatke."));
                        return;
                    }
                    db.collection("users").document(user.getUid()).collection("categories").document(categoryId).delete()
                            .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                            .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<List<TaskItem>>> getTasks() {
        CompletableFuture<Result<List<TaskItem>>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        db.collection("users").document(user.getUid()).collection("tasks").get()
                .addOnSuccessListener(snapshot -> {
                    List<TaskItem> tasks = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> tasks.add(mapTask(doc.getId(), doc.getData())));
                    future.complete(new Result.Success<>(tasks));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<TaskItem>> getTaskById(String taskId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<TaskItem>> future = new CompletableFuture<>();
        db.collection("users").document(user.getUid()).collection("tasks").document(taskId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        future.complete(new Result.Error<>("Zadatak ne postoji."));
                        return;
                    }
                    future.complete(new Result.Success<>(mapTask(snapshot.getId(), snapshot.getData())));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> createTask(TaskItem taskItem) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        db.collection("users").document(user.getUid()).collection("tasks").document()
                .set(toMap(taskItem, TaskItem.STATUS_ACTIVE, 0, System.currentTimeMillis()))
                .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> updateTask(TaskItem taskItem) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        DocumentReference taskRef = db.collection("users").document(user.getUid()).collection("tasks").document(taskItem.getId());
        taskRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                future.complete(new Result.Error<>("Zadatak ne postoji."));
                return;
            }
            String status = snapshot.getString("status");
            if (TaskItem.STATUS_DONE.equals(status) || TaskItem.STATUS_NOT_DONE.equals(status) || TaskItem.STATUS_CANCELED.equals(status)) {
                future.complete(new Result.Error<>("Nije moguće izmeniti završene/neaktivne zadatke."));
                return;
            }
            Map<String, Object> data = toMap(taskItem, status == null ? TaskItem.STATUS_ACTIVE : status,
                    ((Number) snapshot.get("awardedXp")==null?0:((Number)snapshot.get("awardedXp")).intValue()),
                    ((Number) snapshot.get("createdAt")==null?System.currentTimeMillis():((Number)snapshot.get("createdAt")).longValue()));
            taskRef.update(data)
                    .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> deleteTask(String taskId) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        DocumentReference ref = db.collection("users").document(user.getUid()).collection("tasks").document(taskId);
        ref.get().addOnSuccessListener(snapshot -> {
            String status = snapshot.getString("status");
            if (TaskItem.STATUS_DONE.equals(status) || TaskItem.STATUS_NOT_DONE.equals(status) || TaskItem.STATUS_CANCELED.equals(status)) {
                future.complete(new Result.Error<>("Nije moguće obrisati završene/neaktivne zadatke."));
                return;
            }
            ref.delete().addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> changeTaskStatus(String taskId, String newStatus) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        DocumentReference taskRef = db.collection("users").document(user.getUid()).collection("tasks").document(taskId);
        taskRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                future.complete(new Result.Error<>("Zadatak ne postoji."));
                return;
            }
            taskRef.update("status", newStatus)
                    .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> incrementCurrentUserXp(int xp) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        incrementUserXp(user.getUid(), xp, future);
        return future;
    }

    private void incrementUserXp(String uid, int xp, CompletableFuture<Result<Void>> future) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnSuccessListener(snapshot -> {
            int currentLevel = readInt(snapshot.get("level"), 1);
            int currentXp = readInt(snapshot.get("xp"), 0);
            int currentPp = readInt(snapshot.get("pp"), 0);

            int level = Math.max(1, currentLevel);
            int levelXp = Math.max(0, currentXp) + Math.max(0, xp);
            int pp = Math.max(0, currentPp);
            boolean leveledUp = false;

            while (levelXp >= ProgressionCalculator.requiredXpForLevel(level)) {
                levelXp -= ProgressionCalculator.requiredXpForLevel(level);
                pp += ProgressionCalculator.ppRewardForLevel(level);
                level++;
                leveledUp = true;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("xp", levelXp);
            updates.put("level", level);
            updates.put("pp", pp);
            updates.put("title", ProgressionCalculator.titleForLevel(level));

            boolean finalLeveledUp = leveledUp;
            userRef.update(updates)
                    .addOnSuccessListener(unused -> {
                        if (finalLeveledUp) {
                            setPendingBossEncounter(true);
                        }
                        future.complete(new Result.Success<>(null));
                    })
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
    }

    private int readInt(Object value, int fallback) {
        if (value instanceof Number) return ((Number) value).intValue();
        return fallback;
    }

    private Map<String, Object> toMap(TaskItem item, String status, int awardedXp, long createdAt) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", item.getTitle());
        map.put("description", item.getDescription());
        map.put("categoryId", item.getCategoryId());
        map.put("categoryName", item.getCategoryName());
        map.put("categoryColorHex", item.getCategoryColorHex());
        map.put("type", item.getType());
        map.put("repeatInterval", item.getRepeatInterval());
        map.put("repeatUnit", item.getRepeatUnit());
        map.put("repeatStartAt", item.getRepeatStartAt());
        map.put("repeatEndAt", item.getRepeatEndAt());
        map.put("executeAt", item.getExecuteAt());
        map.put("difficulty", item.getDifficulty());
        map.put("importance", item.getImportance());
        map.put("xpValue", item.getXpValue());
        map.put("status", status);
        map.put("awardedXp", awardedXp);
        map.put("createdAt", createdAt);
        return map;
    }

    private TaskItem mapTask(String id, Map<String, Object> map) {
        if (map == null) map = new HashMap<>();
        return new TaskItem(
                id,
                (String) map.getOrDefault("title", ""),
                (String) map.getOrDefault("description", ""),
                (String) map.getOrDefault("categoryId", ""),
                (String) map.getOrDefault("categoryName", ""),
                (String) map.getOrDefault("categoryColorHex", "#3F51B5"),
                (String) map.getOrDefault("type", TaskItem.TYPE_ONE_TIME),
                ((Number) map.getOrDefault("repeatInterval", 1)).intValue(),
                (String) map.getOrDefault("repeatUnit", "DAY"),
                ((Number) map.getOrDefault("repeatStartAt", 0L)).longValue(),
                ((Number) map.getOrDefault("repeatEndAt", 0L)).longValue(),
                ((Number) map.getOrDefault("executeAt", 0L)).longValue(),
                (String) map.getOrDefault("difficulty", "LAK"),
                (String) map.getOrDefault("importance", "NORMALAN"),
                ((Number) map.getOrDefault("xpValue", 0)).intValue(),
                (String) map.getOrDefault("status", TaskItem.STATUS_ACTIVE),
                ((Number) map.getOrDefault("createdAt", 0L)).longValue()
        );
    }
}
