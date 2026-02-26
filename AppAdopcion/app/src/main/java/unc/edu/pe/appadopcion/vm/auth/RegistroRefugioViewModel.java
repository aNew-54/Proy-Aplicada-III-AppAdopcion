package unc.edu.pe.appadopcion.vm.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.api.StorageApi;
import unc.edu.pe.appadopcion.data.api.StorageClient;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.RefugioRequest;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class RegistroRefugioViewModel extends ViewModel {

    // Usamos un String para actualizar el texto del botón dinásticamente
    private final MutableLiveData<String> loadingState = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registroSuccess = new MutableLiveData<>();

    public LiveData<String> getLoadingState() { return loadingState; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getRegistroSuccess() { return registroSuccess; }

    public void registrarRefugio(String email, String password, String nombre, String descripcion,
                                 String telefono, String direccion, Double latitud, Double longitud,
                                 byte[] imageBytes) {

        loadingState.setValue("Autenticando...");
        AppRepository repoAnon = new AppRepository(); // Sin token para el registro

        // PASO 1: Crear credenciales en Supabase Auth
        repoAnon.registrar(email, password, new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String uuid = resp.body().getUser().getId();
                    String token = resp.body().getAccessToken();

                    // PASO 2: Subir imagen o saltar directo a BD
                    if (imageBytes != null) {
                        subirImagenYContinuar(uuid, token, nombre, descripcion, telefono, email, direccion, latitud, longitud, imageBytes);
                    } else {
                        insertarEnBD(uuid, token, nombre, descripcion, telefono, email, direccion, latitud, longitud, null);
                    }
                } else {
                    terminarConError("Error: El correo ya está en uso o es inválido");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                terminarConError("Error de red: " + t.getMessage());
            }
        });
    }

    private void subirImagenYContinuar(String uuid, String token, String nombre, String descripcion,
                                       String telefono, String email, String direccion, Double latitud,
                                       Double longitud, byte[] imageBytes) {

        loadingState.postValue("Subiendo portada...");
        StorageApi storageApi = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

        // Subimos a la carpeta "avatars" (o "portadas" si la tienes configurada)
        storageApi.uploadFile("avatars", uuid, "portada.jpg", "image/jpeg", body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> res) {
                        String imageUrl = res.isSuccessful() ? "avatars/" + uuid + "/portada.jpg" : null;
                        insertarEnBD(uuid, token, nombre, descripcion, telefono, email, direccion, latitud, longitud, imageUrl);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Si falla la imagen, guardamos el perfil sin ella
                        insertarEnBD(uuid, token, nombre, descripcion, telefono, email, direccion, latitud, longitud, null);
                    }
                });
    }

    private void insertarEnBD(String uuid, String token, String nombre, String descripcion,
                              String telefono, String email, String direccion, Double latitud,
                              Double longitud, String imageUrl) {

        loadingState.postValue("Guardando perfil...");
        AppRepository repoAuth = new AppRepository(token); // Ahora con token

        // PASO 3: Guardar en tabla base Usuario
        UsuarioRequest nuevoUsuario = new UsuarioRequest(
                uuid, email, telefono, "Refugio", direccion, latitud, longitud, imageUrl
        );

        repoAuth.crearUsuario(nuevoUsuario, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    // PASO 4: Guardar en tabla Refugio
                    RefugioRequest nuevoRefugio = new RefugioRequest(uuid, nombre, descripcion, imageUrl);

                    repoAuth.crearRefugio(nuevoRefugio, new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) {
                                loadingState.postValue(null);
                                registroSuccess.postValue(true); // ¡Fin del proceso exitoso!
                            } else {
                                terminarConError("Error al guardar detalles del refugio");
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            terminarConError("Fallo de red al guardar refugio");
                        }
                    });
                } else {
                    terminarConError("Error al guardar datos de usuario base");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                terminarConError("Fallo de red al guardar usuario base");
            }
        });
    }

    private void terminarConError(String mensaje) {
        loadingState.postValue(null);
        errorMessage.postValue(mensaje);
    }
}