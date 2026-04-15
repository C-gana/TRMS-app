package com.cgana.trmsownerapp.ui.settings;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.ui.auth.LoginActivity;
import com.cgana.trmsownerapp.utils.ThemeManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private TextView tvFullName, tvPhoneNumber, tvEmail, tvAppVersion, tvFCMToken;
    private MaterialButton btnThemeSelector, btnLogout, btnCopyFCMToken;
    private SwitchMaterial switchNotifications;

    private ThemeManager themeManager;
    private TokenManager tokenManager;
    private String fcmToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        tvFullName = view.findViewById(R.id.tvFullName);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);
        tvFCMToken = view.findViewById(R.id.tvFCMToken);
        btnThemeSelector = view.findViewById(R.id.btnThemeSelector);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnCopyFCMToken = view.findViewById(R.id.btnCopyFCMToken);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        // Initialize managers
        themeManager = new ThemeManager(requireContext());
        tokenManager = new TokenManager(requireContext());

        loadUserProfile();
        setupThemeSelector();
        loadFCMToken();
        setupListeners();

        return view;
    }

    private void loadUserProfile() {
        User user = tokenManager.getUser();
        if (user != null) {
            String fullName = user.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                tvFullName.setText(fullName);
            } else {
                tvFullName.setText("Vehicle Owner");
            }

            String phone = user.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                tvPhoneNumber.setText(phone);
            }

            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                tvEmail.setText(email);
                tvEmail.setVisibility(View.VISIBLE);
            } else {
                tvEmail.setVisibility(View.GONE);
            }
        }

        // App version
        try {
            PackageInfo pInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            tvAppVersion.setText("Version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvAppVersion.setText("Version 1.0");
        }
    }

    private void setupThemeSelector() {
        int currentTheme = themeManager.getThemeMode();
        btnThemeSelector.setText(themeManager.getThemeModeName(currentTheme));
    }

    private void setupListeners() {
        // Theme selector
        btnThemeSelector.setOnClickListener(v -> showThemeDialog());

        // Notifications toggle (placeholder)
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Save notification preference
        });

        // Copy FCM Token
        btnCopyFCMToken.setOnClickListener(v -> copyFCMTokenToClipboard());

        // Logout
        btnLogout.setOnClickListener(v -> logout());
    }

    private void showThemeDialog() {
        String[] themeOptions = {"Light", "Dark", "System Default"};
        int currentTheme = themeManager.getThemeMode();

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(themeOptions, currentTheme, (dialog, which) -> {
                    themeManager.saveThemeMode(which);
                    btnThemeSelector.setText(themeManager.getThemeModeName(which));
                    dialog.dismiss();

                    // Recreate activity to apply theme
                    requireActivity().recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadFCMToken() {
        tvFCMToken.setText(R.string.loading);

        // Check if Google Play Services is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services not available: " + resultCode);
            tvFCMToken.setText("Google Play Services not available");
            btnCopyFCMToken.setEnabled(false);
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                        tvFCMToken.setText("Failed to load token");
                        btnCopyFCMToken.setEnabled(false);
                        return;
                    }

                    // Get FCM token
                    fcmToken = task.getResult();
                    tvFCMToken.setText(fcmToken);
                    btnCopyFCMToken.setEnabled(true);
                    Log.d(TAG, "FCM Token loaded: " + fcmToken);
                });
    }

    private void copyFCMTokenToClipboard() {
        if (fcmToken == null || fcmToken.isEmpty()) {
            Toast.makeText(requireContext(), "No token available to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("FCM Token", fcmToken);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(requireContext(), "FCM Token copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    tokenManager.clearAuth();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}

