package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class RazaResponse {
    @SerializedName("id_raza")
    public int idRaza;
    @SerializedName("id_especie")
    public int idEspecie;
    @SerializedName("nombreraza")
    public String nombre;
}