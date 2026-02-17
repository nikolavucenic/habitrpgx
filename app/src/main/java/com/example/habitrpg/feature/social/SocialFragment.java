package com.example.habitrpg.feature.social;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.domain.model.SocialModels;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentSocialBinding;
import com.example.habitrpg.databinding.ItemSocialFriendBinding;
import com.example.habitrpg.databinding.ItemSocialInviteBinding;
import com.example.habitrpg.databinding.ItemSocialMessageBinding;
import com.example.habitrpg.databinding.ItemSocialSearchBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.Locale;

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

        getBinding().btnSearch.setOnClickListener(v -> viewModel.handleAction(new SocialAction.SearchUsers(textOf(getBinding().etSearch))));
        getBinding().btnRefresh.setOnClickListener(v -> viewModel.handleAction(new SocialAction.Load()));
        getBinding().btnCreateAlliance.setOnClickListener(v -> {
            String name = textOf(getBinding().etAllianceName).trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Unesite naziv saveza", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.handleAction(new SocialAction.CreateAlliance(name));
        });
        getBinding().btnStartMission.setOnClickListener(v -> viewModel.handleAction(new SocialAction.StartMission()));
        getBinding().btnSendMessage.setOnClickListener(v -> {
            String message = textOf(getBinding().etMessage).trim();
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
        getBinding().loading.setVisibility(state.loading ? View.VISIBLE : View.GONE);

        renderSearchResults(state);
        renderFriends(state);
        renderInvites(state);
        renderAlliance(state);
        renderMessages(state);
    }

    private void renderSearchResults(SocialUiState state) {
        getBinding().containerSearchResults.removeAllViews();
        getBinding().tvSearchEmpty.setVisibility(state.searchResults.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.Friend user : state.searchResults) {
            ItemSocialSearchBinding row = ItemSocialSearchBinding.inflate(inflater, getBinding().containerSearchResults, false);
            row.tvUsername.setText(user.username + "  (" + user.uid + ")");
            row.btnAdd.setOnClickListener(v -> viewModel.handleAction(new SocialAction.AddFriend(user.uid)));
            getBinding().containerSearchResults.addView(row.getRoot());
        }
    }

    private void renderFriends(SocialUiState state) {
        getBinding().containerFriends.removeAllViews();
        getBinding().tvFriendsEmpty.setVisibility(state.friends.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.Friend friend : state.friends) {
            ItemSocialFriendBinding row = ItemSocialFriendBinding.inflate(inflater, getBinding().containerFriends, false);
            row.tvUsername.setText(friend.username);
            row.tvMeta.setText("avatar #" + friend.avatarId);
            getBinding().containerFriends.addView(row.getRoot());
        }
    }

    private void renderInvites(SocialUiState state) {
        getBinding().containerInvites.removeAllViews();
        getBinding().tvInvitesEmpty.setVisibility(state.invites.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.AllianceInvite invite : state.invites) {
            ItemSocialInviteBinding row = ItemSocialInviteBinding.inflate(inflater, getBinding().containerInvites, false);
            row.tvTitle.setText(invite.allianceName);
            row.tvSubtitle.setText("Pozvao: " + invite.fromUsername);
            row.btnAccept.setOnClickListener(v -> viewModel.handleAction(new SocialAction.AcceptInvite(invite.id)));
            row.btnDecline.setOnClickListener(v -> viewModel.handleAction(new SocialAction.DeclineInvite(invite.id)));
            getBinding().containerInvites.addView(row.getRoot());
        }
    }

    private void renderAlliance(SocialUiState state) {
        if (state.alliance == null) {
            getBinding().tvAllianceTitle.setText("Niste član saveza");
            getBinding().tvAllianceMeta.setText("Prihvatite pozivnicu ili kreirajte novi savez.");
            getBinding().progressBoss.setVisibility(View.GONE);
            getBinding().tvBossHp.setVisibility(View.GONE);
            return;
        }

        getBinding().tvAllianceTitle.setText("Savez: " + state.alliance.name);
        getBinding().tvAllianceMeta.setText(String.format(Locale.getDefault(), "Vođa: %s • Članovi: %d • %s",
                state.alliance.leaderUsername,
                state.alliance.members.size(),
                state.alliance.missionActive ? "Misija aktivna" : "Misija nije pokrenuta"));

        if (state.alliance.missionActive && state.alliance.bossMaxHp > 0) {
            getBinding().progressBoss.setVisibility(View.VISIBLE);
            getBinding().tvBossHp.setVisibility(View.VISIBLE);
            getBinding().progressBoss.setMax(state.alliance.bossMaxHp);
            getBinding().progressBoss.setProgress(Math.max(0, state.alliance.bossHp));
            getBinding().tvBossHp.setText("Boss HP: " + state.alliance.bossHp + " / " + state.alliance.bossMaxHp);
        } else {
            getBinding().progressBoss.setVisibility(View.GONE);
            getBinding().tvBossHp.setVisibility(View.GONE);
        }
    }

    private void renderMessages(SocialUiState state) {
        getBinding().containerMessages.removeAllViews();
        getBinding().tvMessagesEmpty.setVisibility(state.messages.isEmpty() ? View.VISIBLE : View.GONE);

        String myUid = FirebaseAuth.getInstance().getCurrentUser() == null ? "" : FirebaseAuth.getInstance().getCurrentUser().getUid();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.AllianceMessage message : state.messages) {
            ItemSocialMessageBinding row = ItemSocialMessageBinding.inflate(inflater, getBinding().containerMessages, false);
            boolean mine = message.senderUid.equals(myUid);

            row.tvBubble.setText(message.message);
            row.tvMeta.setText(message.senderUsername + " • " + DateFormat.format("dd.MM HH:mm", new Date(message.createdAt)));

            ((ViewGroup.MarginLayoutParams) row.tvBubble.getLayoutParams()).setMargins(0, 0, 0, 0);
            ((ViewGroup.MarginLayoutParams) row.tvMeta.getLayoutParams()).setMargins(0, 4, 0, 0);
            row.root.setGravity(mine ? Gravity.END : Gravity.START);
            row.tvBubble.setBackgroundResource(mine ? android.R.drawable.dialog_holo_light_frame : android.R.drawable.editbox_background_normal);

            row.tvMeta.setTextAlignment(mine ? TextView.TEXT_ALIGNMENT_VIEW_END : TextView.TEXT_ALIGNMENT_VIEW_START);
            getBinding().containerMessages.addView(row.getRoot());
        }
    }

    private String textOf(TextView view) {
        return view.getText() == null ? "" : view.getText().toString();
    }
}
