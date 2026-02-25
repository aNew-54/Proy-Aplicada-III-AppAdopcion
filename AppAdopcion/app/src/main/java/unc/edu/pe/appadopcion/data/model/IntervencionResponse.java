package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class IntervencionResponse {
    @SerializedName("id_intervencionmedica")
    public int id;
    @SerializedName("id_mascota")
    public int idMascota;
    @SerializedName("titulointervencion")
    public String titulo;
    @SerializedName("descripcionintervencion")
    public String descripcion;
    @SerializedName("fechaintervencion")
    public String fecha;
}