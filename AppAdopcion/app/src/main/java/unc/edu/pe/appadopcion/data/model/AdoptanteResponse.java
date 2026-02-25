package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class AdoptanteResponse {
    @SerializedName("id_adoptante")
    public int idAdoptante;
    @SerializedName("nombre")
    public String nombre;
    @SerializedName("apellido")
    public String apellido;
    @SerializedName("genero")
    public String genero;
    @SerializedName("fechanacimiento")
    public String fechaNacimiento;
    @SerializedName("id_usuario")
    public String idUsuario;
    @SerializedName("correo")
    public String correo;
    @SerializedName("telefono")
    public String telefono;

    // --- NUEVOS CAMPOS ---
    @SerializedName("direccion")
    public String direccion;
    @SerializedName("latitud")
    public Double latitud;
    @SerializedName("longitud")
    public Double longitud;
    // ---------------------

    @SerializedName("fotoperfil")
    public String fotoPerfil;
    @SerializedName("fecharegistro")
    public String fechaRegistro;
    @SerializedName("totalfavoritos")
    public int totalFavoritos;
    @SerializedName("totalsolicitudes")
    public int totalSolicitudes;
}