package unc.edu.pe.appadopcion.ui.descubrir;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import unc.edu.pe.appadopcion.databinding.FragmentDescubrirBinding;
import unc.edu.pe.appadopcion.ui.mascotas.AgregarMascotaActivity;

public class DescubrirFragment extends Fragment {

    private FragmentDescubrirBinding binding;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public DescubrirFragment() { }

    public static DescubrirFragment newInstance(String param1, String param2) {
        DescubrirFragment fragment = new DescubrirFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    // AQUÍ se crea el binding correctamente
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDescubrirBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // AQUÍ ya puedes usar binding sin error
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.fbtnAgregarMascota.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AgregarMascotaActivity.class);
            startActivity(intent);
        });
    }

    // Muy importante en fragments con binding
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}