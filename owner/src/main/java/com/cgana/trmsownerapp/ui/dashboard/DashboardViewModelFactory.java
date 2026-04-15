package com.cgana.trmsownerapp.ui.dashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsownerapp.data.repository.DashboardRepository;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {
    private DashboardRepository repository;

    public DashboardViewModelFactory(DashboardRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

