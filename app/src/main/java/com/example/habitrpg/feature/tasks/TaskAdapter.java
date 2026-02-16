package com.example.habitrpg.feature.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.domain.model.TaskItem;
import com.example.habitrpg.databinding.ItemTaskBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskStatusListener {
        void onOpenDetails(String taskId);
        void onSetDone(String taskId);
        void onSetCanceled(String taskId);
        void onSetPaused(String taskId);
        void onSetActive(String taskId);
    }

    private final List<TaskItem> items = new ArrayList<>();
    private final TaskStatusListener listener;

    public TaskAdapter(TaskStatusListener listener) {
        this.listener = listener;
    }

    public void submit(List<TaskItem> tasks) {
        items.clear();
        items.addAll(tasks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;
        private final TaskStatusListener listener;

        TaskViewHolder(ItemTaskBinding binding, TaskStatusListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(TaskItem task) {
            binding.tvTitle.setText(task.getTitle());

            String description = task.getDescription() == null || task.getDescription().trim().isEmpty()
                    ? "Bez opisa" : task.getDescription();
            binding.tvSubtitle.setText(description);

            String slot = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(task.getExecuteAt()));
            binding.tvSlot.setText(slot);

            String meta = prettify(task.getImportance()) + " • " + prettify(task.getDifficulty()) + " • XP " + task.getXpValue();
            binding.tvMeta.setText(meta);

            try {
                binding.viewCategoryDot.setBackgroundColor(Color.parseColor(task.getCategoryColorHex()));
            } catch (Exception ignored) {
                binding.viewCategoryDot.setBackgroundColor(Color.parseColor("#5B5CE2"));
            }

            boolean active = TaskItem.STATUS_ACTIVE.equals(task.getStatus());
            boolean paused = TaskItem.STATUS_PAUSED.equals(task.getStatus());

            binding.actionRow.setVisibility((active || paused) ? View.VISIBLE : View.GONE);
            binding.btnDone.setVisibility(active ? View.VISIBLE : View.GONE);
            binding.btnCancel.setVisibility(active ? View.VISIBLE : View.GONE);
            binding.btnPause.setVisibility(active && TaskItem.TYPE_REPEATING.equals(task.getType()) ? View.VISIBLE : View.GONE);
            binding.btnActivate.setVisibility(paused ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> listener.onOpenDetails(task.getId()));
            binding.btnDone.setOnClickListener(v -> listener.onSetDone(task.getId()));
            binding.btnCancel.setOnClickListener(v -> listener.onSetCanceled(task.getId()));
            binding.btnPause.setOnClickListener(v -> listener.onSetPaused(task.getId()));
            binding.btnActivate.setOnClickListener(v -> listener.onSetActive(task.getId()));
        }

        private String prettify(String raw) {
            if (raw == null || raw.trim().isEmpty()) return "-";
            String normalized = raw.replace("_", " ").toLowerCase(Locale.getDefault());
            StringBuilder sb = new StringBuilder();
            boolean capitalize = true;
            for (char c : normalized.toCharArray()) {
                if (capitalize && Character.isLetter(c)) {
                    sb.append(Character.toUpperCase(c));
                    capitalize = false;
                } else {
                    sb.append(c);
                }
                if (c == ' ') capitalize = true;
            }
            return sb.toString();
        }
    }
}
