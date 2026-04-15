package com.cgana.trmsownerapp.ui.alerts;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsownerapp.data.repository.AlertsRepository;

public class AlertsViewModelFactory implements ViewModelProvider.Factory {
    private AlertsRepository repository;

    public AlertsViewModelFactory(AlertsRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AlertsViewModel.class)) {
            return (T) new AlertsViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

