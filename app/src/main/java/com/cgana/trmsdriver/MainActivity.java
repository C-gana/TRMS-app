package com.cgana.trmsdriver;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml");
        }
        NavController navController = navHostFragment.getNavController();

        // Determine start destination based on auth state
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            navController.navigate(R.id.homeFragment);
        }
        // If not logged in, default start destination (loginFragment) will load automatically
    }
}