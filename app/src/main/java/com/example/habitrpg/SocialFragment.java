package com.example.habitrpg;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentSocialBinding;

public class SocialFragment extends CoreFragment<FragmentSocialBinding> {
    @Override
    protected FragmentSocialBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSocialBinding.inflate(inflater, container, false);
    }
}
