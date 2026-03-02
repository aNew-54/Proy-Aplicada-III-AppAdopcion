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
        binding.etFechaVisita.setText(limpiarFecha(solicitud.fechaVisita));
        binding.etNotas.setText(solicitud.notasRefugio != null ? solicitud.notasRefugio : "");

        ImageLoader.cargarPublica(this, solicitud.urlPortadaMascota, binding.ivMascota, R.drawable.ic_pets);

        if (session.esRefugio()) {
            String[] estados = {"Pendiente", "Aprobada", "Rechazada", "Visita Agendada"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados);
            binding.actvEstado.setAdapter(adapter);

            binding.etFechaVisita.setOnClickListener(v -> mostrarSelectorFechaHora());
            binding.etFechaVisita.setFocusable(false);

            binding.btnGuardar.setOnClickListener(v -> {
                String nuevoEstado = binding.actvEstado.getText().toString();
                String notas = binding.etNotas.getText().toString().trim();
                String fecha = binding.etFechaVisita.getText().toString().trim();

                // 1. Validar "Visita Agendada": Obligar a poner fecha
                if (nuevoEstado.equals("Visita Agendada") && fecha.isEmpty()) {
                    Toast.makeText(this, "Por favor, seleccione la fecha y hora de la visita.", Toast.LENGTH_LONG).show();
                    return; // Detiene la ejecución aquí
                }

                // 2. Validar "Rechazada": Borrar fecha y pedir motivo
                if (nuevoEstado.equals("Rechazada")) {
                    fecha = null; // Ignoramos la fecha si es rechazada
                    if (notas.isEmpty()) {
                        Toast.makeText(this, "Por favor, indique en las notas el motivo del rechazo.", Toast.LENGTH_LONG).show();
                        return; // Detiene la ejecución aquí
                    }
                }

                // 3. Limpieza final para la base de datos (evita errores de sintaxis en PostgreSQL)
                // Si la fecha está en blanco (ej. Pendiente o Aprobada sin visita aún), enviamos null en lugar de ""
                if (fecha != null && fecha.isEmpty()) {
                    fecha = null;
                }

                // 4. Si pasa todas las validaciones, enviamos los datos al servidor
                viewModel.actualizarSolicitud(repo, solicitud.idSolicitud, nuevoEstado, fecha, notas);
            });

        } else {
            // Es adoptante (Solo lectura)
            binding.tilEstado.setEnabled(false);
            binding.tilFechaVisita.setEnabled(false);
            binding.tilNotas.setEnabled(false);
            binding.btnGuardar.setVisibility(View.GONE);

            // NUEVO: Mostramos el botón de WhatsApp y configuramos el clic
            binding.btnContactarWhatsApp.setVisibility(View.VISIBLE);
            binding.btnContactarWhatsApp.setOnClickListener(v -> {
                // Asegúrate de que tu modelo SolicitudResponse tenga el campo telefonoRefugio.
                // Si se llama diferente, cámbialo aquí abajo.
                String numero = solicitud.telefonoRefugio;

                if (numero != null && !numero.trim().isEmpty()) {
                    // Quitamos espacios por si el número se guardó como "999 666 122"
                    numero = numero.replace(" ", "");

                    // Preparamos un mensaje automático amigable
                    String mensaje = "Hola, escribo sobre mi solicitud de adopción para " + solicitud.nombreMascota + ".";
                    String url = "https://api.whatsapp.com/send?phone=51" + numero + "&text=" + android.net.Uri.encode(mensaje);

                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "No tienes WhatsApp instalado.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "El refugio no tiene un número registrado.", Toast.LENGTH_SHORT).show();
                }
            });
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

    private String limpiarFecha(String fechaCruda) {
        if (fechaCruda == null || fechaCruda.isEmpty()) return "";
        try {
            // Si viene de Supabase (ej: "2026-03-09T13:33:00+00:00")
            if (fechaCruda.contains("T")) {
                // Reemplazamos la 'T' por un espacio y cortamos hasta el minuto (los primeros 16 caracteres)
                return fechaCruda.replace("T", " ").substring(0, 16);
            }
            return fechaCruda;
        } catch (Exception e) {
            return fechaCruda; // Si algo falla, devuelve la original por seguridad
        }
    }
}