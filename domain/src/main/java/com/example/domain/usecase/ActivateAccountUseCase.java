package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.error.ActivationError;
import com.example.domain.model.ActivationToken;
import com.example.domain.repository.AuthRepository;

public class ActivateAccountUseCase {
    public static final long ACTIVATION_WINDOW_MILLIS = 24L * 60L * 60L * 1000L;

    private final AuthRepository authRepository;
    private final boolean demoOverrideEnabled;

    public ActivateAccountUseCase(AuthRepository authRepository, boolean demoOverrideEnabled) {
        this.authRepository = authRepository;
        this.demoOverrideEnabled = demoOverrideEnabled;
    }

    public Result<Void> execute(ActivationToken activationToken, long nowEpochMillis) {
        if (!demoOverrideEnabled) {
            long ageMillis = nowEpochMillis - activationToken.getCreatedAtEpochMillis();
            if (ageMillis > ACTIVATION_WINDOW_MILLIS) {
                return new Result.Error<>(ActivationError.TOKEN_EXPIRED.message());
            }
        }

        Result<Void> result = authRepository.activate(activationToken.getToken());
        if (result instanceof Result.Error) {
            return new Result.Error<>(ActivationError.REPOSITORY_FAILURE.message());
        }
        return result;
    }
}
