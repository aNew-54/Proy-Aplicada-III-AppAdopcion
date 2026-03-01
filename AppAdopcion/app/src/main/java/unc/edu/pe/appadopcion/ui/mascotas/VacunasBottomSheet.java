package unc.edu.pe.appadopcion.ui.mascotas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.VacunaResponse;
import unc.edu.pe.appadopcion.databinding.BottomSheetVacunasBinding;

public class VacunasBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "VacunasBottomSheet";
    private static final String ARG_VACUNAS = "vacunas";
    private static final String ARG_VACUNAS_SELECCIONADAS = "vacunas_seleccionadas";

    private BottomSheetVacunasBinding binding;

    /**
     * Referencias directas a los CheckBox inflados.
     * Se usa esta lista al confirmar en lugar de recorrer el DOM,
     * ya que el contenedor mezcla checkboxes con vistas divisoras.
     */
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    // ════════════════════════════════════════════════════════
    // CALLBACK — Devuelve la lista de vacunas marcadas
    // ════════════════════════════════════════════════════════

    public interface OnVacunasConfirmadasListener {
        void onConfirmar(List<VacunaResponse> seleccionadas);
    }

    private OnVacunasConfirmadasListener listener;

    public void setOnVacunasConfirmadasListener(OnVacunasConfirmadasListener l) {
        this.listener = l;
    }

    // ════════════════════════════════════════════════════════
    // FACTORY — Recibe vacunas disponibles y previamente seleccionadas
    // ════════════════════════════════════════════════════════

    public static VacunasBottomSheet newInstance(ArrayList<VacunaResponse> vacunas,
                                                 ArrayList<VacunaResponse> seleccionadas) {
        VacunasBottomSheet sheet = new VacunasBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VACUNAS, vacunas);
        args.putSerializable(ARG_VACUNAS_SELECCIONADAS, seleccionadas);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ════════════════════════════════════════════════════════
    // CONFIGURACIÓN
    // ════════════════════════════════════════════════════════

    /**
     * Infla un CheckBox por cada vacuna disponible y marca los que ya
     * estaban seleccionados en sesiones anteriores (persistencia entre aperturas).
     * Agrega un divisor visual entre items, excepto tras el último.
     */
    private void cargarVacunas(List<VacunaResponse> vacunas, List<VacunaResponse> seleccionadas) {
        binding.llVacunasContenedor.removeAllViews();
        checkBoxes.clear();

        // IDs previamente seleccionados para marcar los checkboxes correspondientes
        List<Integer> idsSeleccionados = new ArrayList<>();
        for (VacunaResponse v : seleccionadas) idsSeleccionados.add(v.id);

        for (int i = 0; i < vacunas.size(); i++) {
            VacunaResponse vacuna = vacunas.get(i);

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_vacuna_checkbox, binding.llVacunasContenedor, false);

            CheckBox cb = item.findViewById(R.id.cbVacuna);
            cb.setText(vacuna.nombre);
            cb.setTag(vacuna);
            cb.setChecked(idsSeleccionados.contains(vacuna.id));

            checkBoxes.add(cb);
            binding.llVacunasContenedor.addView(item);

            // Divisor entre items (no después del último)
            if (i < vacunas.size() - 1)
                binding.llVacunasContenedor.addView(crearDivisor());
        }
    }

    /**
     * Al confirmar, recorre la lista de checkBoxes (no el DOM)
     * para recoger solo los marcados y notificar al listener.
     */
    private void configurarBotonConfirmar() {
        binding.btnConfirmarVacunas.setOnClickListener(v -> {
            List<VacunaResponse> marcadas = new ArrayList<>();
            for (CheckBox cb : checkBoxes)
                if (cb.isChecked()) marcadas.add((VacunaResponse) cb.getTag());

            if (listener != null) listener.onConfirmar(marcadas);
            dismiss();
        });
    }

    // ════════════════════════════════════════════════════════
    // UTILIDADES
    // ════════════════════════════════════════════════════════

    /** Crea una línea divisora de 1px con margen horizontal para separar items */
    private View crearDivisor() {
        View divisor = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMarginStart(dpToPx(20));
        params.setMarginEnd(dpToPx(20));
        divisor.setLayoutParams(params);
        divisor.setBackgroundColor(0xFFEDE7F6);
        return divisor;
    }

    private int dpToPx(int dp) {
        return (int) (dp * requireContext().getResources().getDisplayMetrics().density);
    }
}