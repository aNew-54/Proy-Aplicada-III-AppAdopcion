package unc.edu.pe.appadopcion.vm.auth;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerViewModel extends ViewModel {

    private final MutableLiveData<String> direccionResultado = new MutableLiveData<>();
    private final MutableLiveData<LatLng> coordenadaResultado = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<String> getDireccionResultado() { return direccionResultado; }
    public LiveData<LatLng> getCoordenadaResultado() { return coordenadaResultado; }
    public LiveData<String> getError() { return error; }

    public void obtenerDireccionPorCoordenadas(Context context, LatLng latLng) {
        new Thread(() -> {
            try {
                // Configurado para buscar en Perú (puedes ajustarlo si la app es internacional)
                Geocoder geocoder = new Geocoder(context, new Locale("es", "PE"));
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (address.getThoroughfare() != null) sb.append(address.getThoroughfare());
                    if (address.getSubThoroughfare() != null) sb.append(" ").append(address.getSubThoroughfare());
                    if (address.getLocality() != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(address.getLocality());
                    }
                    if (address.getAdminArea() != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(address.getAdminArea());
                    }

                    String dir = sb.length() > 0 ? sb.toString() : address.getAddressLine(0);
                    direccionResultado.postValue(dir);
                } else {
                    direccionResultado.postValue(String.format(Locale.getDefault(), "%.5f, %.5f", latLng.latitude, latLng.longitude));
                }
            } catch (IOException e) {
                // Si falla el Geocoder, devolvemos las coordenadas como texto
                direccionResultado.postValue(String.format(Locale.getDefault(), "%.5f, %.5f", latLng.latitude, latLng.longitude));
            }
        }).start();
    }

    public void buscarPorTexto(Context context, String texto) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, new Locale("es", "PE"));
                List<Address> addresses = geocoder.getFromLocationName(texto, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    // Publicamos primero la coordenada para mover el mapa
                    coordenadaResultado.postValue(latLng);
                    // Y luego el texto de la dirección encontrada
                    direccionResultado.postValue(address.getAddressLine(0));
                } else {
                    error.postValue("No se encontró esa dirección");
                }
            } catch (IOException e) {
                error.postValue("Error al buscar dirección de red");
            }
        }).start();
    }
}