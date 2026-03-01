package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class VacunaResponse implements Serializable {
    @SerializedName("id_vacunabasica")
    public int id;
    @SerializedName("id_especie")
    public int idEspecie;
    @SerializedName("nombrevacuna")
    public String nombre;

    /** Campo local para manejar la fecha en la UI y el guardado */
    public String fechaAplicacion;
}