package com.cgana.trmsdriver.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle. MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cgana.trmsdriver.data. model.LoginResponse;
import com.cgana.trmsdriver.data.repository.AuthRepository;

public class LoginViewModel extends ViewModel {

    private AuthRepository repository;
    private MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    public LoginViewModel(AuthRepository repository) {
        this.repository = repository;
        loginState.setValue(LoginState.idle());
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    /**
     * Perform login
     */
    public void login(String phoneNumber, String password) {
        // Validation
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            loginState.setValue(LoginState.error("Phone number is required"));
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            loginState.setValue(LoginState.error("Password is required"));
            return;
        }

        // Add +265 prefix if not present
        String fullPhoneNumber = phoneNumber;
        if (!phoneNumber.startsWith("+265")) {
            fullPhoneNumber = "+265" + phoneNumber. replaceFirst("^0+", "");
        }

        // Validate phone number format
        if (!fullPhoneNumber.matches("\\+265\\d{9}")) {
            loginState. setValue(LoginState.error("Invalid phone number format.  Use 9 digits after +265"));
            return;
        }

        // Show loading
        loginState.setValue(LoginState.loading());

        // Call repository
        LiveData<AuthRepository.Result<LoginResponse>> result =
                repository.login(fullPhoneNumber, password);

        result.observeForever(loginResult -> {
            if (loginResult.isSuccess()) {
                loginState.setValue(LoginState.success(loginResult.getData()));
            } else {
                loginState.setValue(LoginState.error(loginResult.getError()));
            }
        });
    }

    /**
     * Login State class
     */
    public static class LoginState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        private Status status;
        private LoginResponse data;
        private String error;

        private LoginState(Status status, LoginResponse data, String error) {
            this. status = status;
            this. data = data;
            this. error = error;
        }

        public static LoginState idle() {
            return new LoginState(Status.IDLE, null, null);
        }

        public static LoginState loading() {
            return new LoginState(Status.LOADING, null, null);
        }

        public static LoginState success(LoginResponse data) {
            return new LoginState(Status.SUCCESS, data, null);
        }

        public static LoginState error(String error) {
            return new LoginState(Status.ERROR, null, error);
        }

        public Status getStatus() { return status; }
        public LoginResponse getData() { return data; }
        public String getError() { return error; }
    }
}