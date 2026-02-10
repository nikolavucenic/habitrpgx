package com.example.data.repository;

import com.example.data.local.db.UserCacheDao;
import com.example.data.local.db.UserCacheEntity;
import com.example.data.mapper.ActivationTokenFirestoreMapper;
import com.example.data.mapper.UserCacheMapper;
import com.example.data.mapper.UserFirestoreMapper;
import com.example.data.remote.firebase.FirebaseService;
import com.example.domain.core.Result;
import com.example.domain.model.ActivationStatus;
import com.example.domain.model.ActivationToken;
import com.example.domain.model.RegistrationRequest;
import com.example.domain.model.User;
import com.example.domain.repository.AuthRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {
    private final FirebaseService firebaseService;
    private final UserCacheDao userCacheDao;

    @Inject
    public AuthRepositoryImpl(FirebaseService firebaseService, UserCacheDao userCacheDao) {
        this.firebaseService = firebaseService;
        this.userCacheDao = userCacheDao;
    }

    @Override
    public Result<String> register(RegistrationRequest request) {
        String userId = firebaseService.users().document().getId();
        long now = System.currentTimeMillis();

        try {
            Tasks.await(
                    firebaseService.users()
                            .document(userId)
                            .set(UserFirestoreMapper.registrationToDocument(request, now))
            );

            userCacheDao.upsert(UserCacheMapper.fromRegistration(userId, request, now));

            return new Result.Success<>(userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result.Error<>("Failed to register user: " + e.getMessage());
        } catch (ExecutionException e) {
            return new Result.Error<>("Failed to register user: " + e.getMessage());
        } catch (Exception e) {
            return new Result.Error<>("Failed to register user: " + e.getMessage());
        }
    }

    @Override
    public Result<ActivationToken> createActivationToken(String userId) {
        String token = firebaseService.activationTokens().document().getId();
        long now = System.currentTimeMillis();
        ActivationToken activationToken = new ActivationToken(token, userId, now);

        try {
            Tasks.await(
                    firebaseService.activationTokens()
                            .document(token)
                            .set(ActivationTokenFirestoreMapper.toDocument(activationToken))
            );
            return new Result.Success<>(activationToken);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result.Error<>("Failed to create activation token: " + e.getMessage());
        } catch (ExecutionException e) {
            return new Result.Error<>("Failed to create activation token: " + e.getMessage());
        } catch (Exception e) {
            return new Result.Error<>("Failed to create activation token: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> activate(String token) {
        FirebaseFirestore firestore = firebaseService.firestore();
        DocumentReference tokenRef = firebaseService.activationTokens().document(token);

        try {
            DocumentSnapshot tokenSnapshot = Tasks.await(tokenRef.get());
            ActivationToken activationToken = ActivationTokenFirestoreMapper.fromDocument(tokenSnapshot);

            if (activationToken == null) {
                return new Result.Error<>("Activation token not found.");
            }

            Boolean used = tokenSnapshot.getBoolean("used");
            if (Boolean.TRUE.equals(used)) {
                return new Result.Error<>("Activation token already used.");
            }

            DocumentReference userRef = firebaseService.users().document(activationToken.getUserId());
            WriteBatch batch = firestore.batch();
            batch.update(userRef, "activationStatus", ActivationStatus.ACTIVE.name());
            batch.update(tokenRef, "used", true);
            Tasks.await(batch.commit());

            UserCacheEntity cached = userCacheDao.getById(activationToken.getUserId());
            if (cached != null) {
                userCacheDao.upsert(
                        UserCacheMapper.updateActivationStatus(cached, ActivationStatus.ACTIVE, System.currentTimeMillis())
                );
            }

            return new Result.Success<>(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result.Error<>("Failed to activate account: " + e.getMessage());
        } catch (ExecutionException e) {
            return new Result.Error<>("Failed to activate account: " + e.getMessage());
        } catch (Exception e) {
            return new Result.Error<>("Failed to activate account: " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> isUserActive(String userId) {
        try {
            DocumentSnapshot userSnapshot = Tasks.await(
                    firebaseService.users()
                            .document(userId)
                            .get()
            );

            if (!userSnapshot.exists()) {
                return new Result.Error<>("User not found.");
            }

            String activationStatus = userSnapshot.getString("activationStatus");
            boolean active = ActivationStatus.ACTIVE.name().equals(activationStatus);
            return new Result.Success<>(active);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result.Error<>("Failed to read activation status: " + e.getMessage());
        } catch (ExecutionException e) {
            return new Result.Error<>("Failed to read activation status: " + e.getMessage());
        } catch (Exception e) {
            return new Result.Error<>("Failed to read activation status: " + e.getMessage());
        }
    }

    @Override
    public Result<User> login(String email, String password) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> logout() {
        firebaseService.auth().signOut();
        return new Result.Success<>(null);
    }

    @Override
    public Result<User> getCurrentUserProfile() {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> changePassword(String oldPassword, String newPassword) {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Boolean> isEmailVerified() {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }

    @Override
    public Result<Void> resendVerificationEmail() {
        return new Result.Error<>("Not implemented in S1-0 skeleton");
    }
}
