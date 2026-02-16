package com.example.habitrpg.feature.tasks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.domain.model.TaskCategory;
import com.example.habitrpg.core.CoreFragment;
import com.example.habitrpg.databinding.FragmentCategoryManagementBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryManagementFragment extends CoreFragment<FragmentCategoryManagementBinding> {

    private TasksViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private final LinkedHashMap<String, String> categoryColorMap = new LinkedHashMap<>();

    @Override
    protected FragmentCategoryManagementBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentCategoryManagementBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
        initColors();

        getBinding().toolbarCategories.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        setupList();
        getBinding().fabAddCategory.setOnClickListener(v -> showCreateDialog());

        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            categoryAdapter.submit(state.getCategories());
            getBinding().tvEmptyCategories.setVisibility(state.getCategories().isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getEffect().observe(getViewLifecycleOwner(), effect -> {
            if (effect instanceof TasksSideEffect.ShowToast) {
                Toast.makeText(requireContext(), ((TasksSideEffect.ShowToast) effect).message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.handleAction(new TasksAction.Load());
    }

    private void setupList() {
        categoryAdapter = new CategoryAdapter(new CategoryAdapter.Listener() {
            @Override
            public void onEdit(TaskCategory category) {
                showEditDialog(category);
            }

            @Override
            public void onDelete(TaskCategory category) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Brisanje kategorije")
                        .setMessage("Obrisati kategoriju " + category.getName() + "?")
                        .setPositiveButton("Obriši", (d, w) -> viewModel.handleAction(new TasksAction.DeleteCategory(category.getId())))
                        .setNegativeButton("Otkaži", null)
                        .show();
            }
        });
        getBinding().rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvCategories.setAdapter(categoryAdapter);
    }

    private void initColors() {
        categoryColorMap.put("Plava", "#5B5CE2");
        categoryColorMap.put("Zelena", "#16A34A");
        categoryColorMap.put("Narandžasta", "#EA580C");
        categoryColorMap.put("Ljubičasta", "#9333EA");
        categoryColorMap.put("Crvena", "#DC2626");
        categoryColorMap.put("Tirkizna", "#0D9488");
        categoryColorMap.put("Ružičasta", "#DB2777");
        categoryColorMap.put("Siva", "#4B5563");
    }

    private void showCreateDialog() {
        showCategoryDialog("Nova kategorija", null, (name, hex) -> viewModel.handleAction(new TasksAction.CreateCategory(name, hex)));
    }

    private void showEditDialog(TaskCategory category) {
        showCategoryDialog("Izmeni kategoriju", category, (name, hex) -> viewModel.handleAction(new TasksAction.UpdateCategory(category.getId(), name, hex)));
    }

    private interface CategorySubmitListener { void onSubmit(String name, String colorHex); }

    private void showCategoryDialog(String title, TaskCategory existing, CategorySubmitListener listener) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 24, 24, 24);

        TextInputEditText etName = new TextInputEditText(requireContext());
        etName.setHint("Naziv kategorije");
        if (existing != null) etName.setText(existing.getName());
        container.addView(etName);

        Spinner spinnerColor = new Spinner(requireContext());
        List<String> colorNames = new ArrayList<>(categoryColorMap.keySet());
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                colorNames
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);
        if (existing != null) {
            int selectedPosition = 0;
            int index = 0;
            for (Map.Entry<String, String> e : categoryColorMap.entrySet()) {
                if (e.getValue().equalsIgnoreCase(existing.getColorHex())) {
                    selectedPosition = index;
                    break;
                }
                index++;
            }
            spinnerColor.setSelection(selectedPosition);
        } else if (!colorNames.isEmpty()) {
            spinnerColor.setSelection(0);
        }
        container.addView(spinnerColor);

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(container)
                .setPositiveButton("Sačuvaj", (d, w) -> {
                    String name = String.valueOf(etName.getText()).trim();
                    String selectedColor = String.valueOf(spinnerColor.getSelectedItem());
                    String colorHex = categoryColorMap.getOrDefault(selectedColor, "#5B5CE2");
                    listener.onSubmit(name, colorHex);
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }
}
