package unc.edu.pe.appadopcion.data.model;

import com.google.gson.annotations.SerializedName;

public class AdoptanteRequest {
    @SerializedName("id_usuario")
    private String id_usuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("genero")
    private String genero;

    @SerializedName("fechanacimiento")  // nombre exacto de la columna en Supabase (lowercase)
    private String fechaNacimiento;

    public AdoptanteRequest(String id_usuario, String nombre, String apellido,
                            String genero, String fechaNacimiento) {
        this.id_usuario     = id_usuario;
        this.nombre         = nombre;
        this.apellido       = apellido;
        this.genero         = (genero != null && !genero.isEmpty()) ? genero : null;
        this.fechaNacimiento = (fechaNacimiento != null && !fechaNacimiento.isEmpty()) ? fechaNacimiento : null;
    }
}