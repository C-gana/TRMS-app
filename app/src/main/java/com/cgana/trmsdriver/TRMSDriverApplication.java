package com.cgana.trmsdriver;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.cgana.trmsdriver.data.local.PreferencesManager;

/**
 * TRMS Driver Application (Module 7)
 * Initializes app-wide settings including theme preferences
 */
public class TRMSDriverApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize and apply saved theme preference
        PreferencesManager preferencesManager = new PreferencesManager(this);
        applyTheme(preferencesManager.getThemeMode());
    }

    private void applyTheme(int themeMode) {
        switch (themeMode) {
            case PreferencesManager.THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case PreferencesManager.THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case PreferencesManager.THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}

