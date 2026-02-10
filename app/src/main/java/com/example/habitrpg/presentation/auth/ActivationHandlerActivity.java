package com.example.habitrpg.presentation.auth;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitrpg.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivationHandlerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation_handler);

        TextView resultText = findViewById(R.id.text_activation_result);
        ActivationHandlerViewModel viewModel = new ViewModelProvider(this).get(ActivationHandlerViewModel.class);

        viewModel.getUiState().observe(this, state -> {
            if (state == null) {
                return;
            }
            if (state.success) {
                resultText.setText(R.string.activation_success);
            } else if (state.errorMessage != null) {
                resultText.setText(getString(R.string.activation_failed_with_reason, state.errorMessage));
            }
        });

        Uri data = getIntent() != null ? getIntent().getData() : null;
        String token = data != null ? data.getQueryParameter("token") : null;
        viewModel.activate(token);
    }
}
