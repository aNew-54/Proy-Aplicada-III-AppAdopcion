package unc.edu.pe.appadopcion.vm.solicitudes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class SolicitudesListViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<SolicitudResponse>> solicitudes = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<List<SolicitudResponse>> getSolicitudes() { return solicitudes; }

    public void cargarSolicitudes(AppRepository repo, boolean esRefugio, int idUsuarioInterno) {
        isLoading.setValue(true);

        Callback<List<SolicitudResponse>> callback = new Callback<List<SolicitudResponse>>() {
            @Override
            public void onResponse(Call<List<SolicitudResponse>> call, Response<List<SolicitudResponse>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    solicitudes.setValue(response.body());
                } else {
                    errorMessage.setValue("No se pudieron cargar las solicitudes");
                }
            }
            @Override
            public void onFailure(Call<List<SolicitudResponse>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red: " + t.getMessage());
            }
        };

        // Derivamos la petición según el rol
        if (esRefugio) {
            repo.obtenerSolicitudesRefugio(idUsuarioInterno, callback);
        } else {
            repo.obtenerSolicitudesAdoptante(idUsuarioInterno, callback);
        }
    }
}