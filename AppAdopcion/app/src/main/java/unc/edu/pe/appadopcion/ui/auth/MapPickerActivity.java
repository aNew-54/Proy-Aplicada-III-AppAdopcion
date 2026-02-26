package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.databinding.ActivityMapPickerBinding;
import unc.edu.pe.appadopcion.vm.auth.MapPickerViewModel;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUD    = "EXTRA_LATITUD";
    public static final String EXTRA_LONGITUD   = "EXTRA_LONGITUD";
    public static final String EXTRA_DIRECCION  = "EXTRA_DIRECCION";

    private ActivityMapPickerBinding binding;
    private MapPickerViewModel viewModel;
    private GoogleMap mMap;

    private LatLng ubicacionSeleccionada = null;
    private String direccionObtenida     = null;

    private static final LatLng POSICION_INICIAL = new LatLng(-7.1638, -78.5003); // Cajamarca
    private static final float  ZOOM_INICIAL     = 13f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MapPickerViewModel.class);
        configurarObservadores();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnCancelar.setOnClickListener(v -> finish());

        binding.btnConfirmar.setOnClickListener(v -> confirmarSeleccion());

        binding.btnBuscar.setOnClickListener(v -> {
            String texto = binding.etBuscar.getText().toString().trim();
            if (!texto.isEmpty()) {
                viewModel.buscarPorTexto(this, texto);
            }
        });
    }

    private void configurarObservadores() {
        // Observamos el resultado del Geocoding (Texto a partir de un click en el mapa)
        viewModel.getDireccionResultado().observe(this, direccion -> {
            if (direccion != null) {
                direccionObtenida = direccion;
                binding.tvDireccionSeleccionada.setText(direccion);
                binding.btnConfirmar.setEnabled(true);
            }
        });

        // Observamos el resultado de una búsqueda manual (Coordenadas a partir de texto)
        viewModel.getCoordenadaResultado().observe(this, latLng -> {
            if (latLng != null && mMap != null) {
                ubicacionSeleccionada = latLng;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                colocarMarcador(latLng);
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POSICION_INICIAL, ZOOM_INICIAL));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapClickListener(latLng -> {
            ubicacionSeleccionada = latLng;
            colocarMarcador(latLng);

            binding.tvDireccionSeleccionada.setText(
                    String.format(Locale.getDefault(), "%.6f, %.6f", latLng.latitude, latLng.longitude));
            binding.tvDireccionSeleccionada.setVisibility(View.VISIBLE);
            binding.btnConfirmar.setEnabled(false);

            // Delegamos la búsqueda pesada al ViewModel
            viewModel.obtenerDireccionPorCoordenadas(this, latLng);
        });
    }

    private void colocarMarcador(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
        binding.tvDireccionSeleccionada.setVisibility(View.VISIBLE);
    }

    private void confirmarSeleccion() {
        if (ubicacionSeleccionada == null) {
            Toast.makeText(this, "Toca el mapa para seleccionar una ubicación", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent result = new Intent();
        result.putExtra(EXTRA_LATITUD, ubicacionSeleccionada.latitude);
        result.putExtra(EXTRA_LONGITUD, ubicacionSeleccionada.longitude);
        result.putExtra(EXTRA_DIRECCION, direccionObtenida != null
                ? direccionObtenida : "Lat: " + ubicacionSeleccionada.latitude + ", Lng: " + ubicacionSeleccionada.longitude);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}