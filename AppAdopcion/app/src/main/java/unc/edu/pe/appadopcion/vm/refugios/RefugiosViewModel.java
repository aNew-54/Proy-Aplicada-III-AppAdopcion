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

public class RefugiosViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading       = new MutableLiveData<>(false);
    private final MutableLiveData<String>  errorMessage    = new MutableLiveData<>();
    private final MutableLiveData<List<RefugioResponse>> listaCompleta      = new MutableLiveData<>();
    private final MutableLiveData<List<RefugioResponse>> refugiosFiltrados  = new MutableLiveData<>();

    public LiveData<Boolean>             getIsLoading()        { return isLoading; }
    public LiveData<String>              getErrorMessage()     { return errorMessage; }
    public LiveData<List<RefugioResponse>> getRefugiosFiltrados() { return refugiosFiltrados; }

    public void cargarRefugios(String token) {
        isLoading.setValue(true);

        // Llamada directa a la API — sin AppRepository intermedio
        SupabaseApi api = SupabaseClient.getClient(token).create(SupabaseApi.class);

        api.obtenerTodosRefugios().enqueue(new Callback<List<RefugioResponse>>() {
            @Override
            public void onResponse(Call<List<RefugioResponse>> call,
                                   Response<List<RefugioResponse>> resp) {
                isLoading.postValue(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    listaCompleta.postValue(resp.body());
                    refugiosFiltrados.postValue(resp.body());
                } else {
                    errorMessage.postValue("Error " + resp.code() + ": no se cargaron refugios");
                }
            }
            @Override
            public void onFailure(Call<List<RefugioResponse>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Error de red: " + t.getMessage());
            }
        });
    }

    public void filtrarPorNombre(String query) {
        List<RefugioResponse> todos = listaCompleta.getValue();
        if (todos == null) return;
        if (query == null || query.trim().isEmpty()) {
            refugiosFiltrados.setValue(todos);
            return;
        }
        String q = query.toLowerCase().trim();
        List<RefugioResponse> resultado = new ArrayList<>();
        for (RefugioResponse r : todos) {
            if (r.nombre != null && r.nombre.toLowerCase().contains(q)) resultado.add(r);
        }
        refugiosFiltrados.setValue(resultado);
    }
}