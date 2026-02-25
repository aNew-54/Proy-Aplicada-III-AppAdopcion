package unc.edu.pe.appadopcion.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import unc.edu.pe.appadopcion.data.model.*;
import unc.edu.pe.appadopcion.data.repository.AuthRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private AuthRepository repository = new AuthRepository();

    // LiveData para avisar a la vista si el registro fue exitoso
    public MutableLiveData<Boolean> registroExitoso = new MutableLiveData<>();
    public MutableLiveData<String> errorMensaje = new MutableLiveData<>();

    public void registrarAdoptanteCompleto(AuthRequest auth, UsuarioRequest user, AdoptanteRequest adopt) {
        repository.registrarCredenciales(auth, new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()) {
                    // Si las credenciales se crean bien, seguimos con las tablas
                    guardarTablasAdoptante(user, adopt);
                } else {
                    errorMensaje.setValue("Error en credenciales");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                errorMensaje.setValue(t.getMessage());
            }
        });
    }

    private void guardarTablasAdoptante(UsuarioRequest user, AdoptanteRequest adopt) {
        repository.crearUsuarioBase(user, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                repository.crearPerfilAdoptante(adopt, new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        registroExitoso.setValue(true);
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) { }
                });
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }
}