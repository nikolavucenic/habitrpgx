package com.example.habitrpg.feature;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
                SignUpUiState.Input inputState = (SignUpUiState.Input) state;
                getBinding().tilUsername.setError(inputState.usernameError);
                getBinding().tilEmail.setError(inputState.emailError);
                getBinding().tilPassword.setError(inputState.passwordError);
                getBinding().tilPasswordConfirm.setError(inputState.confirmPasswordError);

                int checkedAvatarId = getAvatarButtonId(inputState.getAvatarId());
                if (checkedAvatarId != -1 && getBinding().rgAvatars.getCheckedRadioButtonId() != checkedAvatarId) {
                    getBinding().rgAvatars.check(checkedAvatarId);
                }
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

        getBinding().etUsername.setOnEditorActionListener((v, actionId, event) -> handleNextAction(actionId, event, getBinding().etEmail));
        getBinding().etEmail.setOnEditorActionListener((v, actionId, event) -> handleNextAction(actionId, event, getBinding().etPassword));
        getBinding().etPassword.setOnEditorActionListener((v, actionId, event) -> handleNextAction(actionId, event, getBinding().etPasswordConfirm));
        getBinding().etPasswordConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                viewModel.handleAction(new SignUpAction.OnRegisterClicked());
                return true;
            }
            return false;
        });

        getBinding().rgAvatars.setOnCheckedChangeListener((group, checkedId) -> {
            int avatarId = 1;
            if (checkedId == R.id.avatar2) avatarId = 2;
            else if (checkedId == R.id.avatar3) avatarId = 3;
            else if (checkedId == R.id.avatar4) avatarId = 4;
            else if (checkedId == R.id.avatar5) avatarId = 5;
            viewModel.handleAction(new SignUpAction.OnAvatarSelected(avatarId));
        });
    }

    private boolean handleNextAction(int actionId, KeyEvent event, View nextView) {
        if (actionId == EditorInfo.IME_ACTION_NEXT || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            nextView.requestFocus();
            return true;
        }
        return false;
    }

    private int getAvatarButtonId(int avatarId) {
        if (avatarId == 1) return R.id.avatar1;
        if (avatarId == 2) return R.id.avatar2;
        if (avatarId == 3) return R.id.avatar3;
        if (avatarId == 4) return R.id.avatar4;
        if (avatarId == 5) return R.id.avatar5;
        return -1;
    }
}
