package unc.edu.pe.appadopcion.ui.mascotas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

import unc.edu.pe.appadopcion.databinding.BottomSheetAgregarIntervencionBinding;

public class AgregarIntervencionBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AgregarIntervencionBottomSheet";

    private BottomSheetAgregarIntervencionBinding binding;
    private OnIntervencionGuardadaListener listener;

    // ════════════════════════════════════════════════════════
    // CALLBACK — Notifica a la Activity cuando se guarda
    // ════════════════════════════════════════════════════════

    public interface OnIntervencionGuardadaListener {
        void onGuardar(IntervencionLocal intervencion);
    }

    public void setOnIntervencionGuardadaListener(OnIntervencionGuardadaListener listener) {
        this.listener = listener;
    }

    // ════════════════════════════════════════════════════════
    // FACTORY
    // ════════════════════════════════════════════════════════

    public static AgregarIntervencionBottomSheet newInstance() {
        return new AgregarIntervencionBottomSheet();
    }

    // ════════════════════════════════════════════════════════
    // CICLO DE VIDA
    // ════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAgregarIntervencionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configurarFecha();
        configurarBotonGuardar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ════════════════════════════════════════════════════════
    // CONFIGURACIÓN
    // ════════════════════════════════════════════════════════

    /** El campo de fecha abre un DatePickerDialog al tocarlo */
    private void configurarFecha() {
        binding.etFechaIntervencion.setOnClickListener(v -> abrirDatePicker());
    }

    /**
     * Valida título y fecha (obligatorios) antes de notificar al listener.
     * La descripción es opcional.
     */
    private void configurarBotonGuardar() {
        binding.btnGuardarIntervencion.setOnClickListener(v -> {
            String titulo      = texto(binding.etTituloIntervencion.getText());
            String descripcion = texto(binding.etDescripcionIntervencion.getText());
            String fecha       = texto(binding.etFechaIntervencion.getText());

            if (titulo.isEmpty()) {
                binding.tilTituloIntervencion.setError("El título es obligatorio");
                return;
            }
            if (fecha.isEmpty()) {
                binding.tilFechaIntervencion.setError("La fecha es obligatoria");
                return;
            }

            binding.tilTituloIntervencion.setError(null);
            binding.tilFechaIntervencion.setError(null);

            if (listener != null)
                listener.onGuardar(new IntervencionLocal(titulo, descripcion, fecha));

            dismiss();
        });
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    /** Abre el DatePickerDialog con la fecha actual como valor por defecto */
    private void abrirDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) ->
                        binding.etFechaIntervencion.setText(
                                String.format("%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /** Extrae y limpia texto de un Editable, devuelve "" si es null */
    private String texto(android.text.Editable editable) {
        return editable != null ? editable.toString().trim() : "";
    }
}