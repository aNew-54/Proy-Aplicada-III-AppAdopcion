package unc.edu.pe.appadopcion.vm.perfil;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class RefugioPerfilViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<RefugioResponse> perfil = new MutableLiveData<>();
    private final MutableLiveData<List<SolicitudResponse>> solicitudes = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<RefugioResponse> getPerfil() { return perfil; }
    public LiveData<List<SolicitudResponse>> getSolicitudes() { return solicitudes; }

    public void cargarDatosCompletos(String uuid, String token, Context context) {
        isLoading.setValue(true);
        AppRepository repo = new AppRepository(token);

        repo.obtenerRefugioPorUuid(uuid, new Callback<List<RefugioResponse>>() {
            @Override
            public void onResponse(Call<List<RefugioResponse>> call,
                                   Response<List<RefugioResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    RefugioResponse refugio = resp.body().get(0);
                    perfil.setValue(refugio);

                    // Actualiza el idRefugio en sesión si aún no estaba guardado
                    SessionManager session = new SessionManager(context);
                    if (session.getIdRefugio() == -1) {
                        session.guardarSesion(uuid, token, "Refugio", refugio.idRefugio,-1);
                    }

                    cargarSolicitudesRecibidas(repo, refugio.idRefugio);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("No se pudo cargar el perfil del refugio");
                }
            }

            @Override
            public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red al cargar el perfil");
            }
        });
    }

    private void cargarSolicitudesRecibidas(AppRepository repo, int idRefugio) {
        repo.obtenerSolicitudesRefugio(idRefugio, new Callback<List<SolicitudResponse>>() {
            @Override
            public void onResponse(Call<List<SolicitudResponse>> call, Response<List<SolicitudResponse>> resp) {
                isLoading.setValue(false); // Ambas peticiones terminaron
                if (resp.isSuccessful() && resp.body() != null) {
                    solicitudes.setValue(resp.body());
                }
            }

            @Override
            public void onFailure(Call<List<SolicitudResponse>> call, Throwable t) {
                isLoading.setValue(false);
                // Si fallan las solicitudes, no bloqueamos el perfil completo
            }
        });
    }
}