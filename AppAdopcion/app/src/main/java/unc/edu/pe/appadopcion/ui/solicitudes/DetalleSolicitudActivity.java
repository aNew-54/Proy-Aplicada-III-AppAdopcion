package unc.edu.pe.appadopcion.ui.solicitudes;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.ActivityDetalleSolicitudBinding;
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.solicitudes.SolicitudViewModel;

public class DetalleSolicitudActivity extends AppCompatActivity {

    private ActivityDetalleSolicitudBinding binding;
    private SolicitudViewModel viewModel;
    private SessionManager session;
    private SolicitudResponse solicitud;
    private AppRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleSolicitudBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        repo = new AppRepository(session.getToken());
        viewModel = new ViewModelProvider(this).get(SolicitudViewModel.class);

        if (getIntent() != null && getIntent().hasExtra("solicitud")) {
            solicitud = (SolicitudResponse) getIntent().getSerializableExtra("solicitud");
            configurarUI();
        } else { finish(); return; }

        configurarObservadores();
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void configurarUI() {
        binding.tvNombreMascota.setText(solicitud.nombreMascota);
        binding.chipAdoptante.setText("De: " + solicitud.nombreAdoptante);
        binding.chipRefugio.setText("Para: " + solicitud.nombreRefugio);
        binding.tvMensaje.setText(solicitud.mensaje);
        binding.actvEstado.setText(solicitud.estado, false);
        binding.etFechaVisita.setText(solicitud.fechaVisita != null ? solicitud.fechaVisita : "");
        binding.etNotas.setText(solicitud.notasRefugio != null ? solicitud.notasRefugio : "");

        // Al quitar el app:tint del XML, esto cargará la foto correctamente
        ImageLoader.cargarPublica(this, solicitud.urlPortadaMascota, binding.ivMascota, R.drawable.ic_pets);

        if (session.esRefugio()) {
            String[] estados = {"Pendiente", "Aprobada", "Rechazada", "Visita Agendada"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados);
            binding.actvEstado.setAdapter(adapter);

            binding.etFechaVisita.setOnClickListener(v -> mostrarSelectorFechaHora());
            binding.etFechaVisita.setFocusable(false);

            binding.btnGuardar.setOnClickListener(v -> {
                String nuevoEstado = binding.actvEstado.getText().toString();
                String notas = binding.etNotas.getText().toString();
                String fecha = binding.etFechaVisita.getText().toString();
                viewModel.actualizarSolicitud(repo, solicitud.idSolicitud, nuevoEstado, fecha, notas);
            });
        } else {
            binding.tilEstado.setEnabled(false);
            binding.tilFechaVisita.setEnabled(false);
            binding.tilNotas.setEnabled(false);
            binding.btnGuardar.setVisibility(View.GONE);
        }
    }

    private void mostrarSelectorFechaHora() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccionar fecha de visita").build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selection);

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H).setTitleText("Seleccionar hora").build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                cal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                cal.set(Calendar.MINUTE, timePicker.getMinute());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                binding.etFechaVisita.setText(sdf.format(cal.getTime()));
            });
            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void configurarObservadores() {
        // AQUÍ ESTÁ LA LÓGICA QUE FALTABA
        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnGuardar.setEnabled(!loading);
            binding.btnGuardar.setText(loading ? "Guardando..." : "Guardar Actualización");
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Le avisa al fragmento que debe recargar la lista
                finish(); // Cierra esta pantalla
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}