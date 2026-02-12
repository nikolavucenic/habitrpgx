package com.example.data.repository;

import com.example.domain.core.Result;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.CompletableFuture;

public class AuthRepositoryImpl implements AuthRepository {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    @Override
    public CompletableFuture<Result<Void>> register(String email, String password, String username, int avatarId) {
        return null;
    }

    @Override
    public CompletableFuture<Result<User>> login(String email, String password) {
        CompletableFuture<Result<User>> future = new CompletableFuture<>();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (!authTask.isSuccessful()) {
                String error = authTask.getException() != null ? authTask.getException().getMessage() : "Greška pri prijavi.";
                future.complete(new Result.Error<>(error));
                return;
            }

            FirebaseUser fbUser = mAuth.getCurrentUser();
            if (fbUser == null) {
                future.complete(new Result.Error<>("Korisnik nije pronađen."));
                return;
            }

            fbUser.reload().addOnCompleteListener(reloadTask -> {
                if (!fbUser.isEmailVerified()) {
                    future.complete(new Result.Error<>("Nalog nije aktiviran. Proverite email."));
                    return;
                }

                mDb.collection("users").document(fbUser.getUid()).get().addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful() && dbTask.getResult().exists()) {
                        User user = dbTask.getResult().toObject(User.class);
                        future.complete(new Result.Success<>(user));
                    } else {
                        future.complete(new Result.Error<>("Podaci o RPG profilu ne postoje."));
                    }
                });
            });
        });

        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> logout() {
        return null;
    }

    @Override
    public CompletableFuture<Result<User>> getCurrentUserProfile() {
        return null;
    }

    @Override
    public CompletableFuture<Result<Void>> changePassword(String oldPassword, String newPassword) {
        return null;
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
        return null;
    }
}