package com.example.domain.validator;

public class AvatarValidator {

    public boolean isValid(int avatarId) {
        return avatarId >= 1 && avatarId <= 5;
    }
}
