package unc.edu.pe.appadopcion.ui.feed;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.databinding.FragmentDescubrirBinding;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.ui.mascotas.AgregarMascotaActivity;
import unc.edu.pe.appadopcion.vm.feed.DescubrirViewModel;
import unc.edu.pe.appadopcion.vm.feed.DescubrirViewModelFactory;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;

public class DescubrirFragment extends Fragment implements MascotaAdapter.OnMascotaClickListener {

    private FragmentDescubrirBinding binding;
    private DescubrirViewModel viewModel;
    private MascotaAdapter adapter;
    private SessionManager session;

    //Variables de filtrado
    private String especieSeleccionada = "";
    private String edadSeleccionada = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDescubrirBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        AppRepository repo = new AppRepository(session.getToken());

        DescubrirViewModelFactory factory = new DescubrirViewModelFactory(repo);
        viewModel = new ViewModelProvider(this, factory).get(DescubrirViewModel.class);

        setupRecyclerView();
        setupObservables();
        setupBuscador();
        setupFiltrosYOrden();

        if (session.esRefugio()) {
            binding.fbtnAgregarMascota.setVisibility(View.VISIBLE);
            binding.fbtnAgregarMascota.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AgregarMascotaActivity.class);
                startActivity(intent);
            });
        } else {
            binding.fbtnAgregarMascota.setVisibility(View.GONE);
        }

        viewModel.cargarMascotas();

        if (session.esAdoptante()) {
            viewModel.cargarMisFavoritos(session.getIdAdoptante());
        }
    }

    private void setupRecyclerView() {
        // CORRECCIÓN 1: Se agregó session.getToken() como segundo parámetro
        adapter = new MascotaAdapter(session.esAdoptante(), session.getToken(), this);
        binding.rvMascotas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMascotas.setAdapter(adapter);
    }

    private void setupObservables() {
        viewModel.getMascotasLiveData().observe(getViewLifecycleOwner(), mascotas -> {
            if (binding == null) return;
            adapter.setMascotas(mascotas);
            generarChipsDinamicos(mascotas);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Log.e("MI_APP_TEST", "ERROR DE RED: " + error);
        });
        viewModel.getMisFavoritosIds().observe(getViewLifecycleOwner(), idsFavoritos -> {
            if (adapter != null && idsFavoritos != null) {
                adapter.setFavoritosIniciales(idsFavoritos);
            }
        });
    }

    private void setupBuscador() {
        binding.etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.buscarPorNombre(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFiltrosYOrden() {
        //Mostrar/ocultar el panel
        binding.btnFiltrosAvanzados.setOnClickListener(v -> {
            boolean estaVisible = binding.layoutFiltrosAvanzados.getVisibility() == View.VISIBLE;
            binding.layoutFiltrosAvanzados.setVisibility(estaVisible ? View.GONE : View.VISIBLE);
            binding.btnFiltrosAvanzados.setText(estaVisible ? "Filtros ▼" : "Filtros ▲");
        });

        //RadioButtons (Fecha vs Popularidad)
        binding.rgTipoOrden.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOrdenFecha) {
                binding.rbDescendente.setText("Más recientes");
                binding.rbAscendente.setText("Más antiguos");
            } else {
                binding.rbDescendente.setText("Más populares");
                binding.rbAscendente.setText("Menos populares");
            }
            aplicarFiltrosYOrden();
        });

        binding.rgDireccionOrden.setOnCheckedChangeListener((group, checkedId) -> aplicarFiltrosYOrden());

        //Listener para el ChipGroup de Edad
        binding.chipGroupEdad.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                edadSeleccionada = "";
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                edadSeleccionada = chip.getText().toString();
            }
            aplicarFiltrosYOrden();
        });
    }

    private void generarChipsDinamicos(List<MascotaResponse> mascotas) {
        if (binding.chipGroupFiltros.getChildCount() > 0) return;

        Set<String> especiesUnicas = new HashSet<>();
        for (MascotaResponse m : mascotas) {
            if (m.nombreEspecie != null && !m.nombreEspecie.trim().isEmpty()) {
                String especie = m.nombreEspecie.substring(0, 1).toUpperCase() + m.nombreEspecie.substring(1).toLowerCase();
                especiesUnicas.add(especie);
            }
        }

        Chip chipTodas = new Chip(requireContext());
        chipTodas.setText("Todas");
        chipTodas.setCheckable(true);
        chipTodas.setChecked(true);
        chipTodas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                especieSeleccionada = "";
                aplicarFiltrosYOrden();
            }
        });
        binding.chipGroupFiltros.addView(chipTodas);

        for (String especie : especiesUnicas) {
            Chip chip = new Chip(requireContext());
            chip.setText(especie);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    especieSeleccionada = especie;
                    aplicarFiltrosYOrden();
                }
            });
            binding.chipGroupFiltros.addView(chip);
        }
    }

    private void aplicarFiltrosYOrden() {
        if (viewModel == null) return;

        boolean esPorFecha = binding.rbOrdenFecha.isChecked();
        boolean esAscendente = binding.rbAscendente.isChecked();

        String tipo = esPorFecha ? "Fecha" : "Favoritos";
        String dir;
        if (esPorFecha) {
            dir = esAscendente ? "Antiguos" : "Recientes";
        } else {
            dir = esAscendente ? "Menos populares" : "Más populares";
        }
        binding.tvOrdenActual.setText("Orden: " + tipo + " (" + dir + ")");

        //Mandar todo al viewmodel
        viewModel.ordenarYFiltrarLista(especieSeleccionada, edadSeleccionada, esPorFecha, esAscendente);
    }

    @Override
    public void onMascotaClick(MascotaResponse mascota) {
        // 1. Preparamos el fragmento destino
        unc.edu.pe.appadopcion.ui.mascotas.DetalleMascotaFragment detalleFragment =
                new unc.edu.pe.appadopcion.ui.mascotas.DetalleMascotaFragment();

        // 2. Empaquetamos la mascota
        Bundle bundle = new Bundle();
        bundle.putSerializable("mascota", mascota);
        bundle.putBoolean("ocultar_menu", false);
        detalleFragment.setArguments(bundle);

        // 3. Hacemos la transición
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, detalleFragment) // Asegúrate que fragmentContainer es el ID correcto de tu Activity principal
                .addToBackStack(null) // Para que el usuario pueda darle "Atrás"
                .commit();
    }

    @Override
    public void onRefugioClick(int idRefugio) {
        Toast.makeText(requireContext(), "Abrir Perfil de Refugio ID: " + idRefugio, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoritoClick(MascotaResponse mascota, int posicion, boolean fueAgregado) {
        // CORRECCIÓN 2: Le decimos al ViewModel que actualice la BD
        int idAdoptante = session.getIdAdoptante();
        if (idAdoptante != -1) {
            viewModel.toggleFavorito(mascota.idMascota, idAdoptante, fueAgregado);
        } else {
            Toast.makeText(requireContext(), "Error de sesión. No se puede guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dejamos el Toast para darle feedback visual al usuario
        if (fueAgregado) {
            Toast.makeText(requireContext(), mascota.nombre + " agregado a favoritos ❤️", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), mascota.nombre + " eliminado de favoritos 💔", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}