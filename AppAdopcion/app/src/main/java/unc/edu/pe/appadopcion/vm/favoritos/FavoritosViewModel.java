package unc.edu.pe.appadopcion.vm.favoritos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.data.model.FavoritoResponse;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class FavoritosViewModel extends ViewModel {

    // Variables observables que el Fragment va a "escuchar"
    private final MutableLiveData<List<MascotaResponse>> mascotasFavoritas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<MascotaResponse>> getMascotasFavoritas() { return mascotasFavoritas; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<String> getError() { return error; }

    public void cargarMisFavoritos(AppRepository repo, String uuid) {
        cargando.setValue(true);

        // 1. Primero obtenemos el id_adoptante usando tu UUID
        repo.obtenerAdoptanteCompleto(uuid, new Callback<List<AdoptanteResponse>>() {
            @Override
            public void onResponse(Call<List<AdoptanteResponse>> call, Response<List<AdoptanteResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    int idAdoptante = resp.body().get(0).idAdoptante;
                    obtenerListaFavoritos(repo, idAdoptante);
                } else {
                    error.setValue("No se pudo obtener el perfil del adoptante.");
                    cargando.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<List<AdoptanteResponse>> call, Throwable t) {
                error.setValue("Error de conexión: " + t.getMessage());
                cargando.setValue(false);
            }
        });
    }

    private void obtenerListaFavoritos(AppRepository repo, int idAdoptante) {
        // 2. Usamos el id_adoptante para obtener la lista de sus favoritos
        repo.obtenerFavoritos(idAdoptante, new Callback<List<FavoritoResponse>>() {
            @Override
            public void onResponse(Call<List<FavoritoResponse>> call, Response<List<FavoritoResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    List<FavoritoResponse> listaFavs = resp.body();

                    // Si el usuario no tiene favoritos, enviamos una lista vacía
                    if (listaFavs.isEmpty()) {
                        mascotasFavoritas.setValue(new ArrayList<>());
                        cargando.setValue(false);
                        return;
                    }

                    // Si tiene favoritos, descargamos los detalles de cada mascota
                    obtenerDetallesMascotas(repo, listaFavs);
                } else {
                    error.setValue("Error al obtener la lista de favoritos.");
                    cargando.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<List<FavoritoResponse>> call, Throwable t) {
                error.setValue("Error de conexión: " + t.getMessage());
                cargando.setValue(false);
            }
        });
    }

    private void obtenerDetallesMascotas(AppRepository repo, List<FavoritoResponse> listaFavs) {
        // 3. Descargamos la información completa de cada mascota
        List<MascotaResponse> mascotasCargadas = new ArrayList<>();
        final int totalEsperado = listaFavs.size();

        for (FavoritoResponse fav : listaFavs) {
            repo.obtenerMascota(fav.idMascota, new Callback<List<MascotaResponse>>() {
                @Override
                public void onResponse(Call<List<MascotaResponse>> call, Response<List<MascotaResponse>> resp) {
                    if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                        mascotasCargadas.add(resp.body().get(0));
                    }
                    verificarCargaCompleta(mascotasCargadas, totalEsperado);
                }

                @Override
                public void onFailure(Call<List<MascotaResponse>> call, Throwable t) {
                    // Si falla una mascota en particular, igual continuamos verificando
                    verificarCargaCompleta(mascotasCargadas, totalEsperado);
                }
            });
        }
    }

    private void verificarCargaCompleta(List<MascotaResponse> mascotasCargadas, int totalEsperado) {
        // Actualiza la vista SOLAMENTE cuando ha terminado de consultar todas las mascotas
        if (mascotasCargadas.size() == totalEsperado) {
            mascotasFavoritas.setValue(mascotasCargadas);
            cargando.setValue(false);
        }
    }
}