package unc.edu.pe.appadopcion.data.model;

import com.google.gson.annotations.SerializedName;

public class FotoMascotaRequest {
    @SerializedName("id_mascota")
    public int idMascota;

    @SerializedName("urlimagen")
    public String urlImagen;

    @SerializedName("orden")
    public int orden;

    public FotoMascotaRequest(int idMascota, String urlImagen, int orden) {
        this.idMascota = idMascota;
        this.urlImagen = urlImagen;
        this.orden     = orden;
    }
}