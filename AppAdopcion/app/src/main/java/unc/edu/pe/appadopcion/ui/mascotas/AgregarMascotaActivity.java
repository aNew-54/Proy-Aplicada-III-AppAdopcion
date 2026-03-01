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
import androidx.activity.OnBackPressedCallback;
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
import unc.edu.pe.appadopcion.data.model.RazaResponse;
import unc.edu.pe.appadopcion.data.model.VacunaResponse;
import unc.edu.pe.appadopcion.databinding.ActivityAgregarMascotaBinding;
import unc.edu.pe.appadopcion.databinding.DialogConfirmacionRetrocederBinding;
import unc.edu.pe.appadopcion.databinding.DialogValidacionBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.vm.mascotas.AgregarMascotaViewModel;

public class AgregarMascotaActivity extends AppCompatActivity {

    // ════════════════════════════════════════════════════════
    // CAMPOS — Binding, sesión y estado local de la pantalla
    // ════════════════════════════════════════════════════════

    private ActivityAgregarMascotaBinding binding;
    private SessionManager sessionManager;
    private AgregarMascotaViewModel viewModel;

    /** Listas de catálogos cargados para resolución de IDs al guardar */
    private List<EspecieResponse> listaEspeciesActual = new ArrayList<>();
    private List<RazaResponse> listaRazasActual = new ArrayList<>();
    private List<VacunaResponse> listaVacunasActual = new ArrayList<>();

    /** Uri de la foto de portada seleccionada por el usuario */
    private Uri imagenUri;

    /** Launchers para el selector de imágenes del sistema */
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> galeriaFotosLauncher;

    private static final String BUCKET = "mascotas";

    // ════════════════════════════════════════════════════════
    // CICLO DE VIDA
    // ════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAgregarMascotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(AgregarMascotaViewModel.class);
        sessionManager = new SessionManager(this);

        configurarEstadoInicial();
        configurarLaunchers();
        configurarObservadores();
        configurarListeners();
    }

    // ════════════════════════════════════════════════════════
    // CONFIGURACIÓN INICIAL
    // ════════════════════════════════════════════════════════

    /** Desactiva campos que dependen de la especie y carga catálogos iniciales */
    private void configurarEstadoInicial() {
        binding.actvRaza.setEnabled(false);
        binding.tilRaza.setEnabled(false);
        binding.btnAnadirVacunas.setEnabled(false);

        String token = sessionManager.getToken();
        viewModel.cargarGeneros();
        viewModel.cargarEspecies(token);
    }

    // ════════════════════════════════════════════════════════
    // LAUNCHERS — Selector de imágenes del sistema
    // ════════════════════════════════════════════════════════

    /**
     * Registra los launchers para abrir la galería del sistema.
     * Se deben registrar antes de onStart(), por eso van en onCreate().
     */
    private void configurarLaunchers() {
        // Launcher para foto de portada — muestra la imagen y oculta el placeholder
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        binding.ivPortada.setImageURI(imagenUri);
                        binding.ivPortada.setVisibility(View.VISIBLE);
                        binding.llPlaceholderPortada.setVisibility(View.GONE);
                    }
                }
        );

        // Launcher para galería adicional — delega al ViewModel para limitar a 5 fotos
        galeriaFotosLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        viewModel.agregarFotoGaleria(result.getData().getData());
                    }
                }
        );
    }

    // ════════════════════════════════════════════════════════
    // OBSERVADORES — Reacción a cambios del ViewModel
    // ════════════════════════════════════════════════════════

    private void configurarObservadores() {
        observarEspecies();
        observarGeneros();
        observarRazas();
        observarVacunas();
        observarGaleria();
        observarIntervenciones();
        observarGuardado();
    }

    /** Puebla el ComboBox de especies y guarda la lista completa para resolución de IDs */
    private void observarEspecies() {
        viewModel.getEspecies().observe(this, especies -> {
            if (especies == null) return;
            listaEspeciesActual = especies;

            List<String> nombres = new ArrayList<>();
            for (EspecieResponse e : especies) nombres.add(e.nombre);

            binding.actvEspecie.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, nombres));
        });
    }

    /** Puebla el ComboBox de géneros (Macho / Hembra) */
    private void observarGeneros() {
        viewModel.getGeneros().observe(this, listaGeneros -> {
            if (listaGeneros == null) return;
            binding.actvGenero.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, listaGeneros));
        });
    }

    /**
     * Puebla el ComboBox de razas al cambiar la especie.
     * Guarda la lista completa de RazaResponse para resolver el idRaza al guardar.
     */
    private void observarRazas() {
        viewModel.getRazas().observe(this, lista -> {
            if (lista == null) return;
            listaRazasActual = lista;

            List<String> nombres = new ArrayList<>();
            for (RazaResponse r : lista) nombres.add(r.nombre);

            binding.actvRaza.setAdapter(new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, nombres));
            binding.actvRaza.showDropDown();
        });
    }

    /** Guarda la lista de vacunas disponibles para pasarla al BottomSheet */
    private void observarVacunas() {
        viewModel.getVacunas().observe(this, vacunas -> {
            if (vacunas == null) return;
            listaVacunasActual = vacunas;
        });
    }

    /** Renderiza las fotos de galería dinámicamente con botón de eliminar en cada item */
    private void observarGaleria() {
        viewModel.getListaGaleria().observe(this, listaUris -> {
            binding.llGaleriaContenedor.removeAllViews();

            for (int i = 0; i < listaUris.size(); i++) {
                final int index = i;
                Uri uri = listaUris.get(i);

                View item = getLayoutInflater().inflate(
                        R.layout.item_foto_galeria, binding.llGaleriaContenedor, false);

                ((ImageView) item.findViewById(R.id.ivFotoGaleria)).setImageURI(uri);
                item.findViewById(R.id.tvEliminarFoto)
                        .setOnClickListener(v -> viewModel.eliminarFotoGaleria(index));

                binding.llGaleriaContenedor.addView(item);
            }
        });
    }

    /**
     * Renderiza las intervenciones médicas dinámicamente.
     * Cada item abre un BottomSheet de edición al tocarlo.
     */
    private void observarIntervenciones() {
        viewModel.getIntervenciones().observe(this, lista -> {
            binding.llIntervencionesContenedor.removeAllViews();

            if (lista == null || lista.isEmpty()) {
                binding.tvSinIntervenciones.setVisibility(View.VISIBLE);
                return;
            }

            binding.tvSinIntervenciones.setVisibility(View.GONE);

            for (int i = 0; i < lista.size(); i++) {
                final int index = i;
                IntervencionLocal intervencion = lista.get(i);

                View item = getLayoutInflater().inflate(
                        R.layout.item_intervencion, binding.llIntervencionesContenedor, false);

                ((TextView) item.findViewById(R.id.tvTituloIntervencion)).setText(intervencion.titulo);
                ((TextView) item.findViewById(R.id.tvDescripcionIntervencion)).setText(intervencion.descripcion);
                ((TextView) item.findViewById(R.id.tvFechaIntervencion)).setText("📅 " + intervencion.fecha);

                item.setOnClickListener(v -> {
                    EditarIntervencionBottomSheet sheet =
                            EditarIntervencionBottomSheet.newInstance(intervencion, index);
                    sheet.setOnIntervencionEditadaListener(viewModel::editarIntervencion);
                    sheet.setOnIntervencionEliminadaListener(viewModel::eliminarIntervencion);
                    sheet.show(getSupportFragmentManager(), EditarIntervencionBottomSheet.TAG);
                });

                binding.llIntervencionesContenedor.addView(item);
            }
        });
    }

    /**
     * Observa el estado del flujo de guardado:
     * - Bloquea el botón y muestra el ProgressBar mientras guarda
     * - Muestra el dialog de éxito al terminar
     * - Muestra errores como Toast
     */
    private void observarGuardado() {
        viewModel.getGuardando().observe(this, guardando -> {
            binding.btnAgregarMascota.setEnabled(!guardando);
            binding.progressGuardando.setVisibility(guardando ? View.VISIBLE : View.GONE);
            binding.tvGuardando.setVisibility(guardando ? View.VISIBLE : View.GONE);
        });

        viewModel.getExito().observe(this, exito -> {
            if (exito != null && exito)
                mostrarDialog("¡Mascota registrada!", "La mascota fue agregada exitosamente.", true);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }

    // ════════════════════════════════════════════════════════
    // LISTENERS — Interacciones del usuario
    // ════════════════════════════════════════════════════════

    private void configurarListeners() {
        String token = sessionManager.getToken();

        // Al seleccionar especie → habilita raza, habilita vacunas y carga ambos catálogos
        binding.actvEspecie.setOnItemClickListener((parent, view, position, id) -> {
            EspecieResponse especie = listaEspeciesActual.get(position);
            binding.tilRaza.setEnabled(true);
            binding.actvRaza.setEnabled(true);
            binding.actvRaza.setText("", false);
            binding.btnAnadirVacunas.setEnabled(true);
            viewModel.cargarRazasPorEspecie(especie.idEspecie, token);
            viewModel.cargarVacunasPorEspecie(especie.idEspecie, token);
        });

        // Abre el BottomSheet de vacunas pasando las disponibles y las ya seleccionadas
        binding.btnAnadirVacunas.setOnClickListener(v -> {
            List<VacunaResponse> seleccionadas = viewModel.getVacunasSeleccionadas().getValue();
            VacunasBottomSheet sheet = VacunasBottomSheet.newInstance(
                    new ArrayList<>(listaVacunasActual),
                    seleccionadas != null ? new ArrayList<>(seleccionadas) : new ArrayList<>()
            );
            sheet.setOnVacunasConfirmadasListener(viewModel::setVacunasSeleccionadas);
            sheet.show(getSupportFragmentManager(), VacunasBottomSheet.TAG);
        });

        // Abre el BottomSheet para agregar una nueva intervención médica
        binding.btnAnadirIntervencion.setOnClickListener(v -> {
            AgregarIntervencionBottomSheet sheet = AgregarIntervencionBottomSheet.newInstance();
            sheet.setOnIntervencionGuardadaListener(viewModel::agregarIntervencion);
            sheet.show(getSupportFragmentManager(), AgregarIntervencionBottomSheet.TAG);
        });

        // Abre el selector de imagen del sistema para la foto de portada
        binding.flImagenPortada.setOnClickListener(v -> abrirGaleria(galleryLauncher));

        // Abre el selector de imagen para agregar a la galería adicional
        binding.btnAnadirImagen.setOnClickListener(v -> abrirGaleria(galeriaFotosLauncher));

        // Botón atrás en toolbar y físico → muestra dialog de confirmación
        binding.btnBack.setOnClickListener(v -> mostrarDialogConfirmacion());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { mostrarDialogConfirmacion(); }
        });

        // Botón guardar — valida y lanza el flujo de guardado
        binding.btnAgregarMascota.setOnClickListener(v -> validarYGuardar());
    }

    // ════════════════════════════════════════════════════════
    // VALIDACIÓN Y GUARDADO
    // ════════════════════════════════════════════════════════

    /**
     * Valida todos los campos obligatorios antes de lanzar el guardado.
     * Muestra un dialog descriptivo en cada error para guiar al usuario.
     * Reglas:
     *   - Nombre, temperamento e historia: no vacíos
     *   - Edad: al menos un valor > 0, meses máximo 11
     *   - Especie, raza y género: deben haber sido seleccionados (no placeholder)
     *   - Foto de portada: obligatoria
     *   - Vacunas: al menos una seleccionada
     */
    private void validarYGuardar() {
        String nombre = texto(binding.etNombre.getText());
        String edadAniosStr = texto(binding.etEdadAnios.getText());
        String edadMesesStr = texto(binding.etEdadMeses.getText());
        String especie = binding.actvEspecie.getText().toString().trim();
        String raza = binding.actvRaza.getText().toString().trim();
        String genero = binding.actvGenero.getText().toString().trim();
        String temperamento = texto(binding.etTemperamento.getText());
        String historia = texto(binding.etHistoria.getText());

        if (nombre.isEmpty()) {
            binding.tilNombre.setError("Campo requerido");
            mostrarDialog("Datos incompletos", "El nombre de la mascota es obligatorio.", false);
            return;
        }
        binding.tilNombre.setError(null);

        int anios = edadAniosStr.isEmpty() ? 0 : Integer.parseInt(edadAniosStr);
        int meses = edadMesesStr.isEmpty() ? 0 : Integer.parseInt(edadMesesStr);

        if (anios == 0 && meses == 0) {
            mostrarDialog("Datos incompletos", "Debes ingresar al menos un valor mayor a 0 (años o meses).", false);
            return;
        }
        if (meses > 11) {
            mostrarDialog("Edad inválida", "Los meses no pueden ser mayores a 11. Si tiene 12, aumenta un año.", false);
            return;
        }

        if (especie.isEmpty() || especie.equals("Seleccione Especie")) {
            mostrarDialog("Datos incompletos", "Selecciona la especie de la mascota.", false);
            return;
        }
        if (raza.isEmpty() || raza.equals("Seleccione Raza")) {
            mostrarDialog("Datos incompletos", "Selecciona la raza de la mascota.", false);
            return;
        }
        if (genero.isEmpty() || genero.equals("Seleccione el género")) {
            mostrarDialog("Datos incompletos", "Selecciona el género de la mascota.", false);
            return;
        }

        if (temperamento.isEmpty()) {
            binding.tilTemperamento.setError("Campo requerido");
            mostrarDialog("Datos incompletos", "El temperamento de la mascota es obligatorio.", false);
            return;
        }
        binding.tilTemperamento.setError(null);

        if (historia.isEmpty()) {
            binding.tilHistoria.setError("Campo requerido");
            mostrarDialog("Datos incompletos", "La historia de la mascota es obligatoria.", false);
            return;
        }
        binding.tilHistoria.setError(null);

        if (imagenUri == null) {
            binding.flImagenPortada.setStrokeColor(
                    android.content.res.ColorStateList.valueOf(0xFFB3261E));
            mostrarDialog("Foto de portada requerida", "Debes seleccionar una imagen de portada para la mascota.", false);
            return;
        }
        binding.flImagenPortada.setStrokeColor(
                android.content.res.ColorStateList.valueOf(0xFFB8A0E8));

        List<VacunaResponse> vacunas = viewModel.getVacunasSeleccionadas().getValue();
        if (vacunas == null || vacunas.isEmpty()) {
            mostrarDialog("Vacunas requeridas", "Debes asociar al menos una vacuna a la mascota.", false);
            return;
        }

        // Resolver idRaza desde la lista completa usando el nombre seleccionado
        Integer idRaza = null;
        for (RazaResponse r : listaRazasActual) {
            if (r.nombre.equals(raza)) { idRaza = r.idRaza; break; }
        }

        viewModel.guardarMascotaCompleta(
                this,
                sessionManager.getToken(),
                sessionManager.getUuid(),
                sessionManager.getIdRefugio(),
                nombre, idRaza,
                anios, meses,
                genero, temperamento, historia,
                imagenUri, BUCKET
        );
    }

    // ════════════════════════════════════════════════════════
    // DIALOGS
    // ════════════════════════════════════════════════════════

    /**
     * Dialog de validación y éxito con ícono dinámico.
     * Si esExito=true, cierra la Activity al presionar "Entendido".
     */
    private void mostrarDialog(String titulo, String mensaje, boolean esExito) {
        DialogValidacionBinding dialogBinding = DialogValidacionBinding.inflate(getLayoutInflater());

        dialogBinding.tvDialogTitulo.setText(titulo);
        dialogBinding.tvDialogMensaje.setText(mensaje);

        if (esExito) {
            dialogBinding.ivDialogIcon.setImageResource(R.drawable.outline_add_task_24);
            dialogBinding.ivDialogIcon.setColorFilter(getColor(android.R.color.holo_green_dark));
        } else {
            dialogBinding.ivDialogIcon.setImageResource(R.drawable.outline_assignment_late_24);
            dialogBinding.ivDialogIcon.setColorFilter(0xFFE65100);
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

    /**
     * Dialog de confirmación al intentar retroceder con datos sin guardar.
     * Opciones: seguir en la pantalla o salir sin guardar.
     */
    private void mostrarDialogConfirmacion() {
        DialogConfirmacionRetrocederBinding dialogBinding =
                DialogConfirmacionRetrocederBinding.inflate(getLayoutInflater());

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setBackground(new android.graphics.drawable.GradientDrawable() {{
                    setColor(0xFFFFFFFF);
                    setCornerRadius(24f);
                }})
                .create();

        dialogBinding.btnSeguirAgregando.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.btnSalirSinGuardar.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        dialog.show();
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    /** Abre el selector de imágenes del sistema con filtro image/* */
    private void abrirGaleria(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        launcher.launch(intent);
    }

    /** Extrae y limpia el texto de un Editable, devuelve "" si es null */
    private String texto(android.text.Editable editable) {
        return editable != null ? editable.toString().trim() : "";
    }

    // ════════════════════════════════════════════════════════
    // CICLO DE VIDA — Limpieza
    // ════════════════════════════════════════════════════════

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}