package unc.edu.pe.appadopcion.ui.perfil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.databinding.FragmentMisMascotasBinding;
import unc.edu.pe.appadopcion.ui.perfil.adapters.MascotaRefugioAdapter;
import unc.edu.pe.appadopcion.vm.perfil.MisMascotasViewModel;
import unc.edu.pe.appadopcion.ui.mascotas.DetalleMascotaFragment;
import unc.edu.pe.appadopcion.R;

public class MisMascotasFragment extends Fragment {

    private MisMascotasViewModel viewModel;
    private MascotaRefugioAdapter adapter;
    private FragmentMisMascotasBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMisMascotasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();

        SessionManager session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(MisMascotasViewModel.class);

        int idRefugio = -1;
        if (getArguments() != null) {
            idRefugio = getArguments().getInt("id_refugio", -1);
        }

        if (idRefugio == -1) {
            Toast.makeText(requireContext(), "Error: sin ID de refugio", Toast.LENGTH_SHORT).show();
            return;
        }

        setupFiltros(); // Llamamos al nuevo método
        setupObservers();

        if (viewModel.getMascotas().getValue() == null) {
            viewModel.cargarMascotas(idRefugio, session.getToken());
        }
    }

    private void setupToolbar() {

        // Configurar toolbar como ActionBar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(binding.toolbar);

        // Quitar título por defecto (Collapsing lo maneja)
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle("");
        }

        // Título del Collapsing
        binding.collapsingToolbar.setTitle("Mis Mascotas");
    }

    private void setupFiltros() {
        // Configurar Desplegable de Estado (Valores fijos)
        String[] estados = {"Todos", "Disponible", "En Proceso", "Adoptado"};
        android.widget.ArrayAdapter<String> estadoAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, estados);
        binding.actvEstadoFiltro.setAdapter(estadoAdapter);
        binding.actvEstadoFiltro.setText("Todos", false); // Opción por defecto

        binding.actvEstadoFiltro.setOnItemClickListener((parent, view, position, id) -> {
            viewModel.setFiltroEstado(estados[position]);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // Observador para llenar dinámicamente el Desplegable de Especie
        viewModel.getEspeciesDisponibles().observe(getViewLifecycleOwner(), especies -> {
            if (especies != null) {
                android.widget.ArrayAdapter<String> especieAdapter = new android.widget.ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_dropdown_item_1line, especies);

                // USAMOS EL NUEVO ID DEL XML
                binding.actvEspecieFiltro.setAdapter(especieAdapter);
                binding.actvEspecieFiltro.setText("Todas", false); // Opción por defecto

                binding.actvEspecieFiltro.setOnItemClickListener((parent, view, position, id) -> {
                    viewModel.setFiltroEspecie(especies.get(position));
                });
            }
        });

        // Observador de la lista de mascotas filtrada
        viewModel.getMascotas().observe(getViewLifecycleOwner(), mascotas -> {
            if (mascotas == null || mascotas.isEmpty()) {
                binding.tvSinMascotas.setVisibility(View.VISIBLE);
                binding.rvMisMascotas.setVisibility(View.GONE);
            } else {
                binding.tvSinMascotas.setVisibility(View.GONE);
                binding.rvMisMascotas.setVisibility(View.VISIBLE);

                adapter = new MascotaRefugioAdapter(mascotas, mascota -> {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mascota", mascota);
                    DetalleMascotaFragment detalleFragment = new DetalleMascotaFragment();
                    detalleFragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, detalleFragment)
                            .addToBackStack(null)
                            .commit();
                });

                binding.rvMisMascotas.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.rvMisMascotas.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}