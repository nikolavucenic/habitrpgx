package com.example.habitrpg;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.domain.usecase.IsLoggedInUseCase;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    IsLoggedInUseCase isLoggedInUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            boolean isLoggedIn = isLoggedInUseCase.execute();
            navGraph.setStartDestination(isLoggedIn ? R.id.nav_profile : R.id.auth_graph);
            navController.setGraph(navGraph);

            NavigationUI.setupWithNavController(navView, navController);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean showBottomBar = id == R.id.nav_profile || id == R.id.nav_tasks || id == R.id.nav_shop || id == R.id.nav_social || id == R.id.nav_progression;
                navView.setVisibility(showBottomBar ? View.VISIBLE : View.GONE);
            });
        }
    }
}
