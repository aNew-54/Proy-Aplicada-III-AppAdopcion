package unc.edu.pe.appadopcion.data.model;

public class AuthResponse {
    private String access_token;
    private User user;

    public String getAccessToken() { return access_token; }
    public User getUser() { return user; }

    public static class User {
        private String id; // Este es el UUID de Supabase
        public String getId() { return id; }
    }
}