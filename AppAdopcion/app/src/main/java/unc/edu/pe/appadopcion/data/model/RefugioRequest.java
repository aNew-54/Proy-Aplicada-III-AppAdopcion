package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class RefugioRequest {
    @SerializedName("id_usuario")
    private String id_usuario;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("urlportada")
    private String urlPortada;

    public RefugioRequest(String id_usuario, String nombre, String descripcion, String urlPortada) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.descripcion = (descripcion != null && !descripcion.isEmpty()) ? descripcion : null;
        this.urlPortada = urlPortada;
    }
}