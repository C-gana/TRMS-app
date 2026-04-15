package com.cgana.trmsownerapp.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsownerapp.MainActivity;
import com.cgana.trmsownerapp.R;
import com.cgana.trmsownerapp.data.api.FCMApiService;
import com.cgana.trmsownerapp.data.api.RetrofitClient;
import com.cgana.trmsownerapp.data.local.TokenManager;
import com.cgana.trmsownerapp.data.model.FCMTokenRequest;
import com.cgana.trmsownerapp.data.model.GenericResponse;
import com.cgana.trmsownerapp.data.repository.AuthRepository;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etPhone, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private LoginViewModel viewModel;
    private TokenManager tokenManager;

    // Notification permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                    getFCMToken();
                } else {
                    Log.d(TAG, "Notification permission denied");
                    // Still navigate, but notifications won't work
                    navigateToMain();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);

        // Initialize ViewModel
        tokenManager = new TokenManager(this);
        AuthRepository authRepository = new AuthRepository(tokenManager);
        LoginViewModelFactory factory = new LoginViewModelFactory(authRepository);
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Check if already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String phone = "+265" + etPhone.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Get FCM token BEFORE attempting login to ensure backend gets real token
            showLoading(true);
            hideError();
            getFCMTokenBeforeLogin(phone, password);
        });
    }

    private void getFCMTokenBeforeLogin(String phone, String password) {
        Log.d(TAG, "Retrieving FCM token before login...");

        // Check if Google Play Services is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services not available: " + resultCode);
            // Save empty token and proceed with login
            tokenManager.saveFCMToken("");
            viewModel.login(phone, password);
            return;
        }

        // Google Play Services is available, try to get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Successfully got FCM token
                        String fcmToken = task.getResult();
                        Log.d(TAG, "FCM Token retrieved: " + fcmToken);

                        // Save the real FCM token BEFORE login
                        tokenManager.saveFCMToken(fcmToken);

                        // Now proceed with login
                        viewModel.login(phone, password);
                    } else {
                        // Failed to get FCM token
                        Log.w(TAG, "Failed to get FCM token before login", task.getException());

                        // Clear any mock token and use empty string
                        tokenManager.saveFCMToken("");

                        // Proceed with login anyway (notifications won't work until token is updated)
                        viewModel.login(phone, password);
                    }
                });
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, state -> {
            switch (state.getStatus()) {
                case LOADING:
                    // Loading state already shown when button clicked
                    showLoading(true);
                    hideError();
                    break;
                case SUCCESS:
                    showLoading(false);
                    requestNotificationPermissionAndGetToken();
                    break;
                case ERROR:
                    showLoading(false);
                    showError(state.getError());
                    break;
                case IDLE:
                    showLoading(false);
                    break;
            }
        });
    }

    private void requestNotificationPermissionAndGetToken() {
        // For Android 13+ (API 33+), request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission already granted
                getFCMToken();
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // No permission needed for Android 12 and below
            getFCMToken();
        }
    }

    private void getFCMToken() {
        // Check if Google Play Services is available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Google Play Services not available: " + resultCode);
            // Navigate without FCM token
            navigateToMain();
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM token failed", task.getException());
                        navigateToMain();
                        return;
                    }

                    // Get FCM token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Send token to backend
                    sendTokenToServer(token);
                });
    }

    private void sendTokenToServer(String fcmToken) {
        // Only send token if it's valid (non-empty)
        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w(TAG, "No FCM token available to send to server");
            navigateToMain();
            return;
        }

        String authToken = tokenManager.getToken();
        if (authToken == null) {
            Log.w(TAG, "Not authenticated, cannot send FCM token");
            navigateToMain();
            return;
        }

        // Save the real FCM token to ensure it persists locally
        tokenManager.saveFCMToken(fcmToken);
        Log.d(TAG, "Sending real FCM token to server via /api/mobile/fcm-token");

        FCMApiService apiService = RetrofitClient.getInstance().getFCMApi();
        FCMTokenRequest request = new FCMTokenRequest(fcmToken);

        apiService.registerFCMToken(request, "Bearer " + authToken)
                .enqueue(new Callback<GenericResponse>() {
                    @Override
                    public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "FCM token registered successfully via backup call");
                        } else {
                            Log.e(TAG, "Failed to register FCM token: " + response.code());
                        }
                        navigateToMain();
                    }

                    @Override
                    public void onFailure(Call<GenericResponse> call, Throwable t) {
                        Log.e(TAG, "Error registering FCM token: " + t.getMessage());
                        navigateToMain();
                    }
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etPhone.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
