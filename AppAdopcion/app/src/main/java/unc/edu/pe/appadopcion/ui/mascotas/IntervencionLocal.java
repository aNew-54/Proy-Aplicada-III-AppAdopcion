package unc.edu.pe.appadopcion.ui.mascotas;

public class IntervencionLocal {
    public String titulo;
    public String descripcion;
    public String fecha; // formato: dd/MM/yyyy

    public IntervencionLocal(String titulo, String descripcion, String fecha) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }
}
