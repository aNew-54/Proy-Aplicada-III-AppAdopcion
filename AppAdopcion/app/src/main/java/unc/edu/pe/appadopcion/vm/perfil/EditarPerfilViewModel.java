package unc.edu.pe.appadopcion.vm.perfil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.BuildConfig;
import unc.edu.pe.appadopcion.data.api.StorageApi;
import unc.edu.pe.appadopcion.data.api.StorageClient;
import unc.edu.pe.appadopcion.data.model.AdoptanteRequest;
import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.data.model.RefugioRequest;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class EditarPerfilViewModel extends ViewModel {

    private final MutableLiveData<Boolean>           isLoading       = new MutableLiveData<>(false);
    private final MutableLiveData<String>             mensajeExito    = new MutableLiveData<>();
    private final MutableLiveData<String>             errorMessage    = new MutableLiveData<>();
    private final MutableLiveData<AdoptanteResponse>  adoptanteActual = new MutableLiveData<>();
    private final MutableLiveData<RefugioResponse>    refugioActual   = new MutableLiveData<>();

    public LiveData<Boolean>          getIsLoading()       { return isLoading; }
    public LiveData<String>           getMensajeExito()    { return mensajeExito; }
    public LiveData<String>           getErrorMessage()    { return errorMessage; }
    public LiveData<AdoptanteResponse> getAdoptanteActual() { return adoptanteActual; }
    public LiveData<RefugioResponse>   getRefugioActual()   { return refugioActual; }

    // ════════════════════════════════════════════════════════════════════════
    // CARGAR DATOS AL ABRIR LA PANTALLA
    // ════════════════════════════════════════════════════════════════════════
    public void cargarDatosIniciales(String uuid, String token, boolean esRefugio) {
        isLoading.setValue(true);
        AppRepository repo = new AppRepository(token);

        if (esRefugio) {
            repo.obtenerRefugioPorUuid(uuid, new Callback<List<RefugioResponse>>() {
                @Override
                public void onResponse(Call<List<RefugioResponse>> call, Response<List<RefugioResponse>> res) {
                    isLoading.postValue(false);
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        refugioActual.postValue(res.body().get(0));
                    } else {
                        errorMessage.postValue("No se pudo cargar el perfil del refugio.");
                    }
                }
                @Override
                public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Error de red al cargar perfil.");
                }
            });
        } else {
            repo.obtenerAdoptanteCompleto(uuid, new Callback<List<AdoptanteResponse>>() {
                @Override
                public void onResponse(Call<List<AdoptanteResponse>> call, Response<List<AdoptanteResponse>> res) {
                    isLoading.postValue(false);
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        adoptanteActual.postValue(res.body().get(0));
                    } else {
                        errorMessage.postValue("No se pudo cargar el perfil del adoptante.");
                    }
                }
                @Override
                public void onFailure(Call<List<AdoptanteResponse>> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Error de red al cargar perfil.");
                }
            });
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // GUARDAR CAMBIOS
    // ════════════════════════════════════════════════════════════════════════
    public void guardarCambios(String uuid, String token, boolean esRefugio,
                               byte[] perfilBytes, byte[] portadaBytes,
                               String correoActual, String telefono, String direccion,
                               Double lat, Double lng,
                               String nombre, String apellido, String genero,
                               String fechaNac, String descripcion,
                               String urlPerfilVieja, String urlPortadaVieja) {

        isLoading.setValue(true);

        subirFotoPerfil(uuid, token, perfilBytes, urlPerfilVieja, urlPerfilFinal ->
                subirFotoPortada(uuid, token, esRefugio, portadaBytes, urlPortadaVieja, urlPortadaFinal ->
                        actualizarUsuario(uuid, token, esRefugio, correoActual, telefono, direccion,
                                lat, lng, urlPerfilFinal, () -> {
                                    if (esRefugio) {
                                        actualizarRefugio(uuid, token, nombre, descripcion, urlPortadaFinal);
                                    } else {
                                        actualizarAdoptante(uuid, token, nombre, apellido, genero, fechaNac);
                                    }
                                })
                )
        );
    }

    // ── Foto de perfil (bucket "avatars" — privado) ──────────────────────────
    // Guarda RUTA RELATIVA: "avatars/{uuid}/profile.jpg"
    // ImageLoader la reconstruye con el token en cada carga → no necesita cache-busting
    private void subirFotoPerfil(String uuid, String token,
                                 byte[] bytes, String urlVieja,
                                 OnImagenSubidaCallback cb) {
        if (bytes == null) { cb.onSubida(urlVieja); return; }

        StorageApi  api  = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

        // Si ya tenía foto → upsert (sobreescribe); si no → upload normal
        Call<Void> call = (urlVieja != null && !urlVieja.isEmpty())
                ? api.upsertFile("avatars", uuid, "profile.jpg", "image/jpeg", body)
                : api.uploadFile("avatars", uuid, "profile.jpg", "image/jpeg", body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                cb.onSubida(res.isSuccessful()
                        ? "avatars/" + uuid + "/profile.jpg"
                        : urlVieja);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { cb.onSubida(urlVieja); }
        });
    }

    // ── Foto de portada (bucket "refugio-covers" — público) ──────────────────
    // Guarda URL PÚBLICA COMPLETA con timestamp al final:
    // "https://....supabase.co/.../cover.jpg?t=1234567890"
    //
    // ¿Por qué el timestamp?
    // El archivo en Storage siempre se llama "cover.jpg" (mismo nombre).
    // Glide/Picasso cachean la imagen por URL — si la URL no cambia, muestran
    // la versión vieja aunque el archivo ya fue reemplazado en Storage.
    // El timestamp hace que la URL sea diferente cada vez, forzando fetch nuevo.
    private void subirFotoPortada(String uuid, String token, boolean esRefugio,
                                  byte[] bytes, String urlVieja,
                                  OnImagenSubidaCallback cb) {
        // Adoptantes no tienen portada
        if (!esRefugio || bytes == null) { cb.onSubida(urlVieja); return; }

        StorageApi  api  = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);

        // Si ya tenía portada → upsert; si no → upload normal
        Call<Void> call = (urlVieja != null && !urlVieja.isEmpty())
                ? api.upsertFile("refugio-covers", uuid, "cover.jpg", "image/jpeg", body)
                : api.uploadFile("refugio-covers", uuid, "cover.jpg", "image/jpeg", body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    // ✅ Timestamp al final → fuerza a Glide a descargar la imagen nueva
                    String nuevaUrl = BuildConfig.SUPABASE_URL
                            + "/storage/v1/object/public/refugio-covers/"
                            + uuid + "/cover.jpg"
                            + "?t=" + System.currentTimeMillis();
                    cb.onSubida(nuevaUrl);
                } else {
                    cb.onSubida(urlVieja);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { cb.onSubida(urlVieja); }
        });
    }

    // ── Actualizar tabla usuario ─────────────────────────────────────────────
    private void actualizarUsuario(String uuid, String token, boolean esRefugio,
                                   String correo, String telefono, String direccion,
                                   Double lat, Double lng, String urlPerfil,
                                   Runnable onSuccess) {
        AppRepository repo = new AppRepository(token);
        UsuarioRequest req = new UsuarioRequest(
                uuid, correo, telefono,
                esRefugio ? "Refugio" : "Adoptante",
                direccion, lat, lng, urlPerfil);

        repo.actualizarUsuario(uuid, req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    onSuccess.run();
                } else {
                    isLoading.postValue(false);
                    errorMessage.postValue("Error al actualizar datos de usuario.");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Error de red al actualizar usuario.");
            }
        });
    }

    // ── Actualizar tabla refugio ─────────────────────────────────────────────
    private void actualizarRefugio(String uuid, String token,
                                   String nombre, String desc, String urlPortada) {
        AppRepository repo = new AppRepository(token);
        repo.actualizarRefugio(uuid, new RefugioRequest(uuid, nombre, desc, urlPortada),
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> res) {
                        isLoading.postValue(false);
                        if (res.isSuccessful()) {
                            mensajeExito.postValue("Perfil actualizado con éxito.");
                        } else {
                            errorMessage.postValue("Error al actualizar refugio.");
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        isLoading.postValue(false);
                        errorMessage.postValue("Error de red al actualizar refugio.");
                    }
                });
    }

    // ── Actualizar tabla adoptante ───────────────────────────────────────────
    private void actualizarAdoptante(String uuid, String token,
                                     String nombre, String apellido,
                                     String genero, String fechaNac) {
        AppRepository repo = new AppRepository(token);
        repo.actualizarAdoptante(uuid, new AdoptanteRequest(uuid, nombre, apellido, genero, fechaNac),
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> res) {
                        isLoading.postValue(false);
                        if (res.isSuccessful()) {
                            mensajeExito.postValue("Perfil actualizado con éxito.");
                        } else {
                            errorMessage.postValue("Error al actualizar adoptante.");
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        isLoading.postValue(false);
                        errorMessage.postValue("Error de red al actualizar adoptante.");
                    }
                });
    }

    private interface OnImagenSubidaCallback {
        void onSubida(String urlResultante);
    }
}