package com.example.habitrpg.presentation.auth;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habitrpg.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthActivity extends AppCompatActivity implements RegisterFragment.Navigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, RegisterFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void openActivationPending(String activationLink) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, ActivationPendingFragment.newInstance(activationLink))
                .addToBackStack(null)
                .commit();
    }
}
