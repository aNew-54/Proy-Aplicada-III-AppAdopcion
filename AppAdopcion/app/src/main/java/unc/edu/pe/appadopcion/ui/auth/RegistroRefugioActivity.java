package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.BuildConfig;
import unc.edu.pe.appadopcion.data.api.StorageApi;
import unc.edu.pe.appadopcion.data.api.StorageClient;
import unc.edu.pe.appadopcion.data.api.SupabaseApi;
import unc.edu.pe.appadopcion.data.api.SupabaseClient;
import unc.edu.pe.appadopcion.data.model.AuthRequest;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.RefugioRequest;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.databinding.ActivityRegistroRefugioBinding;
import unc.edu.pe.appadopcion.utils.ImageHelper;

public class RegistroRefugioActivity extends AppCompatActivity {

    private ActivityRegistroRefugioBinding binding;

    private double latitudSeleccionada  = 0;
    private double longitudSeleccionada = 0;
    private String direccionFormateada  = null;

    private Uri imagenPerfilUri  = null;
    private Uri imagenPortadaUri = null;

    private final ActivityResultLauncher<String> perfilPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenPerfilUri = uri;
                    binding.ivFotoPerfil.setImageURI(uri);
                    binding.tvPerfilHint.setText("Foto de perfil seleccionada ✓");
                }
            });

    private final ActivityResultLauncher<String> portadaPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenPortadaUri = uri;
                    binding.ivPortada.setImageURI(uri);
                    binding.tvPortadaHint.setText("Imagen de portada seleccionada ✓");
                }
            });

    private final ActivityResultLauncher<Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    latitudSeleccionada  = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LATITUD, 0);
                    longitudSeleccionada = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LONGITUD, 0);
                    direccionFormateada  = result.getData().getStringExtra(MapPickerActivity.EXTRA_DIRECCION);

                    binding.etDireccion.setText(direccionFormateada);
                    binding.tilDireccion.setError(null);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroRefugioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.etDireccion.setOnClickListener(v -> abrirMapa());
        binding.etDireccion.setFocusable(false);
        binding.ivMapIcon.setOnClickListener(v -> abrirMapa());

        binding.cardFotoPerfil.setOnClickListener(v -> perfilPickerLauncher.launch("image/*"));
        binding.cardPortada.setOnClickListener(v -> portadaPickerLauncher.launch("image/*"));

        binding.btnRegistrarse.setOnClickListener(v -> validarYRegistrar());
    }

    private void abrirMapa() {
        mapLauncher.launch(new Intent(this, MapPickerActivity.class));
    }

    private void validarYRegistrar() {
        String nombre      = binding.etNombreRefugio.getText().toString().trim();
        String descripcion = binding.etDescripcion.getText().toString().trim();
        String direccion   = binding.etDireccion.getText().toString().trim();
        String telefono    = binding.etTelefono.getText().toString().trim();
        String email       = binding.etEmail.getText().toString().trim();
        String password    = binding.etPassword.getText().toString().trim();

        if (nombre.isEmpty()) { binding.tilNombreRefugio.setError("Campo requerido"); return; } else binding.tilNombreRefugio.setError(null);
        if (direccion.isEmpty()) { binding.tilDireccion.setError("Selecciona la ubicación en el mapa"); return; } else binding.tilDireccion.setError(null);
        if (telefono.isEmpty()) { binding.tilTelefono.setError("Campo requerido"); return; } else binding.tilTelefono.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido"); return;
        } else binding.tilEmail.setError(null);

        if (password.length() < 6) { binding.tilPassword.setError("Mínimo 6 caracteres"); return; } else binding.tilPassword.setError(null);

        binding.btnRegistrarse.setEnabled(false);
        binding.btnRegistrarse.setText("Procesando...");

        SupabaseApi apiAnon = SupabaseClient.getClient().create(SupabaseApi.class);

        apiAnon.registrarCredenciales(new AuthRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            String uuid  = resp.body().getUser().getId();
                            String token = resp.body().getAccessToken();
                            SupabaseApi apiAuth = SupabaseClient.getClient(token).create(SupabaseApi.class);
                            subirImagenesYContinuar(uuid, token, apiAuth, nombre, descripcion, direccion, telefono, email);
                        } else {
                            Toast.makeText(RegistroRefugioActivity.this, "Error: El correo ya está en uso", Toast.LENGTH_LONG).show();
                            resetBoton();
                        }
                    }
                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(RegistroRefugioActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        resetBoton();
                    }
                });
    }

    private void subirImagenesYContinuar(String uuid, String token, SupabaseApi apiAuth,
                                         String nombre, String descripcion, String direccion,
                                         String telefono, String email) {
        binding.btnRegistrarse.setText("Subiendo imágenes...");

        new Thread(() -> {
            byte[] perfilBytes  = imagenPerfilUri  != null ? ImageHelper.uriToBytes(this, imagenPerfilUri)  : null;
            byte[] portadaBytes = imagenPortadaUri != null ? ImageHelper.uriToBytes(this, imagenPortadaUri) : null;

            runOnUiThread(() -> {
                StorageApi storageApi = StorageClient.getApi(token);

                if (perfilBytes != null) {
                    RequestBody perfilBody = RequestBody.create(MediaType.parse("image/jpeg"), perfilBytes);
                    storageApi.uploadFile("avatars", uuid, "profile.jpg", "image/jpeg", perfilBody)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> r) {
                                    String perfilUrl = r.isSuccessful() ? "avatars/" + uuid + "/profile.jpg" : null;
                                    subirPortadaYFinalizar(uuid, storageApi, apiAuth, nombre, descripcion, direccion, telefono, email, portadaBytes, perfilUrl);
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    subirPortadaYFinalizar(uuid, storageApi, apiAuth, nombre, descripcion, direccion, telefono, email, portadaBytes, null);
                                }
                            });
                } else {
                    subirPortadaYFinalizar(uuid, storageApi, apiAuth, nombre, descripcion, direccion, telefono, email, portadaBytes, null);
                }
            });
        }).start();
    }

    private void subirPortadaYFinalizar(String uuid, StorageApi storageApi, SupabaseApi apiAuth,
                                        String nombre, String descripcion, String direccion,
                                        String telefono, String email, byte[] portadaBytes, String perfilUrl) {
        if (portadaBytes != null) {
            RequestBody portadaBody = RequestBody.create(MediaType.parse("image/jpeg"), portadaBytes);
            storageApi.uploadFile("refugio-covers", uuid, "cover.jpg", "image/jpeg", portadaBody)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> r) {
                            String portadaUrl = r.isSuccessful()
                                    ? BuildConfig.SUPABASE_URL + "/storage/v1/object/public/refugio-covers/" + uuid + "/cover.jpg"
                                    : null;
                            insertarEnBD(uuid, apiAuth, nombre, descripcion, direccion, telefono, email, perfilUrl, portadaUrl);
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            insertarEnBD(uuid, apiAuth, nombre, descripcion, direccion, telefono, email, perfilUrl, null);
                        }
                    });
        } else {
            insertarEnBD(uuid, apiAuth, nombre, descripcion, direccion, telefono, email, perfilUrl, null);
        }
    }

    private void insertarEnBD(String uuid, SupabaseApi apiAuth, String nombre, String descripcion,
                              String direccion, String telefono, String email, String perfilUrl, String portadaUrl) {
        binding.btnRegistrarse.setText("Guardando datos...");

        Double lat = latitudSeleccionada  != 0 ? latitudSeleccionada  : null;
        Double lng = longitudSeleccionada != 0 ? longitudSeleccionada : null;

        // 1. Guardar en tabla Usuario (Incluye ubicación)
        UsuarioRequest nuevoUsuario = new UsuarioRequest(uuid, email, telefono, "Refugio", direccion, lat, lng, perfilUrl);

        apiAuth.crearUsuarioBase(nuevoUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {

                    // 2. Guardar en tabla Refugio (SIN datos de ubicación, según la nueva BD)
                    RefugioRequest nuevoRefugio = new RefugioRequest(uuid, nombre, descripcion, portadaUrl);

                    apiAuth.crearPerfilRefugio(nuevoRefugio).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) {
                                Toast.makeText(RegistroRefugioActivity.this, "¡Refugio registrado exitosamente!", Toast.LENGTH_LONG).show();
                                irAlLogin();
                            } else {
                                Toast.makeText(RegistroRefugioActivity.this, "Error al guardar perfil refugio", Toast.LENGTH_LONG).show();
                                resetBoton();
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { resetBoton(); }
                    });
                } else {
                    Toast.makeText(RegistroRefugioActivity.this, "Error al guardar usuario base", Toast.LENGTH_LONG).show();
                    resetBoton();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { resetBoton(); }
        });
    }

    private void irAlLogin() {
        startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void resetBoton() {
        binding.btnRegistrarse.setEnabled(true);
        binding.btnRegistrarse.setText("Registrarse");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}