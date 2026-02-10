package com.example.data.di;

import com.example.data.repository.AuthRepositoryImpl;
import com.example.data.repository.SettingsRepositoryImpl;
import com.example.domain.repository.AuthRepository;
import com.example.domain.repository.SettingsRepository;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract SettingsRepository bindSettingsRepository(SettingsRepositoryImpl impl);
}
