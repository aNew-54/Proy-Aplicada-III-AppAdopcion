package unc.edu.pe.appadopcion.vm.mascotas;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.model.*;
import unc.edu.pe.appadopcion.data.repository.AppRepository;

public class DetalleMascotaViewModel extends ViewModel {
    private final AppRepository repository;

    private final MutableLiveData<MascotaResponse> mascota = new MutableLiveData<>();
    private final MutableLiveData<List<FotoMascotaResponse>> fotosGaleria = new MutableLiveData<>();
    private final MutableLiveData<List<IntervencionResponse>> intervenciones = new MutableLiveData<>();
    private final MutableLiveData<Boolean> esFavorito = new MutableLiveData<>();
    private final MutableLiveData<List<VacunaUI>> listaVacunasUI = new MutableLiveData<>();
    public LiveData<List<VacunaUI>> getListaVacunasUI() { return listaVacunasUI; }

    public DetalleMascotaViewModel(AppRepository repository) {
        this.repository = repository;
    }

    public LiveData<MascotaResponse> getMascota() { return mascota; }
    public LiveData<List<FotoMascotaResponse>> getFotosGaleria() { return fotosGaleria; }
    public LiveData<List<IntervencionResponse>> getIntervenciones() { return intervenciones; }
    public LiveData<Boolean> getEsFavorito() { return esFavorito; }

    public void cargarDatosMascota(MascotaResponse dataSeleccionada, int idAdoptanteActual) {
        mascota.setValue(dataSeleccionada);
        refrescarTodo(dataSeleccionada.idMascota, dataSeleccionada.idEspecie, idAdoptanteActual);
    }

    /**
     * Refresca toda la información desde el servidor (útil tras una edición)
     */
    public void refrescarDatos(int idMascota, int idAdoptanteActual) {
        repository.obtenerMascota(idMascota, new Callback<List<MascotaResponse>>() {
            @Override
            public void onResponse(Call<List<MascotaResponse>> call, Response<List<MascotaResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    MascotaResponse m = response.body().get(0);
                    mascota.setValue(m);
                    refrescarTodo(m.idMascota, m.idEspecie, idAdoptanteActual);
                }
            }
            @Override
            public void onFailure(Call<List<MascotaResponse>> call, Throwable t) {}
        });
    }

    private void refrescarTodo(int idMascota, int idEspecie, int idAdoptanteActual) {
        cargarGaleria(idMascota);
        cargarIntervenciones(idMascota);
        cargarVacunas(idEspecie, idMascota);
        if (idAdoptanteActual != -1) {
            verificarFavorito(idMascota, idAdoptanteActual);
        }
    }

    private void cargarGaleria(int idMascota) {
        repository.obtenerFotosMascota(idMascota, new Callback<List<FotoMascotaResponse>>() {
            @Override
            public void onResponse(Call<List<FotoMascotaResponse>> call, Response<List<FotoMascotaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fotosGaleria.setValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<FotoMascotaResponse>> call, Throwable t) {}
        });
    }

    private void cargarIntervenciones(int idMascota) {
        repository.obtenerIntervencionesMascota(idMascota, new Callback<List<IntervencionResponse>>() {
            @Override
            public void onResponse(Call<List<IntervencionResponse>> call, Response<List<IntervencionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    intervenciones.setValue(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<IntervencionResponse>> call, Throwable t) {}
        });
    }

    private void verificarFavorito(int idMascota, int idAdoptante) {
        repository.verificarEsFavorito(idMascota, idAdoptante, new Callback<List<FavoritoResponse>>() {
            @Override
            public void onResponse(Call<List<FavoritoResponse>> call, Response<List<FavoritoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    esFavorito.setValue(!response.body().isEmpty());
                }
            }
            @Override
            public void onFailure(Call<List<FavoritoResponse>> call, Throwable t) {}
        });
    }

    public void toggleFavorito(int idAdoptante) {
        if (mascota.getValue() == null) return;
        int idMascota = mascota.getValue().idMascota;
        boolean actual = esFavorito.getValue() != null ? esFavorito.getValue() : false;

        if (actual) {
            repository.eliminarFavorito(idMascota, idAdoptante, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) esFavorito.setValue(false);
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        } else {
            repository.agregarFavorito(idMascota, idAdoptante, new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) esFavorito.setValue(true);
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {}
            });
        }
    }

    private void cargarVacunas(int idEspecie, int idMascota) {
        repository.obtenerVacunasPorEspecie(idEspecie, new Callback<List<VacunaResponse>>() {
            @Override
            public void onResponse(Call<List<VacunaResponse>> call, Response<List<VacunaResponse>> respEspecie) {
                if (respEspecie.isSuccessful() && respEspecie.body() != null) {
                    List<VacunaResponse> todasLasVacunas = respEspecie.body();

                    repository.obtenerVacunasMascota(idMascota, new Callback<List<VacunaMascotaResponse>>() {
                        @Override
                        public void onResponse(Call<List<VacunaMascotaResponse>> call, Response<List<VacunaMascotaResponse>> respMascota) {
                            List<VacunaMascotaResponse> aplicadas = respMascota.isSuccessful() && respMascota.body() != null
                                    ? respMascota.body() : new java.util.ArrayList<>();

                            List<VacunaUI> resultado = new java.util.ArrayList<>();
                            for (VacunaResponse v : todasLasVacunas) {
                                boolean tieneVacuna = false;
                                String fechaAplicada = null;
                                for (VacunaMascotaResponse aplic : aplicadas) {
                                    if (aplic.idVacuna == v.id) {
                                        tieneVacuna = true;
                                        fechaAplicada = aplic.fechaAplicacion;
                                        break;
                                    }
                                }
                                resultado.add(new VacunaUI(v.nombre, tieneVacuna, fechaAplicada));
                            }
                            listaVacunasUI.setValue(resultado);
                        }
                        @Override
                        public void onFailure(Call<List<VacunaMascotaResponse>> call, Throwable t) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<List<VacunaResponse>> call, Throwable t) {}
        });
    }

    public static class VacunaUI {
        public String nombre;
        public boolean aplicada;
        public String fecha;
        public VacunaUI(String n, boolean a, String f) { this.nombre = n; this.aplicada = a; this.fecha = f;}
    }

}