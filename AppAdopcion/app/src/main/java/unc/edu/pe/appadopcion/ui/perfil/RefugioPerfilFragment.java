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
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.FragmentRefugioPerfilBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.utils.ImageLoader;

public class RefugioPerfilFragment extends Fragment {

    private FragmentRefugioPerfilBinding binding;
    private SessionManager session;
    private AppRepository repo;

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
        repo    = new AppRepository(session.getToken());

        cargarPerfil();

        binding.btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar perfil — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnVerMascotas.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mis mascotas — próximamente", Toast.LENGTH_SHORT).show());

        binding.btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cargarPerfil() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        repo.obtenerRefugioPorUuid(session.getUuid(),
                new Callback<List<RefugioResponse>>() {
                    @Override
                    public void onResponse(Call<List<RefugioResponse>> call,
                                           Response<List<RefugioResponse>> resp) {
                        // ── CRÍTICO: null check antes de tocar binding
                        if (binding == null || !isAdded()) return;

                        binding.progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            RefugioResponse r = resp.body().get(0);
                            mostrarDatos(r);
                            cargarSolicitudesRecibidas(r.idRefugio);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                        if (binding == null || !isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
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

        // Foto de perfil — bucket privado: signed URL
        ImageLoader.cargarAvatarCircular(
                requireContext(),
                session.getToken(),
                session.getUuid(),
                binding.ivFotoPerfil,
                R.drawable.ic_pets
        );

        // Portada — bucket público: URL directa
        if (r.urlPortada != null && !r.urlPortada.isEmpty()) {
            ImageLoader.cargarPublica(
                    requireContext(),
                    r.urlPortada,
                    binding.ivPortada,
                    R.drawable.bg_registro_header
            );
        }
    }

    private void cargarSolicitudesRecibidas(int idRefugio) {
        if (binding == null) return;

        repo.obtenerSolicitudesRefugio(idRefugio,
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
                                        new SolicitudesRefugioAdapter(lista, (id, aprobada) ->
                                                Toast.makeText(requireContext(),
                                                        "Gestión — próximamente",
                                                        Toast.LENGTH_SHORT).show()));
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
        binding = null; // ← LA CLAVE
    }
}