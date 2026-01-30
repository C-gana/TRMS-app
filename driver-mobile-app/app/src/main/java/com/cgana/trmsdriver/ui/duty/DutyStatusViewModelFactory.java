package com.cgana.trmsdriver.ui.duty;

import androidx.annotation.NonNull;
import androidx.lifecycle. ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.cgana.trmsdriver.data. repository.AuthRepository;

public class DutyStatusViewModelFactory implements ViewModelProvider.Factory {

    private AuthRepository repository;

    public DutyStatusViewModelFactory(AuthRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DutyStatusViewModel. class)) {
            return (T) new DutyStatusViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}