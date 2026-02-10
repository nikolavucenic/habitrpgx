package com.example.habitrpg.presentation.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private EditText usernameInput;
    private RadioGroup avatarGroup;

    private RegisterViewModel viewModel;
    private Navigator navigator;

    public interface Navigator {
        void openActivationPending(String activationLink);
    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Navigator) {
            navigator = (Navigator) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        emailInput = view.findViewById(R.id.input_email);
        passwordInput = view.findViewById(R.id.input_password);
        confirmInput = view.findViewById(R.id.input_confirm_password);
        usernameInput = view.findViewById(R.id.input_username);
        avatarGroup = view.findViewById(R.id.avatar_group);
        Button registerButton = view.findViewById(R.id.btn_register);

        registerButton.setOnClickListener(v -> {
            clearErrors();
            int avatarId = selectedAvatarId();
            viewModel.register(
                    emailInput.getText().toString().trim(),
                    passwordInput.getText().toString(),
                    confirmInput.getText().toString(),
                    usernameInput.getText().toString().trim(),
                    avatarId
            );
        });

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }
            if (state.errorMessage != null) {
                showValidationError(state.errorMessage);
            } else if (state.activationLink != null && navigator != null) {
                navigator.openActivationPending(state.activationLink);
            }
        });
    }

    private void clearErrors() {
        emailInput.setError(null);
        passwordInput.setError(null);
        confirmInput.setError(null);
        usernameInput.setError(null);
    }

    private int selectedAvatarId() {
        int checkedId = avatarGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.avatar_1) return 1;
        if (checkedId == R.id.avatar_2) return 2;
        if (checkedId == R.id.avatar_3) return 3;
        if (checkedId == R.id.avatar_4) return 4;
        if (checkedId == R.id.avatar_5) return 5;
        return -1;
    }

    private void showValidationError(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            Toast.makeText(requireContext(), R.string.registration_error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        if (errorMessage.toLowerCase().contains("email")) {
            emailInput.setError(errorMessage);
        } else if (errorMessage.toLowerCase().contains("password")) {
            confirmInput.setError(errorMessage);
        } else if (errorMessage.toLowerCase().contains("username")) {
            usernameInput.setError(errorMessage);
        } else if (errorMessage.toLowerCase().contains("avatar")) {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
