package com.example.habitrpg.di;

import com.example.domain.repository.AuthRepository;
import com.example.domain.repository.TaskRepository;
import com.example.domain.repository.SocialRepository;
import com.example.domain.usecase.ApplyBossBattleRewardsUseCase;
import com.example.domain.usecase.ChangePasswordUseCase;
import com.example.domain.usecase.ChangeTaskStatusUseCase;
import com.example.domain.usecase.CreateCategoryUseCase;
import com.example.domain.usecase.CreateTaskUseCase;
import com.example.domain.usecase.DeleteCategoryUseCase;
import com.example.domain.usecase.DeleteTaskUseCase;
import com.example.domain.usecase.GetBossHpUseCase;
import com.example.domain.usecase.GetBossNumberUseCase;
import com.example.domain.usecase.GetCategoriesUseCase;
import com.example.domain.usecase.GetCurrentUserProfileUseCase;
import com.example.domain.usecase.GetLastResolvedBossEncounterLevelUseCase;
import com.example.domain.usecase.GetStageSuccessRateUseCase;
import com.example.domain.usecase.GetTasksUseCase;
import com.example.domain.usecase.IsLoggedInUseCase;
import com.example.domain.usecase.IsPendingBossEncounterUseCase;
import com.example.domain.usecase.LoginUseCase;
import com.example.domain.usecase.LogoutUseCase;
import com.example.domain.usecase.SaveEquipmentStateUseCase;
import com.example.domain.usecase.PurchaseEquipmentUseCase;
import com.example.domain.usecase.RegisterUseCase;
import com.example.domain.usecase.RequestPasswordResetUseCase;
import com.example.domain.usecase.SaveBossStateUseCase;
import com.example.domain.usecase.SaveLastResolvedBossEncounterLevelUseCase;
import com.example.domain.usecase.SetPendingBossEncounterUseCase;
import com.example.domain.usecase.UpdateCategoryUseCase;
import com.example.domain.usecase.UpdateTaskUseCase;
import com.example.domain.usecase.SocialUseCase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

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
    public IsLoggedInUseCase provideIsLoggedInUseCase(AuthRepository authRepository) {
        return new IsLoggedInUseCase(authRepository);
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
    public RequestPasswordResetUseCase provideRequestPasswordResetUseCase(AuthRepository authRepository) {
        return new RequestPasswordResetUseCase(authRepository);
    }

    @Provides
    @Singleton
    public ApplyBossBattleRewardsUseCase provideApplyBossBattleRewardsUseCase(AuthRepository authRepository) {
        return new ApplyBossBattleRewardsUseCase(authRepository);
    }


    @Provides
    @Singleton
    public PurchaseEquipmentUseCase providePurchaseEquipmentUseCase(AuthRepository authRepository) {
        return new PurchaseEquipmentUseCase(authRepository);
    }

    @Provides
    @Singleton
    public SaveEquipmentStateUseCase provideSaveEquipmentStateUseCase(AuthRepository authRepository) {
        return new SaveEquipmentStateUseCase(authRepository);
    }
    @Provides
    @Singleton
    public LogoutUseCase provideLogoutUseCase(AuthRepository authRepository) {
        return new LogoutUseCase(authRepository);
    }

    @Provides
    @Singleton
    public IsPendingBossEncounterUseCase provideIsPendingBossEncounterUseCase(TaskRepository taskRepository) {
        return new IsPendingBossEncounterUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public SetPendingBossEncounterUseCase provideSetPendingBossEncounterUseCase(TaskRepository taskRepository) {
        return new SetPendingBossEncounterUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public GetLastResolvedBossEncounterLevelUseCase provideGetLastResolvedBossEncounterLevelUseCase(TaskRepository taskRepository) {
        return new GetLastResolvedBossEncounterLevelUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public SaveLastResolvedBossEncounterLevelUseCase provideSaveLastResolvedBossEncounterLevelUseCase(TaskRepository taskRepository) {
        return new SaveLastResolvedBossEncounterLevelUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public GetBossNumberUseCase provideGetBossNumberUseCase(TaskRepository taskRepository) {
        return new GetBossNumberUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public GetBossHpUseCase provideGetBossHpUseCase(TaskRepository taskRepository) {
        return new GetBossHpUseCase(taskRepository);
    }

    @Provides
    @Singleton
    public SaveBossStateUseCase provideSaveBossStateUseCase(TaskRepository taskRepository) {
        return new SaveBossStateUseCase(taskRepository);
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

    @Provides
    @Singleton
    public SocialUseCase provideSocialUseCase(SocialRepository socialRepository) {
        return new SocialUseCase(socialRepository);
    }
}
