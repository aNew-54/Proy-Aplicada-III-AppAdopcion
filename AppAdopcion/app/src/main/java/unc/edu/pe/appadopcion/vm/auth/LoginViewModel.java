package unc.edu.pe.appadopcion.vm.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading    = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage  = new MutableLiveData<>();
    private final MutableLiveData<AuthResponse> loginSuccess = new MutableLiveData<>();

    // idRefugio expuesto para que la Activity lo guarde en SessionManager
    private final MutableLiveData<Integer> idRefugioLiveData = new MutableLiveData<>(-1);

    public LiveData<Boolean> getIsLoading()          { return isLoading; }
    public LiveData<String> getErrorMessage()        { return errorMessage; }
    public LiveData<AuthResponse> getLoginSuccess()  { return loginSuccess; }
    public LiveData<Integer> getIdRefugio()          { return idRefugioLiveData; }

    public void iniciarSesion(String email, String password) {
        isLoading.setValue(true);

        AppRepository repoAnon = new AppRepository();
        repoAnon.login(email, password, new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    AuthResponse authResponse = resp.body();
                    String uuid  = authResponse.getUser().getId();
                    String token = authResponse.getAccessToken();
                    obtenerRolUsuario(uuid, token, authResponse);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Correo o contraseña incorrectos");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red: " + t.getMessage());
            }
        });
    }

    private void obtenerRolUsuario(String uuid, String token, AuthResponse authResponse) {
        AppRepository repoAuth = new AppRepository(token);
        repoAuth.obtenerUsuario(uuid, new Callback<List<UsuarioRequest>>() {
            @Override
            public void onResponse(Call<List<UsuarioRequest>> call, Response<List<UsuarioRequest>> r) {
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                    UsuarioRequest usuario = r.body().get(0);
                    authResponse.setRol(usuario.getRol());

                    // Si es refugio → obtener id_refugio antes de emitir el éxito
                    if ("Refugio".equals(usuario.getRol())) {
                        obtenerIdRefugio(uuid, token, authResponse);
                    } else {
                        // Adoptante → guardar con idRefugio = -1
                        idRefugioLiveData.setValue(-1);
                        isLoading.setValue(false);
                        loginSuccess.setValue(authResponse);
                    }
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error: cuenta incompleta");
                }
            }
            @Override
            public void onFailure(Call<List<UsuarioRequest>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error al obtener perfil: " + t.getMessage());
            }
        });
    }

    private void obtenerIdRefugio(String uuid, String token, AuthResponse authResponse) {
        AppRepository repoAuth = new AppRepository(token);
        repoAuth.obtenerRefugioPorUuid(uuid, new Callback<List<RefugioResponse>>() {
            @Override
            public void onResponse(Call<List<RefugioResponse>> call, Response<List<RefugioResponse>> r) {
                isLoading.setValue(false);
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                    int idRefugio = r.body().get(0).idRefugio;
                    idRefugioLiveData.setValue(idRefugio);
                } else {
                    // AGREGA ESTO TEMPORALMENTE
                    String error = "";
                    try {
                        error = r.errorBody() != null ? r.errorBody().string() : "body null o vacío, code: " + r.code();
                    } catch (Exception e) { error = e.getMessage(); }
                    errorMessage.setValue("DEBUG refugio: " + error);
                    idRefugioLiveData.setValue(-1);
                }
                loginSuccess.setValue(authResponse);
            }
            @Override
            public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("DEBUG fallo refugio: " + t.getMessage());
                idRefugioLiveData.setValue(-1);
                loginSuccess.setValue(authResponse);
            }
        });
    }
}