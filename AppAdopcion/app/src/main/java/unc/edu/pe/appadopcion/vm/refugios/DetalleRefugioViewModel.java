package unc.edu.pe.appadopcion.vm.refugios;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.api.SupabaseApi;
import unc.edu.pe.appadopcion.data.api.SupabaseClient;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;

public class DetalleRefugioViewModel extends ViewModel {

    private final MutableLiveData<Boolean>             isLoading    = new MutableLiveData<>(false);
    private final MutableLiveData<String>              errorMessage = new MutableLiveData<>();
    private final MutableLiveData<RefugioResponse>     refugio      = new MutableLiveData<>();
    private final MutableLiveData<List<MascotaResponse>> mascotas   = new MutableLiveData<>();

    public LiveData<Boolean>               getIsLoading()    { return isLoading; }
    public LiveData<String>                getErrorMessage() { return errorMessage; }
    public LiveData<RefugioResponse>       getRefugio()      { return refugio; }
    public LiveData<List<MascotaResponse>> getMascotas()     { return mascotas; }

    public void cargarDetalle(int idRefugio, String token) {
        isLoading.setValue(true);
        SupabaseApi api = SupabaseClient.getClient(token).create(SupabaseApi.class);

        // 1. Cargar datos del refugio
        api.obtenerRefugioPorId("eq." + idRefugio).enqueue(new Callback<List<RefugioResponse>>() {
            @Override
            public void onResponse(Call<List<RefugioResponse>> call,
                                   Response<List<RefugioResponse>> resp) {
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    refugio.postValue(resp.body().get(0));
                    // 2. Encadenar: cargar mascotas del refugio
                    api.obtenerMascotasDeRefugio("eq." + idRefugio, "fecharegistro.desc")
                            .enqueue(new Callback<List<MascotaResponse>>() {
                                @Override
                                public void onResponse(Call<List<MascotaResponse>> c,
                                                       Response<List<MascotaResponse>> r) {
                                    isLoading.postValue(false);
                                    mascotas.postValue(
                                            r.isSuccessful() && r.body() != null
                                                    ? r.body() : new ArrayList<>());
                                }
                                @Override
                                public void onFailure(Call<List<MascotaResponse>> c, Throwable t) {
                                    isLoading.postValue(false);
                                    mascotas.postValue(new ArrayList<>());
                                }
                            });
                } else {
                    isLoading.postValue(false);
                    errorMessage.postValue("Refugio no encontrado");
                }
            }
            @Override
            public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Error de red: " + t.getMessage());
            }
        });
    }
}