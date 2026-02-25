package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class VacunaMascotaResponse {
    @SerializedName("id_vacunasmascota") public int    id;
    @SerializedName("id_mascota")        public int    idMascota;
    @SerializedName("id_vacunabasica")   public int    idVacuna;
    @SerializedName("fechaaplicacion")   public String fechaAplicacion;
}