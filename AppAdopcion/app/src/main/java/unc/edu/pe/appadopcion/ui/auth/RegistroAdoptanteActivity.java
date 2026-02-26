package unc.edu.pe.appadopcion.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import unc.edu.pe.appadopcion.databinding.ActivityRegistroAdoptanteBinding;
import unc.edu.pe.appadopcion.utils.ImageHelper;
import unc.edu.pe.appadopcion.vm.auth.RegistroAdoptanteViewModel;

public class RegistroAdoptanteActivity extends AppCompatActivity {

    private ActivityRegistroAdoptanteBinding binding;
    private RegistroAdoptanteViewModel viewModel;

    private String fechaNacimientoSeleccionada = null;
    private Uri imagenPerfilUri = null;

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

        viewModel = new ViewModelProvider(this).get(RegistroAdoptanteViewModel.class);
        configurarObservadores();

        String[] generos = {"Masculino", "Femenino", "Otro", "Prefiero no decir"};
        binding.actvGenero.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, generos));

        binding.etFechaNacimiento.setFocusable(false);
        binding.etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        binding.etDireccion.setOnClickListener(v -> abrirMapa());
        binding.etDireccion.setFocusable(false);
        binding.ivMapIcon.setOnClickListener(v -> abrirMapa());

        binding.cardFotoPerfil.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegistrarse.setOnClickListener(v -> prepararYRegistrar());
    }

    private void configurarObservadores() {
        viewModel.getLoadingState().observe(this, estado -> {
            if (estado != null) {
                binding.btnRegistrarse.setEnabled(false);
                binding.btnRegistrarse.setText(estado);
            } else {
                binding.btnRegistrarse.setEnabled(true);
                binding.btnRegistrarse.setText("✓ Registrarse");
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getRegistroSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "¡Bienvenido! Tu cuenta fue creada.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    private void abrirMapa() {
        mapLauncher.launch(new Intent(this, MapPickerActivity.class));
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            fechaNacimientoSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, day);
            binding.etFechaNacimiento.setText(String.format("%02d/%02d/%04d", day, month + 1, year));
        }, cal.get(Calendar.YEAR) - 20, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void prepararYRegistrar() {
        String nombre    = binding.etNombre.getText().toString().trim();
        String apellido  = binding.etApellido.getText().toString().trim();
        String genero    = binding.actvGenero.getText().toString().trim();
        String telefono  = binding.etTelefono.getText().toString().trim();
        String email     = binding.etEmail.getText().toString().trim();
        String password  = binding.etPassword.getText().toString().trim();
        String direccion = binding.etDireccion.getText().toString().trim();

        // Validaciones locales
        if (nombre.isEmpty()) { binding.tilNombre.setError("Campo requerido"); return; } else binding.tilNombre.setError(null);
        if (apellido.isEmpty()) { binding.tilApellido.setError("Campo requerido"); return; } else binding.tilApellido.setError(null);
        if (telefono.isEmpty()) { binding.tilTelefono.setError("Campo requerido"); return; } else binding.tilTelefono.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido"); return;
        } else binding.tilEmail.setError(null);

        if (password.length() < 6) {
            binding.tilPassword.setError("Mínimo 6 caracteres"); return;
        } else binding.tilPassword.setError(null);

        Double latFinal = latitudSeleccionada != 0 ? latitudSeleccionada : null;
        Double lngFinal = longitudSeleccionada != 0 ? longitudSeleccionada : null;
        String dirFinal = (direccion.isEmpty()) ? null : direccion;

        // Convertir Uri a byte[] y delegar al ViewModel
        if (imagenPerfilUri != null) {
            new Thread(() -> {
                byte[] imageBytes = ImageHelper.uriToBytes(this, imagenPerfilUri);
                runOnUiThread(() -> viewModel.registrarAdoptante(
                        email, password, nombre, apellido, genero, fechaNacimientoSeleccionada,
                        telefono, dirFinal, latFinal, lngFinal, imageBytes
                ));
            }).start();
        } else {
            viewModel.registrarAdoptante(
                    email, password, nombre, apellido, genero, fechaNacimientoSeleccionada,
                    telefono, dirFinal, latFinal, lngFinal, null
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}