package com.cgana.trmsdriver.ui.settings;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Driver;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvProfileInitial;
    private TextInputEditText etFullName;
    private TextInputEditText etPhoneNumber;
    private TextInputEditText etEmail;
    private TextView tvVehicleId;
    private TextView tvRegistrationNumber;
    private FrameLayout loadingOverlay;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tokenManager = new TokenManager(this);

        initializeViews();
        setupToolbar();
        loadProfileData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        tvVehicleId = findViewById(R.id.tvVehicleId);
        tvRegistrationNumber = findViewById(R.id.tvRegistrationNumber);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadProfileData() {
        Driver driver = tokenManager.getDriver();
        if (driver != null) {
            // Set profile initial
            String fullName = driver.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                String[] nameParts = fullName.split(" ");
                if (nameParts.length >= 2) {
                    tvProfileInitial.setText(
                        String.valueOf(nameParts[0].charAt(0)) +
                        String.valueOf(nameParts[1].charAt(0))
                    );
                } else {
                    tvProfileInitial.setText(String.valueOf(fullName.charAt(0)));
                }
            }

            // Set text fields
            etFullName.setText(fullName);
            etPhoneNumber.setText(driver.getPhoneNumber());
            etEmail.setText(driver.getEmail());
            tvVehicleId.setText(driver.getVehicleId() != null ? driver.getVehicleId() : "-");
            tvRegistrationNumber.setText(driver.getVehicleRegistration() != null ?
                driver.getVehicleRegistration() : "-");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        String fullName = etFullName.getText() != null ?
            etFullName.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            etFullName.setError(getString(R.string.name_required));
            return;
        }

        loadingOverlay.setVisibility(View.VISIBLE);

        // Update driver in TokenManager
        Driver driver = tokenManager.getDriver();
        if (driver != null) {
            driver.setFullName(fullName);
            tokenManager.saveDriver(driver);

            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadingOverlay.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.profile_update_failed), Toast.LENGTH_SHORT).show();
        }
    }
}

