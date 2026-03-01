package unc.edu.pe.appadopcion.ui.mascotas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.VacunaResponse;
import unc.edu.pe.appadopcion.databinding.BottomSheetVacunasBinding;

public class VacunasBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "VacunasBottomSheet";
    private static final String ARG_VACUNAS = "vacunas";
    private static final String ARG_VACUNAS_SELECCIONADAS = "vacunas_seleccionadas";

    private BottomSheetVacunasBinding binding;
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    public interface OnVacunasConfirmadasListener {
        void onConfirmar(List<VacunaResponse> seleccionadas);
    }

    private OnVacunasConfirmadasListener listener;

    public void setOnVacunasConfirmadasListener(OnVacunasConfirmadasListener l) {
        this.listener = l;
    }

    public static VacunasBottomSheet newInstance(ArrayList<VacunaResponse> vacunas,
                                                 ArrayList<VacunaResponse> seleccionadas) {
        VacunasBottomSheet sheet = new VacunasBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VACUNAS, vacunas);
        args.putSerializable(ARG_VACUNAS_SELECCIONADAS, seleccionadas);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetVacunasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() == null) return;

        ArrayList<VacunaResponse> vacunas =
                (ArrayList<VacunaResponse>) getArguments().getSerializable(ARG_VACUNAS);
        ArrayList<VacunaResponse> seleccionadas =
                (ArrayList<VacunaResponse>) getArguments().getSerializable(ARG_VACUNAS_SELECCIONADAS);

        if (vacunas != null)
            cargarVacunas(vacunas, seleccionadas != null ? seleccionadas : new ArrayList<>());

        configurarBotonConfirmar();
    }

    private void cargarVacunas(List<VacunaResponse> vacunas, List<VacunaResponse> seleccionadas) {
        binding.llVacunasContenedor.removeAllViews();
        checkBoxes.clear();

        for (int i = 0; i < vacunas.size(); i++) {
            VacunaResponse vacuna = vacunas.get(i);
            
            // Buscar si ya estaba seleccionada para recuperar la fecha
            VacunaResponse previa = null;
            for (VacunaResponse s : seleccionadas) {
                if (s.id == vacuna.id) { previa = s; break; }
            }

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_vacuna_checkbox, binding.llVacunasContenedor, false);

            CheckBox cb = item.findViewById(R.id.cbVacuna);
            TextView tvFecha = item.findViewById(R.id.tvFechaVacuna);

            cb.setText(vacuna.nombre);
            cb.setTag(vacuna);
            
            if (previa != null) {
                cb.setChecked(true);
                vacuna.fechaAplicacion = previa.fechaAplicacion;
                tvFecha.setVisibility(View.VISIBLE);
                tvFecha.setText(previa.fechaAplicacion != null ? previa.fechaAplicacion : "Seleccionar fecha");
            }

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tvFecha.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked && vacuna.fechaAplicacion == null) {
                    // Fecha hoy por defecto
                    String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    vacuna.fechaAplicacion = hoy;
                    tvFecha.setText(hoy);
                }
            });

            tvFecha.setOnClickListener(v -> mostrarDatePicker(tvFecha, vacuna));

            checkBoxes.add(cb);
            binding.llVacunasContenedor.addView(item);

            if (i < vacunas.size() - 1) binding.llVacunasContenedor.addView(crearDivisor());
        }
    }

    private void mostrarDatePicker(TextView tv, VacunaResponse vacuna) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            vacuna.fechaAplicacion = fecha;
            tv.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void configurarBotonConfirmar() {
        binding.btnConfirmarVacunas.setOnClickListener(v -> {
            List<VacunaResponse> marcadas = new ArrayList<>();
            for (CheckBox cb : checkBoxes) {
                if (cb.isChecked()) {
                    VacunaResponse vTag = (VacunaResponse) cb.getTag();
                    marcadas.add(vTag);
                }
            }
            if (listener != null) listener.onConfirmar(marcadas);
            dismiss();
        });
    }

    private View crearDivisor() {
        View divisor = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divisor.setLayoutParams(params);
        divisor.setBackgroundColor(0xFFEDE7F6);
        return divisor;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}