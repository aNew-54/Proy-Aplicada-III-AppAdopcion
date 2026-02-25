package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class MascotaRequest {
    @SerializedName("id_refugio")
    public Integer idRefugio;
    @SerializedName("id_raza")
    public Integer idRaza;
    @SerializedName("nombremascota")
    public String  nombre;
    @SerializedName("edadanios")
    public Integer edadAnios;
    @SerializedName("edadmeses")
    public Integer edadMeses;
    @SerializedName("urlportadamascota")
    public String  urlPortada;
    @SerializedName("genero")
    public String  genero;
    @SerializedName("temperamento")
    public String  temperamento;
    @SerializedName("historia")
    public String  historia;
    @SerializedName("estado")
    public String  estado;

    public MascotaRequest(int idRefugio, Integer idRaza, String nombre, int edadAnios, int edadMeses, String urlPortada, String genero, String temperamento, String historia) {
        this.idRefugio    = idRefugio;
        this.idRaza       = idRaza;
        this.nombre       = nombre;
        this.edadAnios    = edadAnios;
        this.edadMeses    = edadMeses;
        this.urlPortada   = urlPortada;
        this.genero       = genero;
        this.temperamento = temperamento;
        this.historia     = historia;
        this.estado       = "Disponible";
    }
}