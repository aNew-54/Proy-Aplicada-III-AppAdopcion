package unc.edu.pe.appadopcion.vm.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class LoginViewModel extends ViewModel {

    // --- CLASE ENCAPSULADORA LIMPIA ---
    public static class LoginResult {
        public String uuid;
        public String token;
        public String rol;
        public int idRefugio;
        public int idAdoptante;

        public LoginResult(String uuid, String token, String rol, int idRefugio, int idAdoptante) {
            this.uuid = uuid;
            this.token = token;
            this.rol = rol;
            this.idRefugio = idRefugio;
            this.idAdoptante = idAdoptante;
        }
    }

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<LoginResult> getLoginSuccess() { return loginSuccess; }

    // PASO 1: Auth
    public void iniciarSesion(String email, String password) {
        isLoading.setValue(true);
        AppRepository repoAnon = new AppRepository();

        repoAnon.login(email, password, new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String uuid  = resp.body().getUser().getId();
                    String token = resp.body().getAccessToken();
                    obtenerRolUsuario(uuid, token);
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

    // PASO 2: Obtener el Rol
    private void obtenerRolUsuario(String uuid, String token) {
        AppRepository repoAuth = new AppRepository(token);

        repoAuth.obtenerUsuario(uuid, new Callback<List<UsuarioRequest>>() {
            @Override
            public void onResponse(Call<List<UsuarioRequest>> call, Response<List<UsuarioRequest>> r) {
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                    String rol = r.body().get(0).getRol();
                    obtenerPerfilEspecifico(uuid, token, rol);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error: cuenta sin rol asignado");
                }
            }
            @Override
            public void onFailure(Call<List<UsuarioRequest>> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error al obtener perfil: " + t.getMessage());
            }
        });
    }

    // PASO 3: Obtener el ID numérico
    private void obtenerPerfilEspecifico(String uuid, String token, String rol) {
        AppRepository repoAuth = new AppRepository(token);

        if ("Adoptante".equals(rol)) {
            repoAuth.obtenerAdoptanteCompleto(uuid, new Callback<List<AdoptanteResponse>>() {
                @Override
                public void onResponse(Call<List<AdoptanteResponse>> call, Response<List<AdoptanteResponse>> res) {
                    isLoading.setValue(false);
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        int idAdoptante = res.body().get(0).idAdoptante;
                        // Es adoptante: idRefugio = -1, idAdoptante = valor real
                        loginSuccess.setValue(new LoginResult(uuid, token, rol, -1, idAdoptante));
                    } else {
                        errorMessage.setValue("No se encontró el perfil de adoptante.");
                    }
                }
                @Override
                public void onFailure(Call<List<AdoptanteResponse>> call, Throwable t) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error al descargar perfil de adoptante.");
                }
            });
        } else {
            repoAuth.obtenerRefugioPorUuid(uuid, new Callback<List<RefugioResponse>>() {
                @Override
                public void onResponse(Call<List<RefugioResponse>> call, Response<List<RefugioResponse>> res) {
                    isLoading.setValue(false);
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        int idRefugio = res.body().get(0).idRefugio;
                        // Es refugio: idRefugio = valor real, idAdoptante = -1
                        loginSuccess.setValue(new LoginResult(uuid, token, rol, idRefugio, -1));
                    } else {
                        errorMessage.setValue("No se encontró el perfil de refugio.");
                    }
                }
                @Override
                public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error al descargar perfil de refugio.");
                }
            });
        }
    }
}