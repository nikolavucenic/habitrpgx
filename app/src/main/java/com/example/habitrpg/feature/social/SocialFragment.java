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
import com.example.habitrpg.R;
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
                showToast(getString(R.string.social_toast_alliance_name_required));
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
                SocialSideEffect.ShowToast toast = (SocialSideEffect.ShowToast) effect;
                if (toast.messageRes != 0) showToast(getString(toast.messageRes));
                else if (toast.message != null && !toast.message.trim().isEmpty()) showToast(toast.message);
            }
        });

        viewModel.handleAction(new SocialAction.Load());
    }

    private void render(SocialUiState state) {
        SocialUiState.Data data = state.getData();
        getBinding().loading.setVisibility(state instanceof SocialUiState.Loading ? View.VISIBLE : View.GONE);

        if (state instanceof SocialUiState.Error && data.error != null && !data.error.trim().isEmpty()) {
            showToast(data.error);
        }

        renderSearchResults(data);
        renderFriends(data);
        renderInvites(data);
        renderAlliance(data);
        renderMessages(data);
    }

    private void renderSearchResults(SocialUiState.Data data) {
        getBinding().containerSearchResults.removeAllViews();
        getBinding().tvSearchEmpty.setVisibility(data.searchResults.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.Friend user : data.searchResults) {
            ItemSocialSearchBinding row = ItemSocialSearchBinding.inflate(inflater, getBinding().containerSearchResults, false);
            row.tvUsername.setText(getString(R.string.social_search_result_value, user.username, user.uid));
            row.btnAdd.setOnClickListener(v -> viewModel.handleAction(new SocialAction.AddFriend(user.uid)));
            getBinding().containerSearchResults.addView(row.getRoot());
        }
    }

    private void renderFriends(SocialUiState.Data data) {
        getBinding().containerFriends.removeAllViews();
        getBinding().tvFriendsEmpty.setVisibility(data.friends.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.Friend friend : data.friends) {
            ItemSocialFriendBinding row = ItemSocialFriendBinding.inflate(inflater, getBinding().containerFriends, false);
            row.tvUsername.setText(friend.username);
            row.tvMeta.setText(getString(R.string.social_friend_avatar_value, friend.avatarId));
            getBinding().containerFriends.addView(row.getRoot());
        }
    }

    private void renderInvites(SocialUiState.Data data) {
        getBinding().containerInvites.removeAllViews();
        getBinding().tvInvitesEmpty.setVisibility(data.invites.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.AllianceInvite invite : data.invites) {
            ItemSocialInviteBinding row = ItemSocialInviteBinding.inflate(inflater, getBinding().containerInvites, false);
            row.tvTitle.setText(invite.allianceName);
            row.tvSubtitle.setText(getString(R.string.social_invite_from_value, invite.fromUsername));
            row.btnAccept.setOnClickListener(v -> viewModel.handleAction(new SocialAction.AcceptInvite(invite.id)));
            row.btnDecline.setOnClickListener(v -> viewModel.handleAction(new SocialAction.DeclineInvite(invite.id)));
            getBinding().containerInvites.addView(row.getRoot());
        }
    }

    private void renderAlliance(SocialUiState.Data data) {
        if (data.alliance == null) {
            getBinding().tvAllianceTitle.setText(R.string.social_alliance_none_title);
            getBinding().tvAllianceMeta.setText(R.string.social_alliance_none_subtitle);
            getBinding().progressBoss.setVisibility(View.GONE);
            getBinding().tvBossHp.setVisibility(View.GONE);
            return;
        }

        getBinding().tvAllianceTitle.setText(getString(R.string.social_alliance_title_value, data.alliance.name));
        getBinding().tvAllianceMeta.setText(String.format(Locale.getDefault(), "%s • %s • %s",
                getString(R.string.social_alliance_leader_value, data.alliance.leaderUsername),
                getString(R.string.social_alliance_members_value, data.alliance.members.size()),
                data.alliance.missionActive ? getString(R.string.social_alliance_mission_active) : getString(R.string.social_alliance_mission_inactive)));

        if (data.alliance.missionActive && data.alliance.bossMaxHp > 0) {
            getBinding().progressBoss.setVisibility(View.VISIBLE);
            getBinding().tvBossHp.setVisibility(View.VISIBLE);
            getBinding().progressBoss.setMax(data.alliance.bossMaxHp);
            getBinding().progressBoss.setProgress(Math.max(0, data.alliance.bossHp));
            getBinding().tvBossHp.setText(getString(R.string.social_boss_hp_value, data.alliance.bossHp, data.alliance.bossMaxHp));
        } else {
            getBinding().progressBoss.setVisibility(View.GONE);
            getBinding().tvBossHp.setVisibility(View.GONE);
        }
    }

    private void renderMessages(SocialUiState.Data data) {
        getBinding().containerMessages.removeAllViews();
        getBinding().tvMessagesEmpty.setVisibility(data.messages.isEmpty() ? View.VISIBLE : View.GONE);

        String myUid = FirebaseAuth.getInstance().getCurrentUser() == null ? "" : FirebaseAuth.getInstance().getCurrentUser().getUid();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SocialModels.AllianceMessage message : data.messages) {
            ItemSocialMessageBinding row = ItemSocialMessageBinding.inflate(inflater, getBinding().containerMessages, false);
            boolean mine = message.senderUid.equals(myUid);

            row.tvBubble.setText(message.message);
            row.tvMeta.setText(getString(R.string.social_message_meta_value,
                    message.senderUsername,
                    DateFormat.format("dd.MM HH:mm", new Date(message.createdAt))));

            row.root.setGravity(mine ? Gravity.END : Gravity.START);
            row.tvBubble.setBackgroundResource(mine ? android.R.drawable.dialog_holo_light_frame : android.R.drawable.editbox_background_normal);
            row.tvMeta.setTextAlignment(mine ? TextView.TEXT_ALIGNMENT_VIEW_END : TextView.TEXT_ALIGNMENT_VIEW_START);
            getBinding().containerMessages.addView(row.getRoot());
        }
    }

    private String textOf(TextView view) {
        return view.getText() == null ? "" : view.getText().toString();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
