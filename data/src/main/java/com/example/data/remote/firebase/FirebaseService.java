package com.example.data.remote.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseService {
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
}
