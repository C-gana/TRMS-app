package com.cgana.trmsdriver.ui.auth;

import android. content.Intent;
import android. os.Bundle;
import android. text. Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget. LinearLayout;
import android.widget. ProgressBar;
import android. widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle. ViewModelProvider;
import com. google.android.material.button.MaterialButton;
import com.google.android.material.textfield. TextInputEditText;
import com.google. android.material.textfield.TextInputLayout;
import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.repository.AuthRepository;
import com.cgana.trmsdriver.ui.duty.DutyStatusActivity;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputLayout tilPhoneNumber, tilPassword;
    private TextInputEditText etPhoneNumber, etPassword;
    private MaterialButton btnLogin;
    private CheckBox cbRememberMe;
    private LinearLayout errorContainer;
    private FrameLayout loadingContainer;
    private TextView tvError;
    private ProgressBar progressBar;

    // ViewModel
    private LoginViewModel viewModel;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize TokenManager
        tokenManager = new TokenManager(this);

        // Check if already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToNextScreen();
            return;
        }

        // Initialize views
        initializeViews();

        // Initialize ViewModel
        AuthRepository repository = new AuthRepository(tokenManager);
        LoginViewModelFactory factory = new LoginViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Setup listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();

        // Load saved credentials if remember me was checked
        loadSavedCredentials();
    }

    private void initializeViews() {
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilPassword = findViewById(R.id.tilPassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id. btnLogin);
        cbRememberMe = findViewById(R.id. cbRememberMe);
        errorContainer = findViewById(R.id. errorContainer);
        tvError = findViewById(R.id. tvError);
        loadingContainer = findViewById(R.id.loadingContainer);
        progressBar = findViewById(R.id. progressBar);
    }

    private void setupListeners() {
        // Login button click
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Password field - handle Enter key
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo. IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // Clear errors when user starts typing
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPhoneNumber.setError(null);
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    showLoading(true);
                    hideError();
                    break;

                case SUCCESS:
                    showLoading(false);

                    // Save remember me preference
                    tokenManager.saveRememberMe(cbRememberMe.isChecked());

                    // Show success message briefly
                    showSuccessAndNavigate();
                    break;

                case ERROR:
                    showLoading(false);
                    showError(state.getError());

                    // Add haptic feedback for error
                    btnLogin.performHapticFeedback(android.view.HapticFeedbackConstants.REJECT);
                    break;

                case IDLE:
                    showLoading(false);
                    break;
            }
        });
    }

    private void attemptLogin() {
        // Hide keyboard
        hideKeyboard();

        // Get input values
        String phoneNumber = etPhoneNumber.getText() != null ?
                etPhoneNumber.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        // Validate input
        if (!validateInput(phoneNumber, password)) {
            return;
        }
        // Call ViewModel to perform login
        viewModel.login(phoneNumber, password);
    }
    private boolean validateInput(String phoneNumber, String password) {
        boolean isValid = true;
        // Validate phone number
        if (phoneNumber.isEmpty()) {
            tilPhoneNumber.setError(getString(R.string.error_phone_required));
            isValid = false;
        } else if (phoneNumber.length() != 9) {
            tilPhoneNumber.setError(getString(R.string.error_phone_invalid));
            isValid = false;
        } else if (!phoneNumber.matches("\\d+")) {
            tilPhoneNumber.setError(getString(R.string.error_phone_digits_only));
            isValid = false;
        }
        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        }
        return isValid;
    }
    private void hideKeyboard() {
        android.view.View view = getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    private void showLoading(boolean show) {
        if (show) {
            loadingContainer.setVisibility(android.view.View.VISIBLE);
            btnLogin.setEnabled(false);
            etPhoneNumber.setEnabled(false);
            etPassword.setEnabled(false);
            cbRememberMe.setEnabled(false);
        } else {
            loadingContainer.setVisibility(android.view.View.GONE);
            btnLogin.setEnabled(true);
            etPhoneNumber.setEnabled(true);
            etPassword.setEnabled(true);
            cbRememberMe.setEnabled(true);
        }
    }
    private void showError(String message) {
        if (errorContainer != null && tvError != null) {
            errorContainer.setVisibility(android.view.View.VISIBLE);
            tvError.setText(message);
        }
    }
    private void hideError() {
        if (errorContainer != null) {
            errorContainer.setVisibility(android.view.View.GONE);
        }
    }
    private void navigateToNextScreen() {
        android.util.Log.d("LoginActivity", "navigateToNextScreen() called");
        android.util.Log.d("LoginActivity", "  - Creating intent for DutyStatusActivity");
        Intent intent = new Intent(this, DutyStatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.util.Log.d("LoginActivity", "  - Starting DutyStatusActivity");
        startActivity(intent);
        android.util.Log.d("LoginActivity", "  - Calling finish() on LoginActivity");
        finish();
    }
    private void showSuccessAndNavigate() {
        // Show brief success message
        android.widget.Toast.makeText(this, "Login successful!", android.widget.Toast.LENGTH_SHORT).show();
        // Navigate after short delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, 500);
    }
    private void loadSavedCredentials() {
        // Check if remember me was previously enabled
        if (tokenManager.getRememberMe()) {
            cbRememberMe.setChecked(true);
            // For security, we don't save/reload the actual credentials
            // Just check the remember me box
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
        if (viewModel != null) {
            viewModel.getLoginState().removeObservers(this);
        }
    }
}
