package com.example.habitrpg.di;

import com.example.domain.repository.AuthRepository;
import com.example.domain.repository.TaskRepository;
import com.example.domain.usecase.ChangeTaskStatusUseCase;
import com.example.domain.usecase.ChangePasswordUseCase;
import com.example.domain.usecase.ApplyBossBattleRewardsUseCase;
import com.example.domain.usecase.CreateCategoryUseCase;
import com.example.domain.usecase.CreateTaskUseCase;
import com.example.domain.usecase.DeleteCategoryUseCase;
import com.example.domain.usecase.DeleteTaskUseCase;
import com.example.domain.usecase.GetCategoriesUseCase;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.GetTasksUseCase;
import com.example.domain.usecase.GetStageSuccessRateUseCase;
import com.example.domain.usecase.LoginUseCase;
import com.example.domain.usecase.LogoutUseCase;
import com.example.domain.usecase.RegisterUseCase;
import com.example.domain.usecase.UpdateCategoryUseCase;
import com.example.domain.usecase.UpdateTaskUseCase;

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

    @Provides
    @Singleton
    public ChangePasswordUseCase provideChangePasswordUseCase(AuthRepository authRepository) {
        return new ChangePasswordUseCase(authRepository);
    }

    @Provides
    @Singleton
    public ApplyBossBattleRewardsUseCase provideApplyBossBattleRewardsUseCase(AuthRepository authRepository) {
        return new ApplyBossBattleRewardsUseCase(authRepository);
    }

    @Provides
    @Singleton
    public LogoutUseCase provideLogoutUseCase(AuthRepository authRepository) {
        return new LogoutUseCase(authRepository);
    }

    @Provides
    @Singleton
    public GetTasksUseCase provideGetTasksUseCase(TaskRepository taskRepository) {
        return new GetTasksUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public GetCategoriesUseCase provideGetCategoriesUseCase(TaskRepository taskRepository) {
        return new GetCategoriesUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public CreateTaskUseCase provideCreateTaskUseCase(TaskRepository taskRepository) {
        return new CreateTaskUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public UpdateTaskUseCase provideUpdateTaskUseCase(TaskRepository taskRepository) {
        return new UpdateTaskUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public DeleteTaskUseCase provideDeleteTaskUseCase(TaskRepository taskRepository) {
        return new DeleteTaskUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public CreateCategoryUseCase provideCreateCategoryUseCase(TaskRepository taskRepository) {
        return new CreateCategoryUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public UpdateCategoryUseCase provideUpdateCategoryUseCase(TaskRepository taskRepository) {
        return new UpdateCategoryUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public DeleteCategoryUseCase provideDeleteCategoryUseCase(TaskRepository taskRepository) {
        return new DeleteCategoryUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public GetStageSuccessRateUseCase provideGetStageSuccessRateUseCase(TaskRepository taskRepository) {
        return new GetStageSuccessRateUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public ChangeTaskStatusUseCase provideChangeTaskStatusUseCase(TaskRepository taskRepository) {
        return new ChangeTaskStatusUseCase(taskRepository);
    }
}
