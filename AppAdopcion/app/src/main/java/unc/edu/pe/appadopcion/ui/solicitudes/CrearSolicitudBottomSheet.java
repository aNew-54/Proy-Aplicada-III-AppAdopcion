package unc.edu.pe.appadopcion.ui.solicitudes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.BottomSheetCrearSolicitudBinding;
import unc.edu.pe.appadopcion.vm.solicitudes.SolicitudViewModel;

public class CrearSolicitudBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetCrearSolicitudBinding binding;
    private MascotaResponse mascota;
    private SolicitudViewModel viewModel;

    public CrearSolicitudBottomSheet(MascotaResponse mascota) {
        this.mascota = mascota;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCrearSolicitudBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SolicitudViewModel.class);
        SessionManager session = new SessionManager(requireContext());
        AppRepository repo = new AppRepository(session.getToken());

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnEnviar.setEnabled(!loading);
        });

        viewModel.getIsSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "¡Solicitud enviada con éxito!", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

        binding.btnEnviar.setOnClickListener(v -> {
            String msj = binding.etMensajeSolicitud.getText().toString().trim();
            if (msj.isEmpty()) {
                Toast.makeText(requireContext(), "Escribe un mensaje", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.enviarSolicitud(repo, mascota.idRefugio, mascota.idMascota, session.getIdAdoptante(), msj);
        });
    }
}