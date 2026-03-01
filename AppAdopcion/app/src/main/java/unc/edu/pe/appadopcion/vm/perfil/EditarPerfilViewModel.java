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

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // --- NUEVOS LIVEDATA PARA CARGAR DATOS ---
    private final MutableLiveData<AdoptanteResponse> adoptanteActual = new MutableLiveData<>();
    private final MutableLiveData<RefugioResponse> refugioActual = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensajeExito() { return mensajeExito; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<AdoptanteResponse> getAdoptanteActual() { return adoptanteActual; }
    public LiveData<RefugioResponse> getRefugioActual() { return refugioActual; }

    // ========================================================================
    // MÉTODO PARA CARGAR LOS DATOS AL ABRIR LA VISTA
    // ========================================================================
    public void cargarDatosIniciales(String uuid, String token, boolean esRefugio) {
        isLoading.setValue(true);
        AppRepository repo = new AppRepository(token);

        if (esRefugio) {
            repo.obtenerRefugioPorUuid(uuid, new Callback<List<RefugioResponse>>() {
                @Override
                public void onResponse(Call<List<RefugioResponse>> call, Response<List<RefugioResponse>> res) {
                    isLoading.setValue(false);
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        refugioActual.setValue(res.body().get(0));
                    } else {
                        errorMessage.setValue("No se pudo cargar el perfil del refugio.");
                    }
                }
                @Override
                public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error de red al cargar perfil.");
                }
            });
        } else {
            repo.obtenerAdoptanteCompleto(uuid, new Callback<List<AdoptanteResponse>>() {
                @Override
                public void onResponse(Call<List<AdoptanteResponse>> call, Response<List<AdoptanteResponse>> res) {
                    isLoading.setValue(false);
                    if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                        adoptanteActual.setValue(res.body().get(0));
                    } else {
                        errorMessage.setValue("No se pudo cargar el perfil del adoptante.");
                    }
                }
                @Override
                public void onFailure(Call<List<AdoptanteResponse>> call, Throwable t) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error de red al cargar perfil.");
                }
            });
        }
    }

    // ========================================================================
    // MÉTODOS PARA GUARDAR LOS CAMBIOS (Cascada)
    // ========================================================================
    public void guardarCambios(String uuid, String token, boolean esRefugio,
                               byte[] perfilBytes, byte[] portadaBytes,
                               String correoActual, String telefono, String direccion, Double lat, Double lng,
                               String nombre, String apellido, String genero, String fechaNac, String descripcion,
                               String urlPerfilVieja, String urlPortadaVieja) {

        isLoading.setValue(true);

        subirFotoPerfil(uuid, token, perfilBytes, urlPerfilVieja, urlPerfilSubida -> {
            subirFotoPortada(uuid, token, esRefugio, portadaBytes, urlPortadaVieja, urlPortadaSubida -> {
                actualizarUsuario(uuid, token, esRefugio, correoActual, telefono, direccion, lat, lng, urlPerfilSubida, () -> {
                    if (esRefugio) {
                        actualizarRefugio(uuid, token, nombre, descripcion, urlPortadaSubida);
                    } else {
                        actualizarAdoptante(uuid, token, nombre, apellido, genero, fechaNac);
                    }
                });
            });
        });
    }

    // --- MÉTODOS PRIVADOS DE CASCADA (Ya los tenías) ---
    private void subirFotoPerfil(String uuid, String token, byte[] bytes, String urlVieja, OnImagenSubidaCallback cb) {
        if (bytes == null) { cb.onSubida(urlVieja); return; }
        StorageApi api = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
        api.uploadFile("avatars", uuid, "profile.jpg", "image/jpeg", body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                cb.onSubida(res.isSuccessful() ? "avatars/" + uuid + "/profile.jpg" : urlVieja);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { cb.onSubida(urlVieja); }
        });
    }

    private void subirFotoPortada(String uuid, String token, boolean esRefugio, byte[] bytes, String urlVieja, OnImagenSubidaCallback cb) {
        if (!esRefugio || bytes == null) { cb.onSubida(urlVieja); return; }
        StorageApi api = StorageClient.getApi(token);
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
        api.uploadFile("refugio-covers", uuid, "cover.jpg", "image/jpeg", body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                String nuevaUrl = res.isSuccessful() ? BuildConfig.SUPABASE_URL + "/storage/v1/object/public/refugio-covers/" + uuid + "/cover.jpg" : urlVieja;
                cb.onSubida(nuevaUrl);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { cb.onSubida(urlVieja); }
        });
    }

    private void actualizarUsuario(String uuid, String token, boolean esRefugio, String correo, String telefono,
                                   String direccion, Double lat, Double lng, String urlPerfil, Runnable onSuccess) {
        AppRepository repo = new AppRepository(token);
        String rol = esRefugio ? "Refugio" : "Adoptante";
        UsuarioRequest req = new UsuarioRequest(uuid, correo, telefono, rol, direccion, lat, lng, urlPerfil);

        repo.actualizarUsuario(uuid, req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) onSuccess.run();
                else errorMessage.setValue("Error al actualizar datos base del usuario.");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { errorMessage.setValue("Error de red."); }
        });
    }

    private void actualizarRefugio(String uuid, String token, String nombre, String desc, String urlPortada) {
        AppRepository repo = new AppRepository(token);
        RefugioRequest req = new RefugioRequest(uuid, nombre, desc, urlPortada);
        repo.actualizarRefugio(uuid, req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) mensajeExito.setValue("Perfil de refugio actualizado con éxito.");
                else errorMessage.setValue("Error al actualizar refugio.");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { errorMessage.setValue("Error de red."); }
        });
    }

    private void actualizarAdoptante(String uuid, String token, String nombre, String apellido, String genero, String fechaNac) {
        AppRepository repo = new AppRepository(token);
        AdoptanteRequest req = new AdoptanteRequest(uuid, nombre, apellido, genero, fechaNac);
        repo.actualizarAdoptante(uuid, req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) mensajeExito.setValue("Perfil de adoptante actualizado con éxito.");
                else errorMessage.setValue("Error al actualizar adoptante.");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { errorMessage.setValue("Error de red."); }
        });
    }

    private interface OnImagenSubidaCallback { void onSubida(String urlResultante); }
}