package com.example.habitrpg.presentation.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.core.Result;
import com.example.domain.model.ActivationToken;
import com.example.domain.model.RegistrationRequest;
import com.example.domain.repository.AuthRepository;
import com.example.domain.usecase.GenerateActivationLinkUseCase;
import com.example.domain.usecase.RegisterUserUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private static final String ACTIVATION_BASE_URI = "myapp://activate?token=";

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthRepository authRepository;
    private final GenerateActivationLinkUseCase generateActivationLinkUseCase;

    private final MutableLiveData<RegisterUiState> uiState = new MutableLiveData<>(RegisterUiState.idle());

    @Inject
    public RegisterViewModel(
            RegisterUserUseCase registerUserUseCase,
            AuthRepository authRepository,
            GenerateActivationLinkUseCase generateActivationLinkUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.authRepository = authRepository;
        this.generateActivationLinkUseCase = generateActivationLinkUseCase;
    }

    public LiveData<RegisterUiState> getUiState() {
        return uiState;
    }

    public void register(String email, String password, String confirmPassword, String username, int avatarId) {
        uiState.setValue(RegisterUiState.loading());

        RegistrationRequest request = new RegistrationRequest(email, password, confirmPassword, username, avatarId);
        Result<String> registerResult = registerUserUseCase.execute(request);

        if (registerResult instanceof Result.Error) {
            String message = ((Result.Error<String>) registerResult).message;
            uiState.setValue(RegisterUiState.error(message));
            return;
        }

        String userId = ((Result.Success<String>) registerResult).data;
        Result<ActivationToken> activationResult = authRepository.createActivationToken(userId);

        if (activationResult instanceof Result.Error) {
            String message = ((Result.Error<ActivationToken>) activationResult).message;
            uiState.setValue(RegisterUiState.error(message));
            return;
        }

        ActivationToken activationToken = ((Result.Success<ActivationToken>) activationResult).data;
        String link = generateActivationLinkUseCase.execute(ACTIVATION_BASE_URI, activationToken.getToken());
        uiState.setValue(RegisterUiState.success(link));
    }

    public static class RegisterUiState {
        public final boolean loading;
        public final String activationLink;
        public final String errorMessage;

        private RegisterUiState(boolean loading, String activationLink, String errorMessage) {
            this.loading = loading;
            this.activationLink = activationLink;
            this.errorMessage = errorMessage;
        }

        public static RegisterUiState idle() {
            return new RegisterUiState(false, null, null);
        }

        public static RegisterUiState loading() {
            return new RegisterUiState(true, null, null);
        }

        public static RegisterUiState success(String activationLink) {
            return new RegisterUiState(false, activationLink, null);
        }

        public static RegisterUiState error(String message) {
            return new RegisterUiState(false, null, message);
        }
    }
}
