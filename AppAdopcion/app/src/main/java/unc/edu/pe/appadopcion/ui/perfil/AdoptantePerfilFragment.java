package unc.edu.pe.appadopcion.ui.perfil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.databinding.FragmentAdoptantePerfilBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.utils.ImageLoader;
// Asegúrate de importar tu ViewModel real del Adoptante
import unc.edu.pe.appadopcion.vm.perfil.AdoptantePerfilViewModel;

public class AdoptantePerfilFragment extends Fragment {

    private FragmentAdoptantePerfilBinding binding;
    private SessionManager session;
    private AdoptantePerfilViewModel viewModel;

    // --- ESCUCHADOR DE LA ACTIVIDAD DE EDICIÓN ---
    private final ActivityResultLauncher<Intent> editarPerfilLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Si la actividad guardó los cambios y devolvió RESULT_OK...
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // ¡Forzamos la recarga de los datos desde Supabase!
                    viewModel.cargarDatosCompletos(session.getUuid(), session.getToken());
                }
            });

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

        // Carga inicial (Evita recargar al rotar la pantalla)
        if (viewModel.getPerfil().getValue() == null) {
            viewModel.cargarDatosCompletos(session.getUuid(), session.getToken());
        }

        // --- BOTÓN PARA EDITAR EL PERFIL ---
        binding.btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditarPerfilActivity.class);
            editarPerfilLauncher.launch(intent);
        });

        // Botón de cerrar sesión
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

        // Si tienes observadores para favoritos o solicitudes, agrégalos aquí
    }

    private void mostrarDatos(AdoptanteResponse a) {
        if (binding == null) return;

        // Concatenamos nombre y apellido
        binding.tvNombreCompleto.setText(a.nombre + " " + a.apellido);

        // Datos de contacto
        binding.tvCorreo.setText(a.correo);
        binding.tvTelefono.setText(a.telefono != null ? a.telefono : "Sin teléfono");
        binding.tvGenero.setText(a.genero != null ? a.genero : "Sin especificar");
        binding.tvUbicacion.setText(a.direccion != null ? a.direccion : "Sin dirección");

        // Mostrar contadores
        binding.tvTotalFavoritos.setText(String.valueOf(a.totalFavoritos));
        binding.tvTotalSolicitudes.setText(String.valueOf(a.totalSolicitudes));

        // Cargar Foto de perfil
        ImageLoader.cargarAvatarCircular(
                requireContext(),
                session.getToken(),
                session.getUuid(),
                binding.ivFotoPerfil,
                R.drawable.ic_person // Usando el placeholder de tu XML
        );
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