package unc.edu.pe.appadopcion.vm.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // Ahora usamos AuthResponse directamente
    private final MutableLiveData<AuthResponse> loginSuccess = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AuthResponse> getLoginSuccess() { return loginSuccess; }

    public void iniciarSesion(String email, String password) {
        isLoading.setValue(true);

        AppRepository repoAnon = new AppRepository();

        repoAnon.login(email, password, new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    // Guardamos la respuesta inicial (que aún no tiene el rol)
                    AuthResponse authResponse = resp.body();
                    String uuid = authResponse.getUser().getId();
                    String token = authResponse.getAccessToken();

                    // Pasamos el objeto para completarlo en el siguiente paso
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
                isLoading.setValue(false);

                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                    UsuarioRequest usuario = r.body().get(0);

                    // ¡AQUÍ ESTÁ LA MAGIA! Inyectamos el rol en tu modelo existente
                    authResponse.setRol(usuario.getRol());

                    // Emitimos el objeto ya completo a la Vista
                    loginSuccess.setValue(authResponse);
                } else {
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
}