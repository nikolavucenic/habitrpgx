package com.example.habitrpg.feature.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentProfileBinding;

public class ProfileFragment extends CoreFragment<FragmentProfileBinding> {
    @Override
    protected FragmentProfileBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }
}
