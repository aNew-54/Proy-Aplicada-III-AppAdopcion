package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class FavoritoRequest {
    @SerializedName("id_mascota")
    public int idMascota;
    @SerializedName("id_adoptante")
    public int idAdoptante;

    public FavoritoRequest(int idMascota, int idAdoptante) {
        this.idMascota   = idMascota;
        this.idAdoptante = idAdoptante;
    }
}