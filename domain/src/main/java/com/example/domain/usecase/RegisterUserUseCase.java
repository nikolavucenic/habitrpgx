package com.example.domain.usecase;

import com.example.domain.core.Result;
import com.example.domain.error.RegistrationError;
import com.example.domain.model.RegistrationRequest;
import com.example.domain.repository.AuthRepository;
import com.example.domain.validator.AvatarValidator;
import com.example.domain.validator.EmailValidator;
import com.example.domain.validator.PasswordMatchValidator;
import com.example.domain.validator.UsernameValidator;

public class RegisterUserUseCase {
    private final AuthRepository authRepository;
    private final EmailValidator emailValidator;
    private final PasswordMatchValidator passwordMatchValidator;
    private final UsernameValidator usernameValidator;
    private final AvatarValidator avatarValidator;

    public RegisterUserUseCase(
            AuthRepository authRepository,
            EmailValidator emailValidator,
            PasswordMatchValidator passwordMatchValidator,
            UsernameValidator usernameValidator,
            AvatarValidator avatarValidator
    ) {
        this.authRepository = authRepository;
        this.emailValidator = emailValidator;
        this.passwordMatchValidator = passwordMatchValidator;
        this.usernameValidator = usernameValidator;
        this.avatarValidator = avatarValidator;
    }

    public Result<String> execute(RegistrationRequest request) {
        if (!emailValidator.isValid(request.getEmail())) {
            return new Result.Error<>(RegistrationError.INVALID_EMAIL.message());
        }
        if (!passwordMatchValidator.isValid(request.getPassword(), request.getConfirmPassword())) {
            return new Result.Error<>(RegistrationError.PASSWORD_MISMATCH.message());
        }
        if (!usernameValidator.isValid(request.getUsername())) {
            return new Result.Error<>(RegistrationError.INVALID_USERNAME.message());
        }
        if (!avatarValidator.isValid(request.getAvatarId())) {
            return new Result.Error<>(RegistrationError.INVALID_AVATAR.message());
        }

        Result<String> repoResult = authRepository.register(request);
        if (repoResult instanceof Result.Error) {
            return new Result.Error<>(RegistrationError.REPOSITORY_FAILURE.message());
        }
        return repoResult;
    }
}
