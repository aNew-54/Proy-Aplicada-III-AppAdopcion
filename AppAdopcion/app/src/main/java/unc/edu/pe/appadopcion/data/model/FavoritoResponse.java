package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class FavoritoResponse {
    @SerializedName("id_favorito")
    public int idFavorito;
    @SerializedName("id_mascota")
    public int idMascota;
    @SerializedName("id_adoptante")
    public int idAdoptante;
    @SerializedName("fechaagregado")
    public String fechaAgregado;
}