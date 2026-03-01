package unc.edu.pe.appadopcion.vm.feed;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import unc.edu.pe.appadopcion.data.model.FavoritoResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;

public class DescubrirViewModel extends ViewModel {

    private final AppRepository repository;
    private final MutableLiveData<List<MascotaResponse>> mascotasLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> misFavoritosIds = new MutableLiveData<>();
    public LiveData<List<Integer>> getMisFavoritosIds() { return misFavoritosIds; }

    private String ordenActual = "fecharegistro.desc";
    private List<MascotaResponse> listaOriginal = new ArrayList<>();

    public DescubrirViewModel(AppRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<MascotaResponse>> getMascotasLiveData() { return mascotasLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }

    public void cargarMascotas() {
        isLoading.setValue(true);
        repository.obtenerMascotasDisponibles(ordenActual, 50, 0, new Callback<List<MascotaResponse>>() {
            @Override
            public void onResponse(Call<List<MascotaResponse>> call, Response<List<MascotaResponse>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaOriginal = response.body();
                    mascotasLiveData.setValue(listaOriginal);
                } else {
                    errorLiveData.setValue("Error al cargar las mascotas");
                }
            }

            @Override
            public void onFailure(Call<List<MascotaResponse>> call, Throwable t) {
                isLoading.setValue(false);
                errorLiveData.setValue("Error de conexión: " + t.getMessage());
            }
        });
    }

    public void buscarPorNombre(String query) {
        if (query.isEmpty()) {
            cargarMascotas();
            return;
        }

        isLoading.setValue(true);
        repository.buscarMascotasPorNombre(query, ordenActual, new Callback<List<MascotaResponse>>() {
            @Override
            public void onResponse(Call<List<MascotaResponse>> call, Response<List<MascotaResponse>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaOriginal = response.body();
                    mascotasLiveData.setValue(listaOriginal);
                }
            }
            @Override
            public void onFailure(Call<List<MascotaResponse>> call, Throwable t) {
                isLoading.setValue(false);
                errorLiveData.setValue("Error al buscar: " + t.getMessage());
            }
        });
    }

    public void setOrden(String nuevoOrden) {
        this.ordenActual = nuevoOrden;
        cargarMascotas();
    }

    // Filtra por especie, etapa de vida y ordena la lista
    public void ordenarYFiltrarLista(String especie, String rangoEdad, boolean porFecha, boolean ascendente) {
        if (listaOriginal == null || listaOriginal.isEmpty()) return;

        List<MascotaResponse> listaFiltrada = new ArrayList<>();

        for (MascotaResponse mascota : listaOriginal) {
            // 1. Filtro de Especie
            boolean cumpleEspecie = (especie == null || especie.isEmpty() ||
                    (mascota.nombreEspecie != null && mascota.nombreEspecie.equalsIgnoreCase(especie)));

            // 2. Filtro de Edad (Etapa de vida)
            boolean cumpleEdad = true;
            if (rangoEdad != null && !rangoEdad.isEmpty()) {
                int anios = mascota.edadAnios;
                int meses = mascota.edadMeses;

                switch (rangoEdad) {
                    case "Cachorro (0 - 1 año)":
                        cumpleEdad = (anios == 0) || (anios == 1 && meses == 0);
                        break;
                    case "Joven (1 - 3 años)":
                        cumpleEdad = (anios >= 1 && anios <= 3) && !(anios == 1 && meses == 0);
                        break;
                    case "Adulto (3 - 7 años)":
                        cumpleEdad = (anios > 3 && anios <= 7);
                        break;
                    case "Senior (Más de 7 años)":
                        cumpleEdad = (anios > 7);
                        break;
                }
            }

            if (cumpleEspecie && cumpleEdad) {
                listaFiltrada.add(mascota);
            }
        }

        // 3. Ordenamiento de la lista resultante
        Collections.sort(listaFiltrada, (m1, m2) -> {
            int resultado;
            if (porFecha) {
                String f1 = m1.fechaRegistro != null ? m1.fechaRegistro : "";
                String f2 = m2.fechaRegistro != null ? m2.fechaRegistro : "";
                resultado = f1.compareTo(f2);
            } else {
                resultado = Integer.compare(m1.contadorFavoritos, m2.contadorFavoritos);
            }
            return ascendente ? resultado : -resultado;
        });

        mascotasLiveData.setValue(listaFiltrada);
    }

    // =================================================================================
    // NUEVO MÉTODO AÑADIDO: Para guardar o eliminar el favorito en la BD
    // =================================================================================
    public void toggleFavorito(int idMascota, int idAdoptante, boolean agregar) {
        if (idAdoptante == -1) return;

        if (agregar) {
            repository.agregarFavorito(idMascota, idAdoptante, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Operación silenciosa en background exitosa
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    errorLiveData.setValue("Fallo de red al guardar favorito");
                }
            });
        } else {
            repository.eliminarFavorito(idMascota, idAdoptante, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Operación silenciosa en background exitosa
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    errorLiveData.setValue("Fallo de red al quitar favorito");
                }
            });
        }
    }
    public void cargarMisFavoritos(int idAdoptante) {
        if (idAdoptante == -1) return;

        repository.obtenerFavoritos(idAdoptante, new Callback<List<FavoritoResponse>>() {
            @Override
            public void onResponse(Call<List<FavoritoResponse>> call, Response<List<FavoritoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Integer> ids = new ArrayList<>();
                    for (FavoritoResponse fav : response.body()) {
                        ids.add(fav.idMascota);
                    }
                    misFavoritosIds.setValue(ids);
                }
            }

            @Override
            public void onFailure(Call<List<FavoritoResponse>> call, Throwable t) {
                // Falla silenciosa, simplemente no se pintarán los corazones iniciales
            }
        });
    }
}