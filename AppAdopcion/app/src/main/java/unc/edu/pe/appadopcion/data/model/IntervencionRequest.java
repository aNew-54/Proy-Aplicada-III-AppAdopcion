package unc.edu.pe.appadopcion.data.model;

import com.google.gson.annotations.SerializedName;

public class IntervencionRequest {
    @SerializedName("id_mascota")
    public int idMascota;

    @SerializedName("titulointervencion")
    public String titulo;

    @SerializedName("descripcionintervencion")
    public String descripcion;

    @SerializedName("fechaintervencion")
    public String fecha;

    public IntervencionRequest(int idMascota, String titulo, String descripcion, String fecha) {
        this.idMascota    = idMascota;
        this.titulo       = titulo;
        this.descripcion  = descripcion;
        this.fecha        = fecha;
    }
}
