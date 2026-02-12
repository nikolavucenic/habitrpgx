package com.example.habitrpg.feature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.core.SimpleTextWatcher;
import com.example.habitrpg.databinding.FragmentSignUpBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpFragment extends CoreFragment<FragmentSignUpBinding> {

    private SignUpViewModel viewModel;

    @Override
    protected FragmentSignUpBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSignUpBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            boolean isLoading = state instanceof SignUpUiState.Loading;
            getBinding().btnRegister.setEnabled(!isLoading);
            getBinding().btnBackToLogin.setEnabled(!isLoading);
            if (state instanceof SignUpUiState.Input) {
                getBinding().tilUsername.setError(((SignUpUiState.Input) state).error);
            }
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof SignUpSideEffect.ShowToast) {
                Toast.makeText(getContext(), ((SignUpSideEffect.ShowToast) effect).message, Toast.LENGTH_LONG).show();
            } else if (effect instanceof SignUpSideEffect.NavigateToLogin) {
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    private void setupListeners() {
        getBinding().etUsername.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.handleAction(new SignUpAction.OnUsernameChanged(s))));
        getBinding().etEmail.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.handleAction(new SignUpAction.OnEmailChanged(s))));
        getBinding().etPassword.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.handleAction(new SignUpAction.OnPasswordChanged(s))));
        getBinding().etPasswordConfirm.addTextChangedListener(new SimpleTextWatcher(s -> viewModel.handleAction(new SignUpAction.OnConfirmPasswordChanged(s))));
        getBinding().btnRegister.setOnClickListener(v -> viewModel.handleAction(new SignUpAction.OnRegisterClicked()));
        getBinding().btnBackToLogin.setOnClickListener(v -> viewModel.handleAction(new SignUpAction.OnBackToLoginClicked()));

        getBinding().rgAvatars.setOnCheckedChangeListener((group, checkedId) -> {
            int avatarId = 1;
            if (checkedId == R.id.avatar2) avatarId = 2;
            else if (checkedId == R.id.avatar3) avatarId = 3;
            else if (checkedId == R.id.avatar4) avatarId = 4;
            else if (checkedId == R.id.avatar5) avatarId = 5;
            viewModel.handleAction(new SignUpAction.OnAvatarSelected(avatarId));
        });
    }
}
