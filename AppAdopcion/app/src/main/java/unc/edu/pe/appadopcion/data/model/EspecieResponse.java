package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class EspecieResponse {
    @SerializedName("id_especie")
    public int    idEspecie;
    @SerializedName("nombreespecie")
    public String nombre;
}