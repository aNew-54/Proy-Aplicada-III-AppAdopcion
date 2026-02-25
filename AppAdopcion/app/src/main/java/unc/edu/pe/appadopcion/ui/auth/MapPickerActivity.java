package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.databinding.ActivityMapPickerBinding;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Claves para el Intent de retorno
    public static final String EXTRA_LATITUD    = "EXTRA_LATITUD";
    public static final String EXTRA_LONGITUD   = "EXTRA_LONGITUD";
    public static final String EXTRA_DIRECCION  = "EXTRA_DIRECCION";

    private ActivityMapPickerBinding binding;
    private GoogleMap mMap;

    private LatLng ubicacionSeleccionada = null;
    private String direccionObtenida     = null;

    // Posición inicial: Cajamarca, Perú
    private static final LatLng POSICION_INICIAL = new LatLng(-7.1638, -78.5003);
    private static final float  ZOOM_INICIAL     = 13f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnCancelar.setOnClickListener(v -> finish());

        binding.btnConfirmar.setOnClickListener(v -> {
            if (ubicacionSeleccionada == null) {
                Toast.makeText(this, "Toca el mapa para seleccionar una ubicación",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent result = new Intent();
            result.putExtra(EXTRA_LATITUD,   ubicacionSeleccionada.latitude);
            result.putExtra(EXTRA_LONGITUD,  ubicacionSeleccionada.longitude);
            result.putExtra(EXTRA_DIRECCION, direccionObtenida != null
                    ? direccionObtenida : "Lat: " + ubicacionSeleccionada.latitude
                    + ", Lng: " + ubicacionSeleccionada.longitude);
            setResult(RESULT_OK, result);
            finish();
        });

        // Barra de búsqueda manual (opcional, busca con Geocoder)
        binding.btnBuscar.setOnClickListener(v -> {
            String texto = binding.etBuscar.getText().toString().trim();
            if (!texto.isEmpty()) buscarDireccion(texto);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Mover cámara a posición inicial
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POSICION_INICIAL, ZOOM_INICIAL));

        // Estilo del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Al tocar el mapa → colocar marcador y obtener dirección
        mMap.setOnMapClickListener(latLng -> {
            ubicacionSeleccionada = latLng;

            // Limpiar marcadores anteriores y poner el nuevo
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Ubicación del refugio"));

            // Mostrar coordenadas mientras se obtiene la dirección
            binding.tvDireccionSeleccionada.setText(
                    String.format(Locale.getDefault(),
                            "%.6f, %.6f", latLng.latitude, latLng.longitude));
            binding.tvDireccionSeleccionada.setVisibility(View.VISIBLE);
            binding.btnConfirmar.setEnabled(false);

            // Obtener dirección en hilo de fondo (Geocoder puede ser lento)
            obtenerDireccionAsync(latLng);
        });
    }

    private void obtenerDireccionAsync(LatLng latLng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
                List<Address> addresses = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        StringBuilder sb = new StringBuilder();

                        // Construir dirección legible
                        if (address.getThoroughfare() != null)
                            sb.append(address.getThoroughfare());
                        if (address.getSubThoroughfare() != null)
                            sb.append(" ").append(address.getSubThoroughfare());
                        if (address.getLocality() != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(address.getLocality());
                        }
                        if (address.getAdminArea() != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(address.getAdminArea());
                        }

                        direccionObtenida = sb.length() > 0
                                ? sb.toString()
                                : address.getAddressLine(0);

                        binding.tvDireccionSeleccionada.setText(direccionObtenida);
                    } else {
                        direccionObtenida = String.format(Locale.getDefault(),
                                "%.5f, %.5f", latLng.latitude, latLng.longitude);
                        binding.tvDireccionSeleccionada.setText(
                                "Coordenadas: " + direccionObtenida);
                    }
                    binding.btnConfirmar.setEnabled(true);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    direccionObtenida = String.format(Locale.getDefault(),
                            "%.5f, %.5f", latLng.latitude, latLng.longitude);
                    binding.tvDireccionSeleccionada.setText("Coordenadas: " + direccionObtenida);
                    binding.btnConfirmar.setEnabled(true);
                });
            }
        }).start();
    }

    private void buscarDireccion(String texto) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
                List<Address> addresses = geocoder.getFromLocationName(texto, 1);
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                        // Simular tap en esa posición
                        ubicacionSeleccionada = latLng;
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng));
                        direccionObtenida = address.getAddressLine(0);
                        binding.tvDireccionSeleccionada.setText(direccionObtenida);
                        binding.tvDireccionSeleccionada.setVisibility(View.VISIBLE);
                        binding.btnConfirmar.setEnabled(true);
                    } else {
                        Toast.makeText(this, "No se encontró esa dirección", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al buscar dirección", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}