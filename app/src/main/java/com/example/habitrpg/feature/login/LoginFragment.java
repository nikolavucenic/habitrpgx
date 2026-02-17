package com.example.habitrpg.feature.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.core.SimpleTextWatcher;
import com.example.habitrpg.databinding.FragmentLoginBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends CoreFragment<FragmentLoginBinding> {

    private LoginViewModel viewModel;

    @Override
    protected FragmentLoginBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentLoginBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof LoginUiState.Input) {
                LoginUiState.Input inputState = (LoginUiState.Input) state;
                getBinding().tilEmail.setError(inputState.getEmailError());
                getBinding().tilPassword.setError(inputState.getPasswordError());
                getBinding().loadingOverlay.setVisibility(View.GONE);
                getBinding().btnLogin.setEnabled(true);
            }
            else if (state instanceof LoginUiState.Loading) {
                getBinding().loadingOverlay.setVisibility(View.VISIBLE);
                getBinding().btnLogin.setEnabled(false);
            }
            else if (state instanceof LoginUiState.Error) {
                LoginUiState.Error errorState = (LoginUiState.Error) state;
                getBinding().loadingOverlay.setVisibility(View.GONE);
                getBinding().tilEmail.setError(errorState.getEmailError());
                getBinding().tilPassword.setError(errorState.getPasswordError());
                getBinding().btnLogin.setEnabled(true);
            }
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof LoginSideEffect.NavigateToHome) {
                Navigation.findNavController(requireView()).navigate(R.id.action_auth_to_profile);
            }
            else if (effect instanceof LoginSideEffect.NavigateToRegister) {
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_signUpFragment);
            }
            else if (effect instanceof LoginSideEffect.NavigateToForgotPassword) {
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
            }
            else if (effect instanceof LoginSideEffect.ShowToast) {
                Toast.makeText(getContext(), ((LoginSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        getBinding().btnLogin.setOnClickListener(v -> viewModel.handleAction(new LoginAction.OnLoginClicked()));
        getBinding().btnGoToRegister.setOnClickListener(v -> viewModel.handleAction(new LoginAction.OnGoToRegisterClicked()));
        getBinding().btnForgotPassword.setOnClickListener(v -> viewModel.handleAction(new LoginAction.OnGoToForgotPasswordClicked()));

        getBinding().etEmail.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.handleAction(new LoginAction.OnEmailChanged(s))
        ));
        getBinding().etPassword.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.handleAction(new LoginAction.OnPasswordChanged(s))
        ));
    }
}
