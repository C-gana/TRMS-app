package com.cgana.trmsdriver.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.PreferencesManager;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.ui.auth.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

/**
 * Settings Activity (Module 7 Part 3 - Enhanced)
 * Main settings screen with profile, preferences, and account options
 */
public class SettingsActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private MaterialCardView profileCard;
    private TextView tvProfileInitial;
    private TextView tvProfileName;
    private TextView tvProfilePhone;
    private TextView tvCurrentTheme;
    private TextView tvAppVersion;
    private LinearLayout appearanceSetting;
    private LinearLayout notificationsSetting;
    private LinearLayout changePasswordSetting;
    private LinearLayout logoutSetting;
    private LinearLayout helpSetting;
    private LinearLayout aboutSetting;

    // Data
    private TokenManager tokenManager;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize
        tokenManager = new TokenManager(this);
        preferencesManager = new PreferencesManager(this);

        // Initialize views
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Load profile data
        loadProfileData();

        // Setup listeners
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        profileCard = findViewById(R.id.profileCard);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvCurrentTheme = findViewById(R.id.tvCurrentTheme);
        tvAppVersion = findViewById(R.id.tvAppVersion);
        appearanceSetting = findViewById(R.id.appearanceSetting);
        notificationsSetting = findViewById(R.id.notificationsSetting);
        changePasswordSetting = findViewById(R.id.changePasswordSetting);
        logoutSetting = findViewById(R.id.logoutSetting);
        helpSetting = findViewById(R.id.helpSetting);
        aboutSetting = findViewById(R.id.aboutSetting);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadProfileData() {
        // Load driver data from TokenManager
        if (tokenManager.getDriver() != null) {
            String driverName = tokenManager.getDriver().getFullName();
            String phoneNumber = tokenManager.getDriver().getPhoneNumber();

            tvProfileName.setText(driverName != null ? driverName : "Driver");
            tvProfilePhone.setText(phoneNumber != null ? phoneNumber : "");

            // Set initial
            if (driverName != null && !driverName.isEmpty()) {
                String[] nameParts = driverName.split(" ");
                if (nameParts.length >= 2) {
                    tvProfileInitial.setText(
                        String.valueOf(nameParts[0].charAt(0)) +
                        String.valueOf(nameParts[1].charAt(0))
                    );
                } else {
                    tvProfileInitial.setText(String.valueOf(driverName.charAt(0)));
                }
            }
        }

        // Load theme preference
        updateThemeValue();

        // Set app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(getString(R.string.app_version_format, versionName));
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0.0");
        }
    }

    private void updateThemeValue() {
        int themeMode = preferencesManager.getThemeMode();
        String themeText;
        switch (themeMode) {
            case PreferencesManager.THEME_LIGHT:
                themeText = getString(R.string.light_theme);
                break;
            case PreferencesManager.THEME_DARK:
                themeText = getString(R.string.dark_theme);
                break;
            case PreferencesManager.THEME_SYSTEM:
            default:
                themeText = getString(R.string.system_default);
                break;
        }
        tvCurrentTheme.setText(themeText);
    }

    private void setupListeners() {
        // Profile card click
        profileCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Appearance setting click
        appearanceSetting.setOnClickListener(v -> {
            showThemeDialog();
        });

        // Notifications setting click
        notificationsSetting.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationSettingsActivity.class);
            startActivity(intent);
        });

        // Change password setting click
        changePasswordSetting.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        // Logout setting click
        logoutSetting.setOnClickListener(v -> {
            showLogoutConfirmation();
        });

        // Help setting click
        helpSetting.setOnClickListener(v -> {
            Intent intent = new Intent(this, HelpSupportActivity.class);
            startActivity(intent);
        });

        // About setting click
        aboutSetting.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        int currentTheme = preferencesManager.getThemeMode() - 1; // Convert to 0-based index

        new AlertDialog.Builder(this)
            .setTitle(R.string.appearance)
            .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                int themeMode;
                switch (which) {
                    case 0:
                        themeMode = PreferencesManager.THEME_LIGHT;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case 1:
                        themeMode = PreferencesManager.THEME_DARK;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case 2:
                    default:
                        themeMode = PreferencesManager.THEME_SYSTEM;
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
                preferencesManager.setThemeMode(themeMode);
                updateThemeValue();
                dialog.dismiss();
            })
            .show();
    }

    private void showChangePasswordDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.change_password)
            .setMessage("Change password feature will be implemented with backend API")
            .setPositiveButton(R.string.ok, null)
            .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void performLogout() {
        // Clear token and navigate to login
        tokenManager.clearAuth();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile data in case it was updated
        loadProfileData();
    }
}

