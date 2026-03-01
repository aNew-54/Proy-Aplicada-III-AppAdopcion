package unc.edu.pe.appadopcion.ui.refugios;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.databinding.FragmentRefugiosBinding;
import unc.edu.pe.appadopcion.vm.refugios.RefugiosViewModel;

public class RefugiosFragment extends Fragment implements OnMapReadyCallback {

    private FragmentRefugiosBinding binding;
    private RefugiosViewModel viewModel;
    private SessionManager session;
    private RefugioAdapter adapter;
    private GoogleMap mMap;

    public RefugiosFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Como usas <layout> en XML, inflamos usando DataBindingUtil
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_refugios, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(RefugiosViewModel.class);

        configurarUI();
        configurarObservadores();

        // Inicializar el Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_refugios);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Cargar datos solo si no existen (evita que se vuelva a llamar a la API al girar el celular)
        if (viewModel.getRefugiosFiltrados().getValue() == null) {
            viewModel.cargarRefugios(session.getToken());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Al tocar la burbuja de un pin, abrir el detalle del refugio
        mMap.setOnInfoWindowClickListener(marker -> {
            RefugioResponse refugio = (RefugioResponse) marker.getTag();
            if (refugio != null) {
                abrirDetalleRefugio(refugio.idRefugio);
            }
        });

        // Si la lista ya había cargado, pintamos los pines
        if (viewModel.getRefugiosFiltrados().getValue() != null) {
            pintarPinesEnMapa(viewModel.getRefugiosFiltrados().getValue());
        }
    }

    private void configurarUI() {
        // Inicializar el Adapter usando ListAdapter (submitList) y configurar RecyclerView
        adapter = new RefugioAdapter(refugio -> abrirDetalleRefugio(refugio.idRefugio));
        binding.rvRefugios.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRefugios.setAdapter(adapter);

        // Controlar el botón segmentado (Lista vs Mapa)
        binding.toggleVistaRefugios.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_vista_lista) {
                    binding.rvRefugios.setVisibility(View.VISIBLE);
                    binding.contenedorMapaRefugios.setVisibility(View.GONE);
                } else if (checkedId == R.id.btn_vista_mapa) {
                    binding.rvRefugios.setVisibility(View.GONE);
                    binding.contenedorMapaRefugios.setVisibility(View.VISIBLE);
                }
            }
        });

        // Controlar la barra de búsqueda superior
        binding.etBusquedaRefugio.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filtrarPorNombre(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarObservadores() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;
            binding.progressRefugios.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (binding == null || error == null) return;
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

        // Observamos los refugios filtrados (que provee tu ViewModel actual)
        viewModel.getRefugiosFiltrados().observe(getViewLifecycleOwner(), refugios -> {
            if (binding == null || refugios == null) return;
            adapter.submitList(refugios); // Pasamos la lista al adaptador
            pintarPinesEnMapa(refugios);  // Pasamos la lista al mapa
        });
    }

    private void pintarPinesEnMapa(List<RefugioResponse> refugios) {
        if (mMap == null || refugios == null) return;
        mMap.clear();

        for (RefugioResponse r : refugios) {
            if (r.latitud != null && r.longitud != null) {
                LatLng ubicacion = new LatLng(r.latitud, r.longitud);
                mMap.addMarker(new MarkerOptions()
                                .position(ubicacion)
                                .title(r.nombre)
                                .snippet(r.mascotasDisponibles + " mascotas disponibles - ¡Toca aquí!"))
                        .setTag(r); // Guardamos la info del refugio en el pin
            }
        }

        // Mover la cámara de Google Maps hacia el primer refugio para no iniciar en el mar
        if (!refugios.isEmpty() && refugios.get(0).latitud != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(refugios.get(0).latitud, refugios.get(0).longitud), 12f));
        }
    }

    private void abrirDetalleRefugio(int idRefugio) {
        Intent intent = new Intent(requireContext(), DetalleRefugioActivity.class);
        intent.putExtra(DetalleRefugioActivity.EXTRA_ID_REFUGIO, idRefugio);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Regla del Null Check
    }
}