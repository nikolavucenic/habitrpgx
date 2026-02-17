package com.example.habitrpg.feature.forgotpassword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.core.SimpleTextWatcher;
import com.example.habitrpg.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends CoreFragment<FragmentForgotPasswordBinding> {

    private ForgotPasswordViewModel viewModel;

    @Override
    protected FragmentForgotPasswordBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentForgotPasswordBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            boolean isLoading = state instanceof ForgotPasswordUiState.Loading;
            getBinding().loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            getBinding().btnSendResetEmail.setEnabled(!isLoading);
            getBinding().btnBackToLogin.setEnabled(!isLoading);

            if (state instanceof ForgotPasswordUiState.Input) {
                getBinding().tilEmail.setError(((ForgotPasswordUiState.Input) state).getEmailError());
            }
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof ForgotPasswordSideEffect.ShowToast) {
                Toast.makeText(getContext(), ((ForgotPasswordSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            } else if (effect instanceof ForgotPasswordSideEffect.NavigateBackToLogin) {
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    private void setupListeners() {
        getBinding().etEmail.addTextChangedListener(new SimpleTextWatcher(
                value -> viewModel.handleAction(new ForgotPasswordAction.OnEmailChanged(value))
        ));

        getBinding().btnSendResetEmail.setOnClickListener(v ->
                viewModel.handleAction(new ForgotPasswordAction.OnSendResetEmailClicked()));

        getBinding().btnBackToLogin.setOnClickListener(v ->
                viewModel.handleAction(new ForgotPasswordAction.OnBackToLoginClicked()));
    }
}
