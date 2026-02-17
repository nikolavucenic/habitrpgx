package com.example.habitrpg.feature.social;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.domain.model.SocialModels;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentSocialBinding;

import java.text.DateFormat;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SocialFragment extends CoreFragment<FragmentSocialBinding> {

    private SocialViewModel viewModel;

    @Override
    protected FragmentSocialBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSocialBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SocialViewModel.class);

        getBinding().btnSearch.setOnClickListener(v -> viewModel.handleAction(new SocialAction.SearchUsers(getBinding().etSearch.getText().toString())));
        getBinding().btnCreateAlliance.setOnClickListener(v -> viewModel.handleAction(new SocialAction.CreateAlliance(getBinding().etAllianceName.getText().toString().trim())));
        getBinding().btnStartMission.setOnClickListener(v -> viewModel.handleAction(new SocialAction.StartMission()));
        getBinding().btnSendMessage.setOnClickListener(v -> {
            String message = getBinding().etMessage.getText().toString().trim();
            if (message.isEmpty()) return;
            viewModel.handleAction(new SocialAction.SendMessage(message));
            getBinding().etMessage.setText("");
        });

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof SocialSideEffect.ShowToast) {
                Toast.makeText(requireContext(), ((SocialSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.handleAction(new SocialAction.Load());
    }

    private void render(SocialUiState state) {
        StringBuilder search = new StringBuilder("Rezultati pretrage:\n");
        for (SocialModels.Friend user : state.searchResults) search.append("• ").append(user.username).append(" (ID: ").append(user.uid).append(")\n");
        search.append("Unesite QR/UID u polje pretrage i kliknite + kroz dugme Dodaj iz rezultata trenutno nije podržano u listi, koristi se tap na toast workflow.");
        getBinding().tvSearchResults.setText(search.toString());
        getBinding().tvSearchResults.setOnClickListener(v -> {
            if (!state.searchResults.isEmpty()) viewModel.handleAction(new SocialAction.AddFriend(state.searchResults.get(0).uid));
        });

        StringBuilder friends = new StringBuilder("Prijatelji:\n");
        for (SocialModels.Friend friend : state.friends) friends.append("• ").append(friend.username).append("\n");
        getBinding().tvFriends.setText(friends.toString());

        StringBuilder invites = new StringBuilder("Pozivnice (klik = prihvati, long klik = odbij):\n");
        for (SocialModels.AllianceInvite invite : state.invites) invites.append("• ").append(invite.allianceName).append(" / od: ").append(invite.fromUsername).append(" / id: ").append(invite.id).append("\n");
        getBinding().tvInvites.setText(invites.toString());
        getBinding().tvInvites.setOnClickListener(v -> { if (!state.invites.isEmpty()) viewModel.handleAction(new SocialAction.AcceptInvite(state.invites.get(0).id)); });
        getBinding().tvInvites.setOnLongClickListener(v -> {
            if (!state.invites.isEmpty()) viewModel.handleAction(new SocialAction.DeclineInvite(state.invites.get(0).id));
            return true;
        });

        if (state.alliance == null) {
            getBinding().tvAlliance.setText("Niste član saveza.");
        } else {
            StringBuilder alliance = new StringBuilder();
            alliance.append("Savez: ").append(state.alliance.name).append("\nVođa: ").append(state.alliance.leaderUsername)
                    .append("\nČlanovi: ").append(state.alliance.members.size())
                    .append("\nMisija: ").append(state.alliance.missionActive ? "aktivna" : "nije pokrenuta");
            if (state.alliance.missionActive) {
                alliance.append("\nBoss HP: ").append(state.alliance.bossHp).append("/").append(state.alliance.bossMaxHp);
            }
            getBinding().tvAlliance.setText(alliance.toString());
        }

        StringBuilder messages = new StringBuilder("Poruke saveza:\n");
        for (SocialModels.AllianceMessage message : state.messages) {
            String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.createdAt));
            messages.append(message.senderUsername).append(" [").append(time).append("]: ").append(message.message).append("\n");
        }
        getBinding().tvMessages.setText(messages.toString());
    }
}
