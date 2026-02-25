package unc.edu.pe.appadopcion.ui.perfil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.AdoptanteResponse;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.FragmentAdoptantePerfilBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.utils.ImageLoader;

public class AdoptantePerfilFragment extends Fragment {

    private FragmentAdoptantePerfilBinding binding;
    private SessionManager session;
    private AppRepository repo;

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
        repo    = new AppRepository(session.getToken());

        cargarPerfil();

        binding.btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar perfil — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnVerFavoritos.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mis favoritos — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarPerfil() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        repo.obtenerAdoptanteCompleto(session.getUuid(),
                new Callback<List<AdoptanteResponse>>() {
                    @Override
                    public void onResponse(Call<List<AdoptanteResponse>> call,
                                           Response<List<AdoptanteResponse>> resp) {
                        // ── CRÍTICO: el fragment pudo destruirse mientras esperaba la red
                        if (binding == null || !isAdded()) return;

                        binding.progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            AdoptanteResponse a = resp.body().get(0);
                            mostrarDatos(a);
                            cargarSolicitudes(a.idAdoptante);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AdoptanteResponse>> call, Throwable t) {
                        if (binding == null || !isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
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

        // Foto de perfil — bucket privado: usar signed URL
        ImageLoader.cargarAvatarCircular(
                requireContext(),
                session.getToken(),
                session.getUuid(),
                binding.ivFotoPerfil,
                R.drawable.ic_person
        );
    }

    private void cargarSolicitudes(int idAdoptante) {
        if (binding == null) return;

        repo.obtenerSolicitudesAdoptante(idAdoptante,
                new Callback<List<SolicitudResponse>>() {
                    @Override
                    public void onResponse(Call<List<SolicitudResponse>> call,
                                           Response<List<SolicitudResponse>> resp) {
                        if (binding == null || !isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null) {
                            List<SolicitudResponse> lista = resp.body();
                            if (lista.isEmpty()) {
                                binding.tvSinSolicitudes.setVisibility(View.VISIBLE);
                                binding.rvSolicitudes.setVisibility(View.GONE);
                            } else {
                                binding.tvSinSolicitudes.setVisibility(View.GONE);
                                binding.rvSolicitudes.setVisibility(View.VISIBLE);
                                binding.rvSolicitudes.setLayoutManager(
                                        new LinearLayoutManager(requireContext()));
                                binding.rvSolicitudes.setAdapter(
                                        new SolicitudesAdoptanteAdapter(lista));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SolicitudResponse>> call, Throwable t) { /* noop */ }
                });
    }

    private void cerrarSesion() {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).cerrarSesion();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // ← LA CLAVE: evita el NullPointerException
    }
}