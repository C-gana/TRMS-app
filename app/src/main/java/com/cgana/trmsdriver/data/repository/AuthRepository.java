package com.cgana.trmsdriver.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cgana.trmsdriver.data.api.AuthApiService;
import com.cgana.trmsdriver.data.api.RetrofitClient;
import com.cgana.trmsdriver.data.local.TokenManager;
import com.cgana.trmsdriver.data.model.Driver;
import com.cgana.trmsdriver.data.model.DutyStatusRequest;
import com.cgana.trmsdriver.data.model.DutyStatusResponse;
import com.cgana.trmsdriver.data.model.Location;
import com.cgana.trmsdriver.data.model.LoginRequest;
import com.cgana.trmsdriver.data.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private AuthApiService apiService;
    private TokenManager tokenManager;

    public AuthRepository(TokenManager tokenManager) {
        this.apiService = RetrofitClient.getInstance().getAuthApi();
        this.tokenManager = tokenManager;
    }

    /**
     * Login driver
     */
    public LiveData<Result<LoginResponse>> login(String phoneNumber, String password) {
        MutableLiveData<Result<LoginResponse>> result = new MutableLiveData<>();

        LoginRequest request = new LoginRequest(phoneNumber, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                android.util.Log.d("AuthRepository", "Login API response received");
                android.util.Log.d("AuthRepository", "  - Response successful: " + response.isSuccessful());
                android.util.Log.d("AuthRepository", "  - Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    android.util.Log.d("AuthRepository", "  - Login success: " + loginResponse.isSuccess());

                    if (loginResponse.isSuccess()) {
                        // Log received data
                        String token = loginResponse.getToken();
                        Driver driver = loginResponse.getDriver();

                        android.util.Log.d("AuthRepository", "Received data from API:");
                        android.util.Log.d("AuthRepository", "  - Token: " + (token != null ? "Present (length=" + token.length() + ")" : "NULL"));
                        android.util.Log.d("AuthRepository", "  - Driver: " + (driver != null ? "Present" : "NULL"));

                        if (driver != null) {
                            android.util.Log.d("AuthRepository", "  - Driver details:");
                            android.util.Log.d("AuthRepository", "     - Full Name: " + driver.getFullName());
                            android.util.Log.d("AuthRepository", "     - Driver ID: " + driver.getDriverId());
                            android.util.Log.d("AuthRepository", "     - Phone: " + driver.getPhoneNumber());
                            android.util.Log.d("AuthRepository", "     - Vehicle ID: " + driver.getVehicleId());
                            android.util.Log.d("AuthRepository", "     - On Duty: " + driver.isOnDuty());
                        } else {
                            android.util.Log.e("AuthRepository", "  - ❌ DRIVER IS NULL IN API RESPONSE!");
                            android.util.Log.e("AuthRepository", "  - ⚠️ THIS IS A BACKEND ISSUE!");
                            android.util.Log.w("AuthRepository", "  - 🔧 CREATING MOCK DRIVER FOR TESTING...");

                        }

                        if (token == null) {
                            android.util.Log.e("AuthRepository", "  - ❌ TOKEN IS NULL IN API RESPONSE!");
                        }

                        // Save token and driver data
                        android.util.Log.d("AuthRepository", "Saving authentication data...");
                        tokenManager.saveToken(token);
                        tokenManager.saveDriver(driver);

                        android.util.Log.d("AuthRepository", "Verifying data was saved...");
                        String savedToken = tokenManager.getToken();
                        Driver savedDriver = tokenManager.getDriver();

                        android.util.Log.d("AuthRepository", "  - Token retrieved: " + (savedToken != null ? "✅ YES" : "❌ NULL"));
                        android.util.Log.d("AuthRepository", "  - Driver retrieved: " + (savedDriver != null ? "✅ YES" : "❌ NULL"));

                        if (savedDriver != null) {
                            android.util.Log.d("AuthRepository", "  - Saved driver name: " + savedDriver.getFullName());
                        }

                        // Save duty status if available
                        if (driver != null) {
                            boolean onDuty = driver.isOnDuty();
                            android.util.Log.d("AuthRepository", "  - Initial duty status: " + onDuty);
                            tokenManager.saveDutyStatus(onDuty);

                            if (driver.getDutyStartedAt() != null) {
                                tokenManager.saveDutyStartedAt(driver.getDutyStartedAt());
                            }
                        }

                        result.setValue(Result.success(loginResponse));
                    } else {
                        android.util.Log.w("AuthRepository", "Login failed: " + loginResponse.getMessage());
                        result.setValue(Result.error(loginResponse.getMessage()));
                    }
                } else {
                    android.util.Log.e("AuthRepository", "Login request failed with code: " + response.code());
                    result.setValue(Result.error("Login failed.  Please check your credentials."));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                result.setValue(Result.error("Network error:  " + t.getMessage()));
            }
        });

        return result;
    }

    /**
     * Update duty status (ON/OFF)
     */
    public LiveData<Result<DutyStatusResponse>> updateDutyStatus(String vehicleId,
                                                                 boolean onDuty,
                                                                 Location location) {
        android.util.Log.d("AuthRepository", "===== UPDATE DUTY STATUS =====");
        android.util.Log.d("AuthRepository", "Vehicle ID: " + vehicleId);
        android.util.Log.d("AuthRepository", "On Duty: " + onDuty);
        android.util.Log.d("AuthRepository", "Location: " + (location != null ?
            location.getLatitude() + ", " + location.getLongitude() : "NULL"));

        MutableLiveData<Result<DutyStatusResponse>> result = new MutableLiveData<>();

        String token = tokenManager.getToken();
        android.util.Log.d("AuthRepository", "Token: " + (token != null ? "EXISTS" : "NULL"));

        if (token == null) {
            android.util.Log.e("AuthRepository", "Token is NULL! Cannot update duty status");
            result.setValue(Result.error("Not authenticated"));
            return result;
        }

        DutyStatusRequest request = new DutyStatusRequest(vehicleId, onDuty, location);
        android.util.Log.d("AuthRepository", "Request created, making API call...");

        apiService.updateDutyStatus(request, "Bearer " + token)
                .enqueue(new Callback<DutyStatusResponse>() {
                    @Override
                    public void onResponse(Call<DutyStatusResponse> call, Response<DutyStatusResponse> response) {
                        android.util.Log.d("AuthRepository", "===== API RESPONSE RECEIVED =====");
                        android.util.Log.d("AuthRepository", "Response code: " + response.code());
                        android.util.Log.d("AuthRepository", "Is successful: " + response.isSuccessful());
                        android.util.Log.d("AuthRepository", "Body: " + (response.body() != null ? "NOT NULL" : "NULL"));

                        // Log raw response if available
                        try {
                            android.util.Log.d("AuthRepository", "Request URL: " + call.request().url());
                            android.util.Log.d("AuthRepository", "Request body sent: vehicleId=" +
                                (request != null ? request.getVehicleId() : "null") + ", onDuty=" + onDuty);
                        } catch (Exception e) {
                            android.util.Log.e("AuthRepository", "Error logging request details: " + e.getMessage());
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            DutyStatusResponse dutyResponse = response.body();
                            android.util.Log.d("AuthRepository", "Response success: " + dutyResponse.isSuccess());
                            android.util.Log.d("AuthRepository", "Response onDuty: " + dutyResponse.isOnDuty());
                            android.util.Log.d("AuthRepository", "Response message: " + dutyResponse.getMessage());
                            android.util.Log.d("AuthRepository", "Response dutyStartedAt: " + dutyResponse.getDutyStartedAt());

                            android.util.Log.d("AuthRepository", "⚠️ MISMATCH CHECK:");
                            android.util.Log.d("AuthRepository", "  - We sent onDuty: " + onDuty);
                            android.util.Log.d("AuthRepository", "  - API returned onDuty: " + dutyResponse.isOnDuty());
                            android.util.Log.d("AuthRepository", "  - Message says: " + dutyResponse.getMessage());

                            if (dutyResponse.isSuccess()) {
                                // WORKAROUND: If API returns wrong on_duty value, fix it based on message
                                boolean actualOnDuty = dutyResponse.isOnDuty();
                                String message = dutyResponse.getMessage();

                                // Check if message contradicts the on_duty value
                                if (message != null) {
                                    boolean messageIndicatesOnDuty = message.toLowerCase().contains("on duty")
                                                                    || message.toLowerCase().contains("started");
                                    boolean messageIndicatesOffDuty = message.toLowerCase().contains("off duty")
                                                                     || message.toLowerCase().contains("ended");

                                    // If there's a mismatch, trust the message
                                    if (messageIndicatesOnDuty && !actualOnDuty) {
                                        android.util.Log.w("AuthRepository", "⚠️ API BUG DETECTED: Message says ON but field is false. Fixing to true.");
                                        dutyResponse.setOnDuty(true);
                                        actualOnDuty = true;
                                    } else if (messageIndicatesOffDuty && actualOnDuty) {
                                        android.util.Log.w("AuthRepository", "⚠️ API BUG DETECTED: Message says OFF but field is true. Fixing to false.");
                                        dutyResponse.setOnDuty(false);
                                        actualOnDuty = false;
                                    }
                                }

                                android.util.Log.d("AuthRepository", "Final onDuty value after workaround: " + actualOnDuty);

                                // Update local storage with corrected value
                                android.util.Log.d("AuthRepository", "Saving duty status to TokenManager: " + actualOnDuty);
                                tokenManager.saveDutyStatus(actualOnDuty);

                                if (dutyResponse.getDutyStartedAt() != null) {
                                    android.util.Log.d("AuthRepository", "Saving duty started at: " + dutyResponse.getDutyStartedAt());
                                    tokenManager.saveDutyStartedAt(dutyResponse.getDutyStartedAt());
                                }

                                result.setValue(Result.success(dutyResponse));
                                android.util.Log.d("AuthRepository", "Result set to SUCCESS");
                            } else {
                                android.util.Log.e("AuthRepository", "API returned success=false: " + dutyResponse.getMessage());
                                result.setValue(Result.error(dutyResponse.getMessage()));
                            }
                        } else {
                            android.util.Log.e("AuthRepository", "Response not successful or body is null");
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                android.util.Log.e("AuthRepository", "Error body: " + errorBody);
                            } catch (Exception e) {
                                android.util.Log.e("AuthRepository", "Failed to read error body: " + e.getMessage());
                            }
                            result.setValue(Result.error("Failed to update duty status"));
                        }
                    }

                    @Override
                    public void onFailure(Call<DutyStatusResponse> call, Throwable t) {
                        android.util.Log.e("AuthRepository", "===== API CALL FAILED =====");
                        android.util.Log.e("AuthRepository", "Error: " + t.getMessage());
                        android.util.Log.e("AuthRepository", "Error class: " + t.getClass().getName());
                        t.printStackTrace();
                        result.setValue(Result.error("Network error: " + t.getMessage()));
                    }
                });

        return result;
    }

    /**
     * Result wrapper class
     */
    public static class Result<T> {
        private T data;
        private String error;
        private boolean success;

        private Result(T data, String error, boolean success) {
            this.data = data;
            this.error = error;
            this.success = success;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }

        public static <T> Result<T> error(String error) {
            return new Result<>(null, error, false);
        }

        public T getData() { return data; }
        public String getError() { return error; }
        public boolean isSuccess() { return success; }
    }
}