package com.cgana.trmsownerapp.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cgana.trmsownerapp.data.model.User;
import com.cgana.trmsownerapp.data.repository.AuthRepository;

import java.util.regex.Pattern;

public class LoginViewModel extends ViewModel {
    private AuthRepository authRepository;
    private MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        loginState.setValue(LoginState.idle());
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String phoneNumber, String password) {
        // Validate
        if (!isValidPhoneNumber(phoneNumber)) {
            loginState.setValue(LoginState.error("Invalid phone format. Use +265XXXXXXXXX"));
            return;
        }

        if (password.length() < 8) {
            loginState.setValue(LoginState.error("Password must be at least 8 characters"));
            return;
        }

        loginState.setValue(LoginState.loading());

        LiveData<AuthRepository.Result<User>> result = authRepository.login(phoneNumber, password);
        result.observeForever(userResult -> {
            if (userResult.isSuccess()) {
                loginState.setValue(LoginState.success(userResult.getData()));
            } else {
                loginState.setValue(LoginState.error(userResult.getError()));
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        Pattern pattern = Pattern.compile("^\\+265\\d{9}$");
        return pattern.matcher(phone).matches();
    }

    // State class
    public static class LoginState {
        public enum Status {
            IDLE, LOADING, SUCCESS, ERROR
        }

        private Status status;
        private User user;
        private String error;

        private LoginState(Status status, User user, String error) {
            this.status = status;
            this.user = user;
            this.error = error;
        }

        public static LoginState idle() {
            return new LoginState(Status.IDLE, null, null);
        }

        public static LoginState loading() {
            return new LoginState(Status.LOADING, null, null);
        }

        public static LoginState success(User user) {
            return new LoginState(Status.SUCCESS, user, null);
        }

        public static LoginState error(String error) {
            return new LoginState(Status.ERROR, null, error);
        }

        public Status getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public String getError() {
            return error;
        }
    }
}

