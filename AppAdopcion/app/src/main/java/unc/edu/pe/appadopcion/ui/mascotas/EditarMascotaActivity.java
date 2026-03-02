package unc.edu.pe.appadopcion.ui.mascotas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.EspecieResponse;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.model.RazaResponse;
import unc.edu.pe.appadopcion.data.model.VacunaResponse;
import unc.edu.pe.appadopcion.databinding.ActivityEditarMascotaBinding;
import unc.edu.pe.appadopcion.databinding.DialogValidacionBinding;
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.mascotas.EditarMascotaViewModel;

public class EditarMascotaActivity extends AppCompatActivity {

    public static final String EXTRA_MASCOTA_EDITADA = "mascota_editada";

    private ActivityEditarMascotaBinding binding;
    private MascotaResponse mascotaActual;
    private EditarMascotaViewModel viewModel;
    private SessionManager session;

    private List<EspecieResponse> listaEspeciesActual = new ArrayList<>();
    private List<RazaResponse> listaRazasActual = new ArrayList<>();
    private List<VacunaResponse> listaVacunasActual = new ArrayList<>();

    private Uri uriNuevaPortada = null;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> galeriaFotosLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityEditarMascotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(EditarMascotaViewModel.class);
        viewModel.init(session.getToken());

        if (getIntent() != null && getIntent().hasExtra("mascota")) {
            mascotaActual = (MascotaResponse) getIntent().getSerializableExtra("mascota");
            poblarDatosUI(mascotaActual);
            viewModel.cargarDatosMascota(mascotaActual.idMascota);
        } else {
            Toast.makeText(this, "Error al cargar la información", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarLaunchers();
        configurarEstadoInicial();
        configurarObservadores();
        configurarListeners();
    }

    private void configurarLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uriNuevaPortada = result.getData().getData();
                        binding.ivPortada.setImageURI(uriNuevaPortada);
                    }
                }
        );

        galeriaFotosLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        viewModel.agregarFotoGaleria(result.getData().getData());
                    }
                }
        );
    }

    private void configurarEstadoInicial() {
        viewModel.cargarEspecies();
        viewModel.cargarRazasPorEspecie(mascotaActual.idEspecie);
        viewModel.cargarVacunasPorEspecie(mascotaActual.idEspecie);
    }

    private void configurarObservadores() {
        viewModel.getIsLoading().observe(this, cargando -> {
            binding.btnGuardarCambios.setEnabled(!cargando);
            binding.progressGuardando.setVisibility(cargando ? View.VISIBLE : View.GONE);
            binding.tvGuardando.setVisibility(cargando ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getUpdateSuccess().observe(this, exito -> {
            if (exito != null && exito) {
                // Notificar éxito y devolver resultado
                setResult(RESULT_OK);
                mostrarDialog("¡Cambios guardados!", "La información de la mascota fue actualizada.", true);
            }
        });

        viewModel.getEspecies().observe(this, especies -> {
            if (especies == null) return;
            listaEspeciesActual = especies;
            List<String> nombres = new ArrayList<>();
            for (EspecieResponse e : especies) nombres.add(e.nombre);
            binding.actvEspecie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombres));
        });

        viewModel.getRazas().observe(this, razas -> {
            if (razas == null) return;
            listaRazasActual = razas;
            List<String> nombres = new ArrayList<>();
            for (RazaResponse r : razas) nombres.add(r.nombre);
            binding.actvRaza.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nombres));
        });

        viewModel.getVacunasDisponibles().observe(this, vacunas -> {
            if (vacunas == null) return;
            listaVacunasActual = vacunas;
        });

        viewModel.getDeleteSuccess().observe(this, exito -> {
            if (exito != null && exito) {
                Toast.makeText(this, "Mascota eliminada correctamente", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK); // Avisamos que hubo un cambio drástico
                finish();
            }
        });

        viewModel.getListaGaleria().observe(this, this::renderizarGaleria);
        viewModel.getIntervenciones().observe(this, this::renderizarIntervenciones);
    }

    private void renderizarGaleria(List<Uri> listaUris) {
        binding.llGaleriaContenedor.removeAllViews();
        for (int i = 0; i < listaUris.size(); i++) {
            final int index = i;
            Uri uri = listaUris.get(i);
            View item = getLayoutInflater().inflate(R.layout.item_foto_galeria, binding.llGaleriaContenedor, false);
            ImageView iv = item.findViewById(R.id.ivFotoGaleria);
            if (uri.toString().startsWith("http")) {
                ImageLoader.cargarPublica(this, uri.toString(), iv, R.drawable.ic_launcher_background);
            } else {
                iv.setImageURI(uri);
            }
            item.findViewById(R.id.tvEliminarFoto).setOnClickListener(v -> viewModel.eliminarFotoGaleria(index));
            binding.llGaleriaContenedor.addView(item);
        }
    }

    private void renderizarIntervenciones(List<IntervencionLocal> lista) {
        binding.llIntervencionesContenedor.removeAllViews();
        if (lista == null || lista.isEmpty()) {
            binding.tvSinIntervenciones.setVisibility(View.VISIBLE);
            return;
        }
        binding.tvSinIntervenciones.setVisibility(View.GONE);
        for (int i = 0; i < lista.size(); i++) {
            final int index = i;
            IntervencionLocal intervencion = lista.get(i);
            View item = getLayoutInflater().inflate(R.layout.item_intervencion, binding.llIntervencionesContenedor, false);
            ((TextView) item.findViewById(R.id.tvTituloIntervencion)).setText(intervencion.titulo);
            ((TextView) item.findViewById(R.id.tvDescripcionIntervencion)).setText(intervencion.descripcion);
            ((TextView) item.findViewById(R.id.tvFechaIntervencion)).setText("📅 " + intervencion.fecha);
            item.setOnClickListener(v -> {
                EditarIntervencionBottomSheet sheet = EditarIntervencionBottomSheet.newInstance(intervencion, index);
                sheet.setOnIntervencionEditadaListener(viewModel::editarIntervencion);
                sheet.setOnIntervencionEliminadaListener(viewModel::eliminarIntervencion);
                sheet.show(getSupportFragmentManager(), EditarIntervencionBottomSheet.TAG);
            });
            binding.llIntervencionesContenedor.addView(item);
        }
    }

    private void configurarListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.flImagenPortada.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        binding.btnAnadirImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galeriaFotosLauncher.launch(intent);
        });

        binding.actvEspecie.setOnItemClickListener((parent, view, position, id) -> {
            EspecieResponse especie = listaEspeciesActual.get(position);
            binding.actvRaza.setText("", false);
            viewModel.cargarRazasPorEspecie(especie.idEspecie);
            viewModel.cargarVacunasPorEspecie(especie.idEspecie);
        });

        binding.btnAnadirVacunas.setOnClickListener(v -> {
            List<VacunaResponse> seleccionadas = viewModel.getVacunasSeleccionadas().getValue();
            VacunasBottomSheet sheet = VacunasBottomSheet.newInstance(
                    new ArrayList<>(listaVacunasActual),
                    seleccionadas != null ? new ArrayList<>(seleccionadas) : new ArrayList<>()
            );
            sheet.setOnVacunasConfirmadasListener(viewModel::setVacunasSeleccionadas);
            sheet.show(getSupportFragmentManager(), VacunasBottomSheet.TAG);
        });

        binding.btnAnadirIntervencion.setOnClickListener(v -> {
            AgregarIntervencionBottomSheet sheet = AgregarIntervencionBottomSheet.newInstance();
            sheet.setOnIntervencionGuardadaListener(viewModel::agregarIntervencion);
            sheet.show(getSupportFragmentManager(), AgregarIntervencionBottomSheet.TAG);
        });

        binding.btnGuardarCambios.setOnClickListener(v -> validarYGuardar());
        binding.btnEliminarMascota.setOnClickListener(v -> mostrarDialogoEliminacion());
    }

    private void validarYGuardar() {
        String nuevoNombre = binding.etNombre.getText().toString().trim();
        String edadAStr = binding.etEdadAnios.getText().toString().trim();
        String edadMStr = binding.etEdadMeses.getText().toString().trim();
        String nuevoTemperamento = binding.etTemperamento.getText().toString().trim();
        String nuevaHistoria = binding.etHistoria.getText().toString().trim();
        String nuevoGenero = binding.actvGenero.getText().toString().trim();
        String razaNombre = binding.actvRaza.getText().toString().trim();

        if (nuevoNombre.isEmpty() || edadAStr.isEmpty() || edadMStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa los campos básicos.", Toast.LENGTH_SHORT).show();
            return;
        }

        int idRaza = mascotaActual.idRaza;
        for (RazaResponse r : listaRazasActual) {
            if (r.nombre.equals(razaNombre)) {
                idRaza = r.idRaza;
                break;
            }
        }

        viewModel.guardarCambios(
                this,
                session.getToken(),
                session.getUuid(),
                mascotaActual.idMascota,
                mascotaActual.idRefugio,
                idRaza,
                nuevoNombre,
                Integer.parseInt(edadAStr),
                Integer.parseInt(edadMStr),
                nuevoGenero,
                nuevoTemperamento,
                nuevaHistoria,
                mascotaActual.urlPortada,
                uriNuevaPortada
        );
    }

    private void poblarDatosUI(MascotaResponse m) {
        binding.etNombre.setText(m.nombre != null ? m.nombre : "");
        binding.etEdadAnios.setText(String.valueOf(m.edadAnios));
        binding.etEdadMeses.setText(String.valueOf(m.edadMeses));
        binding.etTemperamento.setText(m.temperamento != null ? m.temperamento : "");
        binding.etHistoria.setText(m.historia != null ? m.historia : "");

        binding.actvEspecie.setText(m.nombreEspecie != null ? m.nombreEspecie : "", false);
        binding.actvRaza.setText(m.nombreRaza != null ? m.nombreRaza : "", false);
        binding.actvGenero.setText(m.genero != null ? m.genero : "", false);

        String[] generos = {"Macho", "Hembra"};
        binding.actvGenero.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, generos));

        if (m.urlPortada != null && !m.urlPortada.isEmpty()) {
            ImageLoader.cargarPublica(this, m.urlPortada, binding.ivPortada, R.drawable.ic_launcher_background);
        }
    }

    private void mostrarDialog(String titulo, String mensaje, boolean esExito) {
        DialogValidacionBinding dialogBinding = DialogValidacionBinding.inflate(getLayoutInflater());
        dialogBinding.tvDialogTitulo.setText(titulo);
        dialogBinding.tvDialogMensaje.setText(mensaje);
        if (esExito) {
            dialogBinding.ivDialogIcon.setImageResource(R.drawable.outline_add_task_24);
            dialogBinding.ivDialogIcon.setColorFilter(getColor(android.R.color.holo_green_dark));
        }
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setBackground(new android.graphics.drawable.GradientDrawable() {{
                    setColor(0xFFFFFFFF);
                    setCornerRadius(24f);
                }})
                .create();
        dialogBinding.btnDialogAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            if (esExito) finish();
        });
        dialog.show();
    }

    private void mostrarDialogoEliminacion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Mascota")
                .setMessage("¿Estás seguro de que deseas eliminar a " + mascotaActual.nombre + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.eliminarMascotaCompleta(session.getToken(), mascotaActual.idMascota);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}