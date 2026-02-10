package com.example.habitrpg.presentation.auth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habitrpg.R;

public class ActivationPendingFragment extends Fragment {

    private static final String ARG_LINK = "arg_link";

    public static ActivationPendingFragment newInstance(String activationLink) {
        ActivationPendingFragment fragment = new ActivationPendingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LINK, activationLink);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activation_pending, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String activationLink = getArguments() != null ? getArguments().getString(ARG_LINK, "") : "";

        TextView linkText = view.findViewById(R.id.text_activation_link);
        Button copyButton = view.findViewById(R.id.btn_copy_link);
        Button openButton = view.findViewById(R.id.btn_open_link);

        linkText.setText(activationLink);

        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("activation_link", activationLink);
            clipboardManager.setPrimaryClip(clip);
            Toast.makeText(requireContext(), R.string.activation_link_copied, Toast.LENGTH_SHORT).show();
        });

        openButton.setOnClickListener(v -> {
            Intent openIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activationLink));
            if (openIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(openIntent);
            } else {
                Toast.makeText(requireContext(), R.string.activation_link_open_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
