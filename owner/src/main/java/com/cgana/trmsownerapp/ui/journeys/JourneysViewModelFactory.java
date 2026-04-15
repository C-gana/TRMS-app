package com.cgana.trmsownerapp.ui.journeys;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsownerapp.data.repository.JourneysRepository;

public class JourneysViewModelFactory implements ViewModelProvider.Factory {
    private JourneysRepository repository;

    public JourneysViewModelFactory(JourneysRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(JourneysViewModel.class)) {
            return (T) new JourneysViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

