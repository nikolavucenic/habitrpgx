package com.example.data.di;

import com.example.data.repository.AuthRepositoryImpl;
import com.example.data.repository.TaskRepositoryImpl;
import com.example.domain.repository.AuthRepository;
import com.example.domain.repository.TaskRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public abstract class DataModule {

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl authRepositoryImpl);

    @Binds
    @Singleton
    public abstract TaskRepository bindTaskRepository(TaskRepositoryImpl taskRepositoryImpl);
}
