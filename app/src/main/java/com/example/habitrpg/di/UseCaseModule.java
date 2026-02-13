package com.example.habitrpg.di;

import com.example.domain.repository.AuthRepository;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.LoginUseCase;
import com.example.domain.usecase.RegisterUseCase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    @Singleton
    public LoginUseCase provideLoginUseCase(AuthRepository authRepository) {
        return new LoginUseCase(authRepository);
    }

    @Provides
    @Singleton
    public RegisterUseCase provideRegisterUseCase(AuthRepository authRepository) {
        return new RegisterUseCase(authRepository);
    }

    @Provides
    @Singleton
    public GetCurrentUserProfileUseCase provideGetCurrentUserProfileUseCase(AuthRepository authRepository) {
        return new GetCurrentUserProfileUseCase(authRepository);
    }
}

