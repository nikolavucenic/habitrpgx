package com.example.habitrpg.di;

import com.example.domain.repository.AuthRepository;
import com.example.domain.usecase.ActivateAccountUseCase;
import com.example.domain.usecase.GenerateActivationLinkUseCase;
import com.example.domain.usecase.RegisterUserUseCase;
import com.example.domain.validator.AvatarValidator;
import com.example.domain.validator.EmailValidator;
import com.example.domain.validator.PasswordMatchValidator;
import com.example.domain.validator.UsernameValidator;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class DomainUseCaseModule {

    private DomainUseCaseModule() {
    }

    @Provides
    public static EmailValidator provideEmailValidator() {
        return new EmailValidator();
    }

    @Provides
    public static PasswordMatchValidator providePasswordMatchValidator() {
        return new PasswordMatchValidator();
    }

    @Provides
    public static UsernameValidator provideUsernameValidator() {
        return new UsernameValidator();
    }

    @Provides
    public static AvatarValidator provideAvatarValidator() {
        return new AvatarValidator();
    }

    @Provides
    public static RegisterUserUseCase provideRegisterUserUseCase(
            AuthRepository authRepository,
            EmailValidator emailValidator,
            PasswordMatchValidator passwordMatchValidator,
            UsernameValidator usernameValidator,
            AvatarValidator avatarValidator
    ) {
        return new RegisterUserUseCase(
                authRepository,
                emailValidator,
                passwordMatchValidator,
                usernameValidator,
                avatarValidator
        );
    }

    @Provides
    public static GenerateActivationLinkUseCase provideGenerateActivationLinkUseCase() {
        return new GenerateActivationLinkUseCase();
    }

    @Provides
    public static ActivateAccountUseCase provideActivateAccountUseCase(AuthRepository authRepository) {
        return new ActivateAccountUseCase(authRepository, true);
    }
}
