package com.cgana.trmsdriver.ui.destination;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.cgana.trmsdriver.data.repository.DestinationRepository;

/**
 * ViewModel Factory for DestinationSelectionViewModel (Module 3 Part 2)
 */
public class DestinationSelectionViewModelFactory implements ViewModelProvider.Factory {

    private final DestinationRepository repository;

    public DestinationSelectionViewModelFactory(DestinationRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DestinationSelectionViewModel.class)) {
            return (T) new DestinationSelectionViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}

