package unc.edu.pe.appadopcion.ui.solicitudes;

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

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.FragmentSolicitudesBinding;
import unc.edu.pe.appadopcion.ui.solicitudes.adapters.SolicitudesAdapter;
import unc.edu.pe.appadopcion.vm.solicitudes.SolicitudesListViewModel;

public class SolicitudesFragment extends Fragment {

    private FragmentSolicitudesBinding binding;
    private SolicitudesListViewModel viewModel;
    private SolicitudesAdapter adapter;
    private SessionManager session;
    private AppRepository repo;

    private List<SolicitudResponse> listaOriginal = new ArrayList<>();

    // Escucha cuando volvemos de la pantalla de Detalle (por si hubo edición)
    private final ActivityResultLauncher<Intent> detalleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarDatos(); // Refresca la lista
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSolicitudesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        repo = new AppRepository(session.getToken());
        viewModel = new ViewModelProvider(this).get(SolicitudesListViewModel.class);

        configurarRecyclerView();
        configurarFiltros();
        configurarObservadores();

        if (viewModel.getSolicitudes().getValue() == null) {
            cargarDatos();
        }
    }

    private void configurarFiltros() {
        binding.cgFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || listaOriginal.isEmpty()) return;
            int id = checkedIds.get(0);

            if (id == R.id.chipTodas) {
                adapter.setLista(listaOriginal);
            } else if (id == R.id.chipPendiente) {
                filtrarLista("Pendiente");
            } else if (id == R.id.chipAprobada) {
                filtrarLista("Aprobada");
            }
        });
    }

    // AHORA ESTE MÉTODO ESTÁ AFUERA DE onViewCreated (¡Donde debe estar!)
    private void filtrarLista(String estado) {
        List<SolicitudResponse> filtrada = new ArrayList<>();
        for (SolicitudResponse s : listaOriginal) {
            if (s.estado.equals(estado)) filtrada.add(s);
        }
        adapter.setLista(filtrada);
    }

    private void configurarRecyclerView() {
        // Obtenemos si es refugio desde la sesión
        boolean esRefugio = session.esRefugio();

        // ¡Aquí le pasamos el boolean 'esRefugio' como segundo parámetro!
        adapter = new SolicitudesAdapter(new ArrayList<>(), esRefugio, solicitud -> {
            Intent intent = new Intent(requireContext(), DetalleSolicitudActivity.class);
            intent.putExtra("solicitud", solicitud);
            detalleLauncher.launch(intent); // Abre la actividad de detalle
        });

        binding.rvSolicitudes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSolicitudes.setAdapter(adapter);
    }

    private void configurarObservadores() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding != null) binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getSolicitudes().observe(getViewLifecycleOwner(), lista -> {
            if (binding == null) return;

            if (lista == null || lista.isEmpty()) {
                binding.layoutVacio.setVisibility(View.VISIBLE);
                binding.rvSolicitudes.setVisibility(View.GONE);
            } else {
                binding.layoutVacio.setVisibility(View.GONE);
                binding.rvSolicitudes.setVisibility(View.VISIBLE);
                listaOriginal = lista; // Guardamos la lista original para los filtros
                adapter.setLista(lista); // Mostramos todas por defecto
                binding.cgFiltros.check(R.id.chipTodas); // Reiniciamos el filtro a "Todas"
            }
        });
    }

    private void cargarDatos() {
        boolean esRefugio = session.esRefugio();
        int idUsuarioInterno = esRefugio ? session.getIdRefugio() : session.getIdAdoptante();

        if (idUsuarioInterno != -1) {
            viewModel.cargarSolicitudes(repo, esRefugio, idUsuarioInterno);
        } else {
            Toast.makeText(requireContext(), "Error de sesión", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}