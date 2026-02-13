package com.example.habitrpg.feature.profile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.domain.model.User;
import com.example.habitrpg.R;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.core.SimpleTextWatcher;
import com.example.habitrpg.databinding.FragmentProfileBinding;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends CoreFragment<FragmentProfileBinding> {

    private ProfileViewModel viewModel;

    @Override
    protected FragmentProfileBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupObservers();
        setupListeners();
        viewModel.handleAction(new ProfileAction.LoadProfile());
    }

    private void setupObservers() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            getBinding().loadingOverlay.setVisibility(state.loading ? View.VISIBLE : View.GONE);

            getBinding().tilOldPassword.setError(state.oldPasswordError);
            getBinding().tilNewPassword.setError(state.newPasswordError);
            getBinding().tilConfirmPassword.setError(state.confirmPasswordError);

            if (!getBinding().etOldPassword.getText().toString().equals(state.oldPassword)) {
                getBinding().etOldPassword.setText(state.oldPassword);
            }
            if (!getBinding().etNewPassword.getText().toString().equals(state.newPassword)) {
                getBinding().etNewPassword.setText(state.newPassword);
            }
            if (!getBinding().etConfirmPassword.getText().toString().equals(state.confirmPassword)) {
                getBinding().etConfirmPassword.setText(state.confirmPassword);
            }

            if (state.user != null) {
                bindUser(state.user);
            }

            getBinding().tvError.setVisibility(state.error == null ? View.GONE : View.VISIBLE);
            getBinding().tvError.setText(state.error);
        });

        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof ProfileSideEffect.ShowToast) {
                Toast.makeText(requireContext(), ((ProfileSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            } else if (effect instanceof ProfileSideEffect.NavigateToLogin) {
                Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_login);
            }
        });
    }

    private void setupListeners() {
        getBinding().etOldPassword.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.handleAction(new ProfileAction.OnOldPasswordChanged(s))));
        getBinding().etNewPassword.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.handleAction(new ProfileAction.OnNewPasswordChanged(s))));
        getBinding().etConfirmPassword.addTextChangedListener(new SimpleTextWatcher(s ->
                viewModel.handleAction(new ProfileAction.OnConfirmPasswordChanged(s))));

        getBinding().btnChangePassword.setOnClickListener(v ->
                viewModel.handleAction(new ProfileAction.OnChangePasswordClicked()));
        getBinding().btnLogout.setOnClickListener(v ->
                viewModel.handleAction(new ProfileAction.OnLogoutClicked()));
    }

    private void bindUser(User user) {
        getBinding().ivAvatar.setImageResource(getAvatarDrawable(user.avatarId));
        getBinding().tvUsername.setText(user.username);
        getBinding().tvLevel.setText(getString(R.string.profile_level_value, user.level));
        getBinding().tvTitle.setText(getString(R.string.profile_title_value, user.title));
        getBinding().tvPp.setText(getString(R.string.profile_pp_value, user.pp));
        getBinding().tvXp.setText(getString(R.string.profile_xp_value, user.xp));
        getBinding().tvCoins.setText(getString(R.string.profile_coins_value, user.coins));
        getBinding().tvBadges.setText(getString(R.string.profile_badges_value, user.badges.size(), formatList(user.badges)));
        getBinding().tvEquipment.setText(getString(R.string.profile_equipment_value, formatList(user.equipment)));

        Bitmap qrBitmap = generateQrCode(user.uid);
        if (qrBitmap != null)
            getBinding().ivQrCode.setImageBitmap(qrBitmap);
    }

    private String formatList(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return "nema";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private Bitmap generateQrCode(String text) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int getAvatarDrawable(int avatarId) {
        if (avatarId == 2) return R.drawable.avatar_2;
        if (avatarId == 3) return R.drawable.avatar_3;
        if (avatarId == 4) return R.drawable.avatar_4;
        if (avatarId == 5) return R.drawable.avatar_5;
        return R.drawable.avatar_1;
    }
}
