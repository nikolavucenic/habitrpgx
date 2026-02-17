package com.example.data.repository;

import com.example.domain.core.Result;
import com.example.domain.model.SocialModels;
import com.example.domain.repository.SocialRepository;
import com.example.domain.model.SpecialMissionEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SocialRepositoryImpl implements SocialRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    @Inject
    public SocialRepositoryImpl(FirebaseFirestore db) {
        this.db = db;
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public CompletableFuture<Result<List<SocialModels.Friend>>> getFriends() {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<List<SocialModels.Friend>>> future = new CompletableFuture<>();
        db.collection("users").document(current.getUid()).collection("friends").get()
                .addOnSuccessListener(s -> {
                    List<SocialModels.Friend> friends = new ArrayList<>();
                    s.getDocuments().forEach(d -> friends.add(new SocialModels.Friend(
                            d.getId(),
                            d.getString("username") == null ? "" : d.getString("username"),
                            d.getLong("avatarId") == null ? 1 : d.getLong("avatarId").intValue()
                    )));
                    future.complete(new Result.Success<>(friends));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<List<SocialModels.Friend>>> searchUsers(String query) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));

        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            return CompletableFuture.completedFuture(new Result.Success<>(new ArrayList<>()));
        }

        CompletableFuture<Result<List<SocialModels.Friend>>> future = new CompletableFuture<>();
        db.collection("users").orderBy("username").startAt(normalized).endAt(normalized + "\uf8ff").limit(10).get()
                .addOnSuccessListener(snapshot -> {
                    List<SocialModels.Friend> users = new ArrayList<>();
                    String myUid = current.getUid();
                    snapshot.getDocuments().forEach(d -> {
                        if (!myUid.equals(d.getId())) {
                            users.add(new SocialModels.Friend(
                                    d.getId(),
                                    d.getString("username") == null ? "" : d.getString("username"),
                                    d.getLong("avatarId") == null ? 1 : d.getLong("avatarId").intValue()
                            ));
                        }
                    });
                    future.complete(new Result.Success<>(users));
                })
                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> addFriend(String targetUid) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        if (current.getUid().equals(targetUid)) return CompletableFuture.completedFuture(new Result.Error<>("Ne možete dodati sebe."));

        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        db.collection("users").document(targetUid).get().addOnSuccessListener(target -> {
            if (!target.exists()) {
                future.complete(new Result.Error<>("Korisnik ne postoji."));
                return;
            }

            String username = target.getString("username") == null ? "" : target.getString("username");
            int avatarId = target.getLong("avatarId") == null ? 1 : target.getLong("avatarId").intValue();

            Map<String, Object> friend = new HashMap<>();
            friend.put("username", username);
            friend.put("avatarId", avatarId);
            friend.put("createdAt", System.currentTimeMillis());

            db.collection("users").document(current.getUid()).collection("friends").document(targetUid)
                    .set(friend)
                    .addOnSuccessListener(u -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

        return future;
    }

    @Override
    public CompletableFuture<Result<SocialModels.Alliance>> getAlliance() {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<SocialModels.Alliance>> future = new CompletableFuture<>();
        db.collection("users").document(current.getUid()).get().addOnSuccessListener(userDoc -> {
            String allianceId = userDoc.getString("currentAllianceId");
            if (allianceId == null || allianceId.trim().isEmpty()) {
                future.complete(new Result.Error<>("Niste član saveza."));
                return;
            }
            loadAlliance(allianceId, future);
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    private void loadAlliance(String allianceId, CompletableFuture<Result<SocialModels.Alliance>> future) {
        db.collection("alliances").document(allianceId).get().addOnSuccessListener(a -> {
            if (!a.exists()) {
                future.complete(new Result.Error<>("Savez ne postoji."));
                return;
            }
            db.collection("alliances").document(allianceId).collection("members").get().addOnSuccessListener(membersSnapshot -> {
                List<SocialModels.Friend> members = new ArrayList<>();
                membersSnapshot.getDocuments().forEach(d -> members.add(new SocialModels.Friend(
                        d.getId(),
                        d.getString("username") == null ? "" : d.getString("username"),
                        d.getLong("avatarId") == null ? 1 : d.getLong("avatarId").intValue()
                )));
                boolean missionActive = a.getBoolean("missionActive") != null && a.getBoolean("missionActive");
                int bossHp = a.getLong("missionBossHp") == null ? 0 : a.getLong("missionBossHp").intValue();
                int bossMaxHp = a.getLong("missionBossMaxHp") == null ? 0 : a.getLong("missionBossMaxHp").intValue();
                future.complete(new Result.Success<>(new SocialModels.Alliance(
                        a.getId(),
                        a.getString("name") == null ? "" : a.getString("name"),
                        a.getString("leaderId") == null ? "" : a.getString("leaderId"),
                        a.getString("leaderUsername") == null ? "" : a.getString("leaderUsername"),
                        members,
                        missionActive,
                        bossHp,
                        bossMaxHp
                )));
            }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
    }

    @Override
    public CompletableFuture<Result<Void>> createAlliance(String name, List<String> invitedFriendUids) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        db.collection("users").document(current.getUid()).get().addOnSuccessListener(me -> {
            String username = me.getString("username") == null ? "" : me.getString("username");
            DocumentReference allianceRef = db.collection("alliances").document();
            Map<String, Object> alliance = new HashMap<>();
            alliance.put("name", name);
            alliance.put("leaderId", current.getUid());
            alliance.put("leaderUsername", username);
            alliance.put("createdAt", System.currentTimeMillis());
            alliance.put("missionActive", false);
            alliance.put("missionBossHp", 0);
            alliance.put("missionBossMaxHp", 0);

            allianceRef.set(alliance).addOnSuccessListener(unused -> {
                Map<String, Object> meMember = new HashMap<>();
                meMember.put("username", username);
                meMember.put("avatarId", me.getLong("avatarId") == null ? 1 : me.getLong("avatarId").intValue());
                meMember.put("joinedAt", System.currentTimeMillis());
                allianceRef.collection("members").document(current.getUid()).set(meMember);

                db.collection("users").document(current.getUid()).update("currentAllianceId", allianceRef.getId());

                Set<String> unique = new HashSet<>(invitedFriendUids);
                for (String invitedUid : unique) {
                    Map<String, Object> invite = new HashMap<>();
                    invite.put("allianceId", allianceRef.getId());
                    invite.put("allianceName", name);
                    invite.put("fromUid", current.getUid());
                    invite.put("fromUsername", username);
                    invite.put("createdAt", System.currentTimeMillis());
                    invite.put("status", "PENDING");
                    db.collection("users").document(invitedUid).collection("allianceInvites").add(invite);
                }
                future.complete(new Result.Success<>(null));
            }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<List<SocialModels.AllianceInvite>>> getInvites() {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<List<SocialModels.AllianceInvite>>> future = new CompletableFuture<>();
        db.collection("users").document(current.getUid()).collection("allianceInvites")
                .whereEqualTo("status", "PENDING")
                .get().addOnSuccessListener(snapshot -> {
                    List<com.google.firebase.firestore.DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
                    docs.sort((a, b) -> Long.compare(
                            b.getLong("createdAt") == null ? 0L : b.getLong("createdAt"),
                            a.getLong("createdAt") == null ? 0L : a.getLong("createdAt")
                    ));

                    List<SocialModels.AllianceInvite> list = new ArrayList<>();
                    docs.forEach(d -> list.add(new SocialModels.AllianceInvite(
                            d.getId(),
                            d.getString("allianceId") == null ? "" : d.getString("allianceId"),
                            d.getString("allianceName") == null ? "" : d.getString("allianceName"),
                            d.getString("fromUid") == null ? "" : d.getString("fromUid"),
                            d.getString("fromUsername") == null ? "" : d.getString("fromUsername")
                    )));
                    future.complete(new Result.Success<>(list));
                }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> respondToInvite(String inviteId, boolean accept) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        DocumentReference inviteRef = db.collection("users").document(current.getUid()).collection("allianceInvites").document(inviteId);
        inviteRef.get().addOnSuccessListener(invite -> {
            if (!invite.exists()) {
                future.complete(new Result.Error<>("Pozivnica ne postoji."));
                return;
            }
            if (!accept) {
                inviteRef.update("status", "DECLINED").addOnSuccessListener(u -> future.complete(new Result.Success<>(null)))
                        .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                return;
            }

            String allianceId = invite.getString("allianceId");
            if (allianceId == null || allianceId.trim().isEmpty()) {
                future.complete(new Result.Error<>("Neispravna pozivnica."));
                return;
            }
            db.collection("users").document(current.getUid()).get().addOnSuccessListener(me -> {
                String previousAlliance = me.getString("currentAllianceId");
                Runnable joinNew = () -> db.collection("alliances").document(allianceId).collection("members").document(current.getUid())
                        .set(memberMap(me))
                        .addOnSuccessListener(v -> db.collection("users").document(current.getUid()).update("currentAllianceId", allianceId)
                                .addOnSuccessListener(v2 -> inviteRef.update("status", "ACCEPTED")
                                        .addOnSuccessListener(v3 -> future.complete(new Result.Success<>(null)))
                                        .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage()))))
                                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage()))))
                        .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));

                if (previousAlliance == null || previousAlliance.trim().isEmpty() || previousAlliance.equals(allianceId)) {
                    joinNew.run();
                    return;
                }

                db.collection("alliances").document(previousAlliance).get().addOnSuccessListener(oldAlliance -> {
                    boolean missionActive = oldAlliance.getBoolean("missionActive") != null && oldAlliance.getBoolean("missionActive");
                    if (missionActive) {
                        future.complete(new Result.Error<>("Ne možete napustiti savez dok je misija aktivna."));
                        return;
                    }
                    db.collection("alliances").document(previousAlliance).collection("members").document(current.getUid()).delete()
                            .addOnSuccessListener(x -> joinNew.run())
                            .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
            }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        return future;
    }

    @Override
    public CompletableFuture<Result<List<SocialModels.AllianceMessage>>> getAllianceMessages() {
        CompletableFuture<Result<List<SocialModels.AllianceMessage>>> future = new CompletableFuture<>();
        getAlliance().thenAccept(allianceResult -> {
            if (allianceResult instanceof Result.Error) {
                future.complete(new Result.Error<>(((Result.Error<SocialModels.Alliance>) allianceResult).message));
                return;
            }
            SocialModels.Alliance alliance = ((Result.Success<SocialModels.Alliance>) allianceResult).data;
            db.collection("alliances").document(alliance.id).collection("messages")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .limit(100)
                    .get().addOnSuccessListener(s -> {
                        List<SocialModels.AllianceMessage> list = new ArrayList<>();
                        s.getDocuments().forEach(d -> list.add(new SocialModels.AllianceMessage(
                                d.getId(),
                                d.getString("senderUid") == null ? "" : d.getString("senderUid"),
                                d.getString("senderUsername") == null ? "" : d.getString("senderUsername"),
                                d.getString("message") == null ? "" : d.getString("message"),
                                d.getLong("createdAt") == null ? 0L : d.getLong("createdAt")
                        )));
                        future.complete(new Result.Success<>(list));
                    }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        });
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> sendAllianceMessage(String message) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        getAlliance().thenAccept(allianceResult -> {
            if (allianceResult instanceof Result.Error) {
                future.complete(new Result.Error<>(((Result.Error<SocialModels.Alliance>) allianceResult).message));
                return;
            }
            SocialModels.Alliance alliance = ((Result.Success<SocialModels.Alliance>) allianceResult).data;
            db.collection("users").document(current.getUid()).get().addOnSuccessListener(userDoc -> {
                Map<String, Object> data = new HashMap<>();
                data.put("senderUid", current.getUid());
                data.put("senderUsername", userDoc.getString("username") == null ? "" : userDoc.getString("username"));
                data.put("message", message);
                data.put("createdAt", System.currentTimeMillis());
                db.collection("alliances").document(alliance.id).collection("messages").add(data)
                        .addOnSuccessListener(x -> trackMissionEvent("alliance_message_day", 1)
                                .thenAccept(r -> future.complete(new Result.Success<>(null))))
                        .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
            }).addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        });
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> startSpecialMission() {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        getAlliance().thenAccept(allianceResult -> {
            if (allianceResult instanceof Result.Error) {
                future.complete(new Result.Error<>(((Result.Error<SocialModels.Alliance>) allianceResult).message));
                return;
            }
            SocialModels.Alliance alliance = ((Result.Success<SocialModels.Alliance>) allianceResult).data;
            if (!alliance.leaderId.equals(current.getUid())) {
                future.complete(new Result.Error<>("Samo vođa saveza može pokrenuti misiju."));
                return;
            }
            if (alliance.missionActive) {
                future.complete(new Result.Error<>("Misija je već aktivna."));
                return;
            }
            int maxHp = alliance.members.size() * 100;
            Map<String, Object> update = new HashMap<>();
            update.put("missionActive", true);
            update.put("missionBossHp", maxHp);
            update.put("missionBossMaxHp", maxHp);
            update.put("missionStartedAt", System.currentTimeMillis());
            update.put("missionEndsAt", System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000);
            db.collection("alliances").document(alliance.id).update(update)
                    .addOnSuccessListener(v -> future.complete(new Result.Success<>(null)))
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        });
        return future;
    }

    @Override
    public CompletableFuture<Result<Void>> trackMissionEvent(String eventType, int amount) {
        FirebaseUser current = auth.getCurrentUser();
        if (current == null) return CompletableFuture.completedFuture(new Result.Error<>("Nema aktivnog korisnika."));
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        getAlliance().thenAccept(allianceResult -> {
            if (allianceResult instanceof Result.Error) {
                future.complete(new Result.Success<>(null));
                return;
            }
            SocialModels.Alliance alliance = ((Result.Success<SocialModels.Alliance>) allianceResult).data;
            if (!alliance.missionActive || alliance.bossHp <= 0) {
                future.complete(new Result.Success<>(null));
                return;
            }

            db.collection("alliances").document(alliance.id).collection("missionProgress").document(current.getUid()).get()
                    .addOnSuccessListener(progress -> {
                        long now = System.currentTimeMillis();
                        int damage = calculateDamage(eventType, amount, progress, now);
                        if (damage <= 0) {
                            future.complete(new Result.Success<>(null));
                            return;
                        }
                        Map<String, Object> progressUpdate = new HashMap<>();
                        incrementCounter(progressUpdate, eventType, amount);
                        if ("alliance_message_day".equals(eventType)) progressUpdate.put("lastMessageDay", dayKey(now));
                        db.collection("alliances").document(alliance.id).collection("missionProgress").document(current.getUid())
                                .set(progressUpdate, com.google.firebase.firestore.SetOptions.merge());

                        int nextHp = Math.max(0, alliance.bossHp - damage);
                        Map<String, Object> allianceUpdate = new HashMap<>();
                        allianceUpdate.put("missionBossHp", nextHp);
                        if (nextHp <= 0) allianceUpdate.put("missionActive", false);
                        db.collection("alliances").document(alliance.id).update(allianceUpdate)
                                .addOnSuccessListener(v -> future.complete(new Result.Success<>(null)))
                                .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
                    })
                    .addOnFailureListener(e -> future.complete(new Result.Error<>(e.getMessage())));
        });
        return future;
    }

    private int calculateDamage(String eventType, int amount, com.google.firebase.firestore.DocumentSnapshot progress, long now) {
        if (SpecialMissionEvent.SHOP_PURCHASE.equals(eventType)) {
            int c = intVal(progress.getLong("shop_purchase_count"));
            int allowed = Math.max(0, 5 - c);
            int applied = Math.min(allowed, Math.max(0, amount));
            return applied * 2;
        }
        if (SpecialMissionEvent.REGULAR_BOSS_HIT.equals(eventType)) {
            int c = intVal(progress.getLong("regular_boss_hit_count"));
            int allowed = Math.max(0, 10 - c);
            int applied = Math.min(allowed, Math.max(0, amount));
            return applied * 2;
        }
        if (SpecialMissionEvent.SIMPLE_TASK.equals(eventType)) {
            int c = intVal(progress.getLong("simple_task_count"));
            int allowed = Math.max(0, 10 - c);
            int applied = Math.min(allowed, Math.max(0, amount));
            return applied;
        }
        if (SpecialMissionEvent.COMPLEX_TASK.equals(eventType)) {
            int c = intVal(progress.getLong("complex_task_count"));
            int allowed = Math.max(0, 6 - c);
            int applied = Math.min(allowed, Math.max(0, amount));
            return applied * 4;
        }
        if ("alliance_message_day".equals(eventType)) {
            String last = progress.getString("lastMessageDay");
            return dayKey(now).equals(last) ? 0 : 4;
        }
        return 0;
    }

    private void incrementCounter(Map<String, Object> map, String eventType, int amount) {
        String key = eventType + "_count";
        map.put(key, com.google.firebase.firestore.FieldValue.increment(Math.max(0, amount)));
    }

    private String dayKey(long now) {
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US);
        return f.format(new java.util.Date(now));
    }

    private int intVal(Long value) { return value == null ? 0 : value.intValue(); }

    private Map<String, Object> memberMap(com.google.firebase.firestore.DocumentSnapshot me) {
        Map<String, Object> member = new HashMap<>();
        member.put("username", me.getString("username") == null ? "" : me.getString("username"));
        member.put("avatarId", me.getLong("avatarId") == null ? 1 : me.getLong("avatarId").intValue());
        member.put("joinedAt", System.currentTimeMillis());
        return member;
    }
}
