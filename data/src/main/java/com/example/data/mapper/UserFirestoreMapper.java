package com.example.data.mapper;

import com.example.domain.model.ActivationStatus;
import com.example.domain.model.RegistrationRequest;

import java.util.HashMap;
import java.util.Map;

public final class UserFirestoreMapper {
    private UserFirestoreMapper() {
    }

    public static Map<String, Object> registrationToDocument(RegistrationRequest request, long createdAtEpochMillis) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", request.getEmail());
        data.put("username", request.getUsername());
        data.put("avatarId", request.getAvatarId());
        data.put("activationStatus", ActivationStatus.INACTIVE.name());
        data.put("createdAt", createdAtEpochMillis);
        return data;
    }
}
