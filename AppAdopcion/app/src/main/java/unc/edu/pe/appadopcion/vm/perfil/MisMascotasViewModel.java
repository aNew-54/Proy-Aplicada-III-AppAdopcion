package unc.edu.pe.appadopcion.vm.perfil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class MisMascotasViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Lista expuesta a la vista (filtrada)
    private final MutableLiveData<List<MascotaResponse>> mascotas = new MutableLiveData<>();
    // Lista de especies disponibles para el filtro
    private final MutableLiveData<List<String>> especiesDisponibles = new MutableLiveData<>();

    // Memoria interna
    private List<MascotaResponse> listaOriginal = new ArrayList<>();
    private String filtroEspecieActual = "Todas";
    private String filtroEstadoActual = "Todos";

    public LiveData<Boolean> getIsLoading()          { return isLoading; }
    public LiveData<String> getErrorMessage()        { return errorMessage; }
    public LiveData<List<MascotaResponse>> getMascotas() { return mascotas; }
    public LiveData<List<String>> getEspeciesDisponibles()  { return especiesDisponibles; }

    public void cargarMascotas(int idRefugio, String token) {
        isLoading.setValue(true);
        AppRepository repo = new AppRepository(token);

        repo.obtenerMascotasDeRefugio(idRefugio, "fecharegistro.desc",
                new Callback<List<MascotaResponse>>() {
                    @Override
                    public void onResponse(Call<List<MascotaResponse>> call, Response<List<MascotaResponse>> resp) {
                        isLoading.setValue(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            listaOriginal = resp.body();
                            extraerEspeciesDisponibles(listaOriginal);
                            aplicarFiltros(); // Inicializa la lista mostrada
                        } else {
                            errorMessage.setValue("No se pudieron cargar las mascotas");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MascotaResponse>> call, Throwable t) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error de red: " + t.getMessage());
                    }
                });
    }

    private void extraerEspeciesDisponibles(List<MascotaResponse> lista) {
        Set<String> especiesUnicas = new HashSet<>();
        especiesUnicas.add("Todas"); // Opción por defecto

        for (MascotaResponse m : lista) {
            if (m.nombreEspecie != null && !m.nombreEspecie.isEmpty()) {
                especiesUnicas.add(m.nombreEspecie);
            }
        }

        List<String> listaOrdenada = new ArrayList<>(especiesUnicas);
        Collections.sort(listaOrdenada); // Orden alfabético
        listaOrdenada.remove("Todas");
        listaOrdenada.add(0, "Todas"); // Asegurar que "Todas" quede primero

        especiesDisponibles.setValue(listaOrdenada);
    }

    public void setFiltroEspecie(String especie) {
        this.filtroEspecieActual = especie;
        aplicarFiltros();
    }

    public void setFiltroEstado(String estado) {
        this.filtroEstadoActual = estado;
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (listaOriginal == null || listaOriginal.isEmpty()) {
            mascotas.setValue(new ArrayList<>());
            return;
        }

        List<MascotaResponse> listaFiltrada = new ArrayList<>();
        for (MascotaResponse m : listaOriginal) {
            // EVALUAMOS LA ESPECIE
            boolean pasaEspecie = filtroEspecieActual.equals("Todas") || (m.nombreEspecie != null && m.nombreEspecie.equals(filtroEspecieActual));
            boolean pasaEstado = filtroEstadoActual.equals("Todos") || (m.estado != null && m.estado.equals(filtroEstadoActual));

            if (pasaEspecie && pasaEstado) {
                listaFiltrada.add(m);
            }
        }
        mascotas.setValue(listaFiltrada);
    }
}