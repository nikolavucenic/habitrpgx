package com.example.data.mapper;

import com.example.data.local.db.UserCacheEntity;
import com.example.domain.model.ActivationStatus;
import com.example.domain.model.RegistrationRequest;

public final class UserCacheMapper {
    private UserCacheMapper() {
    }

    public static UserCacheEntity fromRegistration(String userId, RegistrationRequest request, long updatedAtEpochMillis) {
        UserCacheEntity entity = new UserCacheEntity();
        entity.userId = userId;
        entity.email = request.getEmail();
        entity.username = request.getUsername();
        entity.avatarId = request.getAvatarId();
        entity.activationStatus = ActivationStatus.INACTIVE.name();
        entity.updatedAtEpochMillis = updatedAtEpochMillis;
        return entity;
    }

    public static UserCacheEntity updateActivationStatus(UserCacheEntity entity, ActivationStatus activationStatus, long updatedAtEpochMillis) {
        if (entity == null) {
            return null;
        }
        entity.activationStatus = activationStatus.name();
        entity.updatedAtEpochMillis = updatedAtEpochMillis;
        return entity;
    }
}
