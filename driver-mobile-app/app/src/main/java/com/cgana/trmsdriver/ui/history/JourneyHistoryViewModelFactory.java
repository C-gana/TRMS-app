package com.cgana.trmsdriver.ui.history;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsdriver.data.repository.JourneyHistoryRepository;

/**
 * Journey History ViewModel Factory (Module 5 Part 3)
 */
public class JourneyHistoryViewModelFactory implements ViewModelProvider.Factory {

    private final JourneyHistoryRepository repository;

    public JourneyHistoryViewModelFactory(JourneyHistoryRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(JourneyHistoryViewModel.class)) {
            return (T) new JourneyHistoryViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

