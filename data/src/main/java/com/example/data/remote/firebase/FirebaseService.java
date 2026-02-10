package com.example.data.remote.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseService {
    public static final String USERS_COLLECTION = "users";
    public static final String ACTIVATION_TOKENS_COLLECTION = "activationTokens";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    @Inject
    public FirebaseService(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    public FirebaseAuth auth() {
        return firebaseAuth;
    }

    public FirebaseFirestore firestore() {
        return firestore;
    }

    public CollectionReference users() {
        return firestore.collection(USERS_COLLECTION);
    }

    public CollectionReference activationTokens() {
        return firestore.collection(ACTIVATION_TOKENS_COLLECTION);
    }
}
