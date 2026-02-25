package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class SolicitudResponse {
    @SerializedName("id_solicitud")
    public int idSolicitud;
    @SerializedName("id_refugio")
    public int idRefugio;
    @SerializedName("id_mascota")
    public int idMascota;
    @SerializedName("id_adoptante")
    public int idAdoptante;
    @SerializedName("mensaje")
    public String mensaje;
    @SerializedName("fechadesolicitud")
    public String fechaSolicitud;
    @SerializedName("estadodesolicitud")
    public String estado;
    @SerializedName("fechavisita")
    public String fechaVisita;
    @SerializedName("notasrefugio")
    public String notasRefugio;
}