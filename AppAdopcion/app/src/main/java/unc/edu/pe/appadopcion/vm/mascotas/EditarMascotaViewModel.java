package unc.edu.pe.appadopcion.vm.mascotas;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.BuildConfig;
import unc.edu.pe.appadopcion.data.model.EspecieResponse;
import unc.edu.pe.appadopcion.data.model.FotoMascotaRequest;
import unc.edu.pe.appadopcion.data.model.FotoMascotaResponse;
import unc.edu.pe.appadopcion.data.model.IntervencionRequest;
import unc.edu.pe.appadopcion.data.model.IntervencionResponse;
import unc.edu.pe.appadopcion.data.model.MascotaRequest;
import unc.edu.pe.appadopcion.data.model.RazaResponse;
import unc.edu.pe.appadopcion.data.model.VacunaMascotaRequest;
import unc.edu.pe.appadopcion.data.model.VacunaMascotaResponse;
import unc.edu.pe.appadopcion.data.model.VacunaResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.ui.mascotas.IntervencionLocal;

public class EditarMascotaViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<List<EspecieResponse>> especiesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<RazaResponse>> razasLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<VacunaResponse>> vacunasDisponiblesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<VacunaResponse>> vacunasSeleccionadas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<IntervencionLocal>> intervencionesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Uri>> listaGaleria = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();

    private AppRepository repo;

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public LiveData<List<EspecieResponse>> getEspecies() { return especiesLiveData; }
    public LiveData<List<RazaResponse>> getRazas() { return razasLiveData; }
    public LiveData<List<VacunaResponse>> getVacunasDisponibles() { return vacunasDisponiblesLiveData; }
    public LiveData<List<VacunaResponse>> getVacunasSeleccionadas() { return vacunasSeleccionadas; }
    public LiveData<List<IntervencionLocal>> getIntervenciones() { return intervencionesLiveData; }
    public LiveData<List<Uri>> getListaGaleria() { return listaGaleria; }
    public LiveData<Boolean> getDeleteSuccess() { return deleteSuccess; }

    public void init(String token) {
        repo = new AppRepository(token);
    }

    public void cargarEspecies() {
        repo.obtenerEspecies(new Callback<List<EspecieResponse>>() {
            @Override
            public void onResponse(Call<List<EspecieResponse>> call, Response<List<EspecieResponse>> response) {
                if (response.isSuccessful()) especiesLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<EspecieResponse>> call, Throwable t) {
                errorMessage.setValue("Error al cargar especies");
            }
        });
    }

    public void cargarRazasPorEspecie(int idEspecie) {
        repo.obtenerRazasPorEspecie(idEspecie, new Callback<List<RazaResponse>>() {
            @Override
            public void onResponse(Call<List<RazaResponse>> call, Response<List<RazaResponse>> response) {
                if (response.isSuccessful()) razasLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<RazaResponse>> call, Throwable t) {
                errorMessage.setValue("Error al cargar razas");
            }
        });
    }

    public void cargarVacunasPorEspecie(int idEspecie) {
        repo.obtenerVacunasPorEspecie(idEspecie, new Callback<List<VacunaResponse>>() {
            @Override
            public void onResponse(Call<List<VacunaResponse>> call, Response<List<VacunaResponse>> response) {
                if (response.isSuccessful()) vacunasDisponiblesLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<VacunaResponse>> call, Throwable t) {
                errorMessage.setValue("Error al cargar vacunas");
            }
        });
    }

    public void cargarDatosMascota(int idMascota) {
        // Cargar vacunas actuales usando el nuevo modelo con nombre
        repo.obtenerVacunasMascota(idMascota, new Callback<List<VacunaMascotaResponse>>() {
            @Override
            public void onResponse(Call<List<VacunaMascotaResponse>> call, Response<List<VacunaMascotaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VacunaResponse> lista = new ArrayList<>();
                    for (VacunaMascotaResponse v : response.body()) {
                        VacunaResponse vr = new VacunaResponse();
                        vr.id = v.idVacuna;
                        vr.nombre = v.nombreVacuna;
                        vr.fechaAplicacion = v.fechaAplicacion;
                        lista.add(vr);
                    }
                    vacunasSeleccionadas.setValue(lista);
                }
            }
            @Override
            public void onFailure(Call<List<VacunaMascotaResponse>> call, Throwable t) {}
        });

        // Cargar intervenciones actuales
        repo.obtenerIntervencionesMascota(idMascota, new Callback<List<IntervencionResponse>>() {
            @Override
            public void onResponse(Call<List<IntervencionResponse>> call, Response<List<IntervencionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<IntervencionLocal> lista = new ArrayList<>();
                    for (IntervencionResponse r : response.body()) {
                        lista.add(new IntervencionLocal(r.titulo, r.descripcion, r.fecha));
                    }
                    intervencionesLiveData.setValue(lista);
                }
            }
            @Override
            public void onFailure(Call<List<IntervencionResponse>> call, Throwable t) {}
        });

        // Cargar fotos galería
        repo.obtenerFotosMascota(idMascota, new Callback<List<FotoMascotaResponse>>() {
            @Override
            public void onResponse(Call<List<FotoMascotaResponse>> call, Response<List<FotoMascotaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Uri> uris = new ArrayList<>();
                    for (FotoMascotaResponse f : response.body()) {
                        uris.add(Uri.parse(f.urlImagen));
                    }
                    listaGaleria.setValue(uris);
                }
            }
            @Override
            public void onFailure(Call<List<FotoMascotaResponse>> call, Throwable t) {}
        });
    }

    public void setVacunasSeleccionadas(List<VacunaResponse> seleccionadas) {
        vacunasSeleccionadas.setValue(new ArrayList<>(seleccionadas));
    }

    public void agregarIntervencion(IntervencionLocal intervencion) {
        List<IntervencionLocal> lista = new ArrayList<>(intervencionesLiveData.getValue());
        lista.add(intervencion);
        intervencionesLiveData.setValue(lista);
    }

    public void editarIntervencion(int index, IntervencionLocal intervencion) {
        List<IntervencionLocal> lista = new ArrayList<>(intervencionesLiveData.getValue());
        if (index >= 0 && index < lista.size()) {
            lista.set(index, intervencion);
            intervencionesLiveData.setValue(lista);
        }
    }

    public void eliminarIntervencion(int index) {
        List<IntervencionLocal> lista = new ArrayList<>(intervencionesLiveData.getValue());
        if (index >= 0 && index < lista.size()) {
            lista.remove(index);
            intervencionesLiveData.setValue(lista);
        }
    }

    public void agregarFotoGaleria(Uri uri) {
        List<Uri> actual = listaGaleria.getValue();
        if (actual == null) actual = new ArrayList<>();
        if (actual.size() < 5) {
            actual.add(uri);
            listaGaleria.setValue(actual);
        }
    }

    public void eliminarFotoGaleria(int index) {
        List<Uri> actual = listaGaleria.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.remove(index);
            listaGaleria.setValue(actual);
        }
    }

    public void guardarCambios(Context context, String token, String uuidUsuario, int idMascota,
                               int idRefugio, int idRaza, String nombre, int edadAnios, int edadMeses,
                               String genero, String temperamento, String historia,
                               String urlPortadaActual, Uri uriNuevaPortada) {

        isLoading.setValue(true);

        if (uriNuevaPortada != null) {
            String bucketName = "mascotas";
            String path = uuidUsuario + "/portada_edit_" + UUID.randomUUID() + ".jpg";

            subirImagenEditada(context, token, uriNuevaPortada, bucketName, path, nuevaUrl -> {
                actualizarTodo(context, token, uuidUsuario, idMascota, idRefugio, idRaza, nombre, edadAnios,
                        edadMeses, genero, temperamento, historia, nuevaUrl);
            });
        } else {
            actualizarTodo(context, token, uuidUsuario, idMascota, idRefugio, idRaza, nombre, edadAnios,
                    edadMeses, genero, temperamento, historia, urlPortadaActual);
        }
    }

    private void actualizarTodo(Context context, String token, String uuid, int idMascota, int idRefugio, int idRaza,
                                String nombre, int edadAnios, int edadMeses, String genero,
                                String temperamento, String historia, String urlPortada) {

        MascotaRequest request = new MascotaRequest(
                idRefugio, idRaza, nombre, edadAnios, edadMeses,
                urlPortada, genero, temperamento, historia
        );

        repo.actualizarMascota(idMascota, request, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    sincronizarVacunas(token, idMascota, () ->
                        sincronizarIntervenciones(token, idMascota, () ->
                            sincronizarGaleria(context, token, uuid, idMascota, () -> {
                                isLoading.postValue(false);
                                updateSuccess.postValue(true);
                            })
                        )
                    );
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error al actualizar mascota");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red");
            }
        });
    }

    private void sincronizarVacunas(String token, int idMascota, Runnable onDone) {
        repo.eliminarVacunasMascota(idMascota, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                registrarVacunas(token, idMascota, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                registrarVacunas(token, idMascota, onDone);
            }
        });
    }

    private void registrarVacunas(String token, int idMascota, Runnable onDone) {
        List<VacunaResponse> seleccionadas = vacunasSeleccionadas.getValue();
        if (seleccionadas == null || seleccionadas.isEmpty()) {
            onDone.run();
            return;
        }
        registrarVacunaRecursivo(token, idMascota, seleccionadas, 0, onDone);
    }

    private void registrarVacunaRecursivo(String token, int idMascota, List<VacunaResponse> lista, int index, Runnable onDone) {
        if (index >= lista.size()) {
            onDone.run();
            return;
        }
        VacunaResponse vacuna = lista.get(index);
        String fecha = vacuna.fechaAplicacion != null ? vacuna.fechaAplicacion : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        VacunaMascotaRequest req = new VacunaMascotaRequest(idMascota, vacuna.id, fecha);
        repo.agregarVacunaMascota(req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                registrarVacunaRecursivo(token, idMascota, lista, index + 1, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                registrarVacunaRecursivo(token, idMascota, lista, index + 1, onDone);
            }
        });
    }

    private void sincronizarIntervenciones(String token, int idMascota, Runnable onDone) {
        repo.eliminarIntervencionesMascota(idMascota, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                registrarIntervenciones(token, idMascota, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                registrarIntervenciones(token, idMascota, onDone);
            }
        });
    }

    private void registrarIntervenciones(String token, int idMascota, Runnable onDone) {
        List<IntervencionLocal> lista = intervencionesLiveData.getValue();
        if (lista == null || lista.isEmpty()) {
            onDone.run();
            return;
        }
        registrarIntervencionRecursivo(token, idMascota, lista, 0, onDone);
    }

    private void registrarIntervencionRecursivo(String token, int idMascota, List<IntervencionLocal> lista, int index, Runnable onDone) {
        if (index >= lista.size()) {
            onDone.run();
            return;
        }
        IntervencionLocal local = lista.get(index);
        IntervencionRequest req = new IntervencionRequest(idMascota, local.titulo, local.descripcion, convertirFechaIso(local.fecha));
        repo.agregarIntervencion(req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                registrarIntervencionRecursivo(token, idMascota, lista, index + 1, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                registrarIntervencionRecursivo(token, idMascota, lista, index + 1, onDone);
            }
        });
    }

    private void sincronizarGaleria(Context context, String token, String uuid, int idMascota, Runnable onDone) {
        List<Uri> galeria = listaGaleria.getValue();
        if (galeria == null || galeria.isEmpty()) {
            onDone.run();
            return;
        }

        repo.eliminarFotosDeMascota(idMascota, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                subirGaleriaRecursivo(context, token, uuid, "mascotas", idMascota, galeria, 0, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                subirGaleriaRecursivo(context, token, uuid, "mascotas", idMascota, galeria, 0, onDone);
            }
        });
    }

    private void subirGaleriaRecursivo(Context context, String token, String uuid, String bucket, int idMascota, List<Uri> galeria, int index, Runnable onDone) {
        if (index >= galeria.size()) {
            onDone.run();
            return;
        }

        Uri uri = galeria.get(index);
        if (uri.toString().startsWith("http")) {
            registrarFotoGaleria(idMascota, uri.toString(), index + 1, () ->
                    subirGaleriaRecursivo(context, token, uuid, bucket, idMascota, galeria, index + 1, onDone));
        } else {
            String path = uuid + "/galeria_edit_" + idMascota + "_" + index + "_" + UUID.randomUUID() + ".jpg";
            subirImagen(context, token, uri, bucket, path, url -> {
                registrarFotoGaleria(idMascota, url, index + 1, () ->
                        subirGaleriaRecursivo(context, token, uuid, bucket, idMascota, galeria, index + 1, onDone));
            });
        }
    }

    private void registrarFotoGaleria(int idMascota, String url, int orden, Runnable onDone) {
        FotoMascotaRequest foto = new FotoMascotaRequest(idMascota, url, orden);
        repo.agregarFotoMascota(foto, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { onDone.run(); }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { onDone.run(); }
        });
    }

    private void subirImagen(Context context, String token, Uri uri, String bucket, String path, Consumer<String> onSuccess) {
        new Thread(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int bytesLeidos;
                while ((bytesLeidos = is.read(chunk)) != -1) { buffer.write(chunk, 0, bytesLeidos); }
                is.close();
                byte[] bytes = buffer.toByteArray();

                String mimeType = context.getContentResolver().getType(uri);
                if (mimeType == null) mimeType = "image/jpeg";

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse(mimeType), bytes);
                Request request = new Request.Builder()
                        .url(BuildConfig.SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + path)
                        .post(body)
                        .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", mimeType)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String url = BuildConfig.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;
                    onSuccess.accept(url);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void subirImagenEditada(Context context, String token, Uri uri, String bucket, String path, Consumer<String> onSuccess) {
        subirImagen(context, token, uri, bucket, path, onSuccess);
    }

    private String convertirFechaIso(String fecha) {
        try {
            String[] partes = fecha.split("/");
            return partes[2] + "-" + partes[1] + "-" + partes[0];
        } catch (Exception e) { return fecha; }
    }

    public void eliminarMascotaCompleta(String token, int idMascota) {
        isLoading.setValue(true);
        AppRepository repo = new AppRepository(token);

        repo.eliminarMascota(idMascota, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    deleteSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Error al eliminar. Revisa que no tenga solicitudes activas.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Error de red: " + t.getMessage());
            }
        });
    }
}