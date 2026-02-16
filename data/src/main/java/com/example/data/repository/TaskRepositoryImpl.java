package com.example.data.repository;

import com.example.domain.core.Result;
import com.example.domain.model.TaskCategory;
import com.example.domain.model.TaskItem;
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
import javax.inject.Singleton;

@Singleton
public class TaskRepositoryImpl implements TaskRepository {

    private static final long THREE_DAYS_MS = 3L * 24 * 60 * 60 * 1000;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    @Inject
    public TaskRepositoryImpl(FirebaseFirestore db) {
        this.auth = FirebaseAuth.getInstance();
        this.db = db;
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
            String currentStatus = snapshot.getString("status");
            long executeAt = ((Number) snapshot.get("executeAt")).longValue();
            long now = System.currentTimeMillis();

            if (TaskItem.STATUS_NOT_DONE.equals(currentStatus) || TaskItem.STATUS_CANCELED.equals(currentStatus) || TaskItem.STATUS_DONE.equals(currentStatus)) {
                future.complete(new Result.Error<>("Status zadatka se više ne može menjati."));
                return;
            }

            if (TaskItem.STATUS_DONE.equals(newStatus)) {
                if (executeAt > now) {
                    future.complete(new Result.Error<>("Zadatak zakazan u budućnosti ne može biti označen kao urađen."));
                    return;
                }
                if (now - executeAt > THREE_DAYS_MS) {
                    taskRef.update("status", TaskItem.STATUS_NOT_DONE)
                            .addOnSuccessListener(unused -> future.complete(new Result.Error<>("Prošao je rok od 3 dana. Zadatak je označen kao neurađen.")))
                            .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                    return;
                }
                int xp = ((Number) snapshot.get("xpValue")).intValue();
                taskRef.update("status", TaskItem.STATUS_DONE, "awardedXp", xp, "doneAt", now)
                        .addOnSuccessListener(unused -> incrementUserXp(user.getUid(), xp, future))
                        .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                return;
            }

            if (TaskItem.STATUS_PAUSED.equals(newStatus)) {
                String type = snapshot.getString("type");
                if (!TaskItem.TYPE_REPEATING.equals(type)) {
                    future.complete(new Result.Error<>("Samo ponavljajući zadaci mogu biti pauzirani."));
                    return;
                }
            }

            taskRef.update("status", newStatus)
                    .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

        return future;
    }

    private void incrementUserXp(String uid, int xp, CompletableFuture<Result<Void>> future) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnSuccessListener(snapshot -> {
            int currentXp = ((Number) snapshot.get("xp")).intValue();
            userRef.update("xp", currentXp + xp)
                    .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
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
