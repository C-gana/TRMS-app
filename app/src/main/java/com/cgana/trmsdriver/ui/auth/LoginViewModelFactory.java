package com.cgana.trmsdriver.ui.auth;

import androidx. annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.cgana.trmsdriver.data.repository.AuthRepository;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private AuthRepository repository;

    public LoginViewModelFactory(AuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass. isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}