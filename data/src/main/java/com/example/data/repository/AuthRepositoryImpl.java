package com.example.data.repository;

import android.content.SharedPreferences;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.progression.ProgressionCalculator;
import com.example.domain.repository.AuthRepository;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mDb;
    private final SharedPreferences sharedPreferences;

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Inject
    public AuthRepositoryImpl(SharedPreferences sharedPreferences) {
        this.mAuth = FirebaseAuth.getInstance();
        this.mDb = FirebaseFirestore.getInstance();
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public CompletableFuture<Result<Void>> register(String email, String password, String username, int avatarId) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (!authTask.isSuccessful()) {
                String error = authTask.getException() != null ? authTask.getException().getMessage() : "Greška pri registraciji.";
                sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                future.complete(new Result.Error<>(error));
                return;
            }

            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser == null) {
                future.complete(new Result.Error<>("Nalog je kreiran, ali korisnik nije dostupan."));
                return;
            }

            Map<String, Object> profile = new HashMap<>();
            profile.put("uid", firebaseUser.getUid());
            profile.put("email", email);
            profile.put("username", username);
            profile.put("avatarId", avatarId);
            profile.put("level", 1);
            profile.put("title", "Početnik navika");
            profile.put("pp", 40);
            profile.put("xp", 0);
            profile.put("coins", 0);
            profile.put("badges", new ArrayList<String>());
            profile.put("equipment", new ArrayList<String>());

            mDb.collection("users").document(firebaseUser.getUid()).set(profile)
                    .addOnSuccessListener(unused -> firebaseUser.sendEmailVerification()
                            .addOnCompleteListener(mailTask -> {
                                if (mailTask.isSuccessful()) {
                                    mAuth.signOut();
                                    sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                                    future.complete(new Result.Success<>(null));
                                } else {
                                    String mailError = mailTask.getException() != null ? mailTask.getException().getMessage() : "Nije moguće poslati verifikacioni email.";
                                    future.complete(new Result.Error<>(mailError));
                                }
                            }))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>("Greška pri čuvanju profila: " + e.getMessage())));
        });

        return future;
    }

    @Override
    public CompletableFuture<Result<User>> login(String email, String password) {
        CompletableFuture<Result<User>> future = new CompletableFuture<>();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (!authTask.isSuccessful()) {
                String error = authTask.getException() != null ? authTask.getException().getMessage() : "Greška pri prijavi.";
                sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                future.complete(new Result.Error<>(error));
                return;
            }

            FirebaseUser fbUser = mAuth.getCurrentUser();
            if (fbUser == null) {
                sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                future.complete(new Result.Error<>("Korisnik nije pronađen."));
                return;
            }

            fbUser.reload().addOnCompleteListener(reloadTask -> {
                if (!fbUser.isEmailVerified()) {
                    sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                    mAuth.signOut();
                    future.complete(new Result.Error<>("Nalog nije aktiviran. Proverite email."));
                    return;
                }

                mDb.collection("users").document(fbUser.getUid()).get().addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful() && dbTask.getResult().exists()) {
                        Map<String, Object> doc = dbTask.getResult().getData();
                        if (doc == null) {
                            sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                            future.complete(new Result.Error<>("Podaci o korisniku nisu dostupni."));
                            return;
                        }
                        User user = normalizeAndPersistProgressIfNeeded(fbUser.getUid(), mapToUser(doc));
                        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
                        future.complete(new Result.Success<>(user));
                    } else {
                        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
                        future.complete(new Result.Error<>("Podaci o RPG profilu ne postoje."));
                    }
                });
            });
        });

        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> logout() {
        mAuth.signOut();
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
        return CompletableFuture.completedFuture(new Result.Success<>(null));
    }

    @Override
    public CompletableFuture<Result<User>> getCurrentUserProfile() {
        CompletableFuture<Result<User>> future = new CompletableFuture<>();
        FirebaseUser current = mAuth.getCurrentUser();

        if (current == null) {
            sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
            future.complete(new Result.Error<>("Nema aktivnog korisnika."));
            return future;
        }

        mDb.collection("users").document(current.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> doc = snapshot.getData();
                    if (doc == null) {
                        future.complete(new Result.Error<>("Profil nije pronađen."));
                        return;
                    }
                    User user = normalizeAndPersistProgressIfNeeded(current.getUid(), mapToUser(doc));
                    future.complete(new Result.Success<>(user));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> changePassword(String oldPassword, String newPassword) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser current = mAuth.getCurrentUser();

        if (current == null || current.getEmail() == null) {
            future.complete(new Result.Error<>("Nema aktivnog korisnika."));
            return future;
        }

        current.reauthenticate(EmailAuthProvider.getCredential(current.getEmail(), oldPassword))
                .addOnSuccessListener(unused -> current.updatePassword(newPassword)
                        .addOnSuccessListener(unusedPassword -> future.complete(new Result.Success<>(null)))
                        .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage()))))
                .addOnFailureListener(e -> future.complete(new Result.Error<>("Stara lozinka nije ispravna.")));

        return future;
    }

    @Override
    public CompletableFuture<Result<Boolean>> isEmailVerified() {
        CompletableFuture<Result<Boolean>> future = new CompletableFuture<>();
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().reload().addOnCompleteListener(task -> {
                future.complete(new Result.Success<>(mAuth.getCurrentUser().isEmailVerified()));
            });
        } else {
            future.complete(new Result.Success<>(false));
        }
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> resendVerificationEmail() {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser current = mAuth.getCurrentUser();
        if (current == null) {
            future.complete(new Result.Error<>("Nema aktivnog korisnika."));
            return future;
        }

        current.sendEmailVerification()
                .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> applyBossBattleRewards(int earnedCoins, int earnedPp, String earnedEquipment) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        FirebaseUser current = mAuth.getCurrentUser();

        if (current == null) {
            future.complete(new Result.Error<>("Nema aktivnog korisnika."));
            return future;
        }

        mDb.collection("users").document(current.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> doc = snapshot.getData();
                    if (doc == null) {
                        future.complete(new Result.Error<>("Profil nije pronađen."));
                        return;
                    }

                    User user = mapToUser(doc);
                    int nextCoins = Math.max(0, user.coins) + Math.max(0, earnedCoins);
                    int nextPp = Math.max(0, user.pp) + Math.max(0, earnedPp);

                    List<String> nextEquipment = new ArrayList<>(user.equipment != null ? user.equipment : new ArrayList<>());
                    if (earnedEquipment != null && !earnedEquipment.trim().isEmpty()) {
                        nextEquipment.add(earnedEquipment);
                    }
                    Set<String> deduplicated = new LinkedHashSet<>(nextEquipment);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("coins", nextCoins);
                    updates.put("pp", nextPp);
                    updates.put("equipment", new ArrayList<>(deduplicated));

                    mDb.collection("users").document(current.getUid()).update(updates)
                            .addOnSuccessListener(unused -> future.complete(new Result.Success<>(null)))
                            .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

        return future;
    }


    private User normalizeAndPersistProgressIfNeeded(String uid, User user) {
        int level = Math.max(1, user.level);
        int xp = Math.max(0, user.xp);
        int pp = Math.max(0, user.pp);

        boolean changed = false;

        while (xp >= ProgressionCalculator.requiredXpForLevel(level)) {
            xp -= ProgressionCalculator.requiredXpForLevel(level);
            pp += ProgressionCalculator.ppRewardForLevel(level);
            level++;
            changed = true;
        }

        if (level == 1 && pp < 40) {
            pp = 40;
            changed = true;
        }

        if (!changed) {
            return user;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("level", level);
        updates.put("xp", xp);
        updates.put("pp", pp);
        updates.put("title", ProgressionCalculator.titleForLevel(level));
        mDb.collection("users").document(uid).update(updates);
        return new User(user.uid, user.email, user.username, user.avatarId, level,
                ProgressionCalculator.titleForLevel(level), pp, xp, user.coins, user.badges, user.equipment);
    }

    @SuppressWarnings("unchecked")
    private User mapToUser(Map<String, Object> doc) {
        String uid = (String) doc.getOrDefault("uid", "");
        String email = (String) doc.getOrDefault("email", "");
        String username = (String) doc.getOrDefault("username", "");
        int avatarId = ((Number) doc.getOrDefault("avatarId", 1)).intValue();
        int level = ((Number) doc.getOrDefault("level", 1)).intValue();
        String title = (String) doc.getOrDefault("title", "Početnik navika");
        int pp = ((Number) doc.getOrDefault("pp", 40)).intValue();
        int xp = ((Number) doc.getOrDefault("xp", 0)).intValue();
        int coins = ((Number) doc.getOrDefault("coins", 0)).intValue();
        List<String> badges = (List<String>) doc.getOrDefault("badges", new ArrayList<String>());
        List<String> equipment = (List<String>) doc.getOrDefault("equipment", new ArrayList<String>());

        return new User(uid, email, username, avatarId, level, title, pp, xp, coins, badges, equipment);
    }
}
