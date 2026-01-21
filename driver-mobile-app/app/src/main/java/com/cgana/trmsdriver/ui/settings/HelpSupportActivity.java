package com.cgana.trmsdriver.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cgana.trmsdriver.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Help & Support Activity (Module 7)
 * Provides contact options and FAQs for driver assistance
 */
public class HelpSupportActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        initializeViews();
        setupToolbar();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Phone Support
        findViewById(R.id.phoneSupport).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+2651234567"));
                startActivity(intent);
            } catch (Exception e) {
                android.widget.Toast.makeText(this,
                    "Unable to open phone dialer",
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Email Support
        findViewById(R.id.emailSupport).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:support@trms.mw"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "TRMS Driver App Support Request");
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (Exception e) {
                android.widget.Toast.makeText(this,
                    "Unable to open email client",
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}

