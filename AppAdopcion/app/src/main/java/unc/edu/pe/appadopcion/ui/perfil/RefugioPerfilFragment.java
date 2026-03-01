package unc.edu.pe.appadopcion.ui.perfil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.databinding.FragmentRefugioPerfilBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.perfil.RefugioPerfilViewModel;

public class RefugioPerfilFragment extends Fragment {

    private FragmentRefugioPerfilBinding binding;
    private SessionManager session;
    private RefugioPerfilViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRefugioPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(RefugioPerfilViewModel.class);

        configurarObservadores();

        // Evita recargar los datos si el ViewModel ya los tiene (ej: al girar la pantalla)
        if (viewModel.getPerfil().getValue() == null) {
            viewModel.cargarDatosCompletos(session.getUuid(), session.getToken());
        }

        binding.btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar perfil — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnVerMascotas.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mis mascotas — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void configurarObservadores() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getPerfil().observe(getViewLifecycleOwner(), refugio -> {
            if (refugio != null) {
                mostrarDatos(refugio);
            }
        });

        viewModel.getSolicitudes().observe(getViewLifecycleOwner(), solicitudes -> {
            if (solicitudes != null) {
                cargarSolicitudesEnUI(solicitudes);
            }
        });

    }

    private void mostrarDatos(RefugioResponse r) {
        if (binding == null) return;

        binding.tvNombreRefugio.setText(r.nombre);
        binding.tvDireccion.setText(r.direccion);
        binding.tvCorreo.setText(r.correo);
        binding.tvTelefono.setText(r.telefono != null ? r.telefono : "Sin teléfono");
        binding.tvDescripcion.setText(r.descripcion != null ? r.descripcion : "Sin descripción");
        binding.tvMascotasDisponibles.setText(r.mascotasDisponibles + " mascotas disponibles");

        // Foto de perfil
        ImageLoader.cargarAvatarCircular(
                requireContext(),
                session.getToken(),
                session.getUuid(),
                binding.ivFotoPerfil,
                R.drawable.ic_pets
        );

        // Portada
        if (r.urlPortada != null && !r.urlPortada.isEmpty()) {
            ImageLoader.cargarPublica(
                    requireContext(),
                    r.urlPortada,
                    binding.ivPortada,
                    R.drawable.bg_registro_header
            );
        }
    }

    private void cargarSolicitudesEnUI(List<SolicitudResponse> lista) {
        if (binding == null) return;

        if (lista.isEmpty()) {
            binding.tvSinSolicitudes.setVisibility(View.VISIBLE);
            binding.rvSolicitudes.setVisibility(View.GONE);
        } else {
            binding.tvSinSolicitudes.setVisibility(View.GONE);
            binding.rvSolicitudes.setVisibility(View.VISIBLE);
            binding.rvSolicitudes.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvSolicitudes.setAdapter(new SolicitudesRefugioAdapter(lista, (id, aprobada) ->
                    Toast.makeText(requireContext(), "Gestión — próximamente", Toast.LENGTH_SHORT).show()));
        }
    }

    private void cerrarSesion() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).cerrarSesion();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}