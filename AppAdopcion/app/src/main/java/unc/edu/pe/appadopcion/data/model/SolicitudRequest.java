package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class SolicitudRequest {
    @SerializedName("id_refugio")
    public Integer idRefugio;
    @SerializedName("id_mascota")
    public Integer idMascota;
    @SerializedName("id_adoptante")
    public Integer idAdoptante;
    @SerializedName("mensaje")
    public String  mensaje;
    @SerializedName("estadodesolicitud")
    public String  estado;
    @SerializedName("fechavisita")
    public String  fechaVisita;
    @SerializedName("notasrefugio")
    public String  notasRefugio;

    // Constructor para crear nueva solicitud
    public SolicitudRequest(int idRefugio, int idMascota, int idAdoptante, String mensaje) {
        this.idRefugio   = idRefugio;
        this.idMascota   = idMascota;
        this.idAdoptante = idAdoptante;
        this.mensaje     = mensaje;
    }

    // Constructor para actualizar estado
    public SolicitudRequest(String estado, String fechaVisita, String notas) {
        this.estado      = estado;
        this.fechaVisita = fechaVisita;
        this.notasRefugio = notas;
    }
}