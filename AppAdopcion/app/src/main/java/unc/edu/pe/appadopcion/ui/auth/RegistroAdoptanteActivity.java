package unc.edu.pe.appadopcion.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.api.StorageApi;
import unc.edu.pe.appadopcion.data.api.StorageClient;
import unc.edu.pe.appadopcion.data.api.SupabaseApi;
import unc.edu.pe.appadopcion.data.api.SupabaseClient;
import unc.edu.pe.appadopcion.data.model.AdoptanteRequest;
import unc.edu.pe.appadopcion.data.model.AuthRequest;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.databinding.ActivityRegistroAdoptanteBinding;
import unc.edu.pe.appadopcion.utils.ImageHelper;

public class RegistroAdoptanteActivity extends AppCompatActivity {

    private ActivityRegistroAdoptanteBinding binding;
    private String fechaNacimientoSeleccionada = null;
    private Uri imagenPerfilUri = null;

    // Variables para el mapa
    private double latitudSeleccionada  = 0;
    private double longitudSeleccionada = 0;
    private String direccionFormateada  = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenPerfilUri = uri;
                    binding.ivFotoPerfil.setImageURI(uri);
                    binding.tvFotoHint.setText("Foto de perfil seleccionada ✓");
                }
            });

    // Launcher para el mapa
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
        binding = ActivityRegistroAdoptanteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] generos = {"Masculino", "Femenino", "Otro", "Prefiero no decir"};
        binding.actvGenero.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, generos));

        binding.etFechaNacimiento.setFocusable(false);
        binding.etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        // Eventos del mapa
        binding.etDireccion.setOnClickListener(v -> abrirMapa());
        binding.etDireccion.setFocusable(false);
        binding.ivMapIcon.setOnClickListener(v -> abrirMapa());

        binding.cardFotoPerfil.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegistrarse.setOnClickListener(v -> validarYRegistrar());
    }

    private void abrirMapa() {
        mapLauncher.launch(new Intent(this, MapPickerActivity.class));
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            fechaNacimientoSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, day);
            binding.etFechaNacimiento.setText(
                    String.format("%02d/%02d/%04d", day, month + 1, year));
        }, cal.get(Calendar.YEAR) - 20, cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void validarYRegistrar() {
        String nombre    = binding.etNombre.getText().toString().trim();
        String apellido  = binding.etApellido.getText().toString().trim();
        String genero    = binding.actvGenero.getText().toString().trim();
        String telefono  = binding.etTelefono.getText().toString().trim();
        String email     = binding.etEmail.getText().toString().trim();
        String password  = binding.etPassword.getText().toString().trim();
        String direccion = binding.etDireccion.getText().toString().trim();

        if (nombre.isEmpty()) { binding.tilNombre.setError("Campo requerido"); return; } else binding.tilNombre.setError(null);
        if (apellido.isEmpty()) { binding.tilApellido.setError("Campo requerido"); return; } else binding.tilApellido.setError(null);
        if (telefono.isEmpty()) { binding.tilTelefono.setError("Campo requerido"); return; } else binding.tilTelefono.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido"); return;
        } else binding.tilEmail.setError(null);

        if (password.length() < 6) {
            binding.tilPassword.setError("Mínimo 6 caracteres"); return;
        } else binding.tilPassword.setError(null);

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

                            if (imagenPerfilUri != null) {
                                subirImagenYContinuar(uuid, token, apiAuth,
                                        nombre, apellido, genero, telefono, email, direccion);
                            } else {
                                insertarEnBD(uuid, apiAuth, nombre, apellido, genero, telefono, email, direccion, null);
                            }
                        } else {
                            Toast.makeText(RegistroAdoptanteActivity.this,
                                    "Error: El correo ya está en uso", Toast.LENGTH_LONG).show();
                            resetBoton();
                        }
                    }
                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Toast.makeText(RegistroAdoptanteActivity.this,
                                "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        resetBoton();
                    }
                });
    }

    private void subirImagenYContinuar(String uuid, String token, SupabaseApi apiAuth,
                                       String nombre, String apellido, String genero,
                                       String telefono, String email, String direccion) {
        binding.btnRegistrarse.setText("Subiendo foto...");

        new Thread(() -> {
            byte[] imageBytes = ImageHelper.uriToBytes(this, imagenPerfilUri);
            runOnUiThread(() -> {
                if (imageBytes == null) {
                    insertarEnBD(uuid, apiAuth, nombre, apellido, genero, telefono, email, direccion, null);
                    return;
                }

                StorageApi storageApi = StorageClient.getApi(token);
                RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

                storageApi.uploadFile("avatars", uuid, "profile.jpg", "image/jpeg", body)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> res) {
                                String imageUrl = res.isSuccessful() ? "avatars/" + uuid + "/profile.jpg" : null;
                                insertarEnBD(uuid, apiAuth, nombre, apellido, genero, telefono, email, direccion, imageUrl);
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                insertarEnBD(uuid, apiAuth, nombre, apellido, genero, telefono, email, direccion, null);
                            }
                        });
            });
        }).start();
    }

    private void insertarEnBD(String uuid, SupabaseApi apiAuth,
                              String nombre, String apellido, String genero,
                              String telefono, String email, String direccion, String imageUrl) {
        binding.btnRegistrarse.setText("Guardando datos...");

        Double lat = latitudSeleccionada != 0 ? latitudSeleccionada : null;
        Double lng = longitudSeleccionada != 0 ? longitudSeleccionada : null;
        String dirFinal = (direccion != null && !direccion.isEmpty()) ? direccion : null;

        // 1. Guardar en tabla Usuario (Incluye ubicación)
        UsuarioRequest nuevoUsuario = new UsuarioRequest(uuid, email, telefono, "Adoptante", dirFinal, lat, lng, imageUrl);

        apiAuth.crearUsuarioBase(nuevoUsuario).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {

                    // 2. Guardar en tabla Adoptante (Solo datos de perfil)
                    AdoptanteRequest nuevoAdoptante = new AdoptanteRequest(uuid, nombre, apellido, genero, fechaNacimientoSeleccionada);

                    apiAuth.crearPerfilAdoptante(nuevoAdoptante).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) {
                                Toast.makeText(RegistroAdoptanteActivity.this, "¡Bienvenido! Tu cuenta fue creada.", Toast.LENGTH_LONG).show();
                                irAlLogin();
                            } else {
                                Toast.makeText(RegistroAdoptanteActivity.this, "Error al guardar perfil adoptante", Toast.LENGTH_LONG).show();
                                resetBoton();
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { resetBoton(); }
                    });
                } else {
                    Toast.makeText(RegistroAdoptanteActivity.this, "Error al guardar usuario base", Toast.LENGTH_LONG).show();
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