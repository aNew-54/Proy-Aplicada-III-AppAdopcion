package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class FotoMascotaResponse {
    @SerializedName("id_fotomascota") public int idFoto;
    @SerializedName("id_mascota")     public int idMascota;
    @SerializedName("urlimagen")      public String urlImagen;
    @SerializedName("orden")          public int orden;
}