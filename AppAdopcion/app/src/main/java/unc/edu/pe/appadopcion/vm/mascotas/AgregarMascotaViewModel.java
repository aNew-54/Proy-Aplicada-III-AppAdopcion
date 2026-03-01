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
import unc.edu.pe.appadopcion.data.model.IntervencionRequest;
import unc.edu.pe.appadopcion.data.model.MascotaRequest;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.model.RazaResponse;
import unc.edu.pe.appadopcion.data.model.VacunaMascotaRequest;
import unc.edu.pe.appadopcion.data.model.VacunaResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.ui.mascotas.IntervencionLocal;

public class AgregarMascotaViewModel extends ViewModel {

    // ════════════════════════════════════════════════════════
    // LIVEDATA — Estado observable desde la Activity
    // ════════════════════════════════════════════════════════

    /** Catálogos cargados desde la API al seleccionar especie */
    private final MutableLiveData<List<EspecieResponse>> especiesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> generosLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<RazaResponse>> razas = new MutableLiveData<>();
    private final MutableLiveData<List<VacunaResponse>> vacunas = new MutableLiveData<>();

    /** Datos ingresados por el usuario en memoria (no enviados hasta guardar) */
    private final MutableLiveData<List<Uri>> listaGaleria = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<VacunaResponse>> vacunasSeleccionadas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<IntervencionLocal>> intervencionesLiveData = new MutableLiveData<>(new ArrayList<>());

    /** Estado del flujo de guardado */
    private final MutableLiveData<Boolean> guardandoLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> exitoLiveData     = new MutableLiveData<>();
    private final MutableLiveData<String>  errorLiveData     = new MutableLiveData<>();

    private AppRepository repository;

    // ════════════════════════════════════════════════════════
    // GETTERS — Exponen LiveData como solo lectura
    // ════════════════════════════════════════════════════════

    public LiveData<List<EspecieResponse>> getEspecies() { return especiesLiveData; }
    public LiveData<List<String>> getGeneros() { return generosLiveData; }
    public LiveData<List<RazaResponse>> getRazas() { return razas; }
    public LiveData<List<VacunaResponse>> getVacunas() { return vacunas; }
    public LiveData<List<Uri>> getListaGaleria() { return listaGaleria; }
    public LiveData<List<VacunaResponse>> getVacunasSeleccionadas() { return vacunasSeleccionadas; }
    public LiveData<List<IntervencionLocal>> getIntervenciones() { return intervencionesLiveData; }
    public LiveData<Boolean> getGuardando() { return guardandoLiveData; }
    public LiveData<Boolean> getExito() { return exitoLiveData; }
    public LiveData<String> getError() { return errorLiveData; }

    // ════════════════════════════════════════════════════════
    // CATÁLOGOS — Carga de datos desde Supabase
    // ════════════════════════════════════════════════════════

    /** Carga la lista de especies disponibles */
    public void cargarEspecies(String token) {
        repository = new AppRepository(token);
        repository.obtenerEspecies(new Callback<List<EspecieResponse>>() {
            @Override
            public void onResponse(Call<List<EspecieResponse>> call, Response<List<EspecieResponse>> response) {
                if (response.isSuccessful())
                    especiesLiveData.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<EspecieResponse>> call, Throwable t) {
                errorLiveData.setValue("Error al cargar especies: " + t.getMessage());
            }
        });
    }

    /** Genera la lista fija de géneros (Macho / Hembra) */
    public void cargarGeneros() {
        List<String> listaGeneros = new ArrayList<>();
        listaGeneros.add("Macho");
        listaGeneros.add("Hembra");
        generosLiveData.setValue(listaGeneros);
    }

    /** Carga las razas correspondientes a la especie seleccionada */
    public void cargarRazasPorEspecie(int idEspecie, String token) {
        repository = new AppRepository(token);
        repository.obtenerRazasPorEspecie(idEspecie, new Callback<List<RazaResponse>>() {
            @Override
            public void onResponse(Call<List<RazaResponse>> call, Response<List<RazaResponse>> response) {
                if (response.isSuccessful() && response.body() != null)
                    razas.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<RazaResponse>> call, Throwable t) {
                errorLiveData.setValue("Error al cargar razas: " + t.getMessage());
            }
        });
    }

    /** Carga las vacunas básicas disponibles para la especie seleccionada */
    public void cargarVacunasPorEspecie(int idEspecie, String token) {
        repository = new AppRepository(token);
        repository.obtenerVacunasPorEspecie(idEspecie, new Callback<List<VacunaResponse>>() {
            @Override
            public void onResponse(Call<List<VacunaResponse>> call, Response<List<VacunaResponse>> response) {
                if (response.isSuccessful() && response.body() != null)
                    vacunas.setValue(response.body());
            }
            @Override
            public void onFailure(Call<List<VacunaResponse>> call, Throwable t) {
                errorLiveData.setValue("Error al cargar vacunas: " + t.getMessage());
            }
        });
    }

    // ════════════════════════════════════════════════════════
    // GALERÍA — Gestión de fotos adicionales en memoria
    // ════════════════════════════════════════════════════════

    /** Agrega una Uri a la galería (máximo 5 fotos) */
    public void agregarFotoGaleria(Uri uri) {
        List<Uri> actual = listaGaleria.getValue();
        if (actual == null) actual = new ArrayList<>();
        if (actual.size() < 5) {
            actual.add(uri);
            listaGaleria.setValue(actual);
        }
    }

    /** Elimina una foto de la galería por su posición */
    public void eliminarFotoGaleria(int index) {
        List<Uri> actual = listaGaleria.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.remove(index);
            listaGaleria.setValue(actual);
        }
    }

    // ════════════════════════════════════════════════════════
    // VACUNAS SELECCIONADAS — Persistencia en memoria
    // ════════════════════════════════════════════════════════

    /** Guarda las vacunas marcadas en el BottomSheet para persistirlas entre aperturas */
    public void setVacunasSeleccionadas(List<VacunaResponse> seleccionadas) {
        vacunasSeleccionadas.setValue(new ArrayList<>(seleccionadas));
    }

    // ════════════════════════════════════════════════════════
    // INTERVENCIONES — CRUD en memoria
    // ════════════════════════════════════════════════════════

    /** Agrega una nueva intervención a la lista en memoria */
    public void agregarIntervencion(IntervencionLocal intervencion) {
        List<IntervencionLocal> lista = new ArrayList<>(
                intervencionesLiveData.getValue() != null
                        ? intervencionesLiveData.getValue() : new ArrayList<>()
        );
        lista.add(intervencion);
        intervencionesLiveData.setValue(lista);
    }

    /** Reemplaza una intervención existente por su posición */
    public void editarIntervencion(int index, IntervencionLocal intervencion) {
        List<IntervencionLocal> lista = new ArrayList<>(
                intervencionesLiveData.getValue() != null
                        ? intervencionesLiveData.getValue() : new ArrayList<>()
        );
        if (index >= 0 && index < lista.size()) {
            lista.set(index, intervencion);
            intervencionesLiveData.setValue(lista);
        }
    }

    /** Elimina una intervención por su posición */
    public void eliminarIntervencion(int index) {
        List<IntervencionLocal> lista = new ArrayList<>(
                intervencionesLiveData.getValue() != null
                        ? intervencionesLiveData.getValue() : new ArrayList<>()
        );
        if (index >= 0 && index < lista.size()) {
            lista.remove(index);
            intervencionesLiveData.setValue(lista);
        }
    }

    // ════════════════════════════════════════════════════════
    // GUARDADO — Flujo encadenado de inserción en Supabase
    // ════════════════════════════════════════════════════════

    /**
     * Flujo completo de registro de mascota:
     *   1. Sube imagen de portada a Storage → obtiene URL pública
     *   2. Inserta mascota en BD           → obtiene id_mascota generado
     *   3. Sube fotos de galería           → registra cada una en fotomascota
     *   4. Registra vacunas seleccionadas  → con fecha actual (yyyy-MM-dd)
     *   5. Registra intervenciones médicas → convierte fecha dd/MM/yyyy → yyyy-MM-dd
     */
    public void guardarMascotaCompleta(Context context, String token, String uuid, int idRefugio, String nombre, Integer idRaza,
                                       int edadAnios, int edadMeses, String genero, String temperamento, String historia,
                                       Uri uriPortada, String bucketName) {

        guardandoLiveData.setValue(true);

        // Paso 1 — portada
        subirImagen(context, token, uriPortada, bucketName,
                uuid + "/portada_" + UUID.randomUUID() + ".jpg",
                urlPortada -> {

                    // Paso 2 — mascota
                    MascotaRequest req = new MascotaRequest(
                            idRefugio, idRaza, nombre,
                            edadAnios, edadMeses,
                            urlPortada, genero, temperamento, historia
                    );

                    repository = new AppRepository(token);
                    repository.crearMascotaConRetorno(req, new Callback<List<MascotaResponse>>() {
                        @Override
                        public void onResponse(Call<List<MascotaResponse>> call,
                                               Response<List<MascotaResponse>> response) {
                            if (!response.isSuccessful() || response.body() == null
                                    || response.body().isEmpty()) {
                                errorLiveData.postValue("Error al crear la mascota");
                                guardandoLiveData.postValue(false);
                                return;
                            }

                            int idMascota = response.body().get(0).idMascota;

                            // Paso 3 → 4 → 5 encadenados
                            subirGaleria(context, token, uuid, bucketName, idMascota, () ->
                                    registrarVacunas(token, idMascota, () ->
                                            registrarIntervenciones(token, idMascota, () -> {
                                                guardandoLiveData.postValue(false);
                                                exitoLiveData.postValue(true);
                                            })
                                    )
                            );
                        }

                        @Override
                        public void onFailure(Call<List<MascotaResponse>> call, Throwable t) {
                            errorLiveData.postValue("Error de red: " + t.getMessage());
                            guardandoLiveData.postValue(false);
                        }
                    });
                });
    }

    // ════════════════════════════════════════════════════════
    // STORAGE — Subida de imágenes con OkHttp
    // ════════════════════════════════════════════════════════

    /**
     * Lee la Uri como bytes y la sube al bucket de Supabase Storage usando OkHttp.
     * Detecta el MIME type real de la imagen para evitar rechazos por Content-Type incorrecto.
     * Devuelve la URL pública del archivo subido via callback.
     */
    private void subirImagen(Context context, String token,
                             Uri uri, String bucket, String path,
                             Consumer<String> onSuccess) {
        new Thread(() -> {
            try {
                // Leer imagen como bytes en chunks de 8KB (compatible desde API 21)
                InputStream is = context.getContentResolver().openInputStream(uri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int bytesLeidos;
                while ((bytesLeidos = is.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesLeidos);
                }
                is.close();
                byte[] bytes = buffer.toByteArray();

                // Detectar MIME type real (jpeg, png, webp, etc.)
                String mimeType = context.getContentResolver().getType(uri);
                if (mimeType == null) mimeType = "image/jpeg";

                // POST al endpoint de Storage con headers requeridos por Supabase
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.parse(mimeType), bytes);
                Request request = new Request.Builder()
                        .url(BuildConfig.SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + path)
                        .post(body)
                        .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", mimeType)
                        .addHeader("x-upsert", "true")
                        .build();

                okhttp3.Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String url = BuildConfig.SUPABASE_URL
                            + "/storage/v1/object/public/" + bucket + "/" + path;
                    onSuccess.accept(url);
                } else {
                    errorLiveData.postValue("Error al subir imagen: " + response.code());
                    guardandoLiveData.postValue(false);
                }

            } catch (Exception e) {
                errorLiveData.postValue("Error al leer imagen: " + e.getMessage());
                guardandoLiveData.postValue(false);
            }
        }).start();
    }

    /**
     * Sube las fotos de galería de forma secuencial (recursiva).
     * Si no hay fotos, continúa directamente al siguiente paso.
     */
    private void subirGaleria(Context context, String token, String uuid,
                              String bucket, int idMascota, Runnable onDone) {
        List<Uri> galeria = listaGaleria.getValue();
        if (galeria == null || galeria.isEmpty()) {
            onDone.run();
            return;
        }
        subirFotoGaleriaRecursivo(context, token, uuid, bucket, idMascota, galeria, 0, onDone);
    }

    /** Sube y registra cada foto de galería una por una, manteniendo el orden */
    private void subirFotoGaleriaRecursivo(Context context, String token, String uuid,
                                           String bucket, int idMascota,
                                           List<Uri> galeria, int index, Runnable onDone) {
        if (index >= galeria.size()) {
            onDone.run();
            return;
        }

        String path = uuid + "/galeria_" + idMascota + "_" + index + "_" + UUID.randomUUID() + ".jpg";

        subirImagen(context, token, galeria.get(index), bucket, path, url -> {
            FotoMascotaRequest foto = new FotoMascotaRequest(idMascota, url, index + 1);
            repository.agregarFotoMascota(foto, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    subirFotoGaleriaRecursivo(context, token, uuid, bucket,
                            idMascota, galeria, index + 1, onDone);
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Error no crítico — continúa con la siguiente foto
                    subirFotoGaleriaRecursivo(context, token, uuid, bucket,
                            idMascota, galeria, index + 1, onDone);
                }
            });
        });
    }

    // ════════════════════════════════════════════════════════
    // HISTORIAL MÉDICO — Inserción en BD
    // ════════════════════════════════════════════════════════

    /**
     * Registra las vacunas seleccionadas en detallemascotavacunas.
     * Usa la fecha actual como fecha de aplicación.
     * Si no hay vacunas seleccionadas, continúa al siguiente paso.
     */
    private void registrarVacunas(String token, int idMascota, Runnable onDone) {
        List<VacunaResponse> seleccionadas = vacunasSeleccionadas.getValue();
        if (seleccionadas == null || seleccionadas.isEmpty()) {
            onDone.run();
            return;
        }
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        registrarVacunaRecursivo(token, idMascota, seleccionadas, 0, fechaHoy, onDone);
    }

    /** Inserta cada vacuna de forma secuencial */
    private void registrarVacunaRecursivo(String token, int idMascota,
                                          List<VacunaResponse> lista, int index,
                                          String fecha, Runnable onDone) {
        if (index >= lista.size()) {
            onDone.run();
            return;
        }
        VacunaMascotaRequest req = new VacunaMascotaRequest(idMascota, lista.get(index).id, fecha);
        repository.agregarVacunaMascota(req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                registrarVacunaRecursivo(token, idMascota, lista, index + 1, fecha, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Error no crítico — continúa con la siguiente vacuna
                registrarVacunaRecursivo(token, idMascota, lista, index + 1, fecha, onDone);
            }
        });
    }

    /**
     * Registra las intervenciones médicas en intervencionmedica.
     * Si no hay intervenciones, continúa directamente al éxito.
     */
    private void registrarIntervenciones(String token, int idMascota, Runnable onDone) {
        List<IntervencionLocal> lista = intervencionesLiveData.getValue();
        if (lista == null || lista.isEmpty()) {
            onDone.run();
            return;
        }
        registrarIntervencionRecursivo(token, idMascota, lista, 0, onDone);
    }

    /** Inserta cada intervención de forma secuencial */
    private void registrarIntervencionRecursivo(String token, int idMascota,
                                                List<IntervencionLocal> lista,
                                                int index, Runnable onDone) {
        if (index >= lista.size()) {
            onDone.run();
            return;
        }
        IntervencionLocal local = lista.get(index);
        IntervencionRequest req = new IntervencionRequest(
                idMascota, local.titulo, local.descripcion, convertirFechaIso(local.fecha));
        repository.agregarIntervencion(req, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                registrarIntervencionRecursivo(token, idMascota, lista, index + 1, onDone);
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Error no crítico — continúa con la siguiente intervención
                registrarIntervencionRecursivo(token, idMascota, lista, index + 1, onDone);
            }
        });
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    /** Convierte fecha de formato visual "dd/MM/yyyy" al formato ISO "yyyy-MM-dd" requerido por Supabase */
    private String convertirFechaIso(String fecha) {
        try {
            String[] partes = fecha.split("/");
            return partes[2] + "-" + partes[1] + "-" + partes[0];
        } catch (Exception e) {
            return fecha;
        }
    }
}