package unc.edu.pe.appadopcion.vm.perfil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class AdoptantePerfilViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<AdoptanteResponse> perfil = new MutableLiveData<>();
    private final MutableLiveData<List<SolicitudResponse>> solicitudes = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AdoptanteResponse> getPerfil() { return perfil; }
    public LiveData<List<SolicitudResponse>> getSolicitudes() { return solicitudes; }

    public void cargarDatosCompletos(String uuid, String token) {
        isLoading.setValue(true);
        AppRepository repo = new AppRepository(token);

        // 1. Obtenemos el perfil
        repo.obtenerAdoptanteCompleto(uuid, new Callback<List<AdoptanteResponse>>() {
            @Override
            public void onResponse(Call<List<AdoptanteResponse>> call, Response<List<AdoptanteResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    AdoptanteResponse adoptante = resp.body().get(0);
                    perfil.setValue(adoptante); // Emitimos el perfil a la vista

                    // 2. Automáticamente pedimos las solicitudes usando el ID obtenido
                    cargarSolicitudes(repo, adoptante.idAdoptante);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("No se pudo cargar el perfil");
                }
            }

            @Override
            public void onFailure(Call<List<AdoptanteResponse>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red al cargar el perfil");
            }
        });
    }

    private void cargarSolicitudes(AppRepository repo, int idAdoptante) {
        repo.obtenerSolicitudesAdoptante(idAdoptante, new Callback<List<SolicitudResponse>>() {
            @Override
            public void onResponse(Call<List<SolicitudResponse>> call, Response<List<SolicitudResponse>> resp) {
                isLoading.setValue(false); // Ambas peticiones terminaron
                if (resp.isSuccessful() && resp.body() != null) {
                    solicitudes.setValue(resp.body()); // Emitimos la lista a la vista
                }
            }

            @Override
            public void onFailure(Call<List<SolicitudResponse>> call, Throwable t) {
                isLoading.setValue(false);
                // Si fallan solo las solicitudes, no bloqueamos el perfil completo
            }
        });
    }
}