package com.cgana.trmsdriver.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.PreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Notification Settings Activity (Module 7)
 * Allows users to configure notification preferences
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private SwitchMaterial switchEnableAll;
    private SwitchMaterial switchProximityAlerts;
    private SwitchMaterial switchBoardingNotifications;
    private SwitchMaterial switchJourneyCompleted;
    private SwitchMaterial switchNotificationSound;
    private SwitchMaterial switchVibration;

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        preferencesManager = new PreferencesManager(this);

        initializeViews();
        setupToolbar();
        loadPreferences();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        switchEnableAll = findViewById(R.id.switchEnableAll);
        switchProximityAlerts = findViewById(R.id.switchProximityAlerts);
        switchBoardingNotifications = findViewById(R.id.switchBoardingNotifications);
        switchJourneyCompleted = findViewById(R.id.switchJourneyCompleted);
        switchNotificationSound = findViewById(R.id.switchNotificationSound);
        switchVibration = findViewById(R.id.switchVibration);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadPreferences() {
        boolean notificationsEnabled = preferencesManager.isNotificationsEnabled();
        switchEnableAll.setChecked(notificationsEnabled);
        switchProximityAlerts.setChecked(preferencesManager.isProximityAlertsEnabled());
        switchBoardingNotifications.setChecked(preferencesManager.isBoardingAlertsEnabled());
        switchJourneyCompleted.setChecked(preferencesManager.isJourneyCompletedAlertsEnabled());
        switchNotificationSound.setChecked(preferencesManager.isNotificationSoundEnabled());
        switchVibration.setChecked(preferencesManager.isNotificationVibrationEnabled());

        updateSwitchesState(notificationsEnabled);
    }

    private void setupListeners() {
        // Master switch
        switchEnableAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationsEnabled(isChecked);
            updateSwitchesState(isChecked);
        });

        // Individual switches
        switchProximityAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setProximityAlertsEnabled(isChecked);
        });

        switchBoardingNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setBoardingAlertsEnabled(isChecked);
        });

        switchJourneyCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setJourneyCompletedAlertsEnabled(isChecked);
        });

        switchNotificationSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationSoundEnabled(isChecked);
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationVibrationEnabled(isChecked);
        });
    }

    private void updateSwitchesState(boolean enabled) {
        switchProximityAlerts.setEnabled(enabled);
        switchBoardingNotifications.setEnabled(enabled);
        switchJourneyCompleted.setEnabled(enabled);
        switchNotificationSound.setEnabled(enabled);
        switchVibration.setEnabled(enabled);
    }
}

