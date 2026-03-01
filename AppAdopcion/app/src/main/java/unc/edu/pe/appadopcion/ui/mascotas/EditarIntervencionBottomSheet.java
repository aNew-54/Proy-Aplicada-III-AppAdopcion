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

import unc.edu.pe.appadopcion.databinding.BottomSheetEditarIntervencionBinding;

public class EditarIntervencionBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "EditarIntervencionBottomSheet";

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_DESCRIPCION = "descripcion";
    private static final String ARG_FECHA = "fecha";
    private static final String ARG_INDEX = "index";

    private BottomSheetEditarIntervencionBinding binding;
    private OnIntervencionEditadaListener editadaListener;
    private OnIntervencionEliminadaListener eliminadaListener;

    // ════════════════════════════════════════════════════════
    // CALLBACKS — Editar y eliminar notifican por separado
    // ════════════════════════════════════════════════════════

    public interface OnIntervencionEditadaListener {
        void onEditar(int index, IntervencionLocal intervencion);
    }

    public interface OnIntervencionEliminadaListener {
        void onEliminar(int index);
    }

    public void setOnIntervencionEditadaListener(OnIntervencionEditadaListener l) {
        this.editadaListener = l;
    }

    public void setOnIntervencionEliminadaListener(OnIntervencionEliminadaListener l) {
        this.eliminadaListener = l;
    }

    // ════════════════════════════════════════════════════════
    // FACTORY — Recibe los datos actuales del item y su posición
    // ════════════════════════════════════════════════════════

    public static EditarIntervencionBottomSheet newInstance(IntervencionLocal intervencion, int index) {
        EditarIntervencionBottomSheet sheet = new EditarIntervencionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TITULO,      intervencion.titulo);
        args.putString(ARG_DESCRIPCION, intervencion.descripcion);
        args.putString(ARG_FECHA,       intervencion.fecha);
        args.putInt(ARG_INDEX,          index);
        sheet.setArguments(args);
        return sheet;
    }

    // ════════════════════════════════════════════════════════
    // CICLO DE VIDA
    // ════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEditarIntervencionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        precargarCampos();
        configurarFecha();
        configurarBotonGuardar();
        configurarBotonEliminar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ════════════════════════════════════════════════════════
    // CONFIGURACIÓN
    // ════════════════════════════════════════════════════════

    /** Rellena los campos con los datos existentes de la intervención */
    private void precargarCampos() {
        if (getArguments() == null) return;
        binding.etTituloEditar.setText(getArguments().getString(ARG_TITULO, ""));
        binding.etDescripcionEditar.setText(getArguments().getString(ARG_DESCRIPCION, ""));
        binding.etFechaEditar.setText(getArguments().getString(ARG_FECHA, ""));
    }

    /**
     * El campo de fecha y su ícono final abren el DatePickerDialog.
     * El picker parsea la fecha actual del campo para mostrarla preseleccionada.
     */
    private void configurarFecha() {
        binding.etFechaEditar.setOnClickListener(v -> abrirDatePicker());
        binding.tilFechaEditar.setEndIconOnClickListener(v -> abrirDatePicker());
    }

    /** Valida título y fecha, luego notifica al listener con los datos actualizados */
    private void configurarBotonGuardar() {
        binding.btnGuardarCambios.setOnClickListener(v -> {
            String titulo      = texto(binding.etTituloEditar.getText());
            String descripcion = texto(binding.etDescripcionEditar.getText());
            String fecha       = texto(binding.etFechaEditar.getText());

            if (titulo.isEmpty()) {
                binding.tilTituloEditar.setError("El título es obligatorio");
                return;
            }
            if (fecha.isEmpty()) {
                binding.tilFechaEditar.setError("La fecha es obligatoria");
                return;
            }

            binding.tilTituloEditar.setError(null);
            binding.tilFechaEditar.setError(null);

            if (editadaListener != null && getArguments() != null)
                editadaListener.onEditar(
                        getArguments().getInt(ARG_INDEX),
                        new IntervencionLocal(titulo, descripcion, fecha));

            dismiss();
        });
    }

    /** Notifica al listener de eliminación con el índice del item */
    private void configurarBotonEliminar() {
        binding.btnEliminarIntervencion.setOnClickListener(v -> {
            if (eliminadaListener != null && getArguments() != null)
                eliminadaListener.onEliminar(getArguments().getInt(ARG_INDEX));
            dismiss();
        });
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    /**
     * Abre el DatePickerDialog.
     * Si el campo ya tiene una fecha en formato dd/MM/yyyy, la parsea
     * para mostrarla preseleccionada en el picker.
     */
    private void abrirDatePicker() {
        Calendar cal = Calendar.getInstance();
        String fechaActual = texto(binding.etFechaEditar.getText());

        if (!fechaActual.isEmpty()) {
            try {
                String[] partes = fechaActual.split("/");
                cal.set(Integer.parseInt(partes[2]),
                        Integer.parseInt(partes[1]) - 1,
                        Integer.parseInt(partes[0]));
            } catch (Exception ignored) {}
        }

        new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) ->
                        binding.etFechaEditar.setText(
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