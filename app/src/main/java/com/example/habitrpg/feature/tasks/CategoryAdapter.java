package com.example.habitrpg.feature.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.domain.model.TaskCategory;
import com.example.habitrpg.databinding.ItemCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<TaskCategory> items = new ArrayList<>();

    public void submit(List<TaskCategory> categories) {
        items.clear();
        items.addAll(categories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TaskCategory category) {
            binding.tvCategoryName.setText(category.getName());
            try {
                binding.viewCategoryColor.setBackgroundColor(Color.parseColor(category.getColorHex()));
            } catch (Exception ignored) {
                binding.viewCategoryColor.setBackgroundColor(Color.GRAY);
            }
        }
    }
}
