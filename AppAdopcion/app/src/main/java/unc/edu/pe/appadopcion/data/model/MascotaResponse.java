package unc.edu.pe.appadopcion.data.model;
import com.google.gson.annotations.SerializedName;

public class MascotaResponse {
    @SerializedName("id_mascota")
    public int idMascota;
    @SerializedName("nombremascota")
    public String nombre;
    @SerializedName("edadanios")
    public int    edadAnios;
    @SerializedName("edadmeses")
    public int    edadMeses;
    @SerializedName("urlportadamascota")
    public String urlPortada;
    @SerializedName("genero")
    public String genero;
    @SerializedName("temperamento")
    public String temperamento;
    @SerializedName("historia")
    public String historia;
    @SerializedName("estado")
    public String estado;
    @SerializedName("contadorfavoritos")
    public int    contadorFavoritos;
    @SerializedName("fecharegistro")
    public String fechaRegistro;
    @SerializedName("id_raza")
    public int    idRaza;
    @SerializedName("nombreraza")
    public String nombreRaza;
    @SerializedName("id_especie")
    public int    idEspecie;
    @SerializedName("nombreespecie")
    public String nombreEspecie;
    @SerializedName("id_refugio")
    public int    idRefugio;
    @SerializedName("nombrerefugio")
    public String nombreRefugio;
    @SerializedName("direccionrefugio")
    public String direccionRefugio;
    @SerializedName("latitudrefugio")
    public Double latitudRefugio;
    @SerializedName("longitudrefugio")
    public Double longitudRefugio;
    @SerializedName("portadarefugio")
    public String portadaRefugio;
    @SerializedName("telefonorefugio")
    public String telefonoRefugio;
}
