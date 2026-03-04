package unc.edu.pe.appadopcion.vm.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.BuildConfig;
import unc.edu.pe.appadopcion.data.api.StorageApi;
import unc.edu.pe.appadopcion.data.api.StorageClient;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.RefugioRequest;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class RegistroRefugioViewModel extends ViewModel {

    private final MutableLiveData<String>  loadingState   = new MutableLiveData<>(null);
    private final MutableLiveData<String>  errorMessage   = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registroSuccess = new MutableLiveData<>();

    public LiveData<String>  getLoadingState()    { return loadingState; }
    public LiveData<String>  getErrorMessage()    { return errorMessage; }
    public LiveData<Boolean> getRegistroSuccess() { return registroSuccess; }

    public void registrarRefugio(String email, String password, String nombre, String descripcion,
                                 String telefono, String direccion, Double latitud, Double longitud,
                                 byte[] perfilBytes, byte[] portadaBytes) {

        loadingState.setValue("Autenticando...");
        AppRepository repoAnon = new AppRepository();

        repoAnon.registrar(email, password, new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String uuid  = resp.body().getUser().getId();
                    String token = resp.body().getAccessToken();
                    subirPerfil(uuid, token, nombre, descripcion, telefono,
                            email, direccion, latitud, longitud, perfilBytes, portadaBytes);
                } else {
                    terminarConError("El correo ya está en uso o es inválido");
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                terminarConError("Error de red: " + t.getMessage());
            }
        });
    }

    // ── PASO 2: Foto de perfil → bucket "avatars" ────────────────────────────
    // URL guardada en BD: ruta relativa  "avatars/{uuid}/profile.jpg"
    // (ImageLoader la reconstruye con el token para proteger el bucket privado)
    private void subirPerfil(String uuid, String token, String nombre, String descripcion,
                             String telefono, String email, String direccion,
                             Double latitud, Double longitud,
                             byte[] perfilBytes, byte[] portadaBytes) {

        if (perfilBytes == null) {
            subirPortada(uuid, token, nombre, descripcion, telefono,
                    email, direccion, latitud, longitud, null, portadaBytes);
            return;
        }

        loadingState.postValue("Subiendo foto de perfil...");
        StorageApi api  = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), perfilBytes);

        // Primera vez → uploadFile (no existe aún)
        api.uploadFile("avatars", uuid, "profile.jpg", "image/jpeg", body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> res) {
                        // Guardamos RUTA RELATIVA (el bucket avatars es privado)
                        String perfilUrl = res.isSuccessful()
                                ? "avatars/" + uuid + "/profile.jpg"
                                : null;
                        subirPortada(uuid, token, nombre, descripcion, telefono,
                                email, direccion, latitud, longitud, perfilUrl, portadaBytes);
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        subirPortada(uuid, token, nombre, descripcion, telefono,
                                email, direccion, latitud, longitud, null, portadaBytes);
                    }
                });
    }

    // ── PASO 3: Foto de portada → bucket "refugio-covers" ───────────────────
    // URL guardada en BD: URL PÚBLICA COMPLETA (el bucket es público)
    // https://{proyecto}.supabase.co/storage/v1/object/public/refugio-covers/{uuid}/cover.jpg
    private void subirPortada(String uuid, String token, String nombre, String descripcion,
                              String telefono, String email, String direccion,
                              Double latitud, Double longitud,
                              String perfilUrl, byte[] portadaBytes) {

        if (portadaBytes == null) {
            insertarEnBD(uuid, token, nombre, descripcion, telefono,
                    email, direccion, latitud, longitud, perfilUrl, null);
            return;
        }

        loadingState.postValue("Subiendo portada...");
        StorageApi api  = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), portadaBytes);

        // Primera vez → uploadFile
        api.uploadFile("refugio-covers", uuid, "cover.jpg", "image/jpeg", body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> res) {
                        // Guardamos URL PÚBLICA COMPLETA (el bucket refugio-covers es público)
                        String portadaUrl = res.isSuccessful()
                                ? BuildConfig.SUPABASE_URL
                                + "/storage/v1/object/public/refugio-covers/"
                                + uuid + "/cover.jpg"
                                : null;
                        insertarEnBD(uuid, token, nombre, descripcion, telefono,
                                email, direccion, latitud, longitud, perfilUrl, portadaUrl);
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        insertarEnBD(uuid, token, nombre, descripcion, telefono,
                                email, direccion, latitud, longitud, perfilUrl, null);
                    }
                });
    }

    // ── PASO 4: Guardar en PostgreSQL ────────────────────────────────────────
    private void insertarEnBD(String uuid, String token, String nombre, String descripcion,
                              String telefono, String email, String direccion,
                              Double latitud, Double longitud,
                              String perfilUrl, String portadaUrl) {

        loadingState.postValue("Guardando datos del refugio...");
        AppRepository repo = new AppRepository(token);

        UsuarioRequest nuevoUsuario = new UsuarioRequest(
                uuid, email, telefono, "Refugio", direccion, latitud, longitud,
                perfilUrl   // ruta relativa → foto circular privada
        );

        repo.crearUsuario(nuevoUsuario, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    RefugioRequest nuevoRefugio = new RefugioRequest(
                            uuid, nombre, descripcion,
                            portadaUrl  // URL pública completa → banner grande
                    );
                    repo.crearRefugio(nuevoRefugio, new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) {
                                loadingState.postValue(null);
                                registroSuccess.postValue(true);
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