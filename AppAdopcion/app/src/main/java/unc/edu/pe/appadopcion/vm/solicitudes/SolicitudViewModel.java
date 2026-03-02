package unc.edu.pe.appadopcion.vm.solicitudes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.SolicitudRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class SolicitudViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // Método para Adoptante
    public void enviarSolicitud(AppRepository repo, int idRefugio, int idMascota, int idAdoptante, String mensaje) {
        isLoading.setValue(true);
        repo.crearSolicitud(idRefugio, idMascota, idAdoptante, mensaje, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) isSuccess.setValue(true);
                else errorMessage.setValue("Error al enviar la solicitud.");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Método para Refugio
    public void actualizarSolicitud(AppRepository repo, int idSolicitud, String estado, String fechaVisita, String notas) {
        isLoading.setValue(true);
        // Enviamos TODOS los campos. Si el refugio escribió notas, se guardarán sin importar el estado.
        SolicitudRequest req = new SolicitudRequest(estado, fechaVisita, notas);

        repo.actualizarEstadoSolicitud(idSolicitud, req, new Callback<Void>() { // <- Asegúrate de tener este método general en tu repo
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) isSuccess.setValue(true);
                else errorMessage.setValue("Error al actualizar.");
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red.");
            }
        });
    }
}