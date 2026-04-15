package com.cgana.trmsownerapp.ui.destinations;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsownerapp.data.repository.DestinationsRepository;

public class DestinationsViewModelFactory implements ViewModelProvider.Factory {
    private DestinationsRepository repository;

    public DestinationsViewModelFactory(DestinationsRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DestinationsViewModel.class)) {
            return (T) new DestinationsViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

