package com.example.habitrpg;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentShopBinding;

public class ShopFragment extends CoreFragment<FragmentShopBinding> {
    @Override
    protected FragmentShopBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentShopBinding.inflate(inflater, container, false);
    }
}
