package com.cgana.trmsdriver.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cgana.trmsdriver.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * About Activity (Module 7)
 * Displays app information, version, and legal links
 */
public class AboutActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initializeViews();
        setupToolbar();
        setVersionInfo();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvVersion = findViewById(R.id.tvVersion);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setVersionInfo() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName + " (Build " + pInfo.versionCode + ")";
            tvVersion.setText(version);
        } catch (Exception e) {
            tvVersion.setText("Version 1.0.0 (Build 1)");
        }
    }

    private void setupClickListeners() {
        // Terms of Service
        findViewById(R.id.termsLink).setOnClickListener(v -> {
            openUrl("https://trms.mw/terms");
        });

        // Privacy Policy
        findViewById(R.id.privacyLink).setOnClickListener(v -> {
            openUrl("https://trms.mw/privacy");
        });

        // Licenses
        findViewById(R.id.licensesLink).setOnClickListener(v -> {
            // TODO: Show licenses activity or dialog with third-party licenses
            android.widget.Toast.makeText(this,
                "Open source licenses information",
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this,
                "Unable to open link",
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

