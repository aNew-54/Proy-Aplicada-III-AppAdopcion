package unc.edu.pe.appadopcion.data.model;

public class AuthResponse {
    private String access_token;
    private User user;

    // --- NUEVO CAMPO AGREGADO ---
    private String rol;

    public String getAccessToken() { return access_token; }
    public User getUser() { return user; }

    // --- GETTER Y SETTER PARA EL ROL ---
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public static class User {
        private String id; // Este es el UUID de Supabase
        public String getId() { return id; }
    }
}