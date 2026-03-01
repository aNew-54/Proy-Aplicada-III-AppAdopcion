package unc.edu.pe.appadopcion.vm.feed;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class DescubrirViewModelFactory implements ViewModelProvider.Factory {

    private final AppRepository repository;

    public DescubrirViewModelFactory(AppRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DescubrirViewModel.class)) {
            return (T) new DescubrirViewModel(repository);
        }
        throw new IllegalArgumentException("Clase ViewModel desconocida");
    }
}
