package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import unc.edu.pe.appadopcion.databinding.ActivityRegistroRefugioBinding;
import unc.edu.pe.appadopcion.utils.ImageHelper;
import unc.edu.pe.appadopcion.vm.auth.RegistroRefugioViewModel;

public class RegistroRefugioActivity extends AppCompatActivity {

    private ActivityRegistroRefugioBinding binding;
    private RegistroRefugioViewModel viewModel;

    private Uri imagenPortadaUri = null;
    private double latitudSeleccionada = 0;
    private double longitudSeleccionada = 0;
    private String direccionFormateada = null;

    // Launcher de foto
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenPortadaUri = uri;
                    binding.ivFotoPerfil.setImageURI(uri); // Asumiendo que reciclas el layout de diseño
                    binding.tvPerfilHint.setText("Portada seleccionada ✓");
                }
            });

    // Launcher de mapa
    private final ActivityResultLauncher<Intent> mapLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    latitudSeleccionada = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LATITUD, 0);
                    longitudSeleccionada = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LONGITUD, 0);
                    direccionFormateada = result.getData().getStringExtra(MapPickerActivity.EXTRA_DIRECCION);

                    binding.etDireccion.setText(direccionFormateada);
                    binding.tilDireccion.setError(null);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroRefugioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegistroRefugioViewModel.class);
        configurarObservadores();

        // Eventos de botones
        binding.etDireccion.setOnClickListener(v -> mapLauncher.launch(new Intent(this, MapPickerActivity.class)));
        binding.etDireccion.setFocusable(false);
        binding.ivMapIcon.setOnClickListener(v -> mapLauncher.launch(new Intent(this, MapPickerActivity.class)));

        binding.cardFotoPerfil.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegistrarse.setOnClickListener(v -> prepararYRegistrar());
    }

    private void configurarObservadores() {
        viewModel.getLoadingState().observe(this, estado -> {
            if (estado != null) {
                binding.btnRegistrarse.setEnabled(false);
                binding.btnRegistrarse.setText(estado); // Mostrará "Autenticando...", "Subiendo...", etc.
            } else {
                binding.btnRegistrarse.setEnabled(true);
                binding.btnRegistrarse.setText("Registrar Refugio");
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getRegistroSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "¡Refugio registrado con éxito!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    private void prepararYRegistrar() {
        String nombre = binding.etNombreRefugio.getText().toString().trim();
        String descripcion = binding.etDescripcion.getText().toString().trim(); // Asumiendo que lo agregaste al XML
        String telefono = binding.etTelefono.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String direccion = binding.etDireccion.getText().toString().trim();

        // Validaciones locales
        if (nombre.isEmpty()) { binding.tilNombreRefugio.setError("Campo requerido"); return; } else binding.tilNombreRefugio.setError(null);
        if (telefono.isEmpty()) { binding.tilTelefono.setError("Campo requerido"); return; } else binding.tilTelefono.setError(null);
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.tilEmail.setError("Correo inválido"); return; } else binding.tilEmail.setError(null);
        if (password.length() < 6) { binding.tilPassword.setError("Mínimo 6 caracteres"); return; } else binding.tilPassword.setError(null);

        Double latFinal = latitudSeleccionada != 0 ? latitudSeleccionada : null;
        Double lngFinal = longitudSeleccionada != 0 ? longitudSeleccionada : null;

        // Transformamos la Uri en byte[] en un hilo secundario para no congelar la UI,
        // y luego delegamos toda la responsabilidad al ViewModel.
        if (imagenPortadaUri != null) {
            new Thread(() -> {
                byte[] imageBytes = ImageHelper.uriToBytes(this, imagenPortadaUri);
                runOnUiThread(() -> viewModel.registrarRefugio(
                        email, password, nombre, descripcion, telefono,
                        direccion, latFinal, lngFinal, imageBytes
                ));
            }).start();
        } else {
            viewModel.registrarRefugio(
                    email, password, nombre, descripcion, telefono,
                    direccion, latFinal, lngFinal, null
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}