package com.cgana.trmsdriver;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.ui.auth.LoginActivity;
import com.cgana.trmsdriver.ui.duty.DutyStatusActivity;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * MainActivity - Dashboard Placeholder for Module 2
 * This activity is now the dashboard/home screen shown when driver is authenticated AND on duty.
 * It is no longer the launcher activity - LoginActivity is now the entry point.
 */
public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize TokenManager
        tokenManager = new TokenManager(this);

        // Check authentication - redirect to login if not authenticated
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Check duty status - redirect to duty screen if not on duty
        if (!tokenManager.getDutyStatus()) {
            navigateToDutyStatus();
            return;
        }

        // User is authenticated and on duty - show dashboard placeholder
        setContentView(R.layout.activity_main);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
        }

        // Placeholder message for Module 2
        TextView tvPlaceholder = findViewById(R.id.tvPlaceholder);
        if (tvPlaceholder != null) {
            String driverName = tokenManager.getDriver() != null ?
                tokenManager.getDriver().getFullName() : "Driver";
            tvPlaceholder.setText("Welcome, " + driverName + "!\n\nYou are logged in and ON DUTY.");
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToDutyStatus() {
        Intent intent = new Intent(this, DutyStatusActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to login/duty when on dashboard
        // User must logout explicitly
        moveTaskToBack(true);
    }
}