package com.example.data.mapper;

import com.example.domain.model.ActivationToken;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public final class ActivationTokenFirestoreMapper {
    private ActivationTokenFirestoreMapper() {
    }

    public static Map<String, Object> toDocument(ActivationToken token) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", token.getUserId());
        data.put("createdAt", token.getCreatedAtEpochMillis());
        data.put("used", false);
        return data;
    }

    public static ActivationToken fromDocument(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            return null;
        }

        String token = documentSnapshot.getId();
        String userId = documentSnapshot.getString("userId");
        Long createdAt = documentSnapshot.getLong("createdAt");

        if (userId == null || createdAt == null) {
            return null;
        }

        return new ActivationToken(token, userId, createdAt);
    }
}
