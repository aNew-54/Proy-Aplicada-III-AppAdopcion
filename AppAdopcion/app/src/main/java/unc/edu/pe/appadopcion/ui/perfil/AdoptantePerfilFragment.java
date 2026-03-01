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
import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.databinding.FragmentAdoptantePerfilBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.perfil.AdoptantePerfilViewModel;

public class AdoptantePerfilFragment extends Fragment {

    private FragmentAdoptantePerfilBinding binding;
    private SessionManager session;
    private AdoptantePerfilViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdoptantePerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(AdoptantePerfilViewModel.class);

        configurarObservadores();

        // Evita recargar si el ViewModel ya tiene los datos (ej: al girar la pantalla)
        if (viewModel.getPerfil().getValue() == null) {
            viewModel.cargarDatosCompletos(session.getUuid(), session.getToken());
        }

        binding.btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar perfil — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnVerFavoritos.setOnClickListener(v -> {

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.subFragmentContainer, new unc.edu.pe.appadopcion.ui.favoritos.FavoritosFragment())
                    .addToBackStack(null)
                    .commit();
        });

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

        viewModel.getPerfil().observe(getViewLifecycleOwner(), adoptante -> {
            if (adoptante != null) {
                mostrarDatos(adoptante);
            }
        });

        viewModel.getSolicitudes().observe(getViewLifecycleOwner(), solicitudes -> {
            if (solicitudes != null) {
                cargarSolicitudesEnUI(solicitudes);
            }
        });
    }

    private void mostrarDatos(AdoptanteResponse a) {
        if (binding == null) return;

        binding.tvNombreCompleto.setText(a.nombre + " " + a.apellido);
        binding.tvCorreo.setText(a.correo);
        binding.tvTelefono.setText(a.telefono != null ? a.telefono : "Sin teléfono");
        binding.tvGenero.setText(a.genero != null ? a.genero : "No especificado");
        binding.tvTotalFavoritos.setText(String.valueOf(a.totalFavoritos));
        binding.tvTotalSolicitudes.setText(String.valueOf(a.totalSolicitudes));
        binding.tvUbicacion.setText(String.valueOf(a.direccion));

        ImageLoader.cargarAvatarCircular(
                requireContext(),
                session.getToken(),
                session.getUuid(),
                binding.ivFotoPerfil,
                R.drawable.ic_person
        );
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
            binding.rvSolicitudes.setAdapter(new SolicitudesAdoptanteAdapter(lista));
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