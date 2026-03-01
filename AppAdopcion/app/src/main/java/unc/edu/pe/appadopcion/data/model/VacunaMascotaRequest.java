package unc.edu.pe.appadopcion.data.model;

import com.google.gson.annotations.SerializedName;

public class VacunaMascotaRequest {
    @SerializedName("id_mascota")
    public int idMascota;

    @SerializedName("id_vacunabasica")
    public int idVacuna;

    @SerializedName("fechaaplicacion")
    public String fechaAplicacion; // formato: yyyy-MM-dd (ISO para Supabase)

    public VacunaMascotaRequest(int idMascota, int idVacuna, String fechaAplicacion) {
        this.idMascota       = idMascota;
        this.idVacuna        = idVacuna;
        this.fechaAplicacion = fechaAplicacion;
    }
}