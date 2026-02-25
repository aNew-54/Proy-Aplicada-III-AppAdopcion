package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class VacunaResponse {
    @SerializedName("id_vacunabasica")
    public int id;
    @SerializedName("id_especie")
    public int idEspecie;
    @SerializedName("nombrevacuna")
    public String nombre;
}