package com.cgana.trmsownerapp.ui.analytics;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsownerapp.data.repository.AnalyticsRepository;

public class AnalyticsViewModelFactory implements ViewModelProvider.Factory {
    private AnalyticsRepository repository;

    public AnalyticsViewModelFactory(AnalyticsRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AnalyticsViewModel.class)) {
            return (T) new AnalyticsViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

