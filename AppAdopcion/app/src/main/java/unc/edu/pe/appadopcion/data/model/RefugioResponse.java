package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class RefugioResponse {
    @SerializedName("id_refugio")
    public int idRefugio;
    @SerializedName("nombre")
    public String nombre;
    @SerializedName("direccion")
    public String direccion;
    @SerializedName("latitud")
    public Double latitud;
    @SerializedName("longitud")
    public Double longitud;
    @SerializedName("descripcion")
    public String descripcion;
    @SerializedName("urlportada")
    public String urlPortada;
    @SerializedName("telefono")
    public String telefono;
    @SerializedName("correo")
    public String correo;
    @SerializedName("fotoperfil")
    public String fotoPerfil;
    @SerializedName("id_usuario")
    public String idUsuario;
    @SerializedName("mascotasdisponibles")
    public int mascotasDisponibles;
}