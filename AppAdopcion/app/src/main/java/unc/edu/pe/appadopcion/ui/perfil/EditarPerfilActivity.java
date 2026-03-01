package unc.edu.pe.appadopcion.ui.perfil;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.databinding.ActivityEditarPerfilBinding;
import unc.edu.pe.appadopcion.ui.auth.MapPickerActivity;
import unc.edu.pe.appadopcion.utils.ImageHelper;
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.perfil.EditarPerfilViewModel;

public class EditarPerfilActivity extends AppCompatActivity {

    private ActivityEditarPerfilBinding binding;
    private SessionManager session;
    private EditarPerfilViewModel viewModel;

    // Variables locales para selecciones nuevas del usuario
    private Uri nuevaFotoPerfil = null;
    private Uri nuevaFotoPortada = null;
    private double latitudLocal = 0;
    private double longitudLocal = 0;
    private String fechaNacBD = null; // En formato YYYY-MM-DD

    // Variables CRÍTICAS para mantener los datos antiguos si el usuario no los cambia
    private String correoActual = "";
    private String urlPerfilAntigua = null;
    private String urlPortadaAntigua = null;

    private final ActivityResultLauncher<String> perfilLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) { nuevaFotoPerfil = uri; binding.ivFotoPerfil.setImageURI(uri); }
    });

    private final ActivityResultLauncher<String> portadaLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) { nuevaFotoPortada = uri; binding.ivPortada.setImageURI(uri); }
    });

    private final ActivityResultLauncher<Intent> mapLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            latitudLocal = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LATITUD, 0);
            longitudLocal = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LONGITUD, 0);
            binding.etDireccion.setText(result.getData().getStringExtra(MapPickerActivity.EXTRA_DIRECCION));
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(EditarPerfilViewModel.class);

        configurarVistaSegunRol();
        observarViewModel();

        // ¡Solicitamos los datos al abrir la pantalla!
        viewModel.cargarDatosIniciales(session.getUuid(), session.getToken(), session.esRefugio());

        // Listeners
        binding.btnBack.setOnClickListener(v -> finish());
        binding.cardFotoPerfil.setOnClickListener(v -> perfilLauncher.launch("image/*"));
        binding.cardPortada.setOnClickListener(v -> portadaLauncher.launch("image/*"));
        binding.etDireccion.setOnClickListener(v -> mapLauncher.launch(new Intent(this, MapPickerActivity.class)));
        binding.btnGuardar.setOnClickListener(v -> recopilarYGuardar());
    }

    private void observarViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnGuardar.setEnabled(!isLoading);
            binding.btnGuardar.setText(isLoading ? "Guardando cambios..." : "Guardar Cambios");
        });

        viewModel.getMensajeExito().observe(this, exito -> {
            if (exito != null) {
                Toast.makeText(this, exito, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // -------------------------------------------------------------
        // OBSERVADORES DE CARGA INICIAL DE DATOS
        // -------------------------------------------------------------

        viewModel.getAdoptanteActual().observe(this, adoptante -> {
            if (adoptante != null) {
                // Guardar variables intocables
                correoActual = adoptante.correo != null ? adoptante.correo : "";
                urlPerfilAntigua = adoptante.fotoPerfil;
                fechaNacBD = adoptante.fechaNacimiento;
                latitudLocal = adoptante.latitud != null ? adoptante.latitud : 0;
                longitudLocal = adoptante.longitud != null ? adoptante.longitud : 0;

                // Rellenar UI
                binding.etNombre.setText(adoptante.nombre);
                binding.etApellido.setText(adoptante.apellido);
                binding.actvGenero.setText(adoptante.genero, false);
                binding.etTelefono.setText(adoptante.telefono);
                binding.etDireccion.setText(adoptante.direccion);

                // Formatear fecha para UI (De YYYY-MM-DD a DD/MM/YYYY) si existe
                if (fechaNacBD != null && fechaNacBD.length() >= 10) {
                    String[] partes = fechaNacBD.split("-");
                    if(partes.length == 3) {
                        binding.etFechaNacimiento.setText(partes[2] + "/" + partes[1] + "/" + partes[0]);
                    }
                }

                // Cargar imagen de perfil
                if (urlPerfilAntigua != null) {
                    ImageLoader.cargarAvatarCircular(this, session.getToken(), session.getUuid(), binding.ivFotoPerfil, R.drawable.ic_pets);
                }
            }
        });

        viewModel.getRefugioActual().observe(this, refugio -> {
            if (refugio != null) {
                // Guardar variables intocables
                correoActual = refugio.correo != null ? refugio.correo : "";
                urlPerfilAntigua = refugio.fotoPerfil;
                urlPortadaAntigua = refugio.urlPortada;
                latitudLocal = refugio.latitud != null ? refugio.latitud : 0;
                longitudLocal = refugio.longitud != null ? refugio.longitud : 0;

                // Rellenar UI
                binding.etNombre.setText(refugio.nombre);
                binding.etDescripcion.setText(refugio.descripcion);
                binding.etTelefono.setText(refugio.telefono);
                binding.etDireccion.setText(refugio.direccion);

                // Cargar imágenes
                if (urlPerfilAntigua != null) {
                    ImageLoader.cargarAvatarCircular(this, session.getToken(), session.getUuid(), binding.ivFotoPerfil, R.drawable.ic_pets);
                }
                if (urlPortadaAntigua != null) {
                    ImageLoader.cargarPublica(this, urlPortadaAntigua, binding.ivPortada, R.drawable.bg_registro_header);
                }
            }
        });
    }

    private void recopilarYGuardar() {
        byte[] perfilBytes = nuevaFotoPerfil != null ? ImageHelper.uriToBytes(this, nuevaFotoPerfil) : null;
        byte[] portadaBytes = nuevaFotoPortada != null ? ImageHelper.uriToBytes(this, nuevaFotoPortada) : null;

        String telefono = binding.etTelefono.getText().toString().trim();
        String direccion = binding.etDireccion.getText().toString().trim();
        String nombre = binding.etNombre.getText().toString().trim();

        String apellido = session.esAdoptante() ? binding.etApellido.getText().toString().trim() : null;
        String genero = session.esAdoptante() ? binding.actvGenero.getText().toString().trim() : null;
        String descripcion = session.esRefugio() ? binding.etDescripcion.getText().toString().trim() : null;

        // Mandar todo al ViewModel
        viewModel.guardarCambios(
                session.getUuid(), session.getToken(), session.esRefugio(),
                perfilBytes, portadaBytes, correoActual, telefono, direccion,
                latitudLocal != 0 ? latitudLocal : null, longitudLocal != 0 ? longitudLocal : null,
                nombre, apellido, genero, fechaNacBD, descripcion,
                urlPerfilAntigua, urlPortadaAntigua
        );
    }

    private void configurarVistaSegunRol() {
        if (session.esRefugio()) {
            binding.tilApellido.setVisibility(View.GONE);
            binding.llDatosAdoptante.setVisibility(View.GONE);
            binding.tilDescripcion.setVisibility(View.VISIBLE);
            binding.cardPortada.setVisibility(View.VISIBLE);
            binding.tilNombre.setHint("Nombre del Refugio");
        } else {
            binding.tilApellido.setVisibility(View.VISIBLE);
            binding.llDatosAdoptante.setVisibility(View.VISIBLE);
            binding.tilDescripcion.setVisibility(View.GONE);
            binding.cardPortada.setVisibility(View.GONE);
            binding.tilNombre.setHint("Nombre");

            String[] generos = {"Masculino", "Femenino", "Otro", "Prefiero no decir"};
            binding.actvGenero.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, generos));
            binding.etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
            binding.etFechaNacimiento.setFocusable(false);
        }
    }

    private void mostrarDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            fechaNacBD = String.format("%04d-%02d-%02d", year, month + 1, day); // Formato para Base de Datos
            binding.etFechaNacimiento.setText(String.format("%02d/%02d/%04d", day, month + 1, year)); // Formato Visual
        }, cal.get(Calendar.YEAR) - 20, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}