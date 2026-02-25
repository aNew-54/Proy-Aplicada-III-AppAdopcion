package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class UsuarioRequest {
    @SerializedName("id_usuario")
    private String id_usuario;
    @SerializedName("correo")
    private String correo;
    @SerializedName("telefono")
    private String telefono;
    @SerializedName("rol")
    private String rol;
    @SerializedName("direccion")
    private String direccion;
    @SerializedName("latitud")
    private Double latitud;
    @SerializedName("longitud")
    private Double longitud;
    @SerializedName("urlimagenusuario")
    private String urlImagenUsuario;

    public UsuarioRequest(String id_usuario, String correo, String telefono, String rol,
                          String direccion, Double latitud, Double longitud, String urlImagenUsuario) {
        this.id_usuario = id_usuario;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.urlImagenUsuario = urlImagenUsuario;
    }
    public String getRol()       { return rol; }
}